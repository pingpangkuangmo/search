package com.dboper.search.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexQueryBody {

	private String firstAction;
	private Map<String,SecondQueryBody> secondQuery=new HashMap<String,SecondQueryBody>();
	private Map<String,Object> params=new HashMap<String,Object>();
	private List<AppendQueryBody> appendsQuery= new ArrayList<AppendQueryBody>();
	
	public List<AppendQueryBody> getAppendsQuery() {
		return appendsQuery;
	}
	public void setAppendsQuery(List<AppendQueryBody> appendsQuery) {
		this.appendsQuery = appendsQuery;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public String getFirstAction() {
		return firstAction;
	}
	public void setFirstAction(String firstAction) {
		this.firstAction = firstAction;
	}
	public Map<String, SecondQueryBody> getSecondQuery() {
		return secondQuery;
	}
	public void setSecondQuery(Map<String, SecondQueryBody> secondQuery) {
		this.secondQuery = secondQuery;
	}
}
