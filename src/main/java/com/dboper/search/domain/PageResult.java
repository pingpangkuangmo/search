package com.dboper.search.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageResult {

	private Integer start;
	private Integer limit;
	private Integer total;
	private List<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
	public Integer getStart() {
		return start;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public List<Map<String, Object>> getData() {
		return data;
	}
	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}
}
