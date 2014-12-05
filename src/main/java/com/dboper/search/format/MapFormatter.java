package com.dboper.search.format;

import java.util.Map;

public class MapFormatter implements Formatter{

	@Override
	public Object format(Object value, Map<Object, Object> formatRule) {
		Object formatValue=formatRule.get(value);
		if(formatValue==null){
			return value;
		}
		return formatValue;
	}

	@Override
	public String getType() {
		return "map";
	}

}
