package com.dboper.search.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dboper.search.exception.cache.CacheException;

public class MapCache<K, V> implements Cache<K, V>{
	
	private Map<K,V> back;
	
	public MapCache(){
		back=new ConcurrentHashMap<K,V>();
	}
	
	public MapCache(Map<K,V> map){
		if(map==null){
			throw new CacheException("the constructor params map  of MapCache must be not null");
		}
		this.back=map;
	}

	@Override
	public V get(K key) {
		return back.get(key);
	}

	@Override
	public void put(K key, V value) throws CacheException {
		back.put(key,value);
	}

	@Override
	public void remove(K key) throws CacheException {
		back.remove(key);
	}

	@Override
	public int size() {
		return back.size();
	}
	 
}
