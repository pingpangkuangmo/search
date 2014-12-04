package com.dboper.search.observer;

import java.io.File;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.dboper.search.util.FileToQueryBodyUtil;

public class QueryFileListener extends FileAlterationListenerAdaptor{
	
	private ProcessFileChange processFileChange;
	
	public QueryFileListener(ProcessFileChange processFileChange) {
		super();
		this.processFileChange = processFileChange;
	}

	@Override
	public void onFileChange(File file) {
		processFileChange.processChange(FileToQueryBodyUtil.getQueryBodyFromFile(file),file.getName());
	}

	public ProcessFileChange getProcessFileChange() {
		return processFileChange;
	}

	public void setProcessFileChange(ProcessFileChange processFileChange) {
		this.processFileChange = processFileChange;
	}
	
}
