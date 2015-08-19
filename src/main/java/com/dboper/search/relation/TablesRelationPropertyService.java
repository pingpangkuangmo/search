package com.dboper.search.relation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import com.dboper.search.cache.EntityNameCache;
import com.dboper.search.cache.EntityNameContext;
import com.dboper.search.config.BaseTwoTablesRelationConfig;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.domain.SonSearchBody;
import com.dboper.search.sqlparams.DefaultSqlParamsHandlerUtils;
import com.dboper.search.table.TableColumnsModule;
import com.dboper.search.util.FileUtil;
import com.dboper.search.util.ListUtil;

public class TablesRelationPropertyService{
	
	private BaseTwoTablesRelationConfig config;
	
	/**
	 * 两个表之间的连接关系，如 a b a.id=b.aId  则存储为a__b:a.id=b.aId
	 */
	private ConcurrentHashMap<String,String> baseTwoTablesRelation=new ConcurrentHashMap<String,String>();
	
	/**
	 * 每个表的关联表，如 a:[b,c]，是有两部分构成，首先是通过baseTwoTablesRelation配置自动计算，然后就是将手动配置的覆盖计算的
	 */
	private ConcurrentHashMap<String,List<String>> tableConfigAndRelationtables=new ConcurrentHashMap<String,List<String>>();
	
	/**
	 * 每个表的附属表，如organization status则是organization的附属表，附属表不用于和其他表进行关联
	 */
	private ConcurrentHashMap<String,List<String>> sonTables=new ConcurrentHashMap<String,List<String>>();
	
	
	private ConcurrentHashMap<String,String> tablesRelationParseResult=new ConcurrentHashMap<String,String>();
	
	private ConcurrentHashMap<String,String> twoTablesTablesPath=new ConcurrentHashMap<String,String>();
	
	
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private EntityNameCache entityNameCache;
	
	private final Logger logger=LoggerFactory.getLogger(TablesRelationPropertyService.class);
	
	private static final String MANY_RELATION_FLAG="_as_";
	
	private static final String SON_SEARCH_PREFIX="sonSearch__";
	
	private static final String PARAMS_PART="%params%";
	
	public TablesRelationPropertyService(BaseTwoTablesRelationConfig config){
		this.config=config;
	}
	
	@SuppressWarnings("unchecked")
	public void init() {
		entityNameCache=new EntityNameCache();
		//加载配置基础的两个表之间的配置文件，可以是某个目录下多个配置文件
		try {
			HashMap<String,List<String>> tablesAutoAndRelationTables=new HashMap<String,List<String>>();
			Resource[] resources = resolver.getResources("classpath*:"+this.config.getBaseTwoTablesRelation()+"/*.txt");
			if(resources!=null && resources.length>0){
				for(Resource resource:resources){
					File file=resource.getFile();
					BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
					String lineStr=bufferedReader.readLine();
					while(lineStr!=null){
						if(lineStr.length()>0){
							parseLine(lineStr,tablesAutoAndRelationTables);
						}
						lineStr=bufferedReader.readLine();
					}
					bufferedReader.close();
				}
			}
			Resource[] mapResources = resolver.getResources("classpath*:"+this.config.getBaseTwoTablesRelation()+"/*.json");
			if(mapResources!=null && mapResources.length>0){
				tableConfigAndRelationtables.putAll(tablesAutoAndRelationTables);
				for(Resource resource:mapResources){
					File file=resource.getFile();
					tableConfigAndRelationtables.putAll(FileUtil.getClassFromFile(file,Map.class));
				}
			}
			Resource[] sonResources = resolver.getResources("classpath*:"+this.config.getSonTables()+"/*.json");
			if(sonResources!=null && sonResources.length>0){
				for(Resource resource:sonResources){
					File file=resource.getFile();
					sonTables.putAll(FileUtil.getClassFromFile(file, Map.class));
				}
			}
			Resource[] tablesPathResources = resolver.getResources("classpath*:"+this.config.getTablesPathConfig()+"/*.txt");
			if(tablesPathResources!=null && tablesPathResources.length>0){
				for(Resource resource:tablesPathResources){
					File file=resource.getFile();
					BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
					String lineStr=bufferedReader.readLine();
					while(lineStr!=null){
						if(lineStr.length()>0){
							parseLineTablesPath(lineStr,twoTablesTablesPath);
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

	private void parseLineTablesPath(String lineStr,ConcurrentHashMap<String, String> twoTablesTablesPath) {
		String[] parts=lineStr.split(",");
		if(parts.length==3){
			List<String> tables=new ArrayList<String>();
			tables.add(parts[0]);
			tables.add(parts[1]);
			Collections.sort(tables);
			twoTablesTablesPath.put(tables.get(0)+"__"+tables.get(1),parts[2]);
		}
	}

	private void parseLine(String lineStr,HashMap<String,List<String>> tablesAutoAndRelationTables) {
		String[] parts=lineStr.split(",");
		if(parts.length==3){
			List<String> tables=new ArrayList<String>();
			tables.add(parts[0]);
			tables.add(parts[1]);
			List<String> tableOneRelationTables=tablesAutoAndRelationTables.get(parts[0]);
			List<String> tableTwoRelationTables=tablesAutoAndRelationTables.get(parts[1]);
			if(tableOneRelationTables==null){
				tableOneRelationTables=new ArrayList<String>();
				tablesAutoAndRelationTables.put(parts[0], tableOneRelationTables);
			}
			if(!tableOneRelationTables.contains(parts[1])){
				tableOneRelationTables.add(parts[1]);
			}
			if(tableTwoRelationTables==null){
				tableTwoRelationTables=new ArrayList<String>();
				tablesAutoAndRelationTables.put(parts[1], tableTwoRelationTables);
			}
			if(!tableTwoRelationTables.contains(parts[0])){
				tableTwoRelationTables.add(parts[0]);
			}
			Collections.sort(tables);
			baseTwoTablesRelation.put(tables.get(0)+"__"+tables.get(1),parts[2]);
		}
	}
	
	public String getRelation(QueryBody q,TableColumnsModule tableColumnsModule){
		String cachekey=getCachekey(q,tableColumnsModule);
		logger.info("拿到entityNames的cacheKey为="+cachekey);
		Map<String,EntityNameContext> entityNamesData=entityNameCache.get(cachekey);
		if(entityNamesData==null){
			logger.info("entityNames的cacheKey:"+cachekey+" 还没有缓存");
			String relation=parseJoinStrRelation(q,tableColumnsModule);
			if(StringUtils.hasLength(relation)){
				//成功找到，添加到缓存
				if(!cachekey.endsWith("_no_cache")){
					addCache(cachekey,q.getTablesPath(),relation,q.getColumns(),q.isHasSon(),q.getFatherEntity());
					logger.info("entityNames的cacheKey:"+cachekey+" 添加到缓存");
				}
			}
			return relation;
		}else{
			EntityNameContext entityNameContext=entityNamesData.get(q.getTablesPath());
			if(entityNameContext==null){
				logger.info("entityNames的cacheKey:"+cachekey+" 命中缓存，但是没有符合tablePath="+q.getTablesPath()+"的数据");
				String relation=parseJoinStrRelation(q,tableColumnsModule);
				addCache(q.getTablesPath(),relation,q.getColumns(),entityNamesData,q.isHasSon(),q.getFatherEntity());
				return relation;
			}else{
				logger.info("entityNames的cacheKey:"+cachekey+" 命中缓存，同时有tablePath="+q.getTablesPath()+"的数据，所以直接使用缓存");
				q.setColumns(entityNameContext.getColumns());
				q.setHasSon(entityNameContext.getHasSon());
				q.setFatherEntity(entityNameContext.getFatherEntity());
				tableColumnsModule.addGroupColumns(q);
				return entityNameContext.getRelation();
			}
		}
	}
	
	private void addCache(String tablePath,String relation, List<String> columns,Map<String,EntityNameContext> entityNamesData,boolean hasSon,String fatherEntity) {
		EntityNameContext entityNameContext=new EntityNameContext();
		entityNameContext.setColumns(columns);
		entityNameContext.setRelation(relation);
		entityNameContext.setHasSon(hasSon);
		entityNameContext.setFatherEntity(fatherEntity);
		entityNamesData.put(tablePath,entityNameContext);
	}

	private void addCache(String cachekey,String tablePath,String relation,List<String> columns,boolean hasSon,String fatherEntity){
		Map<String,EntityNameContext> currentData=new HashMap<String,EntityNameContext>();
		EntityNameContext entityNameContext=new EntityNameContext();
		entityNameContext.setRelation(relation);
		entityNameContext.setColumns(columns);
		entityNameContext.setHasSon(hasSon);
		entityNameContext.setFatherEntity(fatherEntity);
		currentData.put(tablePath,entityNameContext);
		entityNameCache.put(cachekey,currentData);
	}

	private String getCachekey(QueryBody q,TableColumnsModule tableColumnsModule) {
		StringBuilder sb=new StringBuilder();
		sb.append(q.getAction());
		List<String> entities=tableColumnsModule.getFullEntity(q);
		if(entities!=null && entities.size()==0){
			return System.currentTimeMillis()+"_no_cache";
		}
		sb.append(getMapKeyString(q.getParams()));
		Map<String,Map<String,Object>> sonParams=q.getSonParams();
		if(sonParams!=null && sonParams.size()>0){
			List<String> keys=new ArrayList<String>(sonParams.keySet());
			Collections.sort(keys);
			for(String key:keys){
				sb.append(key);
				sb.append(getMapKeyString(sonParams.get(key)));
			}
		}
		return sb.toString();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String getMapKeyString(Map params){
		if(params==null || params.size()==0){
			return "{}";
		}
		StringBuilder sb=new StringBuilder();
		Set<String> keys=params.keySet();
		List<String> listKeys=new ArrayList<String>(keys);
		Collections.sort(listKeys);
		for(String key:listKeys){
			sb.append(key);
			Object value=params.get(key);
			if(value!=null && value instanceof Map){
				sb.append(getMapKeyString((Map)value));
			}
		}
		return sb.toString();
	}

	//需要优化下，对于只有一个entity时的查询（relation很简单，就是entity表名，然后就是处理附属表）
	private String parseJoinStrRelation(QueryBody q,TableColumnsModule tableColumnsModule) {
		List<String> entityColumns=q.getEntityColumns();
		String joinStr=q.getTablesPath();
		boolean hasLength=StringUtils.hasLength(joinStr);
		if(entityColumns!=null && entityColumns.size()==1 && !hasLength){
			tableColumnsModule.processQueryBodyTableCoumns(q,null);
			StringBuilder sb=new StringBuilder();
			String entity=entityColumns.get(0);
			sb.append(config.getTablePrefix()+entity);
			//添加附属表
			addSonTables(entity,entity,sb,new ArrayList<String>(),null);
			return sb.toString();
		}
		
		if(!hasLength){
			//用户没有指定tablesPath，需要自动根据entity列表和已经配置的relation来进行自动判断，如果没有找到，不处理，此时连接形式默认都处理成  inner join
			String computerTablesPath=computer(q,tableColumnsModule);
			if(!StringUtils.hasLength(computerTablesPath)){
				return "";
			}
			joinStr=computerTablesPath;
			q.setTablesPath(computerTablesPath);
		}
		HashMap<String,Map<String,Map<String,String>>> joinStrChangeTables=new HashMap<String,Map<String,Map<String,String>>>();
		List<String> allTables=new ArrayList<String>();
		String tmp=joinStr.replaceAll("left","");
		tmp=tmp.replaceAll("right","");
		String[] tables=tmp.split("join");
		if(tables.length<2){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		String firstTable=config.getTablePrefix()+tables[0].trim();
		sb.append(firstTable);
		allTables.add(firstTable);
		List<String> list=new ArrayList<String>();
		addSonTables(tables[0].trim(), joinStr, sb, allTables,joinStrChangeTables);
		for(int i=0,len=tables.length;i<len-1;i++){
			String tableOne=tables[i].trim();
			String tableTwo=tables[i+1].trim();
			String realTableRight=tableTwo;
			
			if(tableTwo.startsWith(SON_SEARCH_PREFIX)){
				if(!processSonSearch(q,i,tables,list, tableOne, tableTwo, joinStr, sb,
						allTables, len, joinStrChangeTables)){
					return "";
				}
				continue;
			}
			
			if(!getRealTableRightRelation(realTableRight, i, tables, list, tableOne, tableTwo, joinStr, sb,
					allTables, len, joinStrChangeTables)){
				return "";
			}
		}
		tableColumnsModule.processQueryBodyTableCoumns(q,joinStrChangeTables.get(joinStr));
		String fullRelation=sb.toString();
		tablesRelationParseResult.put(joinStr,fullRelation);
		return fullRelation;
	}
	
	private boolean getRealTableRightRelation(String realTableRight,int i,String[] tables,List<String> list,
			String tableOne,String tableTwo,String joinStr,StringBuilder sb,List<String> allTables,
			int len,HashMap<String,Map<String,Map<String,String>>> joinStrChangeTables){
		String relation=null;
		String realLeftTable=null;
		String intersectionTable=null;
		for(int j=i;j>=0;j--){
			String tableLeft=tables[j].trim();
			String otherTablesStr=getSortStr(realTableRight,tableLeft,list);
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
				List<String> tableTwoRelationTables=tableConfigAndRelationtables.get(realTableRight);
				List<String> realLeftRelationTables=tableConfigAndRelationtables.get(tableLeft);
				List<String> intersection=ListUtil.intersection(tableTwoRelationTables,realLeftRelationTables);
				if(intersection.size()>0){
					//表示他们之间有中间表，选取中间表中的一个（中间表可能有很多，这一点也会产生很多问题，但可以通过配置解决）
					intersectionTable=intersection.get(0);
					logger.info("找到能和"+realTableRight+"联接的中间表"+intersectionTable);
					realLeftTable=tableLeft;
					break;
				}
			}
			if(realLeftTable==null){
				logger.info("也没有找到能和"+realTableRight+"联接的中间表");
				return false;
			}
		}
		String joinType=getJoinType(i, len, tableOne, tableTwo,joinStr);
		if(!StringUtils.hasLength(joinType)){
			return false;
		}
		if(intersectionTable!=null){
			//中间表的级联
			String relationOne=baseTwoTablesRelation.get(getSortStr(intersectionTable,realLeftTable,list));
			appendRelation(joinStr,sb, intersectionTable, realLeftTable, relationOne, joinType,allTables,joinStrChangeTables);
			String relationTwo=baseTwoTablesRelation.get(getSortStr(realTableRight,intersectionTable,list));
			appendRelation(joinStr,sb, realTableRight, intersectionTable, relationTwo, joinType,allTables,joinStrChangeTables);
		}else{
			appendRelation(joinStr,sb, realTableRight, realLeftTable, relation, joinType,allTables,joinStrChangeTables);
		}
		addSonTables(realTableRight, joinStr, sb, allTables, joinStrChangeTables);
		return true;
	}
	
	private boolean processSonSearch(QueryBody q,
			int i,String[] tables,List<String> list,
			String tableOne,String tableTwo,String joinStr,StringBuilder sb,List<String> allTables,
			int len,HashMap<String,Map<String,Map<String,String>>> joinStrChangeTables){
		if(tableTwo.length()==(SON_SEARCH_PREFIX.length())){
			logger.warn(SON_SEARCH_PREFIX+" 里面的内容为空");
			throw new RuntimeException("参数格式不合法");
		}
		String realTableRight=tableTwo.substring(SON_SEARCH_PREFIX.length());
		SonSearchBody realTableRightSonSearchBody=q.getSonSearchs().get(realTableRight);
		if(realTableRightSonSearchBody==null){
			logger.warn("找不到 "+realTableRight+" 的sonSearchs定义，自定义一个");
			realTableRightSonSearchBody=new SonSearchBody();
		}
		String sql=converterSql(realTableRightSonSearchBody.getSql());
		Map<String,Object> params=new HashMap<String,Object>();
		params.putAll(realTableRightSonSearchBody.getParams());
		Map<String,Map<String,Object>> sonParmas=q.getSonParams();
		if(sonParmas!=null){
			Map<String,Object> tableParams=sonParmas.get(realTableRight);
			if(tableParams!=null){
				params.putAll(tableParams);
			}
		}
		int paramsLen=params.size();
		String joinType=getJoinType(i, len, tableOne, tableTwo,joinStr);
		if(!StringUtils.hasLength(joinType)){
			return false;
		}
		if(sql==null){
			StringBuilder sonSearchTable=new StringBuilder();
			String realTableRightFull=config.getTablePrefix()+realTableRight;
			sonSearchTable.append(" (select * from ").append(realTableRightFull);
			if(paramsLen>0){
				String sqlParams=DefaultSqlParamsHandlerUtils.defaultSqlParamsHandler.getSqlWhereParams(params);
				sonSearchTable.append(" where ").append(sqlParams);
			}
			sonSearchTable.append(" ) ").append(realTableRightFull);
			String sonRelation=realTableRightSonSearchBody.getRelation();
			if(sonRelation!=null){
				sonSearchTable.append(" ").append(sonRelation).append(" ");
				sb.append(" ").append(joinType).append(" ").append(sonSearchTable);
			}else{
				//寻找realTableRight与前面的table的连接关系
				StringBuilder partSb=new StringBuilder();
				//这一块还要改造，默认会  left join realTableRight也加上的，目前整体的realTableRight是一个子查询，需要替换的
				if(!getRealTableRightRelation(realTableRight, i, tables, list, tableOne, tableTwo,
						joinStr, partSb, allTables, len, joinStrChangeTables)){
					return false;
				}
				String relation=partSb.toString();
				sb.append(" ").append(relation.replaceFirst(realTableRightFull,sonSearchTable.toString()));
			}
		}else{
			boolean hasWhere=sql.contains(" where ");
			if(hasWhere){
				String[] paramsParts=sql.split(PARAMS_PART);
				if(paramsParts.length==3 && paramsLen>0){
					String sqlParams=DefaultSqlParamsHandlerUtils.defaultSqlParamsHandler.getSqlWhereParams(params);
					if(sqlParams!=null && sqlParams.length()>0){
						sb.append(" ").append(paramsParts[0]);
						sb.append(" ").append(paramsParts[1]).append(" and ").append(sqlParams)
							.append(" ").append(paramsParts[2]);
					}
				}else{
					sb.append(" ").append(sql);
				}
			}else{
				if(paramsLen>0){
					String[] paramsParts=sql.split(PARAMS_PART);
					String sqlParams=DefaultSqlParamsHandlerUtils.defaultSqlParamsHandler.getSqlWhereParams(params);
					if(sqlParams!=null && sqlParams.length()>0){
						if(paramsParts.length==2){
							sb.append(" ").append(paramsParts[0]);
							sb.append(" where ").append(sqlParams)
								.append(" ").append(paramsParts[1]);
						}else if(paramsParts.length==3){
							sb.append(" ").append(paramsParts[0]);
							sb.append(" where ").append(sqlParams)
								.append(" ").append(paramsParts[2]);
						}
					}else{
						sb.append(" ").append(sql);
					}
				}else{
					sb.append(" ").append(sql);
				}
			}
			
		}
		return true;
	}
	
	private String getJoinType(int i,int len,String left,String right,String tablesPath){
		String prefix=i==0?"":" ";
		String sub=i==len-2?"":" ";
		Pattern pattern=Pattern.compile(prefix+left+"\\s+(left join)?(right join)?(join)?\\s+"+right+sub);
		Matcher matcher=pattern.matcher(tablesPath);
		String target=null;
		String joinType=null;
		if(matcher.find()){
			target=matcher.group();
		}
		if(target==null){
			logger.warn("没有找到能和"+left+"和"+right+"的连接类型为 join、left join、right join");
			return "";
		}else{
			String tmp1=target.trim().substring(left.length());
			joinType=tmp1.substring(0,tmp1.indexOf(right)).trim();
		}
		return joinType;
	}
	
	private String converterSql(String sql){
		if(sql==null){
			return null;
		}
		return sql.replaceAll("%tprefix%",config.getTablePrefix());
	}

	//这一块需要单独独立出来，形成算法处理
	private String computer(QueryBody q,TableColumnsModule tableColumnsModule) {
		//过滤下entityColumns中的@list @map等元素，找出其中真正的实体
		List<String> entityColumns=tableColumnsModule.getEntity(q);
		if(entityColumns!=null && entityColumns.size()==2){
			////先简单点，只处理有两个entity的情况,分成两种情况，用户直接配置了和自动计算
			Collections.sort(entityColumns);
			String tablesPath=twoTablesTablesPath.get(entityColumns.get(0)+"__"+entityColumns.get(1));
			if(tablesPath!=null){
				//这里找到了用户配置的两个表之间的连接关系
				return tablesPath;
			}
			//下面的就应该是算法自动去计算两个表的连接关系
			String firstTable=entityColumns.get(0);
			String secondTable=entityColumns.get(1);
			List<String> firstRelations=tableConfigAndRelationtables.get(firstTable);
			List<String> secondRelations=tableConfigAndRelationtables.get(secondTable);
			if(firstRelations==null){
				firstRelations=new ArrayList<String>();
			}
			if(secondRelations==null){
				secondRelations=new ArrayList<String>();
			}
			firstRelations.add(firstTable);
			secondRelations.add(secondTable);
			List<String> intersection=ListUtil.intersection(firstRelations,secondRelations);
			if(intersection.size()>0){
				return firstTable+" join "+secondTable;
			}
			//需要继续不断地扩张，因为他们的级联关系可能有好几级
			//StringBuilder sb=new StringBuilder();
			return "";
		}
		return "";
	}

	private void addSonTables(String table,String joinStr,StringBuilder sb,List<String> allTables,HashMap<String,Map<String,Map<String,String>>> joinStrChangeTables) {
		table=table.trim();
		List<String> sonTable=sonTables.get(table);
		if(sonTable==null || sonTable.size()<1){
			return ;
		}
		for(String son:sonTable){
			String relation=baseTwoTablesRelation.get(getSortStr(table,son,new ArrayList<String>()));
			if(son.contains(MANY_RELATION_FLAG)){
				String[] parts=son.split(MANY_RELATION_FLAG);
				if(parts.length==2){
					relation=addRelationTablePrefix(relation,table,parts[1]);
					addReNameTable(sb,"left join",parts[0],parts[1],relation);
				}
			}else{
				appendRelation(joinStr,sb,son,table,relation,"left join", allTables, joinStrChangeTables);
			}
		}
	}

	private void appendRelation(String joinStr,StringBuilder sb,String table,String tableLeft,
			String relation,String joinType,List<String> allTables,HashMap<String,Map<String,Map<String,String>>> joinStrChangeTables){
		//对于重复表要进行重命名，如a、b、c都与status表有关系，然而a与b的中间表不是status而是a_b
		if(allTables.contains(table)){
			//重命名，包括连接关系的重命名和对应字段的重命名
			Map<String,Map<String,String>> changeTables=joinStrChangeTables.get(joinStr);
			if(changeTables==null){
				changeTables=new HashMap<String,Map<String,String>>();
				joinStrChangeTables.put(joinStr,changeTables);
			}
			Map<String,String> statusRenames=changeTables.get(tableLeft);
			if(statusRenames==null){
				statusRenames=new HashMap<String,String>();
				changeTables.put(tableLeft,statusRenames);
			}
			String reNameTable=table+"_"+tableLeft+"ReName";
			statusRenames.put(table,reNameTable);
			relation=addRelationReNameTablePrefix(relation,tableLeft,table,reNameTable);
			addReNameTable(sb, joinType, table, reNameTable, relation);
			//字段的重命名,放到分析完成之后一起做
		}else{
			allTables.add(table);
			String newTableLeft=tableLeft;
			if(tableLeft.contains(MANY_RELATION_FLAG)){
				String[] parts=tableLeft.split(MANY_RELATION_FLAG);
				if(parts.length==2){
					newTableLeft=parts[1];
				}
			}
			if(table.contains(MANY_RELATION_FLAG)){
				String[] parts=table.split(MANY_RELATION_FLAG);
				if(parts.length==2){
					relation=addRelationTablePrefix(relation,newTableLeft,parts[1]);
					addReNameTable(sb,joinType,parts[0],parts[1], relation);
				}
			}else{
				relation=addRelationTablePrefix(relation,table,newTableLeft);
				sb.append(" ").append(joinType).append(" ").append(config.getTablePrefix()+table).append(" on ").append(relation);
			}
		}
	}
	
	private String addRelationReNameTablePrefix(String relation,String leftTable,String oldTable,String reNameTable){
		relation=config.getTablePrefix()+relation.trim();
		relation=_addRelationTablePrefix(relation,leftTable);
		relation=_addRelationReNameTablePrefix(relation,oldTable,reNameTable);
		return relation;
	}
	
	private String addRelationTablePrefix(String relation,String leftTable,String table){
		relation=config.getTablePrefix()+relation.trim();
		relation=_addRelationTablePrefix(relation, table);
		relation=_addRelationTablePrefix(relation, leftTable);
		return relation;
	}
	
	private String _addRelationTablePrefix(String relation,String table){
		relation=relation.replaceAll("="+table+"\\.","="+config.getTablePrefix()+table+".");
		relation=relation.replaceAll("\\s"+table+"\\."," "+config.getTablePrefix()+table+".");
		return relation;
	}
	
	private String _addRelationReNameTablePrefix(String relation,String oldTable,String reNameTable){
		relation=relation.replaceAll("="+oldTable+"\\.","="+config.getTablePrefix()+reNameTable+".");
		relation=relation.replaceAll("\\s"+oldTable+"\\."," "+config.getTablePrefix()+reNameTable+".");
		return relation;
	}
	
	private void addReNameTable(StringBuilder sb,String joinType,String originTable,String newTable,String relation){
		sb.append(" ").append(joinType).append(" ").append(config.getTablePrefix()+originTable).append(" ").append(config.getTablePrefix()+newTable)
		.append(" on ").append(relation);
	}
	
	private String getSortStr(String tableOne,String tableTwo,List<String> list){
		list.clear();
		list.add(tableOne);
		list.add(tableTwo);
		Collections.sort(list);
		return list.get(0)+"__"+list.get(1);
	}

}
