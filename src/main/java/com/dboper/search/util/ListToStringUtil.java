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
			if(GroupByUtils.containsGroupKey(obj)){
				str.append(obj+join);
			}else{
				if(obj.indexOf(tablePrefix)==0){
					str.append(obj+join);
				}else{
					str.append(tablePrefix+obj+join);
				}
			}
		}
		str.delete(str.length()-join.length(),str.length());
		return str.toString();
	}
	
	public static String getFullTable(String table,String tablePrefix,boolean prefix){
		if(table!=null && tablePrefix!=null){
			if(table.indexOf(tablePrefix)!=0 && prefix){
				return tablePrefix+table;
			}
		}
		return table;
	}
	
}
