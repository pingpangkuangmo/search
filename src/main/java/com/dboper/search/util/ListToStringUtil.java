package com.dboper.search.util;

import java.util.List;

public class ListToStringUtil {

	public static String arrayToString(List<String> list,String join){
		StringBuilder str=new StringBuilder();
		for(String obj:list){
			str.append(obj+join);
		}
		str.delete(str.length()-join.length(),str.length());
		return str.toString();
	}
	
	public static String arrayToStringAliases(List<String> list,String join,String tablePrefix){
		StringBuilder str=new StringBuilder();
		for(String obj:list){
			str.append(tablePrefix+obj+join);
		}
		str.delete(str.length()-join.length(),str.length());
		return str.toString();
	}
	
}
