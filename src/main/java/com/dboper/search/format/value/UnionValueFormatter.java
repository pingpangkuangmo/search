package com.dboper.search.format.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dboper.search.domain.QueryBody;
import com.dboper.search.util.MapUtil;


public class UnionValueFormatter {

	private Map<String,ValueFormatter> formattersMap;
	
	private static final String RULE="rule";
	private static final String FORMATTER="formatter";
	
	public UnionValueFormatter(){
		formattersMap=new HashMap<String,ValueFormatter>();
		ValueFormatter mapValueFormatter=new MapValueFormatter();
		formattersMap.put(mapValueFormatter.getType(),mapValueFormatter);
		ValueFormatter regexValueFormatter=new RegexValueFormatter();
		formattersMap.put(regexValueFormatter.getType(),regexValueFormatter);
	}
	
	public List<Map<String, Object>> formatValue(List<Map<String, Object>> data, QueryBody q) {
		List<ValueFormatterRule> rules=q.getFormat();
		if(rules.size()>0){
			//先找出涉及到哪些 ValueFormatter
			Map<String,List<Map<String,Object>>> allRulesAndFormatters=new HashMap<String,List<Map<String,Object>>>(); 
			for(ValueFormatterRule rule:rules){
				ValueFormatter formatter=formattersMap.get(rule.getRuleType());
				if(formatter!=null){
					String column=rule.getColumn();
					List<Map<String,Object>> formatters=allRulesAndFormatters.get(column);
					if(formatters==null){
						formatters=new ArrayList<Map<String,Object>>();
						allRulesAndFormatters.put(column,formatters);
					}
					formatters.add(MapUtil.getMap(RULE,rule,FORMATTER,formatter));
				}
			}
			if(allRulesAndFormatters.size()>0){
				for(Map<String,Object> dataItem:data){
					for(String column:allRulesAndFormatters.keySet()){
						Object value=dataItem.get(column);
						List<Map<String,Object>> formatters=allRulesAndFormatters.get(column);
						for(Map<String,Object> formatterItem:formatters){
							ValueFormatter formatter=(ValueFormatter)formatterItem.get(FORMATTER);
							ValueFormatterRule rule=(ValueFormatterRule)formatterItem.get(RULE);
							dataItem.put(column,formatter.format(value,rule.getRuleBody()));
						}
					}
				}
			}
		}
		return data;
	}
}
