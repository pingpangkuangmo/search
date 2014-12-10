package com.dboper.search.format;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.domain.QueryBody;

public interface ProcessUnit<T extends HashMap<String,Object>>{
	
	public String getName();

	public T prepareContext(QueryBody q);
	
	public Map<String,Object> processLineData(Map<String,Object> data,Map<String,Object> ret,List<Map<String,Object>> allRets,T context);
}
