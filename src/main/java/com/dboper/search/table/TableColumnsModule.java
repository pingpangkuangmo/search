package com.dboper.search.table;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.dboper.search.config.TableColumnsConfig;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.exception.table.TableColumnException;

public class TableColumnsModule {
	
	private TableColumnsService tableColumnsService;
	
	private static final List<String> flags;
	
	static{
		flags=new ArrayList<String>();
		flags.add("@list");
		flags.add("@map");
	}

	public TableColumnsModule(TableColumnsConfig config){
		tableColumnsService=new TableColumnsService(config);
	}
	
	public QueryBody processQueryBodyTableCoumns(QueryBody q){
		List<String> entityColumns=q.getEntityColumns();
		if(entityColumns!=null && entityColumns.size()>0){
			List<String> columns=new ArrayList<String>();
			columns.clear();
			String fatherEntity=null;
			boolean haveSons=false;
			for(String entity:entityColumns){
				String currentFlag=null;
				for(String flag:flags){
					if(entity.contains(flag)){
						if("@list".equals(flag)){
							haveSons=true;
						}
						currentFlag=flag;
						break;
					}
				}
				if(currentFlag==null){
					fatherEntity=entity;
					List<String> currentColumns=getColumns(entity);
					columns.addAll(currentColumns);
				}else{
					String[] parts=splitTwo(entity,currentFlag);
					List<String> currentColumns=getColumns(parts[1]);
					for(String currentColumn:currentColumns){
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
			List<String> groupColumns=q.getGroupColumns();
			if(haveSons && fatherEntity!=null && groupColumns.size()==0){
				String fatherIdColumn=fatherEntity+".id";
				if(!columns.contains(fatherIdColumn)){
					columns.add(fatherIdColumn);
					q.getDeleteColumns().add(fatherIdColumn);
				}
				groupColumns.add(fatherIdColumn);
			}
			q.setColumns(columns);
		}
		return q;
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
	
	private List<String> getColumns(String table){
		List<String> currentColumns=tableColumnsService.getColumns(table);
		Assert.notEmpty(currentColumns,"在table的columns配置中找不到table为"+table+"的columns");
		return currentColumns;
	}
	
}
