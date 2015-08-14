package com.dboper.search.domain;

import java.util.HashMap;
import java.util.Map;

public class SonSearchBody {

	private String sql;
	
	private String relation;
	
	private Map<String,Object> params=new HashMap<String,Object>();
	
	@Override
	public String toString() {
		return "SonSearchBody [sql=" + sql + ", relation=" + relation
				+ ", params=" + params + "]";
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
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
