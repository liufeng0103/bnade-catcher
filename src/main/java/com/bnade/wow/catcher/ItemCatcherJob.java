package com.bnade.wow.catcher;

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
import java.util.List;

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
//        addNewItems();
        addNewItemBonuses();
    }

    /**
     * 添加新物品到item表
     */
    private void addNewItems() {
        try {
            List<Integer> itemIds = itemDao.getIds();
            List<Integer> auctionItemIds = auctionDao.getItemIds();
            logger.info("现有物品数: {}", itemIds.size());
            auctionItemIds.removeAll(itemIds);
            logger.info("发现新物品数: {}", auctionItemIds.size()); // 不更新宠物笼
            for (Integer itemId : auctionItemIds) {
                if (itemId == 82800) {
                    logger.info("不更新宠物笼: {}", itemId);
                    continue;
                }
                XItem xItem = null;
                try {
                    xItem = WowHeadClient.getInstance().getItem(itemId);
                } catch (WowHeadClientException e) {
                    e.printStackTrace();
                    logger.error("通过client获取物品{}信息失败: {}", itemId, e);
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

    /**
     * 添加新的物品奖励信息到item_bonus库
     */
    private void addNewItemBonuses() {
        try {
            List<ItemBonus> itemBonuses = itemDao.findAllItemBonuses();
            List<ItemBonus> auctionItemBonuses = auctionDao.findAllItemBonuses();
            logger.info("现有item bonus数: {}", itemBonuses.size());
            auctionItemBonuses.removeAll(itemBonuses);
            logger.info("发现新item bonus数: {}", auctionItemBonuses.size());
            for (ItemBonus itemBonus : auctionItemBonuses) {
                itemDao.saveItemBonus(itemBonus);
                logger.info("成功添加新: {}", itemBonus);
            }
        } catch (SQLException e) {
            logger.error("数据库操作异常, {}", e);
        }
    }

}
