package com.dboper.search.util;

import java.util.HashMap;
import java.util.Map;

public class MapValueUtil {

	public static String getString(Map<String,Object> map,String key){
		return (String)map.get(key);
	}
	
	public static Integer getInt(Map<String,Object> map,String key){
		return (Integer)map.get(key);
	}
	
	public static Long getLong(Map<String,Object> map,String key){
		return (Long)map.get(key);
	}
	
	public static Map<String,Object> getMap(String key,Object value){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put(key, value);
		return map;
	}
	
	public static Map<String,Object> getMap(String key1,Object value1,String key2,Object value2){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}
	
	public static boolean compareMapEquals(Map<String,Object> map1,Map<String,Object> map2){
		for(String key:map1.keySet()){
			Object value1=map1.get(key);
			Object value2=map2.get(key);
			if(value1==null){
				if(value2!=null){
					return false;
				}
			}else{
				if(!value1.equals(value2)){
					return false;
				}
			}
		}
		return true;
	}
	
}
