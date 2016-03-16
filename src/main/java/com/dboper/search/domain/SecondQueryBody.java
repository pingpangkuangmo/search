package com.dboper.search.domain;

public class SecondQueryBody {

	private String keyField;
	private String secondAction;
	private String paramsKey;
	private boolean complex;
	
	
	public boolean isComplex() {
		return complex;
	}
	public void setComplex(boolean complex) {
		this.complex = complex;
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
	public String getParamsKey() {
		return paramsKey;
	}
	public void setParamsKey(String paramsKey) {
		this.paramsKey = paramsKey;
	}
	
}
