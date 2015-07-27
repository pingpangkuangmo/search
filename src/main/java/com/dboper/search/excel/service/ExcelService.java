package com.dboper.search.excel.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dboper.search.Bootstrap;
import com.dboper.search.DBSearchService;
import com.dboper.search.config.Configuration;
import com.dboper.search.excel.base.DefaultExportExcelService;
import com.dboper.search.excel.base.ExcelConfigBody;
import com.dboper.search.excel.base.ExportExcelService;
import com.dboper.search.observer.ExcelConfigFileListener;
import com.dboper.search.observer.ObserverItem;
import com.dboper.search.observer.ObserverModule;
import com.dboper.search.observer.ObserverModuleUtil;
import com.dboper.search.observer.ProcessExcelFileChange;
import com.dboper.search.util.FileUtil;

@Service
public class ExcelService implements Bootstrap,ProcessExcelFileChange{
	
	private final Log logger = LogFactory.getLog(DBSearchService.class);
	
	@Autowired
	private DBSearchService dbSearchService;
	
	@Autowired
	private Configuration config;
	
	private Map<String,ExcelConfigBody> excelConfigBodys=new HashMap<String,ExcelConfigBody>();
	
	private ExportExcelService exportExcelService=new DefaultExportExcelService();

	public void generateExcelFile(String action,Map<String,Object> params,String path) throws FileNotFoundException, IOException{
		List<Map<String,Object>> datas=dbSearchService.select(action, params);
		ExcelConfigBody excelConfigBody=excelConfigBodys.get(action);
		if(excelConfigBody!=null){
			exportExcelService.generateExcel(path,excelConfigBody.getColumns(),
					excelConfigBody.getColumnLabels(),datas,excelConfigBody.getColumnsType());
		}
	}
	
	@Override
	public void init() {
		loadExcelConfig();
		initObserverModule();
	}

	private void initObserverModule() {
		ObserverModule observerModule=ObserverModuleUtil.getObserverModule();
		if(config.isMonitorQueryFile()){
			ObserverItem observerItem=getExcelConfigFileObserverItem();
			observerModule.addObserverItem(observerItem);
		}
	}
	
	private void loadExcelConfig(){
		String excelConfigPath=config.getExcelConfig();
		if(!StringUtils.hasLength(excelConfigPath)){
			return ;
		}
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:"+excelConfigPath+"/*.json");  
			if(resources!=null){
				for(Resource resource:resources){
					Map<String,ExcelConfigBody> fileExcelConfigs=FileUtil.getTFromInputStream(resource.getInputStream(),
							new TypeReference<Map<String,ExcelConfigBody>>(){});
					excelConfigBodys.putAll(fileExcelConfigs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ObserverItem getExcelConfigFileObserverItem() {
		ObserverItem observerItem=new ObserverItem();
		observerItem.setDir(config.getExcelConfig());
		observerItem.setInterval(5000L);
		observerItem.setName("excelConfig");
		observerItem.setSuffix("json");
		List<FileAlterationListener> listeners=new ArrayList<FileAlterationListener>();
		listeners.add(new ExcelConfigFileListener(this));
		observerItem.setListeners(listeners);
		return observerItem;
	}

	@Override
	public void processExcelConfigChange(Map<String, ExcelConfigBody> change,
			String fileName) {
		logger.info("observ "+fileName+" changed");
		if(change!=null){
			excelConfigBodys.putAll(change);
		}
	}
}
