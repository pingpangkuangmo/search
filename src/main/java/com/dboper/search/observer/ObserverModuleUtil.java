package com.dboper.search.observer;

public class ObserverModuleUtil {

	public static ObserverModule observerModule;

	public static ObserverModule getObserverModule() {
		return observerModule;
	}

	public static void setObserverModule(ObserverModule observerModule) {
		ObserverModuleUtil.observerModule = observerModule;
	}
	
	public void startMonitor(String monitorName) throws Exception{
		if(observerModule!=null){
			observerModule.start(monitorName);
		}
	}
	
	public void stopMonitor(String monitorName) throws Exception{
		if(observerModule!=null){
			observerModule.stop(monitorName);
		}
	}
	
}
