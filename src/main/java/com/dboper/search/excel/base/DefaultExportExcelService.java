package com.dboper.search.excel.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class DefaultExportExcelService implements ExportExcelService{
	
	private BaseExcelService baseExcelService;
	
	public DefaultExportExcelService() {
		super();
		baseExcelService=new BaseExcelService();
	}

	@Override
	public <T> void generateExcel(String path, List<T> columns,
			List<Map<T, Object>> data) throws FileNotFoundException,IOException {
		generateExcel(path, columns,null, data);
	}

	@Override
	public <T> void generateExcel(String path, List<T> columns,Map<T, String> columnsLabels, List<Map<T, Object>> data) throws FileNotFoundException,IOException {
		generateExcel(path, columns, columnsLabels, data, null);
	}

	@Override
	public <T> void generateExcelFile(OutputStream out,List<T> columns,List<Map<T, Object>> data) throws IOException {
		generateExcelFile(out, columns,null, data);
	}

	@Override
	public <T> void generateExcelFile(OutputStream out,List<T> columns,Map<T, String> columnsLabels, List<Map<T, Object>> data) throws IOException {
		generateExcelFile(out, columns, columnsLabels, data,null);
	}
	
	@Override
	public <T> void generateExcel(String path, List<T> columns,
			List<Map<T, Object>> data, Map<T, Integer> columnsType)
			throws FileNotFoundException, IOException {
		generateExcel(path, columns, null, data, columnsType);
	}

	@Override
	public <T> void generateExcel(String path, List<T> columns,
			Map<T, String> columnsLabels, List<Map<T, Object>> data,
			Map<T, Integer> columnsType) throws FileNotFoundException,
			IOException {
		File file=new File(path);
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream out=new FileOutputStream(path);
		generateExcelFile(out, columns, columnsLabels, data,columnsType);
	}

	@Override
	public <T> void generateExcelFile(OutputStream out, List<T> columns,
			List<Map<T, Object>> data, Map<T, Integer> columnsType)
			throws IOException {
		generateExcelFile(out, columns,null, data, columnsType);
	}

	@Override
	public <T> void generateExcelFile(OutputStream out, List<T> columns,
			Map<T, String> columnsLabels, List<Map<T, Object>> data,
			Map<T, Integer> columnsType) throws IOException {
		Workbook workbook=baseExcelService.generateWorkBook();
		fillData(workbook,workbook.createSheet(),columns,columnsLabels,data,columnsType);
		workbook.write(out);
		out.close();
	}
	
	
	private <T> void fillData(Workbook workbook,Sheet sheet,List<T> columns,Map<T, String> columnsLabels,
			List<Map<T, Object>> data,Map<T, Integer> columnsType){
		int rowNumber=0;
		Row columnsRow=sheet.createRow(rowNumber);
		rowNumber++;
		for(int i=0,len=columns.size();i<len;i++){
			Cell cell=columnsRow.createCell(i);
			setDefaultCellValue(cell,getValue(getColumnsLabel(columns.get(i),columnsLabels)));
		}
		fillData(workbook, sheet, rowNumber, columns, data, columnsType);
	}
	
	public <T> void fillData(Workbook workbook,Sheet sheet,int fromRowIndex,List<T> columns,
			List<Map<T, Object>> data,Map<T, Integer> columnsType){
		for(Map<T,Object> item:data){
			Row row=sheet.createRow(fromRowIndex);
			fromRowIndex++;
			for(int i=0,len=columns.size();i<len;i++){
				Cell cell=row.createCell(i);
				setDefaultCellValue(cell,getCellType(columnsType,columns.get(i)),getValue(item.get(columns.get(i))),workbook);
			}
		}
	}
	
	private <T> Integer getCellType(Map<T, Integer> columnsType,T t){
		if(columnsType!=null){
			return columnsType.get(t);
		}
		return null;
	}
 	
	private Object getValue(Object t){
		if(t==null){
			return "";
		}else{
			return t;
		}
	}
	
	private <T> Object getColumnsLabel(T t,Map<T, String> columnsLabels){
		if(columnsLabels!=null){
			String label=columnsLabels.get(t);
			if(label!=null){
				return label;
			}
		}
		return t;
	}
	
	private <T> void setDefaultCellValue(Cell cell,Integer cellType,T t,Workbook workbook){
		if(cellType==null){
			setDefaultCellValue(cell, t);
		}else{
			if(t instanceof String){
				String tmp=(String) t;
				if(tmp==null || tmp.equals("")){
					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue("");
					return;
				}
			}
			cell.setCellType(cellType);
			switch (cellType) {
			case Cell.CELL_TYPE_NUMERIC:
				cell.setCellValue(Double.parseDouble(t+""));
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cell.setCellValue((Boolean)t);
				break;
			case Cell.CELL_TYPE_FORMULA:
				CellStyle linkStyle=workbook.createCellStyle();
				Font cellFont= workbook.createFont();
				cellFont.setUnderline((byte) 1);
				cellFont.setColor(HSSFColor.BLUE.index);
				linkStyle.setFont(cellFont);
				cell.setCellStyle(linkStyle);
				cell.setCellFormula(t+"");
				break;
			default:
				cell.setCellValue(t+"");
				break;
			}
		}
	}
	
	private <T> void setDefaultCellValue(Cell cell,T t){
		if(t instanceof Number){
			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(Double.parseDouble(t+""));
		}else if(t instanceof Boolean){
			cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
			cell.setCellValue((Boolean)t);
		}else{
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cell.setCellValue(t+"");
		}
	}

}
