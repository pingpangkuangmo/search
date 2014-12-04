package com.dboper.search.util;

import java.util.Map;
import java.util.Set;

public class MapToStringUtil {

	public static String mapToString(Map<String,Object> map,String join,String tablePrefix){
		if(map.isEmpty()){
			return "";
		}
		StringBuilder str=new StringBuilder();
		Set<String> keys=map.keySet();
		for(String key:keys){
			Object value=map.get(key);
			String oper="=";
			String tmp_key=key;
			if(key.endsWith(">=")){
				oper=">=";
				tmp_key=key.substring(0,key.indexOf(">="));
			}else if(key.endsWith("<=")){
				oper="<=";
				tmp_key=key.substring(0,key.indexOf("<="));
			}else if(key.endsWith("!=")){
				oper="!=";
				tmp_key=key.substring(0,key.indexOf("!="));
			}else if(key.endsWith("_in")){
				oper="in";
				tmp_key=key.substring(0,key.indexOf("_in"));
			}else if(value instanceof String && ((String)value).contains("%")){
				oper=" like ";
			}else if(value==null){
				oper="is";
			}
			if(key.contains("time")){
				tmp_key="unix_timestamp("+tablePrefix+tmp_key+")";
				str.append(tmp_key+oper+map.get(key)+" "+join+" ");
			}else if(value instanceof Integer || value instanceof Long || oper.equals("in") || oper.equals("is")){
				str.append(tablePrefix+tmp_key+" "+oper+" "+map.get(key)+" "+join+" ");
			}else{
				str.append(tablePrefix+tmp_key+oper+"'"+map.get(key)+"' "+join+" ");
			}
		}
		int len=str.length();
		str.delete(len-join.length()-2,len-1);
		return str.toString();
	}
	
}
