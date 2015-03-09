package com.dboper.search.util;

public class GroupByUtils {

	private static final String[] GROUP_KEYS={"count(","sum("};
	
	public static boolean containsGroupKey(String key){
		if(key==null){
			return false;
		}
		for(String groupKey:GROUP_KEYS){
			if(key.startsWith(groupKey)){
				return true;
			}
		}
		return false;
	}
}
