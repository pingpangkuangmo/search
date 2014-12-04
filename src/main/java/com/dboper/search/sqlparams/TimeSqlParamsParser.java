package com.dboper.search.sqlparams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeSqlParamsParser implements SqlParamsHandler{

	private String[] supportOpers=new String[]{"time>","time>=","time<","time<="};
	
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
	public String getParams(String key, Object value, String oper) {
		String timeKey="unix_timestamp("+key+")";
		String realOper=oper.substring(4);
		StringBuilder sb=new StringBuilder(timeKey);
		if(value instanceof String){
			String tmp=(String)value;
			value=tmp.substring(1,tmp.length()-1);
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date date=format.parse((String)value);
				value=date.getTime()/1000;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		sb.append(" ").append(realOper).append(" ").append(value);
		return sb.toString();
	}

}
