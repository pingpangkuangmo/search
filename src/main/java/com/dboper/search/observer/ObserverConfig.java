package com.dboper.search.observer;

public interface ObserverConfig {

	public boolean isMonitorModule();
	
	public boolean isMonitorRelationFile();
	
	public boolean isMonitorQueryFile();
	
	public String getQueryFileDirectory();
	
	public String getRelationDir();
}
