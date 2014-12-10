package com.dboper.search.format.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueFormatterContext extends HashMap<String,Object>{

	private static final long serialVersionUID = -7747128323886803023L;

	Map<String,List<Map<String,Object>>> allRulesAndFormatters;

	public Map<String, List<Map<String, Object>>> getAllRulesAndFormatters() {
		return allRulesAndFormatters;
	}

	public void setAllRulesAndFormatters(
			Map<String, List<Map<String, Object>>> allRulesAndFormatters) {
		this.allRulesAndFormatters = allRulesAndFormatters;
	}
	
}
