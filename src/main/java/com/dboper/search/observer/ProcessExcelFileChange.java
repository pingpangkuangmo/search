package com.dboper.search.observer;

import java.util.Map;

import com.dboper.search.excel.base.ExcelConfigBody;

public interface ProcessExcelFileChange {

	public void processExcelConfigChange(Map<String,ExcelConfigBody> change,String fileName);
}
