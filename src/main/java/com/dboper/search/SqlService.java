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
import org.springframework.util.Assert;
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
	
	private final Log logger = LogFactory.getLog(SqlService.class);
	
	private List<SqlParamsHandler> sqlParamsHandlers;
	
	@Override
	public void init() {
		registerSqlParamsHandlers();
		tablesRelationServiceCache.init();
	}
	
	public void initTablesRelationFromDB(){
		tablesRelationServiceCache.refreshTablesRelationFromDB();
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
		String relation=tablesRelationServiceCache.getTablesRelation(q);
		if(!StringUtils.hasLength(relation)){
			return "";
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
			sql.append(" where ");
			sql.append(getParamsByHandlers(params,"and"));
		}
		String groupBy=q.getGroupBy();
		if(StringUtils.hasLength(groupBy)){
			sql.append(" group by ").append(tablePrefix).append(groupBy).append(" ");
		}
		String order_by=q.getOrder_by();
		if(order_by!=null && !order_by.trim().equals("")){
			sql.append(" order by ");
			if(!order_by.contains(".")){
				sql.append(order_by);
			}else{
				sql.append(tablePrefix+order_by);
			}
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
		String sqlStr=sql.toString();
		logger.info(sqlStr);
		return sqlStr;
	}

	private String getParamsByHandlers(Map<String, Object> params,String andOr) {
		StringBuilder sb=new StringBuilder();
		String andOrOper=" "+andOr+" ";
		for(String item:params.keySet()){
			String andOrSql=processAndOr(item,params);
			if(StringUtils.hasLength(andOrSql)){
				sb.append(andOrSql).append(andOrOper);
			}else{
				int operIndex=item.lastIndexOf("@");
				Object value=processStringValue(params.get(item));
				if(operIndex<0){
					sb.append(config.getTablePrefix()+item).append("=").append(value).append(andOrOper);
				}else{
					String key=item.substring(0,operIndex);
					String oper=item.substring(operIndex+1);
					String itemParams=handleKeyValue(config.getTablePrefix()+key,value,oper);
					if(!itemParams.equals("")){
						sb.append(itemParams).append(andOrOper);
					}
				}
			}
		}
		int length=sb.length();
		sb=sb.delete(length-andOr.length()-1,length);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private String processAndOr(String item, Map<String, Object> params) {
		String andOr="";
		if(item==null){
			return "";
		}
		if(item.startsWith("$and")){
			andOr="and";
		}else if(item.startsWith("$or")){
			andOr="or";
		}
		if(StringUtils.hasLength(andOr)){
			Object value=params.get(item);
			Assert.notNull(value,"对于and和or操作，value不能为空");
			Assert.isInstanceOf(Map.class,value,"对于and和or操作，value必须为Map结构");
			String andOrSQL=getParamsByHandlers((Map<String,Object>)value,andOr);
			if(StringUtils.hasLength(andOrSQL)){
				return "( "+andOrSQL+" )";
			}
		}
		return "";
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
		if(isString(obj)){
			obj="'"+obj+"'";
		}
		if(obj!=null && (obj instanceof Collection || obj instanceof Array)){
			List<Object> newObj=new ArrayList<Object>();
			for(Object item:(Iterable)obj){
				if(isString(item)){
					newObj.add("'"+item+"'");
				}else{
					newObj.add(item);
				}
			}
			obj=newObj;
		}
		return obj;
	}
	
	private boolean  isString(Object obj){
		if(obj!=null && (obj instanceof String || obj instanceof Enum)){
			return true;
		}else{
			return false;
		}
	}
	


}
