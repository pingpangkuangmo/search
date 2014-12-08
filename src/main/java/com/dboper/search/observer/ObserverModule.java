package com.dboper.search.observer;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.dboper.search.Bootstrap;
import com.dboper.search.exception.monitor.MonitorModuleException;

public class ObserverModule implements Bootstrap{

	private ConcurrentHashMap<String,ObserverItem> observerItems=new ConcurrentHashMap<String, ObserverItem>();
	private ConcurrentHashMap<String,FileAlterationMonitor> monitors=new ConcurrentHashMap<String,FileAlterationMonitor>();
	ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private final Log logger = LogFactory.getLog(getClass());
	
	
	public void addObserverItem(ObserverItem observerItem){
		if(observerItem!=null){
			observerItems.put(observerItem.getName(),observerItem);
		}
	}
	
	public void removeObserverItem(String observerItemName){
		observerItems.remove(observerItemName);
	}

	@Override
	public void init() {
		for(String observerItemName:observerItems.keySet()){
			try {
				ObserverItem observerItem=observerItems.get(observerItemName);
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
					logger.warn("对于"+observerItemName+"初始化监控完成");
				}else{
					logger.warn("对于"+observerItemName+"初始化监控时没有找到相应的资源文件");
				}
			} catch (Exception e) {
				logger.warn("对于"+observerItemName+"初始化监控时失败");
				e.printStackTrace();
			}
		}
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
