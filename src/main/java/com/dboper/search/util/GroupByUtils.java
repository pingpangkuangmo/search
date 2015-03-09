package com.dboper.search.util;

public class GroupByUtils {

	private static final String[] GROUP_KEYS={"count(","sum(","concat("};
	
	public static boolean containsGroupKey(String key){
		if(key==null){
			return false;
		}
		key=key.toLowerCase();
		for(String groupKey:GROUP_KEYS){
			if(key.startsWith(groupKey)){
				return true;
			}
		}
		return false;
	}
}
