package com.dboper.search.relation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.dboper.search.config.BaseTwoTablesRelationConfig;
import com.dboper.search.observer.BaseRelationProcess;

public class TablesRelationPropertyService implements BaseRelationProcess{
	
	private BaseTwoTablesRelationConfig config;
	
	private ConcurrentHashMap<String,String> baseTwoTablesRelation=new ConcurrentHashMap<String,String>();
	
	private ConcurrentHashMap<String,String> tablesRelationParseResult=new ConcurrentHashMap<String,String>();
	
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	public TablesRelationPropertyService(BaseTwoTablesRelationConfig config){
		this.config=config;
	}
	
	public void init() {
		//加载配置基础的两个表之间的配置文件，可以是某个目录下多个配置文件
		try {
			Resource[] resources = resolver.getResources("classpath*:"+this.config.getBaseTwoTablesRelation()+"/*");
			if(resources!=null && resources.length>0){
				for(Resource resource:resources){
					File file=resource.getFile();
					BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
					String lineStr=bufferedReader.readLine();
					while(lineStr!=null){
						if(lineStr.length()>0){
							parseLine(lineStr);
						}
						lineStr=bufferedReader.readLine();
					}
					bufferedReader.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	private void parseLine(String lineStr) {
		String[] parts=lineStr.split(",");
		if(parts.length==3){
			List<String> tables=new ArrayList<String>();
			tables.add(parts[0]);
			tables.add(parts[1]);
			Collections.sort(tables);
			baseTwoTablesRelation.put(tables.get(0)+"__"+tables.get(1),parts[2]);
		}
	}
	
	/**
	 * 
	 * @param joinStr  如  a left join b join c right join d 
	 * @return
	 */
	public String getRelation(String joinStr){
		String cacheRelation=tablesRelationParseResult.get(joinStr);
		if(cacheRelation==null){
			return parseJoinStrRelation(joinStr);
		}else{
			return cacheRelation;
		}
	}

	private String parseJoinStrRelation(String joinStr) {
		String tmp=joinStr.replaceAll("left","");
		tmp=tmp.replaceAll("right","");
		String[] tables=tmp.split("join");
		if(tables.length<2){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		sb.append(config.getTablePrefix()+tables[0]);
		List<String> list=new ArrayList<String>();
		for(int i=0,len=tables.length;i<len-1;i++){
			String tableOne=tables[i].trim();
			String tableTwo=tables[i+1].trim();
			String tableStr=getSortStr(tableOne,tableTwo,list);
			String relation=baseTwoTablesRelation.get(tableStr);
			String realLeftTable=tableOne;
			if(relation==null){
				//一旦有一个连接关系没找到，继续找和前一个表之间的连接关系
				if(i>0){
					for(int j=i-1;j>=0;j--){
						String otherTablesStr=getSortStr(tableTwo,tables[j],list);
						relation=baseTwoTablesRelation.get(otherTablesStr);
						if(relation!=null){
							realLeftTable=tables[j];
							break;
						}
					}
				}
			}
			//如果都没找到则结束表示无法处理
			if(relation==null){
				return "";
			}
			String prefix="";
			String suffix=" ";
			if(i>0){
				prefix=" ";
			}
			if(i==len-2){
				suffix="";
			}
			int start=joinStr.indexOf(prefix+tableOne+" ")+(prefix+tableOne).length();
			int end=joinStr.indexOf(" "+tableTwo+suffix);
			String joinType=joinStr.substring(start,end).trim();
			relation=relation.replaceAll(tableTwo+"\\.",config.getTablePrefix()+tableTwo+".");
			relation=relation.replaceAll(realLeftTable+"\\.",config.getTablePrefix()+realLeftTable+".");
			sb.append(" ").append(joinType).append(" ").append(config.getTablePrefix()+tableTwo).append(" on ").append(relation);
		}
		String fullRelation=sb.toString();
		tablesRelationParseResult.put(joinStr,fullRelation);
		return fullRelation;
	}
	
	private String getSortStr(String tableOne,String tableTwo,List<String> list){
		list.clear();
		list.add(tableOne);
		list.add(tableTwo);
		Collections.sort(list);
		return list.get(0)+"__"+list.get(1);
	}

	@Override
	public void processBaseRelation(String fileName,Map<String, String> tablesRelation) {
		
	}

}
