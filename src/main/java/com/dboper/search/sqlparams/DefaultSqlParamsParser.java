package com.dboper.search.sqlparams;

import org.springframework.util.StringUtils;

public class DefaultSqlParamsParser implements SqlParamsHandler{
	
	private String[] supportOpers=new String[]{"=","!=","is",">","<",">=","<=","like"};

	@Override
	public boolean support(String oper) {
		for(String item:supportOpers){
			if(item.equals(oper)){
				return true;
			}
		}
		return false;
	}

	@Override
	public String getParams(String key, Object value,String oper) {
		if(StringUtils.hasLength(key)){
			StringBuilder sb=new StringBuilder(key);
			sb.append(" ").append(oper).append(" ").append(value);
			return sb.toString();
		}else{
			return "";
		}
	}

}
