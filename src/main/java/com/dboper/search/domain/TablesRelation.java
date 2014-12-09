package com.dboper.search.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TablesRelation {

	private String tableOne;
	private String tableTwo;
	private String tablesStr;
	private String relation;
	
	public void initTablesStr(){
		List<String> tables=Arrays.asList(tableOne,tableTwo);
		Collections.sort(tables);
		tablesStr=tables.get(0)+"__"+tables.get(1);
	}
	
	public String getRelation() {
		return relation;
	}
	
	public void setRelation(String relation) {
		this.relation = relation;
	}



	public String getTableOne() {
		return tableOne;
	}
	public void setTableOne(String tableOne) {
		this.tableOne = tableOne;
	}
	public String getTableTwo() {
		return tableTwo;
	}
	public void setTableTwo(String tableTwo) {
		this.tableTwo = tableTwo;
	}
	public String getTablesStr() {
		return tablesStr;
	}
	public void setTablesStr(String tablesStr) {
		this.tablesStr = tablesStr;
	}
}
