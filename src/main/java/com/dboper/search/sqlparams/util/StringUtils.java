package com.dboper.search.sqlparams.util;

public class StringUtils {

	public static boolean isNotEmpty(String str){
		if(str!=null && str.length()>0){
			return true;
		}
		return false;
	}
	
	public static boolean isNotEmpty(StringBuilder sb){
		if(sb!=null && sb.length()>0){
			return true;
		}
		return false;
	}
	
}
