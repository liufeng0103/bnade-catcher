package com.bnade.wow.catcher;

import com.bnade.wow.entity.ItemSearchStatistic;
import com.bnade.wow.util.DBUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新物品搜索次数
 *
 * Created by liufeng0103@163.com on 2017/9/9.
 */
public class ItemHotRefreshJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ItemHotRefreshJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        QueryRunner runner = DBUtils.getQueryRunner();
        try {
            List<ItemSearchStatistic> itemSearchStatistics = runner.query("SELECT item_id as itemId,SUM(search_count) as searchCount FROM item_search_statistic WHERE search_date>=? GROUP BY item_id",
                    new BeanListHandler<ItemSearchStatistic>(ItemSearchStatistic.class), LocalDate.now().plusDays(-7).toString());
            logger.info("一周内搜索物品数: " + itemSearchStatistics.size());
            int count = 0;
            for (ItemSearchStatistic itemSearchStatistic : itemSearchStatistics) {
                runner.update("update item set hot=? where id=?", itemSearchStatistic.getSearchCount(), itemSearchStatistic.getItemId());
                if (++count % (itemSearchStatistics.size() / 100) == 0 ) {
                    logger.info("已处理{}%", count * 100/itemSearchStatistics.size() + 1);
                }
            }
            logger.info("运行完毕");
        } catch (SQLException e) {
            logger.error("异常", e);
        }
    }

}
