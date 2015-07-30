package com.dboper.search.sqlparams.util;

import java.util.List;

public class Assert {
	
	public static void notNull(Object obj,String message){
		isTrue(obj!=null, message);
	}
	
	public static void notNull(Object obj,Exception ex){
		isTrue(obj!=null, ex);
	}
	
	public static void notEmpty(Object[] arr,String message){
		isTrue(arr!=null && arr.length>0, message);
	}
	
	public static void notEmpty(List<?> list,String message){
		isTrue(list!=null && list.size()>0, message);
	}
	
	public static void isEqual(Object s,Object t,String message){
		isTrue(s.equals(t), message);
	}
	
	public static void isLarger(int s,int t,String message){
		isTrue(s>t, message);
	}
	
	public static void isLargerEqual(int s,int t,String message){
		isTrue(s>=t, message);
	}
	
	public static <T> void isInstanceof(Object obj,Class<T> clazz,String message){
		notNull(clazz,"clazz must not be null");
		isTrue(clazz.isInstance(obj), message);
	}
	
	private static void isTrue(boolean isTrue,String message){
		if(!isTrue){
			throw new IllegalArgumentException(message);
		}
	}
	
	private static void isTrue(boolean isTrue,Exception ex){
		if(!isTrue){
			throw new IllegalArgumentException(ex);
		}
	}
}
