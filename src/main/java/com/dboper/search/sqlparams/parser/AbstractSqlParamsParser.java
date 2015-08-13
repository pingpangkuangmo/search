package com.dboper.search.sqlparams.parser;

import com.dboper.search.sqlparams.util.SqlStringUtils;

public abstract class AbstractSqlParamsParser implements SqlParamsParser{

	/**
	 * 该解析器所支持的所有操作符
	 */
	private String[] opers;
	
	/**
	 * 进行操作符匹配的时候是否忽略大小写
	 */
	private boolean ignoreCase=true;
	
	/**
	 * 用于子类调用，来设置操作符
	 * @param opers
	 */
	protected void setOpers(String[] opers){
		this.opers=opers;
	}
	
	/**
	 * 用于子类调用，设置是否忽略大小写
	 * @param ignoreCase
	 */
	protected void setIgnoreCase(boolean ignoreCase){
		this.ignoreCase=ignoreCase;
	}
	
	/**
	 * 判断该解析器是否支持oper这个操作符
	 */
	@Override
	public boolean support(String oper) {
		if(opers!=null && oper!=null){
			for(String operItem:opers){
				if(ignoreCase){
					operItem=operItem.toLowerCase();
					oper=oper.toLowerCase();
				}
				if(operItem.equals(oper)){
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public SqlParamsParseItemResult getParamsResult(String key, Object value,
			String oper) {
		return doStringValueParams(key,value, oper);
	}
	
	@Override
	public SqlParamsParseItemResult getPlaceHolderParamsResult(String key,
			Object value, String oper) {
		return doObjectValueParams(key,value,oper);
	}

	/**
	 * 根据解析结果SqlParamsParseItemResult，得到对应的sql字符串
	 */
	@Override
	public String getParams(String key, Object value, String oper) {
		SqlParamsParseItemResult sqlParamsParseResult=doStringValueParams(key,processStringValue(value), oper);
		StringBuilder sb=new StringBuilder();
		if(sqlParamsParseResult!=null){
			sb.append(sqlParamsParseResult.getKey()).append(" ");
			sb.append(sqlParamsParseResult.getOper()).append(" ");
			sb.append(sqlParamsParseResult.getValue());
		}
		return sb.toString();
	}
	
	/**
	 * 当前解析器对key value oper进行相应的解析，由子类来具体实现，以d.time@time>'2015-3-1'
	 * @param key 如 d.time
	 * @param value 如 2015-3-1
	 * @param oper	如 time>
	 * @return  进行相应的解析，key value oper 进行相应的变化，key=unix_timestamp(d.time)*1000; value=1425139200000('2015-3-1'对应的毫秒数); oper=>
	 */
	protected abstract SqlParamsParseItemResult doStringValueParams(String key, Object value, String oper);
	
	
	/**
	 * 与上述方法的主要区别是，对于value的处理。若sql希望包含占位符，此时value则应为正常类型，如list(1,2,3)，若sql不含占位符，则value要转化成对应的string，拼接进去，如(1,2,3)
	 */
	protected abstract SqlParamsParseItemResult doObjectValueParams(String key, Object value, String oper);

	/**
	 * 子类可以复写该方法，可以对obj不进行处理
	 * @param obj 对于obj进行字符串处理，即加上''
	 * @return 返回处理后的结果如字符串 abc--> 'abc'
	 */
	protected Object processStringValue(Object obj) {
		return SqlStringUtils.processString(obj);
	}
	
	
	
}
