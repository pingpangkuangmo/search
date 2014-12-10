package com.dboper.search.format.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColumnsFormatBody {

	private Set<String> objNames=new HashSet<String>();
	private Map<String,List<String>> totalObjColumns=new HashMap<String,List<String>>();
	
	public void addObj(String objName,List<String> objColumn){
		objNames.add(objName);
		totalObjColumns.put(objName,objColumn);
	}
	
	public List<String> getObjColumns(String objName){
		List<String> columns=totalObjColumns.get(objName);
		if(columns==null){
			columns=new ArrayList<String>();
			totalObjColumns.put(objName,columns);
		}
		return columns;
	}

	public Set<String> getObjNames() {
		return objNames;
	}

	public void setObjNames(Set<String> objNames) {
		this.objNames = objNames;
	}

	public Map<String, List<String>> getTotalObjColumns() {
		return totalObjColumns;
	}

	public void setTotalObjColumns(Map<String, List<String>> totalObjColumns) {
		this.totalObjColumns = totalObjColumns;
	}
}
