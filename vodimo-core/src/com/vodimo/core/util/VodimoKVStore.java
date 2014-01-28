package com.vodimo.core.util;

public interface VodimoKVStore<T, E> {

	public void put(T key, E val);
	
	public Object get(T key);
	
	public boolean containsKey(Object key);

	public void flushAll();
	
	public void addToSet();
	
}
