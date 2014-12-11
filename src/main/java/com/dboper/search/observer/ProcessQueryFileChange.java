package com.dboper.search.observer;

import java.util.Map;

import com.dboper.search.domain.QueryBody;

public interface ProcessQueryFileChange {

	public void processQueryBodyChange(Map<String,QueryBody> change,String fileName);
}
