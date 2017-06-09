package com.bnade.wow.dao;


public class RedisDao {

//	private final Logger logger = LoggerFactory.getLogger(RedisDao.class);
//	
//	private final JedisPool jedisPool;
//	
//	public RedisDao(String ip, int port) {
//		jedisPool = new JedisPool(ip, port);
//	}
//	
//	private RuntimeSchema<RedisDao> schema = RuntimeSchema.createFrom(RedisDao.class);
//	
//	public String test() {
//		RedisDao dao = null;
//		try (Jedis jedis = jedisPool.getResource()) {
//			byte[] bytes = jedis.get("".getBytes());
//			if (bytes != null) {
//			
//				ProtostuffIOUtil.mergeFrom(bytes, dao, schema);
//			}
//			byte[] bytes2 = ProtostuffIOUtil.toByteArray(dao, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
//			int timeout = 60*60;
//			jedis.setex("".getBytes(), timeout, bytes2);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//	
//		return null;
//	}
}
