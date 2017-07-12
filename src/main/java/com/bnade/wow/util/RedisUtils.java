package com.bnade.wow.util;

import redis.clients.jedis.Jedis;

/**
 * Redis工具类
 * Created by liufeng0103@163.com on 2017/7/12.
 */
public class RedisUtils {

    private RedisUtils() {}

    private static Jedis jedis;

    /**
     * 获取Jedis实例
     * @return Jedis
     */
    public static Jedis getJedisInstace() {
        if (jedis == null) {
            jedis = new Jedis();
        }
        return jedis;
    }
}
