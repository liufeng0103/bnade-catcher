package com.bnade.wow.catcher;

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
                    .withIdentity("trigger1", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 6 * * ?")) // 每天6点运行物品更新
                    .build();
            scheduler.scheduleJob(itemJob, itemTrigger);
            logger.info("{} Cron表达式：{}", itemJob.getKey(), itemTrigger.getCronExpression());

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
