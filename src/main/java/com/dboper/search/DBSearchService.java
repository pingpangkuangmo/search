package com.dboper.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.TypeReference;
import com.dboper.search.config.Configuration;
import com.dboper.search.domain.ComplexQueryBody;
import com.dboper.search.domain.PageResult;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.domain.SecondQueryBody;
import com.dboper.search.format.ProcessUnit;
import com.dboper.search.format.form.UnionFormFormatter;
import com.dboper.search.format.value.UnionValueFormatter;
import com.dboper.search.observer.ComplexQueryFileListener;
import com.dboper.search.observer.ObserverItem;
import com.dboper.search.observer.ObserverModule;
import com.dboper.search.observer.ObserverModuleUtil;
import com.dboper.search.observer.ProcessComplexQueryFileChange;
import com.dboper.search.observer.ProcessQueryFileChange;
import com.dboper.search.observer.QueryFileListener;
import com.dboper.search.util.FileUtil;
import com.dboper.search.util.GroupColumnsUtils;
import com.dboper.search.util.MapUtil;

@Service
public class DBSearchService implements ProcessQueryFileChange,ProcessComplexQueryFileChange,Bootstrap{
	
	private final Log logger = LogFactory.getLog(DBSearchService.class);

	@Autowired
	private Configuration config;
	@Autowired
	private SqlService sqlService;
	
	private ConcurrentHashMap <String,QueryBody> querys=new ConcurrentHashMap<String,QueryBody>();
	
	private ConcurrentHashMap <String,ComplexQueryBody> complexQuerys=new ConcurrentHashMap<String,ComplexQueryBody>();
	
	/**
	 * 用于存储那些处理单元，如处理返回值得格式化，处理展示形式的格式化
	 */
	private List<ProcessUnit<? extends HashMap<String,Object>>> processUnits;
	
	private Map<String,ProcessorHandler> processorHandlers=new HashMap<String,ProcessorHandler>();
	
	@Override
	public void init() {
		initProcessorHandlers();
		initQueryBodyFiles();
		initProcessUnits();
		sqlService.init();
	}
	
	private void initProcessorHandlers() {
		processorHandlers.putAll(config.getProcessorHandlers());
	}

	/**
	 * 初始化时要加载QueryBody的配置文件
	 */
	private void initQueryBodyFiles() {
		initLoadAllQueryFile();
		initObserverModule();
	}

	/**
	 * 对外提供的刷新 table_str relation action 等配置
	 */
	public void refreshTablesRelationFromDB(){
		sqlService.initTablesRelationFromDB();
	}
	
	public PageResult selectPage(QueryBody q){
		PageResult pageResult=new PageResult();
		pageResult.setStart(q.getStart());
		pageResult.setLimit(q.getLimit());
		q.setStart(null);
		q.setLimit(null);
		String sql=sqlService.getSql(q);
		if(StringUtils.hasLength(sql)){
			String countSql="select count(1) from ("+sql+") tmp";
			Integer start=pageResult.getStart();
			if(start==null || start<0){
				start=0;
				pageResult.setStart(start);
			}
			Integer limit=pageResult.getLimit();
			if(limit==null || limit<0){
				limit=Integer.MAX_VALUE;
				pageResult.setLimit(limit);
			}
			List<Map<String, Object>> data=config.getJdbcTemplate().queryForList(sql+" limit "+start+","+limit);
			pageResult.setTotal(config.getJdbcTemplate().queryForObject(countSql,Integer.class));
			pageResult.setData(process(data,q));
		}
		return pageResult;
	}

	public List<Map<String,Object>> select(QueryBody q){
		List<String> groupColumns=q.getGroupColumns();
		List<String> originGroupColumns=new ArrayList<String>();
		if(groupColumns!=null){
			for(String item:groupColumns){
				originGroupColumns.add(item);
			}
		}
		long sqlParseStartTime=System.currentTimeMillis();
		String sql=sqlService.getSql(q);
		if(StringUtils.hasLength(sql)){
			long sqlSatrtTime=System.currentTimeMillis();
			logger.warn("解析成sql花费了:"+(sqlSatrtTime-sqlParseStartTime)+" ms");
			List<Map<String, Object>> data=config.getJdbcTemplate().queryForList(sql);
			String unionTablesPath=q.getUnionTablesPath();
			if(StringUtils.hasLength(unionTablesPath)){
				QueryBody unionQ=new QueryBody();
				List<String> entityColumns=q.getEntityColumns();
				if(entityColumns!=null && entityColumns.size()>0){
					unionQ.setEntityColumns(q.getEntityColumns());
				}else{
					unionQ.setColumns(q.getColumns());
				}
				unionQ.setGroupColumns(originGroupColumns);
				unionQ.setTablesPath(unionTablesPath);
				unionQ.setParams(q.getUnionParams());
				String unionSql=sqlService.getSql(unionQ);
				if(StringUtils.hasLength(unionSql)){
					logger.warn("使用了联合查询");
					data.addAll(config.getJdbcTemplate().queryForList(unionSql));
				}
			}
			long sqlEndTime=System.currentTimeMillis();
			logger.warn("sql查询花费了:"+(sqlEndTime-sqlSatrtTime)+" ms");
			return process(data,q);
		}else{
			return new ArrayList<Map<String,Object>>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> selectComplex(ComplexQueryBody complexQueryBody){
		if(complexQueryBody==null){
			return new ArrayList<Map<String,Object>>();
		}
		List<Map<String,Object>> firstDatas=select(complexQueryBody.getFirstAction(),complexQueryBody.getParams());
		
		Map<String,SecondQueryBody> secondQuery=complexQueryBody.getSecondQuery();
		
		Map<String,List<Object>> propertyListEntityPks=new HashMap<String,List<Object>>();
		Map<String,Map<Object,Map<String,Object>>> propertyListAndMapEntityPksEntity=new HashMap<String,Map<Object,Map<String,Object>>>();
		
		for(Map<String,Object> firstData:firstDatas){
			for(String property:secondQuery.keySet()){
				SecondQueryBody secondQueryItem=secondQuery.get(property);
				Object propertyDatas=firstData.get(property);
				if(propertyDatas!=null){
					if(propertyDatas instanceof List){
						List<Map<String,Object>> propertyLists=(List<Map<String,Object>>)propertyDatas;
						if(propertyLists.size()>0){
							for(Map<String,Object> propertEntity:propertyLists){
								collectPkAndEntity(property,propertEntity,secondQueryItem,
										propertyListEntityPks,propertyListAndMapEntityPksEntity);
							}
						}
					}else if(propertyDatas instanceof Map){
						collectPkAndEntity(property,(Map<String,Object>)propertyDatas,secondQueryItem,
								propertyListEntityPks,propertyListAndMapEntityPksEntity);
					}
				}
				
			}
		}
		for(String property:secondQuery.keySet()){
			if(propertyListEntityPks.containsKey(property)){
				List<Object> entityPks=propertyListEntityPks.get(property);
				if(entityPks!=null && entityPks.size()>0){
					Map<Object,Map<String,Object>> propertyLists=propertyListAndMapEntityPksEntity.get(property);
					replaceListData(secondQuery.get(property),entityPks,propertyLists);
				}
			}
		}
		return firstDatas;
	}
	
	private void collectPkAndEntity(String property,Map<String,Object> propertyData,SecondQueryBody secondQueryItem,
			Map<String,List<Object>> propertyListEntityPks,
			Map<String,Map<Object,Map<String,Object>>> propertyListAndMapEntityPksEntity){
		if(propertyData!=null){
			Object entityPk=propertyData.get(secondQueryItem.getKeyField());
			if(entityPk!=null){
				List<Object> entityPks=propertyListEntityPks.get(property);
				if(entityPks==null){
					entityPks=new ArrayList<Object>();
					propertyListEntityPks.put(property, entityPks);
				}
				entityPks.add(entityPk);
				Map<Object,Map<String,Object>> entityPksEntity=propertyListAndMapEntityPksEntity.get(property);
				if(entityPksEntity==null){
					entityPksEntity=new HashMap<Object,Map<String,Object>>();
					propertyListAndMapEntityPksEntity.put(property,entityPksEntity);
				}
				entityPksEntity.put(entityPk,propertyData);
			}
		}
	}
	
	private void replaceListData(SecondQueryBody secondQueryItem,List<Object> entityPks,
			Map<Object,Map<String,Object>> propertyLists){
		List<Map<String,Object>> secondDatas=null;
		if(secondQueryItem.isComplex()){
			secondDatas=selectComplex(secondQueryItem.getSecondAction(),
					MapUtil.getMap(secondQueryItem.getParamsKey()+"@in",entityPks));
		}else{
			secondDatas=select(secondQueryItem.getSecondAction(),
					MapUtil.getMap(secondQueryItem.getParamsKey()+"@in",entityPks));
		}
		if(secondDatas!=null && secondDatas.size()>0){
			for(Map<String,Object> secondData:secondDatas){
				Object entityPk=secondData.get(secondQueryItem.getKeyField());
				Map<String,Object> oldSecondData=propertyLists.get(entityPk);
				if(oldSecondData!=null){
					oldSecondData.clear();
					oldSecondData.putAll(secondData);
				}
			}
		}
	}
	
	public List<Map<String,Object>> selectSql(String sql){
		if(StringUtils.hasLength(sql)){
			return config.getJdbcTemplate().queryForList(sql);
		}else{
			return new ArrayList<Map<String,Object>>();
		}
	}
	
	private List<Map<String,Object>> process(List<Map<String, Object>> data,QueryBody q){
		data=processData(data,q);
		List<String> deleteColumns=q.getDeleteColumns();
		List<String> processors=q.getProcessors();
		boolean hasDeleteColumns=deleteColumns!=null && deleteColumns.size()>0;
		List<ProcessorHandler> processorHandlers=getProcessorHandlers(processors);
		boolean hasProcessors=processorHandlers!=null && processorHandlers.size()>0;
		if(hasDeleteColumns || hasProcessors){
			for(Map<String,Object> dataItem:data){
				if(hasDeleteColumns){
					for(String deleteColumn:deleteColumns){
						dataItem.remove(deleteColumn.substring(deleteColumn.indexOf(".")+1));
					}
				}
				if(hasProcessors){
					for(ProcessorHandler item:processorHandlers){
						item.processDataItem(dataItem);
					}
				}
			}
		}
		return data;
	}
	
	private List<ProcessorHandler> getProcessorHandlers(List<String> processors) {
		List<ProcessorHandler> ret=new ArrayList<ProcessorHandler>();
		if(processors!=null && processors.size()>0){
			for(String item:processors){
				ProcessorHandler processorHandler=processorHandlers.get(item);
				if(processorHandler!=null){
					ret.add(processorHandler);
				}			
			}
		}
		return ret;
	}

	public Map<String,Object> selectOne(QueryBody q){
		return getOne(select(q));
	}
	
	public Map<String,Object> selectComplexOne(ComplexQueryBody complexQueryBody){
		return getOne(selectComplex(complexQueryBody));
	}
	
	public List<Map<String,Object>> select(String action,Map<String,Object> params){
		return select(action, params, params);
	}
	
	public List<Map<String,Object>> select(String action,Map<String,Object> params,Map<String,Object> unionParams){
		QueryBody q=querys.get(action);
		if(q==null){
			return new ArrayList<Map<String,Object>>();
		}
		QueryBody copy;
		try {
			copy = q.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException("查询参数构造失败");
		}
		if(params!=null){
			Map<String,Object> oldParams=copy.getParams();
			Map<String,Object> oldUnionParams=copy.getUnionParams();
			Map<String,Object> newParams=new HashMap<String,Object>();
			Map<String,Object> newUnionParams=new HashMap<String,Object>();
			if(oldParams!=null){
				newParams.putAll(oldParams);
			}
			if(oldUnionParams!=null){
				newUnionParams.putAll(oldUnionParams);
			}
			newParams.putAll(params);
			newUnionParams.putAll(unionParams);
			copy.setParams(newParams);
			copy.setUnionParams(newUnionParams);
		}
		return select(copy);
	}
	
	public Map<String,Object> selectOne(String action,Map<String,Object> params){
		return getOne(select(action, params));
	}
	
	public Map<String,Object> selectComplexOne(String action,Map<String,Object> params){
		return getOne(selectComplex(action, params));
	}

	public List<Map<String,Object>> select(String action){
		return select(action,null);
	}
	
	public List<Map<String,Object>> selectComplex(String action){
		return selectComplex(action,null);
	}
	
	public Map<String,Object> selectOne(String action){
		return getOne(select(action));
	}
	
	public Map<String,Object> selectComplexOne(String action){
		return getOne(selectComplex(action));
	}
	
	public List<Map<String,Object>> selectComplex(String action,Map<String,Object> params){
		ComplexQueryBody complexQueryBody=complexQuerys.get(action);
		if(complexQueryBody==null){
			return new ArrayList<Map<String,Object>>();
		}
		if(params!=null){
			complexQueryBody.getParams().putAll(params);
		}
		return selectComplex(complexQueryBody);
	}
	
	
	
	private Map<String,Object> getOne(List<Map<String,Object>> data){
		if(data!=null && data.size()>0){
			return data.get(0);
		}
		return null;
	}

	private List<Map<String, Object>> processData(List<Map<String, Object>> data, QueryBody q) {
		Map<String,HashMap<String,Object>> contexts=prepareContexts(q);
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		List<String> exitsDataInfo=new ArrayList<String>();
		List<String> groupColumns=q.getGroupColumns();
		boolean haveGroupColumns=true;
		if(groupColumns==null || groupColumns.size()<1){
			haveGroupColumns=false;
		}
		for(Map<String,Object> dataItem:data){
			Map<String,Object> dataItemResult=processPerData(dataItem,ret,contexts,q);
			if(dataItemResult!=null){
				dataItem.putAll(q.getConstantData());
				if(haveGroupColumns){
					String currentDataInfo=getCurrentDataInfo(dataItem,groupColumns);
					if(!exitsDataInfo.contains(currentDataInfo)){
						exitsDataInfo.add(currentDataInfo);
						ret.add(dataItemResult);
					}
				}else{
					ret.add(dataItemResult);
				}
			}
		}
		return ret;
	}
	
	private String getCurrentDataInfo(Map<String,Object> currentData,List<String> groupColumns){
		StringBuilder sb=new StringBuilder("");
		for(String groupColumn:groupColumns){
			String key=GroupColumnsUtils.getKey(groupColumn);
			if(key==null){
				throw new RuntimeException("groupColumn "+groupColumn+" is not valid");
			}
			sb.append(currentData.get(key));
		}
		return sb.toString();
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> processPerData(Map<String, Object> dataItem,List<Map<String, Object>> dataToReturn,
			Map<String, HashMap<String, Object>> contexts, QueryBody q) {
		Map<String,Object> ret=dataItem;
		for(ProcessUnit processUnit:processUnits){
			HashMap<String, Object> context=contexts.get(processUnit.getName());
			if(context!=null){
				ret=processUnit.processLineData(dataItem,ret,dataToReturn,context);
			}
		}
		return ret;
	}

	private Map<String, HashMap<String, Object>> prepareContexts(QueryBody q) {
		Map<String,HashMap<String,Object>> contexts=new HashMap<String,HashMap<String,Object>>();
		for(ProcessUnit<?> processUnit:processUnits){
			HashMap<String,Object> context=processUnit.prepareContext(q);
			contexts.put(processUnit.getName(),context);   
		}
		return contexts;
	}
	
	@Override
	public void processQueryBodyChange(Map<String, QueryBody> change,String fileName) {
		logger.info("observ "+fileName+" changed");
		if(change!=null){
			querys.putAll(change);
		}
	}
	
	@Override
	public void processComplexQueryBodyChange(
			Map<String, ComplexQueryBody> change, String fileName) {
		logger.info("observ "+fileName+" changed");
		if(change!=null){
			complexQuerys.putAll(change);
		}
	}
	
	private void initLoadAllQueryFile(){
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:"+config.getQueryFileDirectory()+"/*.json");  
			if(resources!=null){
				for(Resource resource:resources){
					Map<String,QueryBody> fileQueryBody=FileUtil.getQueryBodyFromFile(resource.getInputStream());
					querys.putAll(fileQueryBody);
				}
			}
			Resource[] complexResources = resolver.getResources("classpath*:"+config.getComplexQueryFileDirectory()+"/*.json");  
			if(complexResources!=null){
				for(Resource resource:complexResources){
					Map<String,ComplexQueryBody> fileQueryBody=FileUtil.getTFromInputStream(resource.getInputStream(),
							new TypeReference<Map<String,ComplexQueryBody>>(){});
					complexQuerys.putAll(fileQueryBody);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initProcessUnits() {
		processUnits=new ArrayList<ProcessUnit<? extends HashMap<String,Object>>>();
		processUnits.add(new UnionValueFormatter());
		processUnits.add(new UnionFormFormatter());
	}
	
	private void initObserverModule() {
		ObserverModule observerModule=new ObserverModule();
		ObserverModuleUtil.setObserverModule(observerModule);
		if(config.isMonitorQueryFile()){
			ObserverItem observerItem=getQueryFileObserverItem();
			observerModule.addObserverItem(observerItem);
			
			ObserverItem complexObserverItem=getComplexQueryFileObserverItem();
			observerModule.addObserverItem(complexObserverItem);
			
		}
	}
	
	private ObserverItem getComplexQueryFileObserverItem() {
		ObserverItem observerItem=new ObserverItem();
		observerItem.setDir(config.getComplexQueryFileDirectory());
		observerItem.setInterval(5000L);
		observerItem.setName("complexQuery");
		observerItem.setSuffix("json");
		List<FileAlterationListener> listeners=new ArrayList<FileAlterationListener>();
		listeners.add(new ComplexQueryFileListener(this));
		observerItem.setListeners(listeners);
		return observerItem;
	}
	
	private ObserverItem getQueryFileObserverItem() {
		ObserverItem observerItem=new ObserverItem();
		observerItem.setDir(config.getQueryFileDirectory());
		observerItem.setInterval(5000L);
		observerItem.setName("query");
		observerItem.setSuffix("json");
		List<FileAlterationListener> listeners=new ArrayList<FileAlterationListener>();
		listeners.add(new QueryFileListener(this));
		observerItem.setListeners(listeners);
		return observerItem;
	}
	
	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

}
