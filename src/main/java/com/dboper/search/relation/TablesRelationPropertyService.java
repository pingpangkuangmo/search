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



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.dboper.search.config.BaseTwoTablesRelationConfig;
import com.dboper.search.observer.BaseRelationProcess;
import com.dboper.search.util.ListUtil;

public class TablesRelationPropertyService implements BaseRelationProcess{
	
	private BaseTwoTablesRelationConfig config;
	
	private ConcurrentHashMap<String,String> baseTwoTablesRelation=new ConcurrentHashMap<String,String>();
	
	private ConcurrentHashMap<String,List<String>> tableAndRelationtables=new ConcurrentHashMap<String,List<String>>();
	
	private ConcurrentHashMap<String,String> tablesRelationParseResult=new ConcurrentHashMap<String,String>();
	
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private final Log logger = LogFactory.getLog(getClass());
	
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
			List<String> tableOneRelationTables=tableAndRelationtables.get(parts[0]);
			List<String> tableTwoRelationTables=tableAndRelationtables.get(parts[1]);
			if(tableOneRelationTables==null){
				tableOneRelationTables=new ArrayList<String>();
				tableAndRelationtables.put(parts[0], tableOneRelationTables);
			}
			if(!tableOneRelationTables.contains(parts[1])){
				tableOneRelationTables.add(parts[1]);
			}
			if(tableTwoRelationTables==null){
				tableTwoRelationTables=new ArrayList<String>();
				tableAndRelationtables.put(parts[1], tableTwoRelationTables);
			}
			if(!tableTwoRelationTables.contains(parts[0])){
				tableTwoRelationTables.add(parts[0]);
			}
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
			String realLeftTable=null;
			String relation=null;
			String intersectionTable=null;
			for(int j=i;j>=0;j--){
				String tableLeft=tables[j].trim();
				String otherTablesStr=getSortStr(tableTwo,tableLeft,list);
				relation=baseTwoTablesRelation.get(otherTablesStr);
				if(relation!=null){
					realLeftTable=tableLeft;
					break;
				}
			}
			//如果都没找到能和左边相联接的表，则尝试获取他们的公共表
			if(relation==null){
				for(int j=i;j>=0;j--){
					String tableLeft=tables[j].trim();
					//用于处理中间表，扩展一级 organization 和 product_line都含有organization_product_lines表，所以自动把中间表加入进来
					//需要提前准备这样的数据，只处理一层中间表，不再处理复杂的多级
					List<String> tableTwoRelationTables=tableAndRelationtables.get(tableTwo);
					List<String> realLeftRelationTables=tableAndRelationtables.get(tableLeft);
					List<String> intersection=ListUtil.intersection(tableTwoRelationTables,realLeftRelationTables);
					if(intersection.size()>0){
						//表示他们之间有中间表，选取中间表中的一个（中间表可能有很多，这一点也会产生很多问题）
						intersectionTable=intersection.get(0);
						logger.warn("找到能和"+tableTwo+"联接的中间表"+intersectionTable);
						realLeftTable=tableLeft;
						break;
					}
				}
				if(realLeftTable==null){
					logger.warn("也没有找到能和"+tableTwo+"联接的中间表");
					return "";
				}
			}
			int start=joinStr.indexOf(" "+tableOne+" ")+(" "+tableOne+" ").length();
			int end=joinStr.indexOf(" "+tableTwo+" ");
			if(i==len-2){
				end=joinStr.lastIndexOf(" "+tableTwo);
			}
			if(i==0){
				start=joinStr.indexOf(tableOne+" ")+(tableOne+" ").length();
			}
			String joinType=joinStr.substring(start,end).trim();
			if(intersectionTable!=null){
				//中间表的级联
				String relationOne=baseTwoTablesRelation.get(getSortStr(intersectionTable,realLeftTable,list));
				appendRelation(sb, intersectionTable, realLeftTable, relationOne, joinType);
				String relationTwo=baseTwoTablesRelation.get(getSortStr(tableTwo,intersectionTable,list));
				appendRelation(sb, tableTwo, intersectionTable, relationTwo, joinType);
			}else{
				appendRelation(sb, tableTwo, realLeftTable, relation, joinType);
			}
		}
		String fullRelation=sb.toString();
		tablesRelationParseResult.put(joinStr,fullRelation);
		return fullRelation;
	}
	
	private void appendRelation(StringBuilder sb,String table,String tableLeft,String relation,String joinType){
		relation=relation.replaceAll(table+"\\.",config.getTablePrefix()+table+".");
		relation=relation.replaceAll(tableLeft+"\\.",config.getTablePrefix()+tableLeft+".");
		sb.append(" ").append(joinType).append(" ").append(config.getTablePrefix()+table).append(" on ").append(relation);
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
