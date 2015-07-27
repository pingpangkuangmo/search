package com.dboper.search.format.value;

import java.util.Map;

public class MapValueFormatter implements ValueFormatter{

	@Override
	public Object format(Object value, Map<Object, Object> formatRule) {
		Object formatValue=formatRule.get(value);
		if(formatValue==null){
			formatValue=formatRule.get(value+"");
			if(formatValue==null){
				formatValue=formatRule.get("'"+value+"'");
				if(formatValue==null){
					return value;
				}
			}
		}
		return formatValue;
	}

	@Override
	public String getType() {
		return "map";
	}

}
