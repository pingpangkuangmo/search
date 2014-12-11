package com.dboper.search.observer;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.dboper.search.util.FileUtil;

public class BaseRelationListener extends FileAlterationListenerAdaptor{
	
	private BaseRelationProcess baseRelationProcess;
	
	public BaseRelationListener(BaseRelationProcess baseRelationProcess) {
		super();
		this.baseRelationProcess = baseRelationProcess;
	}

	@Override
	public void onFileChange(File file) {
		baseRelationProcess.processBaseRelation(file.getName(),FileUtil.getTablesRelation(file));
	}

	public BaseRelationProcess getBaseRelationProcess() {
		return baseRelationProcess;
	}

	public void setBaseRelationProcess(BaseRelationProcess baseRelationProcess) {
		this.baseRelationProcess = baseRelationProcess;
	}
}
