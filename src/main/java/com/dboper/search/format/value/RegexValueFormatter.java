package com.dboper.search.format.value;

import java.util.Map;

public class RegexValueFormatter implements ValueFormatter{

	@Override
	public String getType() {
		return "regex";
	}

	@Override
	public Object format(Object value, Map<Object, Object> formatRule) {
		String regex=(String)formatRule.get("regex");
		String replacement=(String)formatRule.get("replacement");
		if(value!=null && value instanceof String){
			return ((String)value).replaceAll(regex,replacement);
		}
		return value;
	}

}
