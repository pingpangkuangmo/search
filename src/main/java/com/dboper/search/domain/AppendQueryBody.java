package com.dboper.search.domain;

public class AppendQueryBody {

	private String keyField;
	private String secondAction;
	private String appendKey;
	private Boolean complex;
	private String paramsKey;
	
	public String getParamsKey() {
		return paramsKey;
	}
	public void setParamsKey(String paramsKey) {
		this.paramsKey = paramsKey;
	}
	public String getKeyField() {
		return keyField;
	}
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
	public String getSecondAction() {
		return secondAction;
	}
	public void setSecondAction(String secondAction) {
		this.secondAction = secondAction;
	}
	public String getAppendKey() {
		return appendKey;
	}
	public void setAppendKey(String appendKey) {
		this.appendKey = appendKey;
	}
	public Boolean getComplex() {
		return complex;
	}
	public void setComplex(Boolean complex) {
		this.complex = complex;
	}
}
