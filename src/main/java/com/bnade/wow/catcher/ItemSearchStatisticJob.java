package com.bnade.wow.catcher;

import com.bnade.wow.dao.ItemDao;
import com.bnade.wow.entity.ItemSearchStatistic;
import com.bnade.wow.util.RedisUtils;
import com.bnade.wow.util.TimeUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 统计物品查询次数
 * 在物品查询时，会把查询信息放到redis，格式 set key=ipxxx.xxx.xxx.xxx value=itemId
 * 把周期内物品查询次数统计，并清除redis，一个ip只统计一次查询
 * Created by liufeng0103@163.com on 2017/7/12.
 */
public class ItemSearchStatisticJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ItemSearchStatisticJob.class);

    private ItemDao itemDao = ItemDao.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Map<String, Integer> itemSearchCountMap = getItemSearchCountMap();
        logger.info("有{}个物品搜索信息需要更新", itemSearchCountMap.size());
        // 更新数据库
        itemSearchCountMap.forEach((k, v) -> {
            int itemId = Integer.valueOf(k);
            int searchCount = v;
            try {
                long todayDateTime = TimeUtils.parse(TimeUtils.getDate(0)).getTime();

                ItemSearchStatistic itemSearchStatistic = itemDao.findItemSearchStatisticByItemIdAndSearchDate(itemId, todayDateTime);
                if (itemSearchStatistic == null) {
                    itemSearchStatistic = new ItemSearchStatistic();
                    itemSearchStatistic.setItemId(itemId);
                    itemSearchStatistic.setSearchCount(searchCount);
                    itemSearchStatistic.setSearchDate(todayDateTime);
                    itemDao.saveItemSeachStatistic(itemSearchStatistic);
                    logger.info("添加 {}", itemSearchStatistic);
                } else {
                    itemSearchStatistic.setSearchCount(itemSearchStatistic.getSearchCount() + searchCount);
                    itemDao.updateItemSeachStatisticCount(itemSearchStatistic);
                    logger.info("更新 {}", itemSearchStatistic);
                }
            } catch (SQLException | ParseException e) {
                logger.error("出错", e);
            }
        });
        // 清理redis
        if (itemSearchCountMap.size() > 0) {
            emptyItemSearchCountOnRedis();
        }
    }

    /**
     * 统计每种物品被搜索的次数放到Map里
     *
     * @return Map key为item id value=搜索次数
     */
    public Map<String, Integer> getItemSearchCountMap() {
        Map<String, Integer> itemSearchCountMap = new HashMap<>();
        Jedis jedis = RedisUtils.getJedisInstace();
        // 所有ip
        Set<String> keys = jedis.keys("ip*");
        logger.info("有{}个ip", keys.size());
        for (String key : keys) {
            // ip查询的item ids
            Set<String> ipItemIds = jedis.smembers(key);
            for (String itemId : ipItemIds) {
                Integer count = itemSearchCountMap.get(itemId);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                itemSearchCountMap.put(itemId, count);
            }
        }
        return itemSearchCountMap;
    }

    /**
     * 清空redis中的统计信息
     */
    public void emptyItemSearchCountOnRedis() {
        Jedis jedis = RedisUtils.getJedisInstace();
        // 所有ip
        Set<String> keys = jedis.keys("ip*");
        for (String key : keys) {
            // ip查询的item ids
           jedis.del(key);
        }
        logger.info("所有ip*的key被删除");
    }
}
