package com.dboper.search.sqlparams;

import java.util.ArrayList;
import java.util.List;

public class SqlParamsParseResult {

	private StringBuilder baseWhereSql;
	private List<Object> arguments;
	
	public SqlParamsParseResult() {
		super();
		arguments=new ArrayList<Object>();
		baseWhereSql=new StringBuilder();
	}
	
	public void addSqlModel(String sqlModel){
		if(sqlModel!=null){
			baseWhereSql.append(sqlModel);
		}
	}
	
	public void addArgument(Object argument){
		arguments.add(argument);
	}
	
	public void addArguments(List<Object> arguments){
		this.arguments.addAll(arguments);
	}
	
	public StringBuilder getBaseWhereSql() {
		return baseWhereSql;
	}
	public void setBaseWhereSql(StringBuilder baseWhereSql) {
		this.baseWhereSql = baseWhereSql;
	}
	public List<Object> getArguments() {
		return arguments;
	}
	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}
	
}
