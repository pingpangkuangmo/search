package com.dboper.search.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.format.value.ValueFormatterRule;

public class QueryBody implements Cloneable{

	private List<String> columns=new ArrayList<String>();
	private Map<String,Object> params=new HashMap<String,Object>();;
	private String order_by="";
	private int limit;
	private boolean distinct=false;
	private String action;
	private List<String> groupColumns=new ArrayList<String>();
	private List<ValueFormatterRule> format=new ArrayList<ValueFormatterRule>();
	private String tablesPath;
	private List<String> entityColumns=new ArrayList<String>();
	private List<String> deleteColumns=new ArrayList<String>();
	
	//传输数据用，不用于查询配置
	private boolean hasSon=false;
	private String fatherEntity;
	
	public QueryBody() {
		super();
	}
	
	@Override
	public QueryBody clone() throws CloneNotSupportedException {
		return (QueryBody)super.clone();
	}
	
	public String getFatherEntity() {
		return fatherEntity;
	}

	public void setFatherEntity(String fatherEntity) {
		this.fatherEntity = fatherEntity;
	}

	public boolean isHasSon() {
		return hasSon;
	}

	public void setHasSon(boolean hasSon) {
		this.hasSon = hasSon;
	}

	public List<String> getDeleteColumns() {
		return deleteColumns;
	}

	public void setDeleteColumns(List<String> deleteColumns) {
		this.deleteColumns = deleteColumns;
	}

	public List<String> getEntityColumns() {
		return entityColumns;
	}

	public void setEntityColumns(List<String> entityColumns) {
		this.entityColumns = entityColumns;
	}

	public String getTablesPath() {
		return tablesPath;
	}

	public void setTablesPath(String tablesPath) {
		this.tablesPath = tablesPath;
	}

	public List<ValueFormatterRule> getFormat() {
		return format;
	}

	public void setFormat(List<ValueFormatterRule> format) {
		this.format = format;
	}

	public List<String> getGroupColumns() {
		return groupColumns;
	}

	public void setGroupColumns(List<String> groupColumns) {
		this.groupColumns = groupColumns;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}



	public void addColumns(String column){
		this.columns.add(column);
	}
	
	public void putParams(String key,Object value){
		this.params.put(key, value);
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getOrder_by() {
		return order_by;
	}

	public void setOrder_by(String order_by) {
		this.order_by = order_by;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	
}
