package com.dboper.search.cache;

import java.util.HashMap;
import java.util.List;

public class EntityNameContext extends HashMap<String,Object>{
	
	private static final long serialVersionUID = 6374704626585922875L;
	private static final String RELATION="relation";
	private static final String COLUMNS="columns";
	
	public String getRelation() {
		return (String)get(RELATION);
	}
	
	public void setRelation(String relation){
		put(RELATION,relation);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getColumns(){
		return (List<String>)get(COLUMNS);
	}
	
	public void setColumns(List<String> columns){
		put(COLUMNS, columns);
	}
}
