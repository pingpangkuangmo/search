package com.dboper.search;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dboper.search.config.Configuration;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.relation.TablesRelationServiceCache;
import com.dboper.search.sqlparams.DefaultSqlParamsParser;
import com.dboper.search.sqlparams.InSqlParamsParser;
import com.dboper.search.sqlparams.SqlParamsHandler;
import com.dboper.search.sqlparams.TimeSqlParamsParser;
import com.dboper.search.util.ListToStringUtil;

@Service
public class SqlService implements Bootstrap{
	
	@Autowired
	private Configuration config;
	
	@Autowired
	private TablesRelationServiceCache tablesRelationServiceCache;
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private List<SqlParamsHandler> sqlParamsHandlers;
	
	@Override
	public void init() {
		registerSqlParamsHandlers();
		tablesRelationServiceCache.init();
	}
	
	private void registerSqlParamsHandlers() {
		sqlParamsHandlers=new ArrayList<SqlParamsHandler>();
		sqlParamsHandlers.add(new DefaultSqlParamsParser());
		sqlParamsHandlers.add(new InSqlParamsParser());
		sqlParamsHandlers.add(new TimeSqlParamsParser());
		List<SqlParamsHandler> customerSqlParamsHandlers=config.getSqlParamsHandlers();
		if(customerSqlParamsHandlers!=null){
			sqlParamsHandlers.addAll(customerSqlParamsHandlers);
		}
	}

	public String getSql(QueryBody q){
		if(q==null){
			return "";
		}
		String tablePrefix=config.getTablePrefix();
		List<String> columns=q.getColumns();
		Map<String,Object> params=q.getParams();
		String relation=tablesRelationServiceCache.getTablesRelation(columns,params,q.getAction());
		if(!StringUtils.hasLength(relation)){
			return "";
		}
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
			sql.append(" where ");
			sql.append(getParamsByHandlers(params));
		}
		String order_by=q.getOrder_by();
		if(order_by!=null && !order_by.trim().equals("")){
			sql.append(" order by ");
			sql.append(tablePrefix+order_by);
		}
		int limit=q.getLimit();
		if(limit>0){
			sql.append(" limit ").append(limit);
		}
		String sqlStr=sql.toString();
		logger.info(sqlStr);
		return sqlStr;
	}

	private String getParamsByHandlers(Map<String, Object> params) {
		StringBuilder sb=new StringBuilder();
		for(String item:params.keySet()){
			int operIndex=item.lastIndexOf("@");
			Object value=processStringValue(params.get(item));
			if(operIndex<0){
				sb.append(config.getTablePrefix()+item).append("=").append(value).append(" and ");
			}else{
				String key=item.substring(0,operIndex);
				String oper=item.substring(operIndex+1);
				String itemParams=handleKeyValue(config.getTablePrefix()+key,value,oper);
				if(!itemParams.equals("")){
					sb.append(itemParams).append(" and ");
				}
			}
		}
		int length=sb.length();
		sb=sb.delete(length-4,length);
		return sb.toString();
	}

	private String handleKeyValue(String key, Object value, String oper) {
		for(SqlParamsHandler sqlParamsHandler:sqlParamsHandlers){
			if(sqlParamsHandler.support(oper)){
				return sqlParamsHandler.getParams(key, value, oper);
			}
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
	private Object processStringValue(Object obj) {
		if(obj!=null && obj instanceof String){
			obj="'"+obj+"'";
		}
		if(obj!=null && (obj instanceof Collection || obj instanceof Array)){
			for(Object item:(Iterable)obj){
				if(item instanceof String){
					item="'"+item+"'";
				}
			}
		}
		return obj;
	}
	


}
