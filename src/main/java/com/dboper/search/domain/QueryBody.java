package com.dboper.search.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.format.value.ValueFormatterRule;

public class QueryBody implements Cloneable{

	private List<String> columns=new ArrayList<String>();
	private Map<String,Object> params=new HashMap<String,Object>();
	private Integer limit=Integer.MAX_VALUE;
	private Integer start=0;
	private boolean distinct=true;
	private String action;
	private List<String> groupColumns=new ArrayList<String>();
	private List<ValueFormatterRule> format=new ArrayList<ValueFormatterRule>();
	private String tablesPath;
	private List<String> entityColumns=new ArrayList<String>();
	private List<String> deleteColumns=new ArrayList<String>();
	private String groupBy;
	
	private Map<String,Object> constantData=new HashMap<String,Object>();
	
	private Map<String,Object> originConstantData=new HashMap<String,Object>();
	
	private String unionTablesPath;
	
	private Map<String,Object> unionParams=new HashMap<String,Object>();
	
	private Map<String,String> baseLists=new HashMap<String,String>();
	
	private List<String> processors=new ArrayList<String>();
	
	private Map<String,SonSearchBody> sonSearchs=new HashMap<String,SonSearchBody>();
	private List<String> order_by=new ArrayList<String>();
	
	private Map<String,Map<String,Object>> sonParams=new HashMap<String,Map<String,Object>>();
	
	//传输数据用，不用于查询配置
	private boolean hasSon=false;
	private String fatherEntity;
	private Map<String,String> tableAlias=new HashMap<String,String>();
	private String cacheKey;
	
	public QueryBody() {
		super();
	}
	
	@Override
	public QueryBody clone() throws CloneNotSupportedException {
		return (QueryBody)super.clone();
	}
	
	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public Map<String, String> getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(Map<String, String> tableAlias) {
		this.tableAlias = tableAlias;
	}

	public Map<String, Map<String, Object>> getSonParams() {
		return sonParams;
	}

	public void setSonParams(Map<String, Map<String, Object>> sonParams) {
		this.sonParams = sonParams;
	}

	public Map<String, SonSearchBody> getSonSearchs() {
		return sonSearchs;
	}

	public void setSonSearchs(Map<String, SonSearchBody> sonSearchs) {
		this.sonSearchs = sonSearchs;
	}

	public List<String> getOrder_by() {
		return order_by;
	}

	public void setOrder_by(List<String> order_by) {
		this.order_by = order_by;
	}
	
	public Map<String, Object> getOriginConstantData() {
		return originConstantData;
	}

	public void setOriginConstantData(Map<String, Object> originConstantData) {
		this.originConstantData = originConstantData;
	}

	public List<String> getProcessors() {
		return processors;
	}

	public void setProcessors(List<String> processors) {
		this.processors = processors;
	}

	public Map<String, String> getBaseLists() {
		return baseLists;
	}

	public void setBaseLists(Map<String, String> baseLists) {
		this.baseLists = baseLists;
	}

	public Map<String, Object> getUnionParams() {
		return unionParams;
	}

	public void setUnionParams(Map<String, Object> unionParams) {
		this.unionParams = unionParams;
	}

	public String getUnionTablesPath() {
		return unionTablesPath;
	}

	public void setUnionTablesPath(String unionTablesPath) {
		this.unionTablesPath = unionTablesPath;
	}

	public Map<String, Object> getConstantData() {
		return constantData;
	}

	public void setConstantData(Map<String, Object> constantData) {
		this.constantData = constantData;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
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

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	
}
