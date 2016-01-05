package com.dboper.search.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.dboper.search.Bootstrap;
import com.dboper.search.config.Configuration;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.table.TableColumnsModule;
import com.dboper.search.util.GroupByUtils;
import com.dboper.search.util.MapUtil;
import com.dboper.search.util.TablesRelationUtil;

@Service
public class TablesRelationServiceCache implements Bootstrap{
	
	private ConcurrentHashMap<String,List<Map<String,Object>>> tablesRelationCache=new ConcurrentHashMap<String,List<Map<String,Object>>>();
	
	@Autowired
	protected Configuration config;
	
	private List<TablesRelationService> tablesRelationServices=new ArrayList<TablesRelationService>();
	
	private TablesRelationDBService dbTablesRelationService;
	
	private TablesRelationPropertyService tablesRelationPropertyService;
	
	private TableColumnsModule tableColumnsModule;
	
	private static final Logger logger=LoggerFactory.getLogger(TablesRelationServiceCache.class);
	
	@Override
	public void init() {
		registerTablesRelationService();
		initTablesRelation();
		tableColumnsModule=new TableColumnsModule(config);
	}
	
	public void clearCache(String cachekey){
		tablesRelationPropertyService.clearCache(cachekey);
	}
	
	public void refreshTablesRelationFromDB(){
		initTablesRelationFromDB();
	}
	
	private void registerTablesRelationService() {
		this.dbTablesRelationService=new TablesRelationDBService(config);
		this.tablesRelationPropertyService=new TablesRelationPropertyService(config);
		tablesRelationServices.add(dbTablesRelationService);
		if(config.getTablesRelationServices()!=null){
			tablesRelationServices.addAll(config.getTablesRelationServices());
		}
	}

	private void initTablesRelation() {
		initTablesRelationFromDB();
		initBaseTablesRelationFromProperties();
	}

	

	private void initBaseTablesRelationFromProperties() {
		this.tablesRelationPropertyService.init();
	}

	private void initTablesRelationFromDB() {
		tablesRelationCache.putAll(dbTablesRelationService.selectAll());
	}
	
	public String getTablesRelation(QueryBody q) {
		String relation="";
		List<String> entities=q.getEntityColumns();
		if(StringUtils.hasLength(q.getTablesPath()) || (entities!=null && entities.size()>=1)){
			relation=this.tablesRelationPropertyService.getRelation(q,tableColumnsModule);
		}
		if(StringUtils.hasLength(relation)){
			logger.debug("使用了tablesPath来寻找表之间的连接关系："+relation);
			return relation;
		}
		logger.debug("tablesPath没有找到连接关系，使用了columns和params字段来推断表之间的连接关系");
		tableColumnsModule.processQueryBodyTableCoumns(q,null);
		List<String> tables=new ArrayList<String>();
		for(String column:q.getColumns()){
			addTable(column,tables);
		}
		processAndOrTableName(q.getParams(), tables);
		Assert.notEmpty(tables);
		return getTablesRelation(tables,q.getAction());
	}
	
	@SuppressWarnings("unchecked")
	private void processAndOrTableName(Map<String, Object> params,List<String> tables){
		if(params!=null && !params.isEmpty()){
			for(String key:params.keySet()){
				if("$and".equals(key) || "$or".equals(key)){
					Object value=params.get(key);
					Assert.isInstanceOf(Map.class,value);
					processAndOrTableName((Map<String, Object>)value,tables);
				}else{
					addTable(key,tables);
				}
			}
		}
	}
	
	public String getTablesRelation(List<String> tables,String action) {
		String tablePrefix=config.getTablePrefix();
		String str="";
		if(tables.size()==1){
			String table=tables.get(0);
			if(table.startsWith(tablePrefix)){
				str=table;
			}else{
				str=tablePrefix+table;
			}
		}else{
			Collections.sort(tables);
			str=queryTablesRelation(tables,action);
		}
		return str;
	}

	private String queryTablesRelation(List<String> tables,String action) {
		String tablesStr=TablesRelationUtil.getTablesStr(tables);
		List<Map<String,Object>> relations=tablesRelationCache.get(tablesStr);
		boolean hasCache=true;
		String relation=null;
		if(relations==null || relations.size()<1){
			hasCache=false;
			relations=new ArrayList<Map<String,Object>>();
		}else{
			if(action!=null){
				boolean hasTarget=false;
				for(Map<String,Object> item:relations){
					if(action.equals(item.get("target"))){
						hasTarget=true;
						relation=(String) item.get("relation");
					}
				}
				if(!hasTarget){
					hasCache=false;
				}
			}else{
				relation=(String)relations.get(0).get("relation");
			}
		}
		if(!hasCache){
			for(TablesRelationService tablesRelationService:tablesRelationServices){
				relation=tablesRelationService.getTablesRelation(tablesStr,action);
				if(StringUtils.hasLength(relation)){
					Map<String,Object> relationItem=MapUtil.getMap("target",action,"relation",relation);
					relations.add(relationItem);
					tablesRelationCache.put(tablesStr,relations);
					insertToDB(tablesStr,relation,action);
					break;
				}
			}
		}
		logger.debug("tables_str="+tablesStr+";relation="+relation);
		return relation;
	}
	
	private void insertToDB(String tablesStr, String relation,String action) {
		dbTablesRelationService.insert(tablesStr,relation,action);
	}

	private void addTable(String key,List<String> tables){
		if(GroupByUtils.containsGroupKey(key)){
			return;
		}
		String[] parts=key.split("\\.");
		if(!(tables.contains(parts[0]))){
			tables.add(parts[0]);
		}
	}

	public List<TablesRelationService> getTablesRelationServices() {
		return tablesRelationServices;
	}

	public void setTablesRelationServices(
			List<TablesRelationService> tablesRelationServices) {
		this.tablesRelationServices = tablesRelationServices;
	}

}
