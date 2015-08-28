package com.dboper.search.format.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dboper.search.domain.QueryBody;
import com.dboper.search.util.MapUtil;

public class ListFormFormatter implements FormFormatter{

	@Override
	public String getFormatterType() {
		return "@list";
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public Map<String, Object> fromat(Map<String, Object> item,Map<String,Object> fatherTotal,
			List<Map<String, Object>> data, ColumnsFormatBody columnsFormatBody, QueryBody q) {
		List<String> groupColumns=q.getGroupColumns();
 		if(groupColumns==null || groupColumns.size()<1 || !q.getColumns().containsAll(groupColumns)){
			//不支持list 
			return fatherTotal;
		}
		boolean fatherExits=false;
		Map<String,Object> equalsFather=null;
		for(Map<String,Object> alreadyFather:data){
			if(MapUtil.compareTwoMapEquals(fatherTotal,alreadyFather,groupColumns)){
				fatherExits=true;
				equalsFather=alreadyFather;
				break;
			}
		}
		List<String> objNames=columnsFormatBody.getObjNames();
		List<String> listNames=columnsFormatBody.getListNames();
		if(!fatherExits){
			for(String objName:objNames){
				Map<String,Object> obj=(Map<String, Object>)fatherTotal.get(objName);
				String[] parts=objName.split("\\.");
				if(parts.length==1){
					Map<String,String> baseLists=q.getBaseLists();
					if(baseLists!=null && obj!=null && baseLists.containsKey(objName)){
						List<Object> objs=new ArrayList<Object>();
						Object keyValue=obj.get(baseLists.get(objName));
						if(keyValue!=null){
							objs.add(keyValue);
							fatherTotal.put(objName,objs);
						}
					}else{
						List<Map<String,Object>> objs=new ArrayList<Map<String,Object>>();
						if(!MapUtil.mapValueEmpty(obj)){
							objs.add(obj);
						}
						fatherTotal.put(objName,objs);
					}
				}else if(parts.length>1){
					MapUtil.addMapsonToList(fatherTotal,objName,parts,obj,q.getBaseLists());
				}
				
			}
			for(String listName:listNames){
				Object obj=fatherTotal.get(listName);
				List<Object> objs=new ArrayList<Object>();
				if(obj!=null){
					objs.add(obj);
				}
				fatherTotal.put(listName,objs);
			}
			return fatherTotal;
		}else{
			for(String objName:objNames){
				String[] parts=objName.split("\\.");
				int len=parts.length;
				Map<String,Object> obj=(Map<String, Object>)fatherTotal.get(objName);
				if(len==1){
					Map<String,String> baseLists=q.getBaseLists();
					if(baseLists!=null && obj!=null && baseLists.containsKey(objName)){
						List<Object> objs=(List<Object>)equalsFather.get(objName);
						Object keyValue=obj.get(baseLists.get(objName));
						MapUtil.judgeObjectExitsAndAdd(objs,keyValue);
					}else{
						List<Map<String,Object>> objs=null;
						Object oldObjs=equalsFather.get(objName);
						if(oldObjs==null){
							objs=new ArrayList<Map<String,Object>>();
							equalsFather.put(objName,objs);
						}else if(oldObjs instanceof Map){
							objs=new ArrayList<Map<String,Object>>();
							objs.add((Map<String, Object>) oldObjs);
							equalsFather.put(objName,objs);
						}else if(oldObjs instanceof List){
							objs=(List<Map<String, Object>>) oldObjs;
						}
						MapUtil.judgeMapExitsAndAdd(objs,obj);
					}
				}else if(len==2){
					MapUtil.addMapsonToList(equalsFather,objName, parts, obj, q.getBaseLists());
				}
				
			}
			for(String listName:listNames){
				List<Object> objs=(List<Object>)equalsFather.get(listName);
				Object obj=fatherTotal.get(listName);
				MapUtil.judgeObjectExitsAndAdd(objs,obj);
			}
			return null;
		}
	}
}
