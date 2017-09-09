package com.bnade.wow.catcher;

import com.bnade.wow.util.ConfigUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 配置和运行各种需要的job
 * Created by liufeng0103@163.com on 2017/6/30.
 */
public class JobRunner {

    private static Logger logger = LoggerFactory.getLogger(JobRunner.class);

    public static void main(String[] args) {
        File shutdown = new File("job_shutdown");
        Scheduler scheduler = null;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();

            // 物品更新job
            JobDetail itemJob = JobBuilder.newJob(ItemCatcherJob.class)
                    .withIdentity("itemJob", "group1")
                    .build();
            CronTrigger itemTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("itemTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.getProperty("item_job.cron")))
                    .build();
            logger.info("{} Cron表达式：{}", itemJob.getKey(), itemTrigger.getCronExpression());

            // 物品搜索统计job
            JobDetail itemSearchStatisticJob = JobBuilder.newJob(ItemSearchStatisticJob.class)
                    .withIdentity("itemSearchStatisticJob", "group1")
                    .build();
            CronTrigger itemSeachStatisticTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("itemSearchStatisticTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.getProperty("item_search_statistic_job.cron")))
                    .build();
            logger.info("{} Cron表达式：{}", itemSearchStatisticJob.getKey(), itemSeachStatisticTrigger.getCronExpression());

            // 物品搜索更新job
            JobDetail itemHotRefreshJob = JobBuilder.newJob(ItemHotRefreshJob.class)
                    .withIdentity("itemHotRefreshJob", "group1")
                    .build();
            CronTrigger itemHotRefreshTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("itemHotRefreshTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.getProperty("item_hot_refresh_job.cron")))
                    .build();
            logger.info("{} Cron表达式：{}", itemHotRefreshJob.getKey(), itemHotRefreshTrigger.getCronExpression());

            // 物品统计job
            JobDetail itemStatisticJob = JobBuilder.newJob(ItemStatisticJob.class)
                    .withIdentity("itemStatisticJob", "group1")
                    .build();
            CronTrigger itemStatisticTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("itemStatisticTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.getProperty("item_statistic_job.cron")))
                    .build();
            logger.info("{} Cron表达式：{}", itemStatisticJob.getKey(), itemStatisticTrigger.getCronExpression());

            scheduler.start();
            scheduler.scheduleJob(itemJob, itemTrigger);
            scheduler.scheduleJob(itemSearchStatisticJob, itemSeachStatisticTrigger);
            scheduler.scheduleJob(itemStatisticJob, itemStatisticTrigger);
            scheduler.scheduleJob(itemHotRefreshJob, itemHotRefreshTrigger);

            while (true) {
                if (shutdown.exists()) {
                    scheduler.shutdown();
                    logger.info("JobRunner关闭");
                    break;
                } else {
                    try {
                        logger.debug("JobRunner关闭检测,等待10s");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
