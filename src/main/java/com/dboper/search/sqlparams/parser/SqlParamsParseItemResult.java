package com.dboper.search.sqlparams.parser;

public class SqlParamsParseItemResult {

	private String key;
	private String oper;
	private Object value;
	
	public SqlParamsParseItemResult() {
		super();
	}
	
	public SqlParamsParseItemResult(String key, String oper, Object value) {
		super();
		this.key = key;
		this.oper = oper;
		this.value = value;
	}
	
	public String getSqlModel(boolean isPlaceHolder,String placeHolder){
		StringBuilder sb=new StringBuilder();
		sb.append(key).append(" ").append(oper).append(" ");
		if(isPlaceHolder){
			sb.append(placeHolder);
		}else{
			sb.append(value);
		}
		return sb.toString();
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getOper() {
		return oper;
	}
	public void setOper(String oper) {
		this.oper = oper;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
 }
