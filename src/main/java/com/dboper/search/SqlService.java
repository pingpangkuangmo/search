package com.dboper.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dboper.search.config.Configuration;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.relation.TablesRelationServiceCache;
import com.dboper.search.sqlparams.DefaultSqlParamsHandler;
import com.dboper.search.sqlparams.DefaultSqlParamsHandlerUtils;
import com.dboper.search.sqlparams.SqlParamsParseResult;
import com.dboper.search.sqlparams.parser.SqlParamsParser;
import com.dboper.search.sqlparams.util.StringUtils;
import com.dboper.search.util.ListToStringUtil;

@Service
public class SqlService implements Bootstrap{
	
	@Autowired
	private Configuration config;
	
	@Autowired
	private TablesRelationServiceCache tablesRelationServiceCache;
	
	private final Logger logger=LoggerFactory.getLogger(SqlService.class);
	
	private DefaultSqlParamsHandler defaultSqlParamsHandler;
	
	private final String SON_SEARCH_FLAG="@son";
	
	@Override
	public void init() {
		registerSqlParamsHandlers();
		tablesRelationServiceCache.init();
	}
	
	public void initTablesRelationFromDB(){
		tablesRelationServiceCache.refreshTablesRelationFromDB();
	}
	
	private void registerSqlParamsHandlers() {
		defaultSqlParamsHandler=new DefaultSqlParamsHandler(config.getTablePrefix());
		List<SqlParamsParser> customerSqlParamsHandlers=config.getSqlParamsParsers();
		if(customerSqlParamsHandlers!=null){
			defaultSqlParamsHandler.registerSqlParamsHandler(customerSqlParamsHandlers);
		}
		DefaultSqlParamsHandlerUtils.defaultSqlParamsHandler=defaultSqlParamsHandler;
	}
	
	public SqlParamsParseResult getSqlParamsResult(QueryBody q){
		return doSqlParse(q,false);
	}

	public String getSql(QueryBody q){
		return doSqlParse(q,false).getBaseWhereSql().toString();
	}
	
	private SqlParamsParseResult doSqlParse(QueryBody q,boolean isPlaceHolder){
		SqlParamsParseResult sqlResult=new SqlParamsParseResult();
		if(q==null){
			return sqlResult;
		}
		initParams(q);
		String tablePrefix=config.getTablePrefix();
		String relation=tablesRelationServiceCache.getTablesRelation(q);
		logger.info("查询得出的表之间的relation为：{}",relation);
		if(!StringUtils.isNotEmpty(relation)){
			return sqlResult;
		}
		List<String> columns=q.getColumns();
		Map<String,Object> params=q.getParams();
		StringBuilder sql=new StringBuilder("select ");
		if(q.isDistinct()){
			sql.append(" distinct ");
		}
		if(columns.get(0).contains("*")){
			sql.append("*");
		}else{
			sql.append(ListToStringUtil.arrayToStringAliases(columns,",",tablePrefix));
		}
		sql.append(" from ");
		sql.append(relation);
		if(params!=null && !params.isEmpty()){
			if(isPlaceHolder){
				SqlParamsParseResult sqlParamsParseResult=defaultSqlParamsHandler.getSqlWhereParamsResult(params);
				String baseWhereSql=sqlParamsParseResult.getBaseWhereSql().toString();
				if(StringUtils.isNotEmpty(baseWhereSql)){
					sql.append(" where ").append(baseWhereSql);
					sqlResult.setArguments(sqlParamsParseResult.getArguments());
				}
			}else{
				String baseWhereSql=defaultSqlParamsHandler.getSqlWhereParams(params);
				if(StringUtils.isNotEmpty(baseWhereSql)){
					sql.append(" where ").append(baseWhereSql);
				}
			}
		}
		String groupBy=q.getGroupBy();
		if(StringUtils.isNotEmpty(groupBy)){
			sql.append(" group by ").append(groupBy).append(" ");
		}
		List<String> order_by=q.getOrder_by();
		if(order_by!=null && order_by.size()>0){
			sql.append(" order by ");
			for(String item:order_by){
				sql.append(ListToStringUtil.getFullTable(item,tablePrefix)).append(",");
			}
			sql.deleteCharAt(sql.length()-1);
		}
		Integer limit=q.getLimit();
		if(limit!=null && limit>0){
			Integer start=q.getStart();
			if(start!=null && start>0){
				sql.append(" limit "+start+","+limit);
			}else{
				sql.append(" limit ").append(limit);
			}
		}
		sqlResult.setBaseWhereSql(sql);
		return sqlResult;
	}

	@SuppressWarnings("unchecked")
	private void initParams(QueryBody q) {
		Map<String,Object> params=q.getParams();
		List<String> sonKeys=new ArrayList<String>();
		if(params!=null){
			for(String key:params.keySet()){
				if(key.startsWith(SON_SEARCH_FLAG)){
					sonKeys.add(key);
				}
			}
		}
		if(sonKeys.size()>0){
			Map<String,Map<String,Object>> sonParams=q.getSonParams();
			if(sonParams==null){
				sonParams=new HashMap<String,Map<String,Object>>();
				q.setSonParams(sonParams);
			}
			for(String sonKey:sonKeys){
				String son=sonKey.substring(SON_SEARCH_FLAG.length());
				Object sonParam=params.remove(sonKey);
				if(sonParam instanceof Map){
					sonParams.put(son,(Map<String, Object>) sonParam);
				}
			}
		}
	}
}
