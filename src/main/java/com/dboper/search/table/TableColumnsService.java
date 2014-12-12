package com.dboper.search.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.dboper.search.config.TableColumnsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TableColumnsService{
	
	private TableColumnsConfig tableColumnsConfig;
	
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private ConcurrentHashMap<String,List<String>> tableColumns=new ConcurrentHashMap<String,List<String>>();
	
	public TableColumnsService(TableColumnsConfig tableColumnsConfig){
		this.tableColumnsConfig=tableColumnsConfig;
		initTableColumns();
	}

	@SuppressWarnings("unchecked")
	private void initTableColumns() {
		try {
			Resource[] resources = resolver.getResources("classpath*:"+this.tableColumnsConfig.getTableColumnsDir()+"/*");
			if(resources!=null && resources.length>0){
				for(Resource resource:resources){
					File file=resource.getFile();
					InputStream in=new FileInputStream(file);
					ObjectMapper mapper=new ObjectMapper();
					Map<String,List<String>> tables=mapper.readValue(in,Map.class);
					tableColumns.putAll(tables);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
	public List<String> getColumns(String table){
		return tableColumns.get(table);
	}
}
