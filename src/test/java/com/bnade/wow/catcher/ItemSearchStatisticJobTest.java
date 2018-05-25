package com.bnade.wow.catcher;

import com.bnade.wow.job.ItemSearchStatisticJob;
import com.bnade.wow.util.RedisUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Created by liufeng0103@163.com on 2017/7/12.
 */
public class ItemSearchStatisticJobTest {

    private ItemSearchStatisticJob itemSearchStatisticJob = new ItemSearchStatisticJob();

    @Test
    public void getItemSearchCountMapTest() throws Exception {
        itemSearchStatisticJob.getItemSearchCountMap().forEach((k, v) -> {
            System.out.println("itemId: " + k + " count: " + v);
        });
    }

    @Test
    public void emptyItemSearchCountOnRedisTest() throws Exception {
        itemSearchStatisticJob.emptyItemSearchCountOnRedis();
    }

    @Test
    public void executeTest() throws Exception {
//        itemSearchStatisticJob.emptyItemSearchCountOnRedis();
//        prepareTestData();
//        itemSearchStatisticJob.execute(null);
    }

    private void prepareTestData() {
        Jedis jedis = RedisUtils.getJedisInstace();
        jedis.sadd("ip127.0.0.1", "1");
        jedis.sadd("ip127.0.0.1", "2");
        jedis.sadd("ip127.0.0.1", "3");
        jedis.sadd("ip127.0.0.1", "3");
        jedis.sadd("ip127.0.0.2", "4");
        jedis.sadd("ip127.0.0.2", "4");
    }

}