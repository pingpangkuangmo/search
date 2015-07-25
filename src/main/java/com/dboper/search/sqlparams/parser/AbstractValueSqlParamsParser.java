package com.dboper.search.sqlparams.parser;

public abstract class AbstractValueSqlParamsParser extends AbstractSqlParamsParser{

	@Override
	protected SqlParamsParseItemResult doStringValueParams(String key,Object value, String oper) {
		return new SqlParamsParseItemResult(getKey(key),getOper(oper),getStringValue(processStringValue(value)));
	}

	@Override
	protected SqlParamsParseItemResult doObjectValueParams(String key,Object value, String oper) {
		return new SqlParamsParseItemResult(getKey(key),getOper(oper),getObjectValue(value));
	}
	
	protected String getKey(String key){
		return key;
	}
	
	protected String getOper(String oper){
		return oper;
	}
	
	/**
	 * 
	 * @param value  value如果是字符串，则含有'value' 单引号处理
	 * @return
	 */
	protected Object getStringValue(Object value){
		return value;
	}
	
	/**
	 * 
	 * @param value  就是原始的value,没有经过单引号处理
	 * @return
	 */
	protected Object getObjectValue(Object value){
		return value;
	}
	
	

}
