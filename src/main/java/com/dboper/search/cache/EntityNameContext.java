package com.dboper.search.cache;

import java.util.HashMap;
import java.util.List;

public class EntityNameContext extends HashMap<String,Object>{
	
	private static final long serialVersionUID = 6374704626585922875L;
	private static final String RELATION="relation";
	private static final String COLUMNS="columns";
	private static final String HAS_SON="hasSon";
	private static final String FATHER_ENTITY="fatherEntity";
	private static final String DELETE_COLUMNS="deleteColumns";
	
	@SuppressWarnings("unchecked")
	public List<String> getDeleteColumns(){
		return (List<String>) get(DELETE_COLUMNS);
	}
	
	public void setDeleteColumns(List<String> deleteColumns){
		put(DELETE_COLUMNS,deleteColumns);
	}
	
	public String getFatherEntity(){
		return (String) get(FATHER_ENTITY);
	}
	
	public void setFatherEntity(String fatherEntity){
		put(FATHER_ENTITY, fatherEntity);
	}
	
	public boolean getHasSon(){
		return (Boolean)get(HAS_SON);
	}
	
	public void setHasSon(boolean hasSon){
		put(HAS_SON,hasSon);
	}

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
