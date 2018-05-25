package com.bnade.wow.job;

import com.bnade.wow.client.WowHeadClient;
import com.bnade.wow.client.WowHeadClientException;
import com.bnade.wow.client.model.XItem;
import com.bnade.wow.dao.ItemDao;
import com.bnade.wow.entity.Item;
import com.bnade.wow.v2.dao.AuctionDao;
import com.bnade.wow.v2.entity.ItemBonus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 更新物品表
 * 1. 添加新物品
 * 2. 添加新的物品奖励
 * 3. 添加新的宠物
 * 4. 添加新的宠物类型
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class ItemCatcherJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ItemCatcherJob.class);

    private ItemDao itemDao = ItemDao.getInstance();
    private AuctionDao auctionDao = AuctionDao.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        addNewItems();
        addNewItemBonuses();
        // 第二次用于更新空的bonusList
        addNewItemBonuses();
    }

    /**
     * 添加新物品到item表
     */
    public void addNewItems() {
        logger.info("开始更新新物品");
        try {
            List<Integer> itemIds = itemDao.getIds();
            logger.info("现有物品数: {}", itemIds.size());
            List<Integer> auctionItemIds = auctionDao.getItemIds();
            auctionItemIds.removeAll(itemIds);
            logger.info("发现新物品数: {}", auctionItemIds.size()); // 不更新宠物笼
            for (Integer itemId : auctionItemIds) {
                if (itemId == 82800) {
                    logger.info("不更新宠物笼: {}", itemId);
                    continue;
                }
                XItem xItem;
                try {
                    xItem = WowHeadClient.getInstance().getItem(itemId);
                } catch (WowHeadClientException e) {
                    e.printStackTrace();
                    logger.error("通过client获取物品{}信息失败: {}", itemId, e);
                    continue;
                }
                if (xItem != null) {
                    Item item = new Item();
                    item.setId(itemId);
                    item.setName(xItem.getName());
                    item.setIcon(xItem.getIcon());
                    item.setItemClass(xItem.getItemClass().getId());
                    item.setItemSubClass(xItem.getSubclass().getId());
                    item.setInventoryType(xItem.getInventorySlot().getId());
                    item.setLevel(xItem.getLevel());
                    itemDao.save(item);
                    logger.info("成功添加新物品: {}", item);
                } else {
                    logger.info("获取物品: {} 失败", itemId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("数据库操作异常, {}", e);
        }
    }

    public void addItem(int id) {
        XItem xItem = WowHeadClient.getInstance().getItem(id);
        Item item = new Item();
        item.setId(id);
        item.setName(xItem.getName());
        item.setIcon(xItem.getIcon());
        item.setItemClass(xItem.getItemClass().getId());
        item.setItemSubClass(xItem.getSubclass().getId());
        item.setInventoryType(xItem.getInventorySlot().getId());
        item.setLevel(xItem.getLevel());
        try {
            itemDao.save(item);
            logger.info("成功添加新物品: {}", item);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.info("获取物品: {} 失败", id);
        }

    }

    /**
     * 添加新的物品奖励信息到item_bonus表
     * 物品的bonus数据非常混乱， 自己总结了一下规则，尽量不把bonus表弄的很乱
     * 1. 宠物(item_id = 82800)不需要
     * 2. 只更新一下类型(class)
     * 武器(2)
     * 护甲(4)
     * 宝石(3) 主要是圣物
     * 3. 使用一个list保存那些确定不需要bonus
     * 4. 注意有空bonus的添加
     */
    public void addNewItemBonuses() {
        logger.info("开始更新物品bonus list");
        // 不需要bonus的物品
        Set<Integer> ignoredItemIds = new HashSet<>();
        ignoredItemIds.add(82800);  // 宠物笼
//        ignoredItemIds.add(146667); // 瑞苏的不竭勇气 橙装 - 护甲(class=2)
//        ignoredItemIds.add(146668); // 警戒栖木 - 护甲(class=2)
//        ignoredItemIds.add(146669); // 哨兵的永恒庇护所 - 护甲(class=2)
//        ignoredItemIds.add(146666); // 塞露布拉，暗夜的双子 - 护甲(class=2)
//        ignoredItemIds.add(128709); // 暗月套牌：地狱火
//        ignoredItemIds.add(128705); // 暗月套牌：统御
//        ignoredItemIds.add(128711); // 暗月套牌：不朽
//        ignoredItemIds.add(128710); // 暗月套牌：应许

        try {
            List<ItemBonus> itemBonuses = itemDao.findAllItemBonuses();
            logger.info("现有item bonus数: {}", itemBonuses.size());
            List<ItemBonus> auctionItemBonuses = auctionDao.findAllItemBonuses();
            // 去掉已经存在的
            auctionItemBonuses.removeAll(itemBonuses);

            // 去掉不需要的item bonus
            Iterator<ItemBonus> itemBonusIterator = auctionItemBonuses.iterator();
            int emptyBonusListCount = 0;
            while (itemBonusIterator.hasNext()) {
                ItemBonus itemBonus = itemBonusIterator.next();
                // 跳过不需要更新的物品
                if (ignoredItemIds.contains(itemBonus.getItemId())) {
                    logger.info("忽略物品{}的bonus更新", itemBonus.getItemId());
                    itemBonusIterator.remove();
                }
                // 对空的bonusList处理， 只添加已经在item_bonus表里存在的item的空bonusList
                else if ("".equals(itemBonus.getBonusList())) {
                    if (itemDao.findItemBonusesByItemId(itemBonus.getItemId()).size() == 0) {
                        emptyBonusListCount++;
                        logger.debug("之前不存在该物品{}bonus list", itemBonus.getItemId());
                        itemBonusIterator.remove();
                    }
                }
            }
            logger.info("bonus list为空且不需要更新的数量: {}", emptyBonusListCount);

            logger.info("发现新item bonus数: {}", auctionItemBonuses.size());
            for (ItemBonus itemBonus : auctionItemBonuses) {
                itemDao.saveItemBonus(itemBonus);
                logger.info("成功添加新: {}", itemBonus);
            }
        } catch (SQLException e) {
            logger.error("数据库操作异常, {}", e);
        }
    }

    /**
     * 刷新物品信息
     * 由于每次版本更新，有些物品信息会被调整
     * 只更新当前版本的物品信息
     */
    public void refreshItems() {
        try {
            // id为大于800等级的最小物品 select min(id) from item where level >=800
            List<Integer> ids = itemDao.findIdsGreaterThan(123910);
            logger.info("找到{}个id大于123910的物品", ids.size());
            for (Integer id : ids) {
                Item item = itemDao.getItemById(id);
                XItem xItem = WowHeadClient.getInstance().getItem(id);
                // 目前只更新level不同的物品
                if (item.getLevel() != xItem.getLevel()) {
                    logger.info("更新物品{} level为{}", item, xItem.getLevel());
                    item.setLevel(xItem.getLevel());
                    itemDao.update(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ItemCatcherJob itemCatcherJob = new ItemCatcherJob();
        itemCatcherJob.addNewItems();
        itemCatcherJob.addNewItemBonuses();
        if (args != null && args.length > 0) {
            if ("refresh".equalsIgnoreCase(args[0])) {
                itemCatcherJob.refreshItems();
            }
        }
    }

}
