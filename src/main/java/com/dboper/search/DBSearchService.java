package com.dboper.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dboper.search.config.Configuration;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.format.form.UnionFormFormatter;
import com.dboper.search.format.value.UnionValueFormatter;
import com.dboper.search.observer.ObserverControll;
import com.dboper.search.observer.ProcessFileChange;
import com.dboper.search.util.FileUtil;

@Service
public class DBSearchService implements ProcessFileChange,Bootstrap{
	
	private final Log logger = LogFactory.getLog(DBSearchService.class);

	@Autowired
	private Configuration config;
	@Autowired
	private SqlService sqlService;
	
	private ConcurrentHashMap <String,QueryBody> querys=new ConcurrentHashMap<String,QueryBody>();
	
	private Map<String,Map<String,QueryBody>> query_tmp=new HashMap<String,Map<String,QueryBody>>();
	
	private ObserverControll observerControll;
	
	private UnionValueFormatter unionValueFormatter;
	
	private UnionFormFormatter unionFormFormatter;
	
	@Override
	public void init() {
		initLoadAllQueryFile();
		initFormatters();
		sqlService.init();
		initObserverModule();
	}
	
	public void refreshTablesRelationFromDB(){
		sqlService.initTablesRelationFromDB();
	}

	//对外提供的监控方法
	
	public void startMonitorModule(){
		observerControll.startMonitorModule();
	}
	
	public void stopMonitorModule(){
		observerControll.stopMonitorModule();
	}
	
	public void startMonitor(String monitorName) throws Exception{
		observerControll.startMonitor(monitorName);
	}
	
	public void stopMonitor(String monitorName) throws Exception{
		observerControll.stopMonitor(monitorName);
	}

	public List<Map<String,Object>> select(QueryBody q){
		String sql=sqlService.getSql(q);
		if(StringUtils.hasLength(sql)){
			return unionFormFormatter.format(unionValueFormatter.formatValue(config.getJdbcTemplate().queryForList(sql),q),q);
		}else{
			return new ArrayList<Map<String,Object>>();
		}
	}
	
	public Map<String,Object> selectOne(QueryBody q){
		return select(q).get(0);
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
		return select(action, params).get(0);
	}

	public List<Map<String,Object>> select(String action){
		return select(action,null);
	}
	
	public Map<String,Object> selectOne(String action){
		return select(action).get(0);
	}
	
	@Override
	public void processChange(Map<String, QueryBody> change,String fileName) {
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
	
	private void initFormatters() {
		unionValueFormatter=new UnionValueFormatter();
		unionFormFormatter=new UnionFormFormatter();
	}

	private void initObserverModule() {
		observerControll=new ObserverControll(config,this);
		observerControll.initObserverModule();
	}
	
	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

}
