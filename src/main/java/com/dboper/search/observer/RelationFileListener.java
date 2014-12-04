package com.dboper.search.observer;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;


public class RelationFileListener extends FileAlterationListenerAdaptor{
	
	

	@Override
	public void onFileChange(File file) {
		//processFileChange.processChange(FileToQueryBodyUtil.getQueryBodyFromFile(file),file.getName());
	}
}
