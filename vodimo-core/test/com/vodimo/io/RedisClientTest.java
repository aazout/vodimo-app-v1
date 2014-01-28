package com.vodimo.io;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.vodimo.core.util.RedisKVStore;

public class RedisClientTest {
	
	@Test
	public void testRedisKVStore() {
		RedisKVStore store = RedisKVStore.newInstance();
		String key  = "foo";
		String value = "bar";
		store.put(key, value);
		
		System.out.println("store.get(key) = " + store.get(key));
		
		assertTrue(store.get(key).equals(value));		
	}
	
}
