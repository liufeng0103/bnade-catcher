package com.bnade.wow.job;

import com.bnade.wow.util.HttpUtils;
import com.bnade.wow.v2.dao.WowtokenDao;
import com.bnade.wow.v2.entity.Wowtoken;
import com.google.gson.Gson;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 时光徽章数据获取
 * Created by liufeng0103@163.com on 2018/1/6.
 */
public class WowtokenCatcherJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(WowtokenCatcherJob.class);

    /**
     * 初始化数据库， wowtokeninfo提供了历史数据，如果第一次运行，可以使用这些历史数据来初始化
     */
    public void init() throws IOException, SQLException {
        logger.info("初始化时光徽章表数据");
        WowtokenDao wowtokenDao = new WowtokenDao();
        wowtokenDao.deleteAll();
        logger.info("清空时光徽章表");
        Gson gson = new Gson();
        String history_url = "https://data.wowtoken.info/wowtoken.json";
        String json = HttpUtils.get(history_url);
        logger.info("下载历史数据");
        List<List<Long>> tokens = gson.fromJson(json, WowTokensHistoryJson.class).getHistory().getCN();
        int count = 0;
        for (List<Long> token : tokens) {
            Wowtoken wowtoken = new Wowtoken();
            wowtoken.setBuy(new Long(token.get(1)).intValue());
            wowtoken.setUpdated(token.get(0) * 1000);
            wowtokenDao.save(wowtoken);
            System.out.println(++count);
        }
        logger.info("完毕");
    }

    public void updateWowtoken() throws IOException, SQLException {
        Gson gson = new Gson();
        WowtokenDao wowtokenDao = new WowtokenDao();      logger.info("开始获取时光徽章信息");
        String tokenJson = HttpUtils.get("https://data.wowtoken.info/snapshot.json");
        logger.info("时光徽章信息获取成功");
        Wowtoken wowtoken = gson.fromJson(tokenJson, WowTokensJson.class).getCN().getRaw();
        // 通过url获取的时间是秒单位的需要转换成毫秒
        wowtoken.setUpdated(wowtoken.getUpdated() * 1000);
        logger.info("从数据库获取更新时间{}的时光徽章", new Date(wowtoken.getUpdated()));
        Wowtoken dbWowToken = wowtokenDao.findByUpdated(wowtoken.getUpdated());
        if (dbWowToken == null && wowtoken.getBuy() != 0) {
            logger.info("时光徽章不在数据库，添加信息{}", wowtoken);
            wowtokenDao.save(wowtoken);
        } else {
            logger.info("时光徽章信息{}已存在，不更新", dbWowToken);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            new WowtokenCatcherJob().updateWowtoken();
        } catch (IOException | SQLException e) {
            logger.error("Error: {}", e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try {
            new WowtokenCatcherJob().updateWowtoken();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class WowTokensJson {
    private WowTokenJson CN;

    public WowTokenJson getCN() {
        return CN;
    }

    public void setCN(WowTokenJson cN) {
        CN = cN;
    }

}

class WowTokenJson {
    private Wowtoken raw;

    public Wowtoken getRaw() {
        return raw;
    }

    public void setRaw(Wowtoken raw) {
        this.raw = raw;
    }

}

class WowTokensHistoryJson {
    private WowTokensCnHistoryJson history;

    public WowTokensCnHistoryJson getHistory() {
        return history;
    }

    public void setHistory(WowTokensCnHistoryJson history) {
        this.history = history;
    }

}

class WowTokensCnHistoryJson {
    private List<List<Long>> CN;

    public List<List<Long>> getCN() {
        return CN;
    }

    public void setCN(List<List<Long>> cN) {
        CN = cN;
    }

}