package com.dboper.search.domain;

public class SonSearchBody {

	private String sql;
	
	private String relation;
	
	@Override
	public String toString() {
		return "SonSearchBody [sql=" + sql + ", relation=" + relation + "]";
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	
}
