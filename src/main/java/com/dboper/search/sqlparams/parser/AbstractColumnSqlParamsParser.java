package com.dboper.search.sqlparams.parser;

public abstract class AbstractColumnSqlParamsParser extends AbstractValueSqlParamsParser{

	/**
	 * 这一系列的类用于处理key value都是column名称的情况，所以obj是字符串，但是不用加上''这样的字符串处理
	 */
	@Override
	protected Object processStringValue(Object obj) {
		return obj;
	}

}
