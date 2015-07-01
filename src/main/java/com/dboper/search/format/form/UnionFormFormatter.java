package com.dboper.search.format.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dboper.search.domain.QueryBody;
import com.dboper.search.format.ProcessUnit;

public class UnionFormFormatter implements ProcessUnit<FormFormatterContext>{
	
	private List<FormFormatter> formatters;
	
	private static final String FATHER="fatherFormatter";
	private static final String FATHER_OBJ="father";
	private static final String NAME="formFormatter";
	
	public UnionFormFormatter(){
		formatters=new ArrayList<FormFormatter>();
		formatters.add(new ListFormFormatter());
		formatters.add(new MapFormFormatter());
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public FormFormatterContext prepareContext(QueryBody q) {
		List<String> columns=q.getColumns();
		List<FormFormatter> containsFormFormatters=getContainsFormFormatters(columns);
		if(containsFormFormatters.size()>0){
			Map<String,ColumnsFormatBody> columnsInfo=collectColumnsInfo(containsFormFormatters,columns);
			FormFormatterContext context=new FormFormatterContext();
			context.setColumnsInfo(columnsInfo);
			context.setContainsFormFormatters(containsFormFormatters);
			context.setQ(q);
			return context;
		}
		return null;
	}
	
	@Override
	public Map<String, Object> processLineData(Map<String, Object> data,Map<String,Object> ret,List<Map<String,Object>> allRets,FormFormatterContext context) {
		//数据值已经格式化了，只对ret进行处理
		Map<String, ColumnsFormatBody> columnsInfo=context.getColumnsInfo();
		List<FormFormatter> containsFormFormatters=context.getContainsFormFormatters();
		QueryBody q=context.getQ();
		//先构造父元素
		List<String> fatherColumns=columnsInfo.get(FATHER).getObjColumns(FATHER_OBJ);
		Map<String,Object> fatherTotal=new HashMap<String,Object>();
		for(String column:fatherColumns){
			fatherTotal.put(column,ret.get(column));
		}
		//处理其他formatter，交给具体的formatter来处理，然后设置进父元素中
		for(FormFormatter formatter:containsFormFormatters){
			ColumnsFormatBody columnsFormatBody=columnsInfo.get(formatter.getFormatterType());
			initObj(ret,fatherTotal,columnsFormatBody,formatter);
			fatherTotal=formatter.fromat(ret,fatherTotal,allRets,columnsFormatBody,q);
			//fatherTotal==null 表示会聚合到已有的fatherTotal中，不需要再新添加
			if(fatherTotal==null){
				break;
			}
		}
		return fatherTotal;
	}

	
	
	public List<Map<String,Object>> format(List<Map<String,Object>> data,QueryBody q){
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		List<String> columns=q.getColumns();
		if(data==null || q==null || columns==null || columns.size()<1){
			return ret;
		}
		List<FormFormatter> containsFormFormatters=getContainsFormFormatters(columns);
		if(containsFormFormatters.size()<1){
			return data;
		}
		ret=new ArrayList<Map<String,Object>>(data.size());
		Map<String,ColumnsFormatBody> columnsInfo=collectColumnsInfo(containsFormFormatters,columns);
		for(Map<String,Object> item:data){
			processOneLineData(item,columnsInfo,containsFormFormatters,ret,q);
		}
		return ret;
	}
	
	private void processOneLineData(Map<String, Object> item,
			Map<String, ColumnsFormatBody> columnsInfo,
			List<FormFormatter> containsFormFormatters,List<Map<String,Object>> ret,QueryBody q) {
		//先构造父元素
		List<String> fatherColumns=columnsInfo.get(FATHER).getObjColumns(FATHER_OBJ);
		Map<String,Object> fatherTotal=new HashMap<String,Object>();
		for(String column:fatherColumns){
			fatherTotal.put(column,item.get(column));
		}
		//处理其他formatter，交给具体的formatter来处理，然后设置进父元素中
		for(FormFormatter formatter:containsFormFormatters){
			ColumnsFormatBody columnsFormatBody=columnsInfo.get(formatter.getFormatterType());
			initObj(item,fatherTotal,columnsFormatBody,formatter);
			fatherTotal=formatter.fromat(item,fatherTotal,ret,columnsFormatBody,q);
			//fatherTotal==null 表示会聚合到已有的fatherTotal中，不需要再新添加
			if(fatherTotal==null){
				break;
			}
		}
		if(fatherTotal!=null){
			ret.add(fatherTotal);
		}
	}

	private void initObj(Map<String, Object> item,Map<String, Object> fatherTotal, ColumnsFormatBody columnsFormatBody,
			FormFormatter formatter) {
		Set<String> objNames=columnsFormatBody.getObjNames();
		for(String objName:objNames){
			Map<String,Object> obj=new HashMap<String,Object>();
			List<String> objColumns=columnsFormatBody.getObjColumns(objName);
			for(String objColumn:objColumns){
				obj.put(objColumn,item.get(objName+formatter.getFormatterType()+objColumn));
			}
			fatherTotal.put(objName,obj);
		}
		Set<String> listNames=columnsFormatBody.getListNames();
		for(String listName:listNames){
			fatherTotal.put(listName,item.get(listName+formatter.getFormatterType()));
		}
	}

	private Map<String,ColumnsFormatBody> collectColumnsInfo(List<FormFormatter> containsFormFormatters, List<String> columns) {
		Map<String,ColumnsFormatBody> columnsInfo=initColumnsInfo(containsFormFormatters);
		List<String> fatherColumns=columnsInfo.get(FATHER).getObjColumns(FATHER_OBJ);
		for(String column:columns){
			if(column.contains("as")){
				String tmp=column.substring(column.indexOf(" as ")+4).trim();
				//去掉as 别名时添加的 ``
				if(tmp.startsWith("`") && tmp.endsWith("`")){
					tmp=tmp.substring(1,tmp.length()-1);
				}
				boolean hasFormatter=false;
				for(FormFormatter formatter:containsFormFormatters){
					String formatterType=formatter.getFormatterType();
					if(tmp.contains(formatterType)){
						hasFormatter=true;
						String[] ObjectAndColumn=tmp.split(formatterType);
						if(ObjectAndColumn.length>1){
							ColumnsFormatBody currentFormatterContext=columnsInfo.get(formatterType);
							currentFormatterContext.getObjNames().add(ObjectAndColumn[0]);
							List<String> objColumns=currentFormatterContext.getObjColumns(ObjectAndColumn[0]);
							objColumns.add(ObjectAndColumn[1]);
						}else{
							//fatherColumns.add(tmp);
							ColumnsFormatBody currentFormatterContext=columnsInfo.get(formatterType);
							currentFormatterContext.addListName(ObjectAndColumn[0]);
						}
						break;
					}
				}
				if(!hasFormatter){
					fatherColumns.add(tmp);
				}
			}else{
				fatherColumns.add(column.substring(column.indexOf(".")+1));
			}
		}
		return columnsInfo;
	}

	private Map<String,ColumnsFormatBody> initColumnsInfo(List<FormFormatter> containsFormFormatters) {
		Map<String,ColumnsFormatBody> columnsInfo=new HashMap<String,ColumnsFormatBody>();
		//初始化父类columns
		ColumnsFormatBody fatherColumnsFormatBody=new ColumnsFormatBody();
		fatherColumnsFormatBody.addObj(FATHER_OBJ,new ArrayList<String>());
		columnsInfo.put(FATHER,fatherColumnsFormatBody);
		//初始化其他FormFormatter的columns
		for(FormFormatter formatter:containsFormFormatters){
			columnsInfo.put(formatter.getFormatterType(),new ColumnsFormatBody());
		}
		return columnsInfo;
	}

	private List<FormFormatter> getContainsFormFormatters(List<String> columns) {
		List<FormFormatter> containsFormatters=new ArrayList<FormFormatter>();
		for(String column:columns){
			for(FormFormatter formatter:formatters){
				if(column.contains(formatter.getFormatterType()) && !containsFormatters.contains(formatter)){
					containsFormatters.add(formatter);
				}
			}
		}
		return containsFormatters;
	}

	public void addFormFormatter(FormFormatter formatter){
		if(formatter!=null){
			this.formatters.add(formatter);
		}
	}

	

	
}
