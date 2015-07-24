package com.dboper.search.excel.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public interface ExportExcelService {

	public <T> void generateExcel(String path,List<T> columns,List<Map<T,Object>> data) throws FileNotFoundException,IOException;
	
	public <T> void generateExcel(String path,List<T> columns,List<Map<T,Object>> data,Map<T,Integer> columnsType) throws FileNotFoundException,IOException;
	
	public <T> void generateExcel(String path,List<T> columns,Map<T,String> columnsLabels,List<Map<T,Object>> data) throws FileNotFoundException,IOException;
	
	public <T> void generateExcel(String path,List<T> columns,Map<T,String> columnsLabels,List<Map<T,Object>> data,Map<T,Integer> columnsType) throws FileNotFoundException,IOException;
	
	public <T> void generateExcelFile(OutputStream out,List<T> columns,List<Map<T,Object>> data) throws IOException;
	
	public <T> void generateExcelFile(OutputStream out,List<T> columns,List<Map<T,Object>> data,Map<T,Integer> columnsType) throws IOException;
	
	public <T> void generateExcelFile(OutputStream out,List<T> columns,Map<T,String> columnsLabels,List<Map<T,Object>> data) throws IOException;
	
	public <T> void generateExcelFile(OutputStream out,List<T> columns,Map<T,String> columnsLabels,List<Map<T,Object>> data,Map<T,Integer> columnsType) throws IOException;
	
	public <T> void fillData(Workbook workbook,Sheet sheet,int fromRowIndex,List<T> columns,
			List<Map<T, Object>> data,Map<T, Integer> columnsType);
}
