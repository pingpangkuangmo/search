package com.dboper.search.observer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dboper.search.DBSearchService;
import com.dboper.search.exception.monitor.MonitorModuleException;

public class ObserverControll{

	private ObserverConfig config;
	private ObserverModule observerModule;
	private ProcessQueryFileChange processFileChange;
	private final Log logger = LogFactory.getLog(DBSearchService.class);
	
	public ObserverControll(ObserverConfig config,ProcessQueryFileChange processFileChange){
		this.config=config;
		this.processFileChange=processFileChange;
	}
	
	public void initObserverModule() {
		if(config.isMonitorModule()){
			startMonitorModule();
		}
	}
	
	public void startMonitorModule(){
		if(observerModule!=null){
			throw new MonitorModuleException("monitor module already start");
		}else{
			observerModule=new ObserverModule();
			if(config.isMonitorQueryFile()){
				observerModule.addObserverItem(getQueryFileObserverItem());
			}
			if(config.isMonitorRelationFile()){
				observerModule.addObserverItem(getRelationFileObserverItem());
			}
			if(config.isMonitorBaseRelationFiles()){
				observerModule.addObserverItem(getBaseRelationObserverItem());
			}
			observerModule.init();
			logger.warn("monitor module start");
		}
	}
	
	
	private ObserverItem getBaseRelationObserverItem() {
		ObserverItem observerItem=new ObserverItem();
		observerItem.setDir(config.getBaseRelationFilesDir());
		observerItem.setInterval(5000L);
		observerItem.setName("baseRelation");
		observerItem.setSuffix("json");
		List<FileAlterationListener> listeners=new ArrayList<FileAlterationListener>();
		//listeners.add(new BaseRelationListener(baseRelationProcess));
		observerItem.setListeners(listeners);
		return observerItem;
	}

	public void stopMonitorModule(){
		if(observerModule!=null){
			try {
				observerModule.stopAllMonitor();
			} catch (Exception e) {
				e.printStackTrace();
				throw new MonitorModuleException(e.getMessage(),e);
			}
			observerModule=null;
			logger.warn("monitor module stop");
		}else{
			throw new MonitorModuleException("monitor module not start");
		}
	}
	
	
	public void startMonitor(String monitorName) throws Exception{
		if(observerModule!=null){
			observerModule.start(monitorName);
			logger.warn("monitor->"+monitorName+" start");
		}else{
			throw new MonitorModuleException("monitor module not start");
		}
	}
	
	public void stopMonitor(String monitorName) throws Exception{
		if(observerModule!=null){
			observerModule.stop(monitorName);
			logger.warn("monitor->"+monitorName+" stop");
		}else{
			throw new MonitorModuleException("monitor module not start");
		}
	}
	
	private ObserverItem getRelationFileObserverItem() {
		ObserverItem observerItem=new ObserverItem();
		observerItem.setDir(config.getQueryFileDirectory());
		observerItem.setInterval(5000L);
		observerItem.setName("relation");
		observerItem.setSuffix("json");
		List<FileAlterationListener> listeners=new ArrayList<FileAlterationListener>();
		listeners.add(new QueryFileListener(processFileChange));
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
		listeners.add(new QueryFileListener(processFileChange));
		observerItem.setListeners(listeners);
		return observerItem;
	}

	public ProcessQueryFileChange getProcessFileChange() {
		return processFileChange;
	}

	public void setProcessFileChange(ProcessQueryFileChange processFileChange) {
		this.processFileChange = processFileChange;
	}
}
