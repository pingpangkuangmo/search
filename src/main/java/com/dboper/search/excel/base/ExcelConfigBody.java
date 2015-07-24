package com.dboper.search.excel.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelConfigBody {

	private List<String> columns=new ArrayList<String>();
	private Map<String,String> columnLabels=new HashMap<String,String>();
	private Map<String,Integer> columnsType=new HashMap<String,Integer>();
	
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public Map<String, String> getColumnLabels() {
		return columnLabels;
	}
	public void setColumnLabels(Map<String, String> columnLabels) {
		this.columnLabels = columnLabels;
	}
	public Map<String, Integer> getColumnsType() {
		return columnsType;
	}
	public void setColumnsType(Map<String, Integer> columnsType) {
		this.columnsType = columnsType;
	}
}
