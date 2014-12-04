package com.dboper.search.sqlparams;

public interface SqlParamsHandler {

	public boolean support(String oper);
	
	public String getParams(String key,Object value,String oper);
}
