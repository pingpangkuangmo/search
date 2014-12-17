package com.dboper.search.observer;

public interface ObserverConfig {
	
	public boolean isMonitorRelationFile();
	
	public boolean isMonitorQueryFile();
	
	public boolean isMonitorBaseRelationFiles();
	
	public String getQueryFileDirectory();
	
	public String getRelationDir();
	
	public String getBaseRelationFilesDir();
}
