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
            scheduler.scheduleJob(itemJob, itemTrigger);
            logger.info("{} Cron表达式：{}", itemJob.getKey(), itemTrigger.getCronExpression());

            // 物品搜索统计job
            JobDetail itemSeachStatisticJob = JobBuilder.newJob(ItemSearchStatisticJob.class)
                    .withIdentity("itemSeachStatisticJob", "group1")
                    .build();
            CronTrigger itemSeachStatisticTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("itemSeachStatisticTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(ConfigUtils.getProperty("item_search_statistic_job.cron")))
                    .build();
            scheduler.scheduleJob(itemSeachStatisticJob, itemSeachStatisticTrigger);
            logger.info("{} Cron表达式：{}", itemSeachStatisticJob.getKey(), itemSeachStatisticTrigger.getCronExpression());

            scheduler.start();

            while (true) {
                if (shutdown.exists()) {
                    scheduler.shutdown();
                    logger.info("JobRunner关闭");
                    break;
                } else {
                    try {
                        logger.info("JobRunner检测,等待10s");
                        Thread.sleep(10000);
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
