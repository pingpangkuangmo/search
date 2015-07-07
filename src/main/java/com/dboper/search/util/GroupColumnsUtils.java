package com.dboper.search.util;

public class GroupColumnsUtils {

	public static String getKey(String groupColumn){
		if(groupColumn==null){
			return null;
		}
		groupColumn=groupColumn.trim();
		int asIndex=groupColumn.indexOf(" as ");
		if(asIndex>=0){
			return groupColumn.substring(asIndex+4).trim();
		}else{
			int pointIndex=groupColumn.indexOf(".");
			if(pointIndex>=0){
				return groupColumn.substring(pointIndex+1);
			}else{
				return groupColumn;
			}
		}
	}
}
