package com.dboper.search.util;

import java.util.List;

public class TablesRelationUtil {

	public static String getTablesStr(List<String> tables){
		String tmp=ListToStringUtil.arrayToString(tables,"__");
		return tmp;
	}
}
