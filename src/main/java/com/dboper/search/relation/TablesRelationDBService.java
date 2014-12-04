package com.dboper.search.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dboper.search.util.MapValueUtil.*;

import com.dboper.search.config.TableDBConfig;

public class TablesRelationDBService implements TablesRelationService{

	private TableDBConfig config;
	
	public TablesRelationDBService(TableDBConfig config) {
		this.config=config;
	}
	
	@Override
	public String getTablesRelation(String tablesStr) {
		return getTablesRelation(tablesStr,null);
	}
	
	@Override
	public String getTablesRelation(String tablesStr, String target) {
		String str="";
		String sql="select relation from "+config.getTablePrefix()+"tables_relation where tables_str='"+tablesStr+"'";
		if(target!=null){
			sql+=" and target='"+target+"'";
		}
		List<Map<String,Object>> list=config.getJdbcTemplate().queryForList(sql);
		if(list!=null && list.size()>=1){
			str=(String)list.get(0).get("relation");
		}
		return str;
	}

	public void insert(String tablesStr, String relation,String target) {
		if(target==null){
			target="normal";
		}
		String sql="insert into "+config.getTablePrefix()+"tables_relation(tables_str,relation,target) values('"+tablesStr+"','"+relation+"','"+target+"')";
		config.getJdbcTemplate().execute(sql);
	}
	
	public Map<String,List<Map<String,Object>>> selectAll(){
		List<Map<String,Object>> ret=config.getJdbcTemplate().queryForList("select tables_str,relation,target from "+config.getTablePrefix()+"tables_relation");
		Map<String,List<Map<String,Object>>> allTablesRelationsData=new HashMap<String,List<Map<String,Object>>>();
		for(Map<String,Object> item:ret){
			String tableStr=getString(item,"tables_str");
			List<Map<String,Object>> relations=allTablesRelationsData.get(tableStr);
			if(relations==null){
				relations=new ArrayList<Map<String,Object>>();
				allTablesRelationsData.put(tableStr,relations);
			}
			relations.add(item);
		}
		return allTablesRelationsData;
	}

	
}
