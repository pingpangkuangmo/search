package com.dboper.search.observer;

import java.util.Map;

import com.dboper.search.domain.QueryBody;

public interface ProcessFileChange {

	public void processChange(Map<String,QueryBody> change,String fileName);
}
