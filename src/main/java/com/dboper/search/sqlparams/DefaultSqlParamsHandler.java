package com.dboper.search.sqlparams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dboper.search.sqlparams.parser.DefaultColumnSqlParamsParser;
import com.dboper.search.sqlparams.parser.DefaultSqlParamsParser;
import com.dboper.search.sqlparams.parser.InSqlParamsParser;
import com.dboper.search.sqlparams.parser.SqlParamsParseItemResult;
import com.dboper.search.sqlparams.parser.SqlParamsParser;
import com.dboper.search.sqlparams.parser.TimeSqlParamsParser;
import com.dboper.search.sqlparams.util.SqlStringUtils;
import com.dboper.search.sqlparams.util.StringUtils;
import com.dboper.search.util.ListToStringUtil;

/**
 * 默认注册了几个常用的SqlParamsParser，也可以扩展自定义的SqlParamsParser
 */
public class DefaultSqlParamsHandler {
	
	private static final String AND="and";
	private static final String OR="or";
	private static final String PLACE_HOLDER="?";
	private String andKey="$and";
	private String orKey="$or";
	private String separatorFlag="@";
	private List<SqlParamsParser> sqlParamsParsers;
	
	private String keyPrefix="";

	public DefaultSqlParamsHandler(){
		registerDefaultHandlers();
	}
	
	public DefaultSqlParamsHandler(String keyPrefix){
		if(keyPrefix!=null){
			this.keyPrefix=keyPrefix;
		}
		registerDefaultHandlers();
	}
	
	private void registerDefaultHandlers(){
		sqlParamsParsers=new ArrayList<SqlParamsParser>();
		sqlParamsParsers.add(new DefaultSqlParamsParser());
		sqlParamsParsers.add(new InSqlParamsParser());
		sqlParamsParsers.add(new TimeSqlParamsParser());
		sqlParamsParsers.add(new TimeSqlParamsParser("yyyy-MM-dd HH:mm:ss","full_"));
		sqlParamsParsers.add(new DefaultColumnSqlParamsParser());
	}
	
	/**
	 * 如返回SqlParamsParseResult(参数baseWhereSql= a.id>?  参数arguments=[12])
	 * @param params 查询参数的map集合
	 * @return	返回带有占位符的sql条件，占位符对应的值存在SqlParamsParseResult的arguments参数中
	 */
	public SqlParamsParseResult getSqlWhereParamsResult(Map<String,Object> params,boolean prefix){
		return getSqlWhereParamsResultByAndOr(params,AND,true,prefix,new SqlParamsParseResult());
	}
	
	/**
	 * @param params
	 * @return	不进行占位符策略，直接返回拼接的字符串，如   a.id>12
	 */
	public String getSqlWhereParams(Map<String,Object> params,boolean prefix){
		return getSqlWhereParamsResultByAndOr(params,AND,false,prefix,new SqlParamsParseResult()).getBaseWhereSql().toString();
	}
	
	@SuppressWarnings("unchecked")
	private SqlParamsParseResult getSqlWhereParamsResultByAndOr(Map<String,Object> params,String andOr,
			boolean isPlaceHolder,boolean prefix,SqlParamsParseResult sqlParamsParseResult){
		if(params!=null){
			String andOrDelititer=" "+andOr+" ";
			for(String key:params.keySet()){
				Object value=params.get(key);
				if(value instanceof Map){
					//这里需要进行递归处理嵌套的查询条件
					SqlParamsParseResult SqlParamsParseResultModel=null;
					if(key.startsWith(andKey)){
						SqlParamsParseResultModel=processModelSqlWhereParams((Map<String,Object>)value,AND,isPlaceHolder,prefix);
					}else if(key.startsWith(orKey)){
						SqlParamsParseResultModel=processModelSqlWhereParams((Map<String,Object>)value,OR,isPlaceHolder,prefix);
					}
					if(SqlParamsParseResultModel!=null && StringUtils.isNotEmpty(SqlParamsParseResultModel.getBaseWhereSql())){
						sqlParamsParseResult.addSqlModel(andOrDelititer);
						sqlParamsParseResult.addSqlModel("("+SqlParamsParseResultModel.getBaseWhereSql()+")");
						sqlParamsParseResult.addArguments(SqlParamsParseResultModel.getArguments());
					}
				}else{
					SqlParamsParseItemResult sqlParamsParseItemResult=processNormalSqlWhereParams(key,value,isPlaceHolder,prefix);
					if(sqlParamsParseItemResult!=null){
						sqlParamsParseResult.addSqlModel(andOrDelititer);
						sqlParamsParseResult.addSqlModel(sqlParamsParseItemResult.getSqlModel(isPlaceHolder,PLACE_HOLDER));
						sqlParamsParseResult.addArgument(sqlParamsParseItemResult.getValue());
					}
				}
			}
			StringBuilder baseWhereSql=sqlParamsParseResult.getBaseWhereSql();
			if(StringUtils.isNotEmpty(baseWhereSql)){
				sqlParamsParseResult.setBaseWhereSql(new StringBuilder(baseWhereSql.substring(andOrDelititer.length())));
			}
		}
		return sqlParamsParseResult;
	}
	
	private SqlParamsParseResult processModelSqlWhereParams(Map<String,Object> params,String andOr,boolean isPlaceHolder,boolean prefix){
		return getSqlWhereParamsResultByAndOr(params,andOr,isPlaceHolder,prefix,new SqlParamsParseResult());
	}
	
	private SqlParamsParseItemResult processNormalSqlWhereParams(String key,Object value,boolean isPlaceHolder,boolean prefix) {
		SqlParamsParseItemResult sqlParamsParseItemResult=null;
		String[] parts=key.split(separatorFlag);
		if(parts.length==2){
			for(SqlParamsParser sqlParamsParser:sqlParamsParsers){
				if(sqlParamsParser.support(parts[1])){
					String fullColumn=ListToStringUtil.getFullTable(parts[0],keyPrefix,prefix);
					if(isPlaceHolder){
						sqlParamsParseItemResult=sqlParamsParser.getPlaceHolderParamsResult(fullColumn,value,parts[1]);
					}else{
						sqlParamsParseItemResult=sqlParamsParser.getParamsResult(fullColumn,value,parts[1]);
					}
					break;
				}
			}
		}else{
			Object tmpValue=value;
			if(!isPlaceHolder){
				tmpValue=SqlStringUtils.processString(value);
			}
			sqlParamsParseItemResult=new SqlParamsParseItemResult(ListToStringUtil.getFullTable(key,keyPrefix,prefix),"=",tmpValue);
		}
		return sqlParamsParseItemResult;
	}

	public void setSeparatorFlag(String separatorFlag) {
		this.separatorFlag = separatorFlag;
	}

	public void registerSqlParamsHandler(SqlParamsParser sqlParamsParser){
		if(sqlParamsParser!=null){
			sqlParamsParsers.add(sqlParamsParser);
		}
	}
	
	public void registerSqlParamsHandler(List<SqlParamsParser> sqlParamsParsers){
		if(sqlParamsParsers!=null){
			for(SqlParamsParser sqlParamsParser:sqlParamsParsers){
				registerSqlParamsHandler(sqlParamsParser);
			}
		}
	}

	public void setAndKey(String andKey) {
		this.andKey = andKey;
	}

	public void setOrKey(String orKey) {
		this.orKey = orKey;
	}

	public String getAndKey() {
		return andKey;
	}

	public String getOrKey() {
		return orKey;
	}
	
}
