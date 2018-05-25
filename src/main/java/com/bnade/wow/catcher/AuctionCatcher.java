package com.bnade.wow.catcher;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bnade.wow.catcher.entity.JAuction;
import com.bnade.wow.catcher.entity.JAuctions;
import com.bnade.wow.dao.AuctionDao;
import com.bnade.wow.dao.DaoFactory;
import com.bnade.wow.dao.RealmDao;
import com.bnade.wow.dao.UserDao;
import com.bnade.wow.entity.Auction;
import com.bnade.wow.entity.Realm;
import com.bnade.wow.entity.UserItemNotification;
import com.bnade.wow.util.ConfigUtils;
import com.bnade.wow.util.HttpUtils;
import com.bnade.wow.util.MailUtils;
import com.bnade.wow.util.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 获取服务器的拍卖数据，并把3种数据保存到数据库
 * 
 * 1. 所有拍卖数据
 * 2. 最低一口价的数据
 * 3. 最低一口价数据到每日的历史表中
 *  
 * @author liufeng0103
 *
 */
public class AuctionCatcher {

	private static Logger logger = LoggerFactory.getLogger(AuctionCatcher.class);

	public static void process(Realm realm) throws ParseException, IOException, JsonSyntaxException, SQLException {
		long interval = Long.valueOf(ConfigUtils.getProperty("catcher.interval", "0"));
		if (System.currentTimeMillis() - realm.getLastModified() > interval) {
			long lastModified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:sss zzz", Locale.US).parse(HttpUtils.getHeaderFields(realm.getUrl()).get("Last-Modified").get(0)).getTime();
			if (lastModified > realm.getLastModified()) {
				logger.info("[{}]2次更新间隔{}", realm.getName(), TimeUtils.format(lastModified - realm.getLastModified()));
				realm.setLastModified(lastModified);

				processAuctions(realm);
			} else {
				logger.info("[{}]数据还未更新", realm.getName());
			}
		} else {
			logger.info("[{}]未超过间隔{}s，不更新", realm.getName(), interval/1000);
		}
	}
	
	/**
	 * 计算每种物品的最低一口单价，并把拍卖数据保存到数据库
	 * @param realm 服务器信息
	 * @throws SQLException 数据库异常
	 * @throws IOException io异常
	 * @throws JsonSyntaxException json处理异常
	 */
	public static void processAuctions(Realm realm) throws SQLException, JsonSyntaxException, IOException {
		List<Auction> aucs = getAuctions(realm);
		Map<String, Auction> minBuyoutAucs = new HashMap<>();
		int maxAuc = 0;
		for (Auction auc : aucs) {
			// 计算最大auc
			if (maxAuc < auc.getAuc()) {
				maxAuc = auc.getAuc();
			}
			// 计算每种物品的最低一口价
			// 去除没有一口价的物品
			if (auc.getBuyout() != 0) {
				String key = "" + auc.getItem() + "-" + auc.getPetSpeciesId() + "-" + auc.getPetBreedId() + "-" + auc.getBonusLists();
				// 计算单间物品的一口价
				long buyout = auc.getBuyout()/auc.getQuantity();
				long bid = auc.getBid()/auc.getQuantity();
				Auction aucTmp = minBuyoutAucs.get(key);
				if (aucTmp == null) {
					aucTmp = new Auction();
					aucTmp.setAuc(auc.getAuc());
					aucTmp.setItem(auc.getItem());
					aucTmp.setOwner(auc.getOwner());
					aucTmp.setOwnerRealm(auc.getOwnerRealm());
					aucTmp.setBid(bid);
					aucTmp.setBuyout(buyout);
					aucTmp.setQuantity(auc.getQuantity());
					aucTmp.setTimeLeft(auc.getTimeLeft());
					aucTmp.setPetSpeciesId(auc.getPetSpeciesId());
					aucTmp.setPetBreedId(auc.getPetBreedId());
					aucTmp.setContext(auc.getContext());
					aucTmp.setPetLevel(auc.getPetLevel());
					aucTmp.setBonusLists(auc.getBonusLists());
					aucTmp.setLastModified(realm.getLastModified());

					minBuyoutAucs.put(key, aucTmp);
				} else {
					// 计算总数量
					aucTmp.setQuantity(auc.getQuantity() + aucTmp.getQuantity());
					if (aucTmp.getBuyout() > buyout) {
						// 更新最低一口价拍卖信息
						aucTmp.setAuc(auc.getAuc());
						aucTmp.setOwner(auc.getOwner());
						aucTmp.setOwnerRealm(auc.getOwnerRealm());
						aucTmp.setBid(bid);
						aucTmp.setBuyout(buyout);
						aucTmp.setTimeLeft(auc.getTimeLeft());
						aucTmp.setContext(auc.getContext());
						aucTmp.setPetLevel(auc.getPetLevel());
					}
				}
			}

			// 对于810等级101可穿戴圣物的特殊处理
			// 影响效率，不需要的可以注释
			processSpecialAuctions(auc, minBuyoutAucs);
		}

		// 物品通知
		new Thread(() -> {
			try {
				processItemNotification(realm, minBuyoutAucs);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}).start();

		long start;
		// 保存拍卖数据
		start = System.currentTimeMillis();
		AuctionDao auctionDao = DaoFactory.getAuctionDao();
		logger.info("[{}]删除上一次拍卖行数据", realm.getName());
		auctionDao.deleteAll(realm.getId());
		auctionDao.insert(realm.getId(), aucs);
		logger.info("[{}]保存{}条拍卖行数据完毕, 用时{}", realm.getName(), aucs.size(), TimeUtils.format(System.currentTimeMillis() - start));
//
		// 保存所有最低一口价数据
		logger.info("[{}]删除拍卖行最低一口价数据", realm.getName());
		auctionDao.deleteAllMinBuyout(realm.getId());
		start = System.currentTimeMillis();
		List<Auction> minBuyoutAucList = new ArrayList<>(minBuyoutAucs.values());
		auctionDao.insertMinBuyout(realm.getId(), minBuyoutAucList);
		logger.info("[{}]保存{}条拍卖行最低一口价数据完毕,用时{}", realm.getName(), minBuyoutAucs.size(), TimeUtils.format(System.currentTimeMillis() - start));
		
		// 更新realm信息
		logger.info("[{}]新增拍卖{}", realm.getName(), maxAuc - realm.getMaxAucId());
		RealmDao realmDao = DaoFactory.getRealmDao();
		realm.setMaxAucId(maxAuc);
		realm.setAuctionQuantity(aucs.size());
		realm.setItemQuantity(minBuyoutAucs.size());
		logger.info("[{}]拍卖数据文件信息更新{}条记录完毕", realm.getName(), realmDao.update(realm));
		
		// 保存所有最低一口价数据到历史表
		start = System.currentTimeMillis();
		auctionDao.copyMinBuyoutToDaily(realm);
		logger.info("[{}]保存{}条拍卖行最低一口价数据到历史表完毕,用时{}", realm.getName(), minBuyoutAucs.size(), TimeUtils.format(System.currentTimeMillis() - start));
	}

	// 810等级101可穿戴圣物的id
	private static List<Integer> itemIds = new ArrayList<>();
	static {
		itemIds.add(141284);
		itemIds.add(141285);
		itemIds.add(141286);
		itemIds.add(141287);
		itemIds.add(141288);
		itemIds.add(141289);
		itemIds.add(141290);
		itemIds.add(141291);
		itemIds.add(141292);
		itemIds.add(141293);
	}
	/**
	 * 对于810等级101可穿戴圣物的特殊处理
	 * 保存所有这类数据
	 * @param auc 一条拍卖数据信息
	 * @param minBuyoutAucs 保存所有最低一口价
	 */
	private static void processSpecialAuctions(Auction auc, Map<String, Auction> minBuyoutAucs) {
		if (auc.getContext() != 0
				&& "".equals(auc.getBonusLists())
				&& itemIds.contains(auc.getItem())) {
			minBuyoutAucs.put(auc.getItem() + "_special_" + auc.getContext(), auc);
			logger.info("Add special={}", auc);
		}
	}

	/**
	 * 获取拍卖数据，并转化成数据库的Auction对象
	 * @param realm
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	private static List<Auction> getAuctions(Realm realm) throws JsonSyntaxException, IOException {
		List<JAuction> jAucs = new Gson().fromJson(HttpUtils.get(realm.getUrl()), JAuctions.class).getAuctions();
		List<Auction> aucs = new ArrayList<>(jAucs.size());
		for (JAuction jAuc : jAucs) {
			Auction auc = new Auction();
			auc.setAuc(jAuc.getAuc());
			auc.setItem(jAuc.getItem());
			auc.setOwner(jAuc.getOwner());
			auc.setOwnerRealm(jAuc.getOwnerRealm());
			auc.setBid(jAuc.getBid());
			auc.setBuyout(jAuc.getBuyout());
			auc.setQuantity(jAuc.getQuantity());
			auc.setTimeLeft(jAuc.getTimeLeft());
			auc.setRand(jAuc.getRand());
			auc.setSeed(jAuc.getSeed());
			auc.setPetSpeciesId(jAuc.getPetSpeciesId());
			auc.setPetLevel(jAuc.getPetLevel());
			auc.setPetBreedId(jAuc.getPetBreedId());
			auc.setContext(jAuc.getContext());
			auc.setBonusLists(jAuc.convertBonusListsToString());
			aucs.add(auc);
		}
		return aucs;
	}

	private static void processItemNotification(Realm realm, Map<String, Auction> aucs) throws SQLException {
		UserDao userDao = DaoFactory.getUserDao();
		List<UserItemNotification> itemNs = userDao.getItemNotificationsByRealmId(realm.getId());
		Map<Integer, List<UserItemNotification>> matchedItems = new HashMap<>();
		logger.info("找到{}条服务器{}的物品通知", itemNs.size(), realm.getId());
		for (UserItemNotification itemN : itemNs) {
			String key = "" + itemN.getItemId() + "-" + itemN.getPetSpeciesId() + "-" + itemN.getPetBreedId() + "-" + itemN.getBonusList();
			Auction auc = aucs.get(key);
			if (auc != null) {
				if (itemN.getIsInverted() == 0) { // 低于
					if (auc.getBuyout() <= itemN.getPrice()) {
						itemN.setMinBuyout(auc.getBuyout());
						List<UserItemNotification> tmpList = matchedItems.get(itemN.getUserId());
						if (tmpList == null) {
							tmpList = new ArrayList<>();
							tmpList.add(itemN);
							matchedItems.put(itemN.getUserId(), tmpList);
						} else {
							tmpList.add(itemN);
						}
					}
				}
				if (itemN.getIsInverted() == 1) { // 高于
					if (auc.getBuyout() >= itemN.getPrice()) {
						itemN.setMinBuyout(auc.getBuyout());
						List<UserItemNotification> tmpList = matchedItems.get(itemN.getUserId());
						if (tmpList == null) {
							tmpList = new ArrayList<>();
							tmpList.add(itemN);
							matchedItems.put(itemN.getUserId(), tmpList);
						} else {
							tmpList.add(itemN);
						}
					}
				}
			}
		}
		pushNotification(matchedItems, realm);
	}
	
	private static void pushNotification(Map<Integer, List<UserItemNotification>> matchedItems, Realm realm) {
		if (matchedItems.size() > 0) {
			logger.info("开始推送{}条服务器[{}]", matchedItems.size(), realm.getId());
			for (Map.Entry<Integer, List<UserItemNotification>> entry : matchedItems.entrySet()) {
				List<UserItemNotification> items = entry.getValue();
				String mail = null;
				String mailContent = "";
				for (UserItemNotification item : items) {
					mailContent += "";
					mail = item.getEmail();
					mailContent += item.getItemName() + " 当前最低一口单价：" + getGold(item.getMinBuyout());
					if (item.getIsInverted() == 0) {
						mailContent += "低与您的价格：" + getGold(item.getPrice());
					} else {
						mailContent += "高与您的价格：" + getGold(item.getPrice());
					}
					mailContent += "\r\n";
				}
				MailUtils.sendSimpleEmail(TimeUtils.getDate2(System.currentTimeMillis()) + " [BNADE] " + items.size() + "条物品满足在[" + realm.getName() + "]", mailContent, mail);
			}
		}		
	}
	
	private static String getGold(long price) {
		String s = "";
		long gold = price/10000;
		if (gold > 0) {
			s += gold + "g";
			price -= gold * 10000;
		}
		long silver = price / 100;
		if (silver > 0) {
			s += silver + "s";
			price -= silver * 100;
		}
		if (price > 0) {
			s += price + "c";
		}
		return s;
	}
	
	private static int count; // 记录运行中的线程数

	public static void main(String[] args) {
		File shutdown = new File("shutdown"); // 标识catcher是否关闭
		if (!shutdown.exists()) {
			int threadCount = count = Integer.valueOf(ConfigUtils.getProperty(
					"catcher.threadCount", "1"));
			logger.info("启动{}个线程运行", threadCount);
			ExecutorService pool = Executors.newFixedThreadPool(threadCount);
			for (int i = 0; i < threadCount; i++) {
				pool.execute(() -> {
					while (true) {
						if (!shutdown.exists()) {
							try {
								AuctionCatcher.process(RealmQueue.next());
								Thread.sleep(100);
							} catch (SQLException e) {
								String msg = e.getMessage();
								if (msg.length() > 255) {
									msg = msg.substring(0, 255);
									logger.error(msg);
								} else {
									logger.error(msg, e);	
								}								
							} catch (Exception e) {
								logger.error(e.getMessage(), e);								
							}
						} else {
							if (--count == 0) {
								logger.info("catcher已关闭");
								new File("running").delete(); // 标识catcher是否正在运行
							} else {
								logger.info("正在关闭线程,剩余{}个线程", count);
							}
							break;
						}
					}
				});
			}
			pool.shutdown();
		} else {
			logger.info("检测到catcher处于关闭状态，不启动");
		}
	}
	
}

class RealmQueue {

	private static List<Realm> realms;
	private static int index = 0;

	public static int getSize() {
		return realms.size();
	}

	public static synchronized Realm next() throws SQLException {
		if (realms == null) {
			realms = DaoFactory.getRealmDao().getAll();
		}
		Realm realm = realms.get(index);
		if (index == realms.size() - 1) {
			index = 0;
			System.out.println("已到最后服务器，重置为第一个服务器");
		} else {
			index++;
		}
		return realm;
	}

}
