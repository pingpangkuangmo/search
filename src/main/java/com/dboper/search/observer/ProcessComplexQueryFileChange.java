package com.dboper.search.observer;

import java.util.Map;

import com.dboper.search.domain.ComplexQueryBody;

public interface ProcessComplexQueryFileChange {

	public void processComplexQueryBodyChange(Map<String,ComplexQueryBody> change,String fileName);
}
