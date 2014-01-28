package com.vodimo.core.util;

import redis.clients.jedis.Jedis;

public class RedisKVStore implements VodimoKVStore<Object, Object> {

	private static RedisKVStore store;	
	private Jedis jedis;
	
	private final static String HOST = "localhost";
	
	private RedisKVStore(){
		this.jedis = new Jedis(HOST);				
	}
	
	public static RedisKVStore newInstance() {
		if(store == null) {
			store = new RedisKVStore();
			return store;
		} else {
			return store;
		}
	}
		
	@Override
	public void put(Object key, Object val) {				
		jedis.set(VodimoUtils.serialize(key), VodimoUtils.serialize(val));				
	}

	@Override
	public Object get(Object key) {
		return VodimoUtils.deserialize(jedis.get(VodimoUtils.serialize(key)));
	}	
	
	@Override
	public void flushAll() {
		this.jedis.flushAll();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.jedis.keys(VodimoUtils.serialize(key)).isEmpty();
	}

	@Override
	public void addToSet() {
		// TODO Auto-generated method stub		
	}
	
}
