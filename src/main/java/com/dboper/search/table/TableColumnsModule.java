package com.dboper.search.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.dboper.search.config.TableColumnsConfig;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.exception.table.TableColumnException;

public class TableColumnsModule {
	
	private TableColumnsService tableColumnsService;
	
	private static final List<String> flags;
	
	private static final String TABLE_ALIAS="@as";
	
	static{
		flags=new ArrayList<String>();
		flags.add("@list");
		flags.add("@map");
	}

	public TableColumnsModule(TableColumnsConfig config){
		tableColumnsService=new TableColumnsService(config);
	}
	
	public List<String> getEntity(QueryBody q){
		Map<String,String> tableAlias=q.getTableAlias();
		if(tableAlias==null){
			tableAlias=new HashMap<String,String>();
			q.setTableAlias(tableAlias);
		}
		List<String> entityColumns=q.getEntityColumns();
		List<String> entities=new ArrayList<String>();
		if(entityColumns!=null){
			for(String entityColumn:entityColumns){
				String currentFlag=null;
				for(String flag:flags){
					if(entityColumn.contains(flag)){
						currentFlag=flag;
						break;
					}
				}
				if(currentFlag==null){
					if(!entities.contains(entityColumn)){
						entities.add(entityColumn);
					}
				}else{
					String tableName=entityColumn.substring(entityColumn.indexOf(currentFlag)+currentFlag.length());
					if(tableName.contains(TABLE_ALIAS)){
						int index=tableName.indexOf(TABLE_ALIAS);
						String realTableName=tableName.substring(0,index);
						String alias=tableName.substring(index+TABLE_ALIAS.length());
						tableAlias.put(alias,realTableName);
						if(!entities.contains(alias)){
							entities.add(alias);
						}
					}else{
						if(!entities.contains(tableName)){
							entities.add(tableName);
						}
					}
				}
			}
		}
		return entities;
	}
	
	public List<String> getFullEntity(QueryBody q){
		return q.getEntityColumns();
	}
	
	public QueryBody processQueryBodyTableCoumns(QueryBody q,Map<String,Map<String,String>> reNameTables){
		List<String> entityColumns=q.getEntityColumns();
		if(entityColumns!=null && entityColumns.size()>0){
			List<String> columns=new ArrayList<String>();
			String fatherEntity=null;
			for(String entity:entityColumns){
				String currentFlag=null;
				for(String flag:flags){
					if(entity.contains(flag)){
						if("@list".equals(flag)){
							q.setHasSon(true);
						}
						currentFlag=flag;
						break;
					}
				}
				if(currentFlag==null){
					fatherEntity=entity;
					q.setFatherEntity(fatherEntity);
					List<String> currentColumns=getColumns(entity,q);
					columns.addAll(currentColumns);
				}else{
					String[] parts=splitTwo(entity,currentFlag);
					List<String> currentColumns=getColumns(parts[1],q);
					for(String currentColumn:currentColumns){
						if(reNameTables!=null){
							currentColumn=processRenameTable(currentColumn,reNameTables.get(parts[1]));
						}
						if(currentColumn.contains(" as ")){
							String[] tableColumnAs=splitTwo(currentColumn," as ");
							columns.add(tableColumnAs[0]+" as `"+parts[0]+currentFlag+tableColumnAs[1]+"`");
						}else{
							String[] tableColumn=splitTwo(currentColumn,"\\.");
							columns.add(currentColumn+" as `"+parts[0]+currentFlag+tableColumn[1]+"`");
						}
					}
				}
			}
			q.setColumns(columns);
			addGroupColumns(q);
		}
		return q;
	}
	
	public void addGroupColumns(QueryBody q){
		List<String> columns=q.getColumns();
		List<String> groupColumns=q.getGroupColumns();
		String fatherEntity=q.getFatherEntity();
		if(q.isHasSon() && fatherEntity!=null && groupColumns.size()==0){
			String fatherIdColumn=fatherEntity+".id";
			if(!columns.contains(fatherIdColumn)){
				columns.add(fatherIdColumn);
				q.getDeleteColumns().add(fatherIdColumn);
				q.getDeleteColumnsCache().add(fatherIdColumn);
			}
			groupColumns.add(fatherIdColumn);
		}
	}
	
	private String processRenameTable(String currentColumn,Map<String,String> reNameTables) {
		if(reNameTables==null){
			return currentColumn;
		}
		for(String table:reNameTables.keySet()){
			if(currentColumn.startsWith(table+".")){
				return currentColumn.replaceFirst(table+"\\.",reNameTables.get(table)+".");
			}
		}
		return currentColumn;
	}

	private String[] splitTwo(String str,String flag){
		String[] ret=str.split(flag);
		if(ret.length==2){
			ret[0]=ret[0].trim();
			ret[1]=ret[1].trim();
		}else{
			throw new TableColumnException("表对应的字段信息："+str+"不合法");
		}
		return ret;
	}
	
	private List<String> getColumns(String table,QueryBody q){
		String realTable=table;
		String alias=null;
		if(table.contains(TABLE_ALIAS)){
			int index=table.indexOf(TABLE_ALIAS);
			realTable=table.substring(0,index);
			alias=table.substring(index+TABLE_ALIAS.length());
		}
		List<String> currentColumns=tableColumnsService.getColumns(realTable);
		Assert.notEmpty(currentColumns,"在table的columns配置中找不到table为"+realTable+"的columns");
		if(alias!=null){
			List<String> aliasCurrentColumns=new ArrayList<String>();
			for(String column:currentColumns){
				if(column.startsWith(realTable)){
					aliasCurrentColumns.add(column.replaceFirst(realTable+"\\.",alias+"."));
				}else{
					aliasCurrentColumns.add(column);
				}
			}
			return aliasCurrentColumns;
		}
		return currentColumns;
	}
	
}
