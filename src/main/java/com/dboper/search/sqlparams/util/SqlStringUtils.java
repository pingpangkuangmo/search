package com.dboper.search.sqlparams.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlStringUtils {

	public static Object processString(Object obj){
		if(isString(obj)){
			String tmp=obj+"";
			if(tmp.startsWith("'") && tmp.endsWith("'")){
				return tmp;
			}
			obj="'"+obj+"'";
		}
		if(obj!=null && (obj instanceof Collection || obj instanceof Array)){
			List<Object> newObj=new ArrayList<Object>();
			for(Object item:(Iterable<?>)obj){
				if(isString(item)){
					newObj.add("'"+item+"'");
				}else{
					newObj.add(item);
				}
			}
			obj=newObj;
		}
		return obj;
	}
	
	private static boolean  isString(Object obj){
		if(obj!=null && (obj instanceof String || obj instanceof Enum)){
			return true;
		}else{
			return false;
		}
	}
}
