package com.dboper.search.cache;

import com.dboper.search.exception.cache.CacheException;

public interface Cache<K, V> {

	public V get(K k);
	
	public void put(K k,V v) throws CacheException;
	
	public void remove(K k) throws CacheException;
	
	public int size();
	
}
