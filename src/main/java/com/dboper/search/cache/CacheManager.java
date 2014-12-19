package com.dboper.search.cache;

/**
 * 对多处的缓存进行统一管理，如统一的缓存开关和子缓存开关
 * @author g_li
 *
 */
public interface CacheManager{

	public <K,V> Cache<K,V> getCache(String cacheName);
}
