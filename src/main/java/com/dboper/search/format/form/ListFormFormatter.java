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
					List<Map<String,Object>> objs=new ArrayList<Map<String,Object>>();
					if(!MapUtil.mapValueEmpty(obj)){
						objs.add(obj);
					}
					fatherTotal.put(objName,objs);
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
					boolean exitsSonItem=false;
					for(Map<String,Object> objItem:objs){
						if(MapUtil.compareMapEquals(objItem,obj)){
							exitsSonItem=true;
							break;
						}
					}
					if(!exitsSonItem){
						if(!MapUtil.mapValueEmpty(obj)){
							objs.add(obj);
						}
					}
				}else if(len==2){
					addItemToList(equalsFather,obj,objName, parts,q.getBaseLists());
				}
				
			}
			for(String listName:listNames){
				List<Object> objs=(List<Object>)equalsFather.get(listName);
				Object obj=fatherTotal.get(listName);
				boolean exitsSonItem=false;
				for(Object objItem:objs){
					if(objItem.equals(obj)){
						exitsSonItem=true;
						break;
					}
				}
				if(!exitsSonItem){
					if(obj!=null){
						objs.add(obj);
					}
				}
			}
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addItemToList(Map<String,Object> equalsFather,Map<String,Object> dataItem,
			String objName,String[] parts,Map<String,String> baseLists){
		boolean baseListObj=false;
		if(baseLists.containsKey(objName)){
			baseListObj=true;
		}
		Object prefixObj=equalsFather.get(parts[0]);
		if(prefixObj!=null && prefixObj instanceof Map){
			Map<String,Object> firstMap=((Map<String,Object>)prefixObj);
			ArrayList<Object> secondObj=(ArrayList<Object>) firstMap.get(parts[1]);
			if(secondObj==null){
				secondObj=new ArrayList<Object>();
				firstMap.put(parts[1],secondObj);
			}
			if(baseListObj && dataItem!=null){
				Object base=dataItem.get(baseLists.get(objName));
				if(base!=null && !secondObj.contains(base)){
					if(base instanceof List){
						secondObj.addAll((List)base);
					}else{
						secondObj.add(base);
					}
				}
			}else if(!baseListObj && dataItem!=null){
				secondObj.add(dataItem);
			}
		}
	}
}
