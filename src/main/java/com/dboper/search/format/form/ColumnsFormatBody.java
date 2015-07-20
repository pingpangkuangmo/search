package com.dboper.search.format.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColumnsFormatBody {

	private List<String> objNames=new ArrayList<String>();
	private Map<String,List<String>> totalObjColumns=new HashMap<String,List<String>>();
	
	private List<String> listNames=new ArrayList<String>();
	
	public void addListName(String listName){
		if(!listNames.contains(listName)){
			listNames.add(listName);
		}
	}
	
	public void addObjName(String objName){
		if(!objNames.contains(objName)){
			objNames.add(objName);
		}
	}
	
	public void addObj(String objName,List<String> objColumn){
		if(!objNames.contains(objName)){
			objNames.add(objName);
			totalObjColumns.put(objName,objColumn);
		}
	}
	
	public List<String> getListNames() {
		return listNames;
	}

	public void setListNames(List<String> listNames) {
		this.listNames = listNames;
	}

	public List<String> getObjColumns(String objName){
		List<String> columns=totalObjColumns.get(objName);
		if(columns==null){
			columns=new ArrayList<String>();
			totalObjColumns.put(objName,columns);
		}
		return columns;
	}

	public List<String> getObjNames() {
		return objNames;
	}

	public void setObjNames(List<String> objNames) {
		this.objNames = objNames;
	}

	public Map<String, List<String>> getTotalObjColumns() {
		return totalObjColumns;
	}

	public void setTotalObjColumns(Map<String, List<String>> totalObjColumns) {
		this.totalObjColumns = totalObjColumns;
	}
}
