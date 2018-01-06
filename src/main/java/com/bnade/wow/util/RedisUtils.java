package com.bnade.wow.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis工具类
 * Created by liufeng0103@163.com on 2017/7/12.
 */
public class RedisUtils {

    private RedisUtils() {}

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        // 最大可用连接数，默认值为8，如果赋值为-1则表示不限制
        config.setMaxTotal(8);
        // 最大空闲连接数，默认值为8
        config.setMaxIdle(8);
        // 最小空闲连接数
        config.setMinIdle(4);
        // 最大等待连接毫秒数，默认值为-1表示永不超时
        config.setMaxWaitMillis(3000);
        // true表示验证连接

        jedisPool = new JedisPool(config, "localhost");
    }

    /**
     * 获取Jedis实例
     * @return Jedis
     */
    public static Jedis getJedisInstace() {
        return jedisPool.getResource();
    }
}
