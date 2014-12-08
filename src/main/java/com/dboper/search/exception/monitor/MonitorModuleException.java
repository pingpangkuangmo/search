package com.dboper.search.exception.monitor;

public class MonitorModuleException extends RuntimeException{

	private static final long serialVersionUID = 469502959065107098L;
	
	public MonitorModuleException(String paramString) {
		super(paramString);
	} 
	
	public MonitorModuleException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}

}
