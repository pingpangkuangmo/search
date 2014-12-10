package com.dboper.search.format.form;

import java.util.List;
import java.util.Map;

import com.dboper.search.domain.QueryBody;

public interface FormFormatter {

	public String getFormatterType();
	
	public Map<String, Object> fromat(Map<String, Object> item,Map<String,Object> fatherTotal,
			List<Map<String,Object>> data,ColumnsFormatBody columnsFormatBody, QueryBody q);
}
