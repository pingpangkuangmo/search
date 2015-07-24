package com.dboper.search.observer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.alibaba.fastjson.TypeReference;
import com.dboper.search.domain.ComplexQueryBody;
import com.dboper.search.util.FileUtil;

public class ComplexQueryFileListener extends FileAlterationListenerAdaptor{

	private ProcessComplexQueryFileChange processComplexQueryFileChange;

	public ComplexQueryFileListener(
			ProcessComplexQueryFileChange processComplexQueryFileChange) {
		super();
		this.processComplexQueryFileChange = processComplexQueryFileChange;
	}
	
	@Override
	public void onFileChange(File file) {
		InputStream in=null;
		try {
			in = new FileInputStream(file);
			processComplexQueryFileChange.processComplexQueryBodyChange(FileUtil.getTFromInputStream(in,
					new TypeReference<Map<String,ComplexQueryBody>>(){}),file.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(in!=null){
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ProcessComplexQueryFileChange getProcessComplexQueryFileChange() {
		return processComplexQueryFileChange;
	}

	public void setProcessComplexQueryFileChange(
			ProcessComplexQueryFileChange processComplexQueryFileChange) {
		this.processComplexQueryFileChange = processComplexQueryFileChange;
	}
	
}
