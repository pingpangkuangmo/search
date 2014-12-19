package com.dboper.search.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

public abstract class AbstractCacheManager implements CacheManager{
	
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<String,Cache> caches;
	
	private boolean cacheEnabled=true;
	
	@SuppressWarnings("rawtypes")
	public AbstractCacheManager(){
		this.caches=new ConcurrentHashMap<String,Cache>();
	}
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <K, V> Cache<K, V> getCache(String cacheName) {
		if(StringUtils.hasLength(cacheName)){
			throw new IllegalArgumentException("cacheName must be not null");
		}
		Cache cache=this.caches.get(cacheName);
		if(cache==null){
			cache=creatCache(cacheName);
			Cache exitsCache=caches.putIfAbsent(cacheName,cache);
			if(exitsCache!=null){
				cache=exitsCache;
			}
		}
		return cache;
	}

	@SuppressWarnings("rawtypes")
	public abstract Cache creatCache(String cacheName);

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}
	
	

	
}
