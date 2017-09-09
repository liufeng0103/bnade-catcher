package com.bnade.wow.catcher;

import com.bnade.wow.dao.ItemDao;
import com.bnade.wow.entity.Item;
import com.bnade.wow.entity.ItemStatistic;
import com.bnade.wow.v2.dao.AuctionDao;
import com.bnade.wow.v2.entity.CheapestAuction;
import com.bnade.wow.v2.entity.ItemBonus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * 物品相关统计
 *
 * Created by liufeng0103@163.com on 2017/8/6.
 */
public class ItemStatisticJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ItemStatisticJob.class);

    private ItemDao itemDao = ItemDao.getInstance();
    private AuctionDao auctionDao = AuctionDao.getInstance();

    private Set<String> filterOwner = new HashSet<>(Arrays.asList("贼面贼霸,冲进女澡堂,漏屁屁火星人".split(",")));

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("开始物品统计");
        processItemsMarketPrice();
        logger.info("物品统计完毕");
    }

    /**
     * 物品市场价统计
     */
    public void processItemsMarketPrice() {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            List<Integer> itemIds = itemDao.getIds();
            logger.info("物品数: {}", itemIds.size());
            int count = 0;
            for (Integer itemId : itemIds) {
                processItemMarketPrice(itemId, localDateTime);

                // 打印处理进度
                if (++count % 200 == 0) {
                    logger.info("已处理完{}%", count * 100 / itemIds.size());
                }
            }
        } catch (SQLException e) {
            logger.error("数据库操作出错", e);
        }
    }

    public void processItemMarketPrice(int itemId, LocalDateTime processTime) throws SQLException {
        if (itemId != Item.PET_CAGE_ID) { // 普通物品
            List<ItemBonus> itemBonuses = itemDao.findItemBonusesByItemId(itemId);
            // 如果物品没有bonusList设置一个""
            if (itemBonuses.size() == 0) {
                ItemBonus itemBonus = new ItemBonus();
                itemBonus.setItemId(itemId);
                itemBonus.setBonusList("");
                itemBonuses.add(itemBonus);
            }
//            logger.info("物品id: {} bonusList数量: {}", itemId, itemBonuses.size());
            for (ItemBonus itemBonus : itemBonuses) {
                // 查询物品在全服的最低一口价
                CheapestAuction selectAuction = new CheapestAuction();
                selectAuction.setItemId(itemId);
                selectAuction.setBonusList(itemBonus.getBonusList());
                List<CheapestAuction> auctions = auctionDao.findCheapestAuctions(selectAuction);
//                logger.info("{}", auctions);
                // 计算总数量
                int quantitySum = auctions.stream()
                        .map(CheapestAuction::getTotalQuantity)
                        .reduce(0, (a, b) -> a + b);
                // 排序并去掉扰乱市场价的卖家
                List<CheapestAuction> calculateAuctions = auctions.stream()
                        .filter(auc -> !filterOwner.contains(auc.getOwner()))
                        .sorted(comparing(CheapestAuction::getBuyout))
                        .collect(toList());
//                logger.info("{}", calculateAuctions);
                if (calculateAuctions.size() > 0) {
                    // 计算市场价
//                    long marketPrice = calculatePrice(calculateAuctions);
//                    logger.info("物品id：{} bonusList：{} 物品数：{} 服务器数：{} 市场价：{}", itemId, itemBonus.getBonusList(), quantitySum, calculateAuctions.size(), marketPrice);
                    long marketPrice = calculateMarketValue(calculateAuctions.stream().map((auc) -> auc.getBuyout()).collect(toList()));
                    // 最低价
                    long cheapestPrice = calculateAuctions.get(0).getBuyout();

                    // 保存为历史
                    ItemStatistic newItemStatistic = new ItemStatistic();
                    newItemStatistic.setItemId(itemId);
                    newItemStatistic.setBonusList(itemBonus.getBonusList());
                    newItemStatistic.setMarketPrice(marketPrice);
                    newItemStatistic.setCheapestPrice(cheapestPrice);
                    newItemStatistic.setRealmQuantity(auctions.size());
                    newItemStatistic.setValidRealmQuantity(calculateAuctions.size());
                    newItemStatistic.setQuantity(quantitySum);
                    newItemStatistic.setValidTime(Timestamp.valueOf(processTime));
                    itemDao.saveItemStatistic(newItemStatistic);

                    // 更新最新价格
                    ItemStatistic selectItemStatistic = new ItemStatistic();
                    selectItemStatistic.setItemId(itemId);
                    selectItemStatistic.setBonusList(itemBonus.getBonusList());
                    selectItemStatistic.setValidTime(Timestamp.valueOf(LocalDateTime.of(9999, Month.DECEMBER, 31, 0, 0, 0)));
//                logger.info("select {}", selectItemStatistic);
                    ItemStatistic itemStatistic = itemDao.findItemStatistic(selectItemStatistic);
//                logger.info("{}", itemStatistic);
                    if (itemStatistic == null) { // 新数据
                        selectItemStatistic.setMarketPrice(marketPrice);
                        selectItemStatistic.setCheapestPrice(cheapestPrice);
                        selectItemStatistic.setQuantity(quantitySum);
                        selectItemStatistic.setRealmQuantity(auctions.size());
                        selectItemStatistic.setValidRealmQuantity(calculateAuctions.size());
                        itemDao.saveItemStatistic(selectItemStatistic);
                    } else { // 已经存在
                        if (calculateAuctions.size() >= 85 || marketPrice < itemStatistic.getMarketPrice()) {
                            logger.info("原服务器数{} 计算服务器数{} 当前价格{} 历史价格{}", auctions.size(), calculateAuctions.size(), marketPrice, itemStatistic.getMarketPrice());
                            itemStatistic.setMarketPrice(marketPrice);
                            itemStatistic.setCheapestPrice(cheapestPrice);
                            itemStatistic.setQuantity(quantitySum);
                            itemStatistic.setRealmQuantity(auctions.size());
                            itemStatistic.setValidRealmQuantity(calculateAuctions.size());
                            itemDao.updateItemStatistic(itemStatistic);
                        }
                    }
                }
            }
        } else { // 宠物价格

        }
    }

    /**
     * 计算市场价
     *
     * @param auctions
     * @return
     */
     public long calculatePrice(List<CheapestAuction> auctions) {
        if (auctions.size() == 0) {
            return 0L;
        } else if (auctions.size() == 1) {
            return auctions.get(0).getBuyout();
        } else {
            // 取前80%数据计算
            int count = auctions.size() * 8 / 10;
//            logger.info("count:{}", count);public
            long priceSum = auctions.stream()
                    .limit(count)
                    .map(CheapestAuction::getBuyout)
                    .reduce(0L, (a, b) -> (a + b));
            long price = priceSum / count;
            // 如果计算的最高价大于3倍的平均价则重新计算价格
            if (auctions.get(count - 1).getBuyout() > price * 3) {
//                logger.info("price:{} 最高价：{} 重新计算", price, auctions.get(count).getBuyout());
                price = calculatePrice(auctions.stream().limit(count).collect(toList()));
            }
            return price;
        }
    }

    /**
     * 计算市场价
     * 参考tsm市场价算法, 计算前15%到30%的数据，去除极值，计算的平均价
     *
     * @param buyouts 价格数组
     */
    public static long calculateMarketValue(List<Long> buyouts) {
        if (buyouts.size() == 0)
            return 0;

        double MIN_PERCENTILE = 0.15; // 至少考虑拍卖的最低的15％
        double MAX_PERCENTILE = 0.30; // 最多考虑拍卖的最低的30％
        double MAX_JUMP = 1.2; // 在最小和最大百分位数之间，任何价格上涨超过120％都将导致丢弃剩余的拍卖

        int totalNum = 0;
        long totalBuyout = 0;
        int	numRecords = buyouts.size();

        for (int i = 0; i < numRecords; i++) {
            totalNum = i;
            if (i > 0 && i > numRecords * MIN_PERCENTILE && (i > numRecords * MAX_PERCENTILE || buyouts.get(i) >= MAX_JUMP * buyouts.get(i - 1))) {
                break;
            }

            totalBuyout += buyouts.get(i);
            if (i == numRecords - 1) {
                totalNum = numRecords;
            }
        }

        long uncorrectedMean = totalBuyout / totalNum;
        double varience = 0;

        for (int i = 0; i < totalNum; i++) {
            varience = varience + Math.pow(buyouts.get(i) - uncorrectedMean, 2); // 返回 x 的 y 次幂
        }

        double stdDev = Math.sqrt(varience / totalNum);// 返回数的平方根
        int correctedTotalNum = 1;
        long correctedTotalBuyout = uncorrectedMean;

        for (int i = 0; i < totalNum; i++) {
            if (Math.abs(uncorrectedMean - buyouts.get(i)) < 1.5 * stdDev) { // 返回 x 的绝对值
                correctedTotalNum++;
                correctedTotalBuyout += buyouts.get(i);
            }
        }
        return (long)Math.floor(correctedTotalBuyout / correctedTotalNum + 0.5);
    }

}
