package com.dboper.search.sqlparams.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dboper.search.sqlparams.util.Assert;

public class TimeSqlParamsParser extends AbstractValueSqlParamsParser{
	
	private String timeFormat="yyyy-MM-dd";
	
	private String fullTimeFlag="";
	
	private String[] opers=new String[]{"time>","time>=","time<","time<="};

	public TimeSqlParamsParser(){
		setOpers(opers);
	}
	
	public TimeSqlParamsParser(String timeFormat,String fullTimeFlag){
		setTimeFormat(timeFormat);
		setFullTimeFlag(fullTimeFlag);
		String[] newOpers=new String[opers.length];
		for(int i=0,len=opers.length;i<len;i++){
			newOpers[i]=this.fullTimeFlag+opers[i];
		}
		setOpers(newOpers);
	}
	
	public void setFullTimeFlag(String fullTimeFlag) {
		this.fullTimeFlag=fullTimeFlag==null?this.fullTimeFlag:fullTimeFlag;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	@Override
	protected String getKey(String key) {
		return "unix_timestamp("+key+")*1000";
	}

	@Override
	protected String getOper(String oper) {
		return oper.substring(4+fullTimeFlag.length());
	}

	/**
	 * 以d.time@time>'2015-3-1'为例
	 * 初始参数 key=d.time; value='2015-3-1'; oper=time>
	 * 解析后的key=unix_timestamp(d.time)*1000; value=1425139200000('2015-3-1'对应的毫秒数); oper=>
	 */
	@Override
	protected Object getStringValue(Object value) {
		if(value instanceof String){
			//去掉''的字符串处理
			String tmp=(String) value;
			Assert.isLarger(tmp.length(),2,"时间的value值 "+tmp+" 不合法");
			value=tmp.substring(1,tmp.length()-1);
		}
		return getObjectValue(value);
	}

	@Override
	protected Object getObjectValue(Object value) {
		if(value instanceof String){
			try {
				SimpleDateFormat format=new SimpleDateFormat(timeFormat);
				Date date=format.parse((String)value);
				value=date.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
				throw new IllegalArgumentException("timeFormat为"+timeFormat+";value="+value+";出现了解析异常");
			}
		}else{
			Assert.isInstanceof(value,Number.class,"时间参数必须为时间的毫秒数");
		}
		return value;
	}

}
