package com.dboper.search.sqlparams.parser;

public class DefaultColumnSqlParamsParser extends AbstractColumnSqlParamsParser{

	public DefaultColumnSqlParamsParser(){
		setOpers(new String[]{"col_=","col_!=","col_>","col_<","col_>=","col_<="});
	}
	
	/**
	 * 只需更改oper，去掉col_前缀
	 */
	@Override
	protected String getOper(String oper) {
		return oper.substring("col_".length());
	}

}
