package com.dboper.search.format.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.domain.QueryBody;

public class FormFormatterContext extends HashMap<String,Object>{

	private static final long serialVersionUID = -8601161044117102087L;

	private Map<String,ColumnsFormatBody> columnsInfo;
	
	private List<FormFormatter> containsFormFormatters;
	
	private QueryBody q;

	public Map<String, ColumnsFormatBody> getColumnsInfo() {
		return columnsInfo;
	}

	public void setColumnsInfo(Map<String, ColumnsFormatBody> columnsInfo) {
		this.columnsInfo = columnsInfo;
	}

	public List<FormFormatter> getContainsFormFormatters() {
		return containsFormFormatters;
	}

	public void setContainsFormFormatters(List<FormFormatter> containsFormFormatters) {
		this.containsFormFormatters = containsFormFormatters;
	}

	public QueryBody getQ() {
		return q;
	}

	public void setQ(QueryBody q) {
		this.q = q;
	}
	
}
