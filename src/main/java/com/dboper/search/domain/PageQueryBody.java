package com.dboper.search.domain;

public class PageQueryBody {

	private QueryBody q;
	private Integer start;
	private Integer limit;
	public QueryBody getQ() {
		return q;
	}
	public void setQ(QueryBody q) {
		this.q = q;
	}
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
}
