package com.dboper.search.observer;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.dboper.search.exception.monitor.MonitorModuleException;

public class ObserverModule{

	private ConcurrentHashMap<String,ObserverItem> observerItems=new ConcurrentHashMap<String, ObserverItem>();
	private ConcurrentHashMap<String,FileAlterationMonitor> monitors=new ConcurrentHashMap<String,FileAlterationMonitor>();
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private final Logger logger=LoggerFactory.getLogger(ObserverModule.class);
	
	
	public void addObserverItem(ObserverItem observerItem){
		if(observerItem!=null){
			String observerItemName=observerItem.getName();
			observerItems.put(observerItemName,observerItem);
			try {
				Resource[] resources = resolver.getResources("classpath*:"+observerItem.getDir()+"/*."+observerItem.getSuffix());  
				if(resources!=null && resources.length>0){
					String dir=resources[0].getFile().getParentFile().getAbsolutePath();
					FileAlterationObserver observer=new FileAlterationObserver(dir,FileFilterUtils.suffixFileFilter("."+observerItem.getSuffix()));
					for(FileAlterationListener listener:observerItem.getListeners()){
						observer.addListener(listener);
					}
					FileAlterationMonitor monitor=new FileAlterationMonitor(observerItem.getInterval(),observer);
					monitors.put(observerItemName,monitor);
					monitor.start();
					logger.info("对于"+observerItemName+"初始化监控完成");
				}else{
					logger.info("对于"+observerItemName+"初始化监控时没有找到相应的资源文件");
				}
			}catch (Exception e) {
				e.printStackTrace();
				logger.info("对于"+observerItemName+"初始化监控时失败");
			}
		}
	}
	
	public void removeObserverItem(String observerItemName) throws Exception{
		stop(observerItemName);
		observerItems.remove(observerItemName);
	}
	
	public void start(String monitorName) throws Exception{
		FileAlterationMonitor monitor=monitors.get(monitorName);
		if(monitor!=null){
			monitor.start();
		}else{
			throw new MonitorModuleException("monitorName为"+monitorName+"的monitor不存在");
		}
	}
	
	public void stop(String monitorName) throws Exception{
		FileAlterationMonitor monitor=monitors.get(monitorName);
		if(monitor!=null){
			monitor.stop();
		}else{
			throw new MonitorModuleException("monitorName为"+monitorName+"的monitor不存在");
		}
	}
	
	public void stopAllMonitor() throws Exception{
		for(String monitorName:monitors.keySet()){
			stop(monitorName);
		}
	}
}
