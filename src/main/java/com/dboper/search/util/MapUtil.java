package com.dboper.search.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtil {

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
	
	public static boolean mapValueEmpty(Map<String,Object> map){
		if(map==null || map.size()<1){
			return true;
		}
		for(String key:map.keySet()){
			if(map.get(key)!=null){
				return false;
			}
		}
		return true;
	}
	
	public static boolean compareTwoMapEquals(Map<String, Object> fatherTotal,Map<String, Object> alreadyFather, List<String> groupColumns) {
		for(String key:groupColumns){
			String tmp=GroupColumnsUtils.getKey(key);
			if(tmp==null){
				throw new RuntimeException("groupColumn "+key+" is not valid");
			}
			Object value1=fatherTotal.get(tmp);
			Object value2=alreadyFather.get(tmp);
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

	public static boolean compareMapEquals(Map<String,Object> map1,Map<String,Object> map2){
		if(map1==null || map1.size()<1){
			return false;
		}
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
	
	@SuppressWarnings("unchecked")
	public static void addMapsonToList(Map<String,Object> fatherTotal,String objName,String[] prefixs,
			Map<String,Object> obj,Map<String,String> baseLists){
		Map<String,Object> tmpObj=fatherTotal;
		boolean baseListObj=false;
		if(baseLists.containsKey(objName)){
			baseListObj=true;
		}
		int len=prefixs.length;
		if(len==2){
			Object prefixObj=tmpObj.get(prefixs[0]);
			if(prefixObj!=null && prefixObj instanceof Map){
				Map<String,Object> firstMap=((Map<String,Object>)prefixObj);
				ArrayList<Object> secondObj=(ArrayList<Object>) firstMap.get(prefixs[1]);
				if(secondObj==null){
					secondObj=new ArrayList<Object>();
					firstMap.put(prefixs[1],secondObj);
				}
				if(baseListObj && obj!=null){
					Object base=obj.get(baseLists.get(objName));
					if(base!=null && !secondObj.contains(obj)){
						secondObj.add(base);
					}
				}
			}
		}
		fatherTotal.remove(objName);
	}
	
	@SuppressWarnings("unchecked")
	public static void addMapsonToMap(Map<String,Object> fatherTotal,String[] prefixs,Map<String,Object> obj){
		int len=prefixs.length;
		Map<String,Object> tmpObj=fatherTotal;
		for(int i=0;i<len-1;i++){
			Object prefixObj=tmpObj.get(prefixs[i]);
			if(prefixObj!=null && prefixObj instanceof Map){
				tmpObj=((Map<String,Object>)prefixObj);
			}else{
				//不应该出现此种情况
				tmpObj=null;
				break;
			}
		}
		if(tmpObj!=null){
			tmpObj.put(prefixs[len-1],obj);
		}
	}
	
}
