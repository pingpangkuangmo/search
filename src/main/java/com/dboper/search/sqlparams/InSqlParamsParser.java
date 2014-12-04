package com.dboper.search.sqlparams;


import java.lang.reflect.Array;
import java.util.Collection;

import org.springframework.util.Assert;

public class InSqlParamsParser implements SqlParamsHandler{

	@Override
	public boolean support(String oper) {
		if("in".equals(oper) || "notIn".equals(oper)){
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String getParams(String key, Object value, String oper) {
		Assert.notNull(value,"对于in和notIn操作，value不能为空");
		Object[] tmpValues=null;
		if(value instanceof Array){
			tmpValues=(Object[])value;
		}
		if(value instanceof Collection){
			tmpValues=((Collection)value).toArray();
		}
		Assert.notNull(tmpValues,"对于in和notIn操作，value只能为数组或者集合");
		StringBuilder sb=new StringBuilder(key);
		if(oper.equals("notIn")){
			sb.append(" not in");
		}else{
			sb.append(" ").append(oper);
		}
		sb.append("(");
		for(Object item:tmpValues){
			sb.append(item).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}

}
