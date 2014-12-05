package com.dboper.search.format;

import java.util.Map;

public class Rule {

	private String ruleType;
	private String column;
	private Map<Object,Object> ruleBody;
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public Map<Object, Object> getRuleBody() {
		return ruleBody;
	}
	public void setRuleBody(Map<Object, Object> ruleBody) {
		this.ruleBody = ruleBody;
	}
}
