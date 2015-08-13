package com.dboper.search.sqlparams.parser;

public interface SqlParamsParser {

	/**
	 * @param oper 操作符
	 * @return
	 */
	public boolean support(String oper);
	
	/**
	 * 例如a.age@>12
	 * @param key	a.age
	 * @param value	12
	 * @param oper	>
	 * @return	返回字符串 a.age>12
	 */
	public String getParams(String key,Object value,String oper);
	
	/**
	 * 例如d.time@time>'2015-3-1'
	 * @param key	d.time
	 * @param value	2015-3-1 
	 * @param oper	time>
	 * @return	返回结果保存的key=unix_timestamp(d.time)*1000; value=1425139200000('2015-3-1'对应的毫秒数); oper=>
	 */
	public SqlParamsParseItemResult getPlaceHolderParamsResult(String key,Object value,String oper);
	
	public SqlParamsParseItemResult getParamsResult(String key,Object value,String oper);

}
