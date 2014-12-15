package com.dboper.search.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

	public static <T> List<T> intersection(List<T> list1,List<T> list2){
		if(list1==null || list2==null){
			return new ArrayList<T>();
		}
		List<T> ret=new ArrayList<T>();
		for(T t:list1){
			if(list2.contains(t)){
				ret.add(t);
			}
		}
		return ret;
	}
}
