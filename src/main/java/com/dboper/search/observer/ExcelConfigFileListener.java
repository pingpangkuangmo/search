package com.dboper.search.observer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import com.alibaba.fastjson.TypeReference;
import com.dboper.search.excel.base.ExcelConfigBody;
import com.dboper.search.util.FileUtil;

public class ExcelConfigFileListener extends FileAlterationListenerAdaptor{

	private ProcessExcelFileChange processFileChange;
	
	public ExcelConfigFileListener(ProcessExcelFileChange processFileChange) {
		super();
		this.processFileChange = processFileChange;
	}

	@Override
	public void onFileChange(File file) {
		InputStream in=null;
		try {
			in = new FileInputStream(file);
			processFileChange.processExcelConfigChange(FileUtil.getTFromInputStream(in,
					new TypeReference<Map<String,ExcelConfigBody>>(){}),file.getName());
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

	
}
