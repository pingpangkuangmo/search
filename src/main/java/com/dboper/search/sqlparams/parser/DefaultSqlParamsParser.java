package com.dboper.search.sqlparams.parser;

public class DefaultSqlParamsParser extends AbstractValueSqlParamsParser{
	
	public DefaultSqlParamsParser(){
		setOpers(new String[]{"=","!=","is","is not",">","<",">=","<=","like"});
	}
}
