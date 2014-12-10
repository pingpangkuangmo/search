package com.dboper.search.format.value;

import java.util.Map;

public interface ValueFormatter {

	public String getType();
	
	public Object format(Object value,Map<Object,Object> formatRule);
	
}
