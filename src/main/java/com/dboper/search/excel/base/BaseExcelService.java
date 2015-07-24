package com.dboper.search.excel.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class BaseExcelService {

	public Workbook generateWorkBook(){
		return new HSSFWorkbook();
	}
	
	public Sheet getSheet(String filePath) throws InvalidFormatException, IOException{
		FileInputStream in=new FileInputStream(filePath);
		return getSheet(in);
	}
	
	public Sheet getSheet(InputStream in) throws InvalidFormatException, IOException{
		return getSheet(in,0);
	}
	
	public Sheet getSheet(InputStream in,int index) throws InvalidFormatException, IOException{
		Workbook workbook=WorkbookFactory.create(in);
		return workbook.getSheetAt(index);
	}
}
