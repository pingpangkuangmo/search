package com.dboper.search.observer;

import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;

public class ObserverItem {

	private String name;
	private String dir;
	private String suffix;
	private Long interval;
	private List<FileAlterationListener> listeners;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public List<FileAlterationListener> getListeners() {
		return listeners;
	}
	public void setListeners(List<FileAlterationListener> listeners) {
		this.listeners = listeners;
	}
	public Long getInterval() {
		return interval;
	}
	public void setInterval(Long interval) {
		this.interval = interval;
	}
}
