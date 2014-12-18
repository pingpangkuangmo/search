package com.dboper.search.exception.cache;

public class CacheException extends RuntimeException{
	
	private static final long serialVersionUID = -5087501810619945606L;

	public CacheException(String paramString) {
		super(paramString);
	} 
	
	public CacheException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}
}
