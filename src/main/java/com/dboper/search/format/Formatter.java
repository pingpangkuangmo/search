package com.dboper.search.format;

import java.util.Map;

public interface Formatter {

	public String getType();
	
	public Object format(Object value,Map<Object,Object> formatRule);
	
}
