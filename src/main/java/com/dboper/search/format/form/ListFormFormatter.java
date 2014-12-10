package com.dboper.search.format.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dboper.search.domain.QueryBody;
import com.dboper.search.util.MapUtil;

public class ListFormFormatter implements FormFormatter{

	@Override
	public String getFormatterType() {
		return "@list";
	}

	@SuppressWarnings("unchecked")
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
		Set<String> objNames=columnsFormatBody.getObjNames();
		if(!fatherExits){
			for(String objName:objNames){
				Map<String,Object> obj=(Map<String, Object>)fatherTotal.get(objName);
				List<Map<String,Object>> objs=new ArrayList<Map<String,Object>>();
				objs.add(obj);
				fatherTotal.put(objName,objs);
			}
			return fatherTotal;
		}else{
			for(String objName:objNames){
				List<Map<String,Object>> objs=(List<Map<String,Object>>)equalsFather.get(objName);
				Map<String,Object> obj=(Map<String, Object>)fatherTotal.get(objName);
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
			}
			return null;
		}
	}

	

}
