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

import com.dboper.search.config.Configuration;
import com.dboper.search.domain.PageResult;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.format.ProcessUnit;
import com.dboper.search.format.form.UnionFormFormatter;
import com.dboper.search.format.value.UnionValueFormatter;
import com.dboper.search.observer.ObserverItem;
import com.dboper.search.observer.ObserverModule;
import com.dboper.search.observer.ObserverModuleUtil;
import com.dboper.search.observer.ProcessQueryFileChange;
import com.dboper.search.observer.QueryFileListener;
import com.dboper.search.util.FileUtil;

@Service
public class DBSearchService implements ProcessQueryFileChange,Bootstrap{
	
	private final Log logger = LogFactory.getLog(DBSearchService.class);

	@Autowired
	private Configuration config;
	@Autowired
	private SqlService sqlService;
	
	private ConcurrentHashMap <String,QueryBody> querys=new ConcurrentHashMap<String,QueryBody>();
	
	/**
	 * 用于保存某些文件的配置，当某个文件发生变化时，需要将其他没变化的文件的数据和变化文件的数据合并到querys
	 */
	private ConcurrentHashMap<String,Map<String,QueryBody>> query_tmp=new ConcurrentHashMap<String,Map<String,QueryBody>>();
	
	/**
	 * 用于存储那些处理单元，如处理返回值得格式化，处理展示形式的格式化
	 */
	private List<ProcessUnit<? extends HashMap<String,Object>>> processUnits;
	
	/**
	 * 用于添加到监控的对象，有了该对象就可以通过ObserverModuleUtil来打开或者关闭监控
	 */
	private ObserverItem observerItem;
	
	@Override
	public void init() {
		initQueryBodyFiles();
		initProcessUnits();
		sqlService.init();
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
		long sqlParseStartTime=System.currentTimeMillis();
		String sql=sqlService.getSql(q);
		if(StringUtils.hasLength(sql)){
			long sqlSatrtTime=System.currentTimeMillis();
			logger.warn("解析成sql花费了:"+(sqlSatrtTime-sqlParseStartTime)+" ms");
			List<Map<String, Object>> data=config.getJdbcTemplate().queryForList(sql);
			String unionTablesPath=q.getUnionTablesPath();
			if(StringUtils.hasLength(unionTablesPath)){
				try {
					QueryBody unionQ=q.clone();
					unionQ.setTablesPath(unionTablesPath);
					unionQ.setParams(q.getUnionParams());
					String unionSql=sqlService.getSql(unionQ);
					if(StringUtils.hasLength(unionSql)){
						logger.warn("使用了联合查询");
						data.addAll(config.getJdbcTemplate().queryForList(unionSql));
					}
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			long sqlEndTime=System.currentTimeMillis();
			logger.warn("sql查询花费了:"+(sqlEndTime-sqlSatrtTime)+" ms");
			return process(data,q);
		}else{
			return new ArrayList<Map<String,Object>>();
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
		if(deleteColumns!=null && deleteColumns.size()>0){
			for(Map<String,Object> dataItem:data){
				for(String deleteColumn:deleteColumns){
					dataItem.remove(deleteColumn.substring(deleteColumn.indexOf(".")+1));
				}
			}
		}
		return data;
	}
	
	public Map<String,Object> selectOne(QueryBody q){
		return getOne(select(q));
	}
	
	public List<Map<String,Object>> select(String action,Map<String,Object> params){
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
			Map<String,Object> newParams=new HashMap<String,Object>();
			for(String key:oldParams.keySet()){
				newParams.put(key,oldParams.get(key));
			}
			newParams.putAll(params);
			copy.setParams(newParams);
		}
		return select(copy);
	}
	
	public Map<String,Object> selectOne(String action,Map<String,Object> params){
		return getOne(select(action, params));
	}

	public List<Map<String,Object>> select(String action){
		return select(action,null);
	}
	
	public Map<String,Object> selectOne(String action){
		return getOne(select(action));
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
		for(Map<String,Object> dataItem:data){
			Map<String,Object> dataItemResult=processPerData(dataItem,ret,contexts,q);
			if(dataItemResult!=null){
				dataItem.putAll(q.getConstantData());
				ret.add(dataItemResult);
			}
		}
		return ret;
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
			query_tmp.put(fileName,change);
			querys.clear();
			for(String key:query_tmp.keySet()){
				querys.putAll(query_tmp.get(key));
			}
		}
	}
	
	private void initLoadAllQueryFile(){
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:"+config.getQueryFileDirectory()+"/*.json");  
			if(resources!=null){
				for(Resource resource:resources){
					Map<String,QueryBody> fileQueryBody=FileUtil.getQueryBodyFromFile(resource.getInputStream());
					query_tmp.put(resource.getFile().getName(),fileQueryBody);
					querys.putAll(fileQueryBody);
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
			observerItem=getQueryFileObserverItem();
			observerModule.addObserverItem(observerItem);
		}
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
