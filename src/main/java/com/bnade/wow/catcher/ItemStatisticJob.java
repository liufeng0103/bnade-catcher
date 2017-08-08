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
import java.time.LocalDate;
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

    }

    /**
     * 物品市场价统计
     */
    public void processItemsMarketPrice() {

        try {
            List<Integer> itemIds = itemDao.getIds();
            logger.info("物品数: {}", itemIds.size());
            for (Integer itemId : itemIds) {
                processItemMarketPrice(itemId);
            }
        } catch (SQLException e) {
            logger.error("数据库操作出错", e);
        }
    }

    public void processItemMarketPrice(int itemId) throws SQLException {
        if (itemId != Item.PET_CAGE_ID) { // 普通物品
            List<ItemBonus> itemBonuses = itemDao.findItemBonusesByItemId(itemId);
            // 如果物品没有bonuslist设置一个""
            if (itemBonuses.size() == 0) {
                ItemBonus itemBonus = new ItemBonus();
                itemBonus.setItemId(itemId);
                itemBonus.setBonusList("");
                itemBonuses.add(itemBonus);
            }
            logger.info("物品id: {} bonuslist数量: {}", itemId, itemBonuses.size());
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
                // 计算市场价
                long marketPrice = calculatePrice(calculateAuctions);
                logger.info("物品id：{} bonuslist：{} 数量：{} 服务器数：{} 市场价：{}", itemId, itemBonus.getBonusList(), quantitySum, calculateAuctions.size(), marketPrice);
                ItemStatistic selectItemStatistic = new ItemStatistic();
                selectItemStatistic.setItemId(itemId);
                selectItemStatistic.setBonusList(itemBonus.getBonusList());
                selectItemStatistic.setValidTime(Timestamp.valueOf(LocalDateTime.of(9999, Month.DECEMBER, 31, 0, 0, 0)));
                ItemStatistic itemStatistic = itemDao.findItemStatistic(selectItemStatistic);
                logger.info("{}", itemStatistic);
                if (itemStatistic == null) { // 新数据
                    selectItemStatistic.setMarketPrice(marketPrice);
                    selectItemStatistic.setQuantity(quantitySum);
                    itemDao.saveItemStatistic(selectItemStatistic);
                } else { // 已经存在
                    if (calculateAuctions.size() > 85) {
                        itemStatistic.setMarketPrice(marketPrice);
                        itemStatistic.setQuantity(quantitySum);
                        itemDao.updateItemStatistic(itemStatistic);
                    } else {
                        if (marketPrice < itemStatistic.getMarketPrice()) {
                            itemStatistic.setMarketPrice(marketPrice);
                            itemStatistic.setQuantity(quantitySum);
                            itemDao.updateItemStatistic(itemStatistic);
                        }
                    }
                }
//                LocalDateTime localDateTime = LocalDateTime.now();
            }
        } else { // 宠物价格

        }
//                break;
    }

    /**
     * 计算市场价
     *
     * @param auctions
     * @return
     */
    private long calculatePrice(List<CheapestAuction> auctions) {
        if (auctions.size() == 0) {
            return 0L;
        } else if (auctions.size() == 1) {
            return auctions.get(0).getBuyout();
        } else {
            // 取前80%数据计算
            int count = auctions.size() * 8 / 10;
//            logger.info("count:{}", count);
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

    public static void main(String[] args) throws SQLException {
//        new ItemStatisticJob().processItemsMarketPrice();
        new ItemStatisticJob().processItemMarketPrice(124441);
//        new ItemStatisticJob().processItemMarketPrice(24660);
//        new ItemStatisticJob().processItemMarketPrice(147429);

    }
}
