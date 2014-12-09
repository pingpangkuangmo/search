package com.dboper.search.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.format.Rule;

public class QueryBody implements Cloneable{

	private List<String> columns=new ArrayList<String>();;
	private Map<String,Object> params=new HashMap<String,Object>();;
	private String order_by="";
	private int limit;
	private boolean distinct=false;
	private String action;
	private List<String> groupColumns=new ArrayList<String>();
	private List<Rule> format=new ArrayList<Rule>();
	private String tablesPath;
	
	public QueryBody() {
		super();
	}
	
	@Override
	public QueryBody clone() throws CloneNotSupportedException {
		return (QueryBody)super.clone();
	}

	public String getTablesPath() {
		return tablesPath;
	}

	public void setTablesPath(String tablesPath) {
		this.tablesPath = tablesPath;
	}

	public List<Rule> getFormat() {
		return format;
	}

	public void setFormat(List<Rule> format) {
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
