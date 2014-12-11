package com.dboper.search.observer;

import java.io.File;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.dboper.search.util.FileUtil;

public class QueryFileListener extends FileAlterationListenerAdaptor{
	
	private ProcessQueryFileChange processFileChange;
	
	public QueryFileListener(ProcessQueryFileChange processFileChange) {
		super();
		this.processFileChange = processFileChange;
	}

	@Override
	public void onFileChange(File file) {
		processFileChange.processQueryBodyChange(FileUtil.getQueryBodyFromFile(file),file.getName());
	}

	public ProcessQueryFileChange getProcessFileChange() {
		return processFileChange;
	}

	public void setProcessFileChange(ProcessQueryFileChange processFileChange) {
		this.processFileChange = processFileChange;
	}
	
}
