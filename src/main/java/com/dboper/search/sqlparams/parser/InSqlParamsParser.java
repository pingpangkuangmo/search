package com.dboper.search.sqlparams.parser;

import java.util.Collection;

import com.dboper.search.sqlparams.util.Assert;

public class InSqlParamsParser extends AbstractValueSqlParamsParser{
	
	public InSqlParamsParser(){
		setOpers(new String[]{"in","notIn"});
	}

	@Override
	protected String getOper(String oper) {
		if(oper.equals("notIn")){
			return "not in";
		}else{
			return "in";
		}
	}

	/**
	 * 以 c.id in [1,2,3] 为例
	 * 最初 key=c.id ; value为[1,2,3]数组或者集合 ; oper为in
	 * 解析后key=c.id ; value变为sql中的(1,2,3)这样的字符串 ; oper变为in(如果之前是notIn，则变为sql中的 not in 字符串)
	 */
	@Override
	protected Object getStringValue(Object value) {
		Assert.notNull(value,"对于in和notIn操作，value不能为空");
		Object[] tmpValues=null;
		if(value.getClass().isArray()){
			tmpValues=(Object[])value;
		}
		if(value instanceof Collection){
			tmpValues=((Collection<?>)value).toArray();
		}
		Assert.notEmpty(tmpValues,"对于in和notIn操作，value只能为数组或者集合，并且长度必须大于0");
		StringBuilder valueStr=new StringBuilder();
		valueStr.append("(");
		for(Object item:tmpValues){
			valueStr.append(item).append(",");
		}
		valueStr.deleteCharAt(valueStr.length()-1);
		valueStr.append(")");
		return valueStr.toString();
	}

	@Override
	protected Object getObjectValue(Object value) {
		return getStringValue(value);
	}

}
