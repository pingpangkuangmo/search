package com.dboper.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.dboper.search.config.Configuration;
import com.dboper.search.domain.QueryBody;
import com.dboper.search.format.Formatter;
import com.dboper.search.format.MapFormatter;
import com.dboper.search.format.Rule;
import com.dboper.search.observer.ObserverControll;
import com.dboper.search.observer.ProcessFileChange;
import com.dboper.search.util.FileToQueryBodyUtil;
import com.dboper.search.util.MapValueUtil;

@Service
public class DBSearchService implements ProcessFileChange,Bootstrap{
	
	private final Log logger = LogFactory.getLog(DBSearchService.class);

	@Autowired
	private Configuration config;
	@Autowired
	private SqlService sqlService;
	
	private ConcurrentHashMap <String,QueryBody> querys=new ConcurrentHashMap<String,QueryBody>();
	
	private Map<String,Map<String,QueryBody>> query_tmp=new HashMap<String,Map<String,QueryBody>>();
	
	private ObserverControll observerControll;
	
	private final static String formatListFlag="@list";
	
	private final static String formatMapFlag="@map";
	
	private Map<String,Formatter> formattersMap;
	
	@Override
	public void init() {
		initLoadAllQueryFile();
		initFormatters();
		sqlService.init();
		initObserverModule();
	}
	
	public void refreshTablesRelationFromDB(){
		sqlService.initTablesRelationFromDB();
	}

	private void initFormatters() {
		formattersMap=new HashMap<String,Formatter>();
		Formatter mapFormatter=new MapFormatter();
		formattersMap.put(mapFormatter.getType(),mapFormatter);
	}

	private void initObserverModule() {
		observerControll=new ObserverControll(config);
		observerControll.initObserverModule();
	}
	
	//对外提供的监控方法
	
	public void startMonitorModule(){
		observerControll.startMonitorModule();
	}
	
	public void stopMonitorModule(){
		observerControll.stopMonitorModule();
	}
	
	public void startMonitor(String monitorName) throws Exception{
		observerControll.startMonitor(monitorName);
	}
	
	public void stopMonitor(String monitorName) throws Exception{
		observerControll.stopMonitor(monitorName);
	}


	private void initLoadAllQueryFile(){
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:"+config.getQueryFileDirectory()+"/*.json");  
			if(resources!=null){
				for(Resource resource:resources){
					Map<String,QueryBody> fileQueryBody=FileToQueryBodyUtil.getQueryBodyFromFile(resource.getInputStream());
					query_tmp.put(resource.getFile().getName(),fileQueryBody);
					querys.putAll(fileQueryBody);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Map<String,Object>> select(QueryBody q){
		String sql=sqlService.getSql(q);
		if(StringUtils.hasLength(sql)){
			return formatData(formatValue(config.getJdbcTemplate().queryForList(sql),q),q);
		}else{
			return new ArrayList<Map<String,Object>>();
		}
	}
	
	private List<Map<String, Object>> formatValue(List<Map<String, Object>> data, QueryBody q) {
		List<Rule> rules=q.getFormat();
		if(rules.size()>0){
			Map<String,List<Map<String,Object>>> allRulesAndFormatters=new HashMap<String,List<Map<String,Object>>>(); 
			for(Rule rule:rules){
				Formatter formatter=formattersMap.get(rule.getRuleType());
				if(formatter!=null){
					String column=rule.getColumn();
					List<Map<String,Object>> formatters=allRulesAndFormatters.get(column);
					if(formatters==null){
						formatters=new ArrayList<Map<String,Object>>();
						allRulesAndFormatters.put(column,formatters);
					}
					formatters.add(MapValueUtil.getMap("rule",rule,"formatter",formatter));
				}
			}
			if(allRulesAndFormatters.size()>0){
				for(Map<String,Object> dataItem:data){
					for(String column:allRulesAndFormatters.keySet()){
						Object value=dataItem.get(column);
						List<Map<String,Object>> formatters=allRulesAndFormatters.get(column);
						for(Map<String,Object> formatterItem:formatters){
							Formatter formatter=(Formatter)formatterItem.get("formatter");
							Rule rule=(Rule)formatterItem.get("rule");
							dataItem.put(column,formatter.format(value,rule.getRuleBody()));
						}
					}
				}
			}
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String,Object>> formatData(List<Map<String,Object>> data,QueryBody q){
		if(data==null || q==null){
			return new ArrayList<Map<String,Object>>();
		}
		List<String> groupColumns=q.getGroupColumns();
		List<String> columns=q.getColumns();
		boolean haveSons=false;
		if(groupColumns.size()>0 && columns.containsAll(groupColumns)){
			haveSons=true;
		}
		List<String> fatherColumns=new ArrayList<String>();
		Set<String> sons=new HashSet<String>();
		Map<String,List<String>> sonsColumnsInfo=new HashMap<String,List<String>>();
		Set<String> oneSons=new HashSet<String>();
		Map<String,List<String>> oneSonsColumnsInfo=new HashMap<String,List<String>>();
		//格式化结构
		List<String> originColumns=q.getColumns();
		for(String column:originColumns){
			if(column.contains("as")){
				String tmp=column.substring(column.indexOf(" as ")+4).trim();
				//去掉as 别名时添加的 ``
				if(tmp.startsWith("`") && tmp.endsWith("`")){
					tmp=tmp.substring(1,tmp.length()-1);
				}
				if(tmp.contains(formatListFlag) && haveSons){
					String[] tableAndColumn=tmp.split(formatListFlag);
					if(tableAndColumn.length>1){
						sons.add(tableAndColumn[0]);
						List<String> sonColumns=sonsColumnsInfo.get(tableAndColumn[0]);
						if(sonColumns==null){
							sonColumns=new ArrayList<String>();
						}
						sonColumns.add(tableAndColumn[1]);
						sonsColumnsInfo.put(tableAndColumn[0],sonColumns);
					}else{
						fatherColumns.add(tmp);
					}
				}else if(tmp.contains(formatMapFlag)){
					String[] tableAndColumn=tmp.split(formatMapFlag);
					if(tableAndColumn.length>1){
						oneSons.add(tableAndColumn[0]);
						List<String> sonColumns=oneSonsColumnsInfo.get(tableAndColumn[0]);
						if(sonColumns==null){
							sonColumns=new ArrayList<String>();
						}
						sonColumns.add(tableAndColumn[1]);
						oneSonsColumnsInfo.put(tableAndColumn[0],sonColumns);
					}else{
						fatherColumns.add(tmp);
					}
				}else{
					fatherColumns.add(tmp);
				}
			}else{
				fatherColumns.add(column.substring(column.indexOf(".")+1));
			}
		}
		if(sons.size()>0 || oneSons.size()>0){
			List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
			//先处理父元素
			for(Map<String,Object> item:data){
				//先构造本条数据
				Map<String,Object> fatherTotal=new HashMap<String,Object>();
				for(String fatherColumn:fatherColumns){
					fatherTotal.put(fatherColumn,item.get(fatherColumn));
				}
				
				//然后构造一个对象的数据
				for(String oneSon:oneSons){
					Map<String,Object> oneSonMap=new HashMap<String,Object>();
					List<String> oneSonColumns=oneSonsColumnsInfo.get(oneSon);
					for(String oneSonColumn:oneSonColumns){
						oneSonMap.put(oneSonColumn,item.get(oneSon+formatMapFlag+oneSonColumn));
					}
					fatherTotal.put(oneSon,oneSonMap);
				}
				for(String son:sons){
					List<String> sonColumns=sonsColumnsInfo.get(son);
					Map<String,Object> sonData=new HashMap<String,Object>();
					for(String sonCoulumn:sonColumns){
						sonData.put(sonCoulumn,item.get(son+formatListFlag+sonCoulumn));
					}
					fatherTotal.put(son,sonData);
				}
				if(haveSons){
					//再进行判断和聚合
					boolean fatherExits=false;
					Map<String,Object> equalsFather=null;
					for(Map<String,Object> alreadyFather:ret){
						if(compareTwoMapEquals(fatherTotal,alreadyFather,groupColumns)){
							fatherExits=true;
							equalsFather=alreadyFather;
							break;
						}
					}
					if(fatherExits){
						for(String son:sons){
							List<Map<String,Object>> sonList=(List<Map<String,Object>>) equalsFather.get(son);
							if(sonList==null){
								sonList=new ArrayList<Map<String,Object>>();
								equalsFather.put(son, sonList);
							}
							Map<String,Object> fatherSon=(Map<String, Object>)fatherTotal.get(son);
							boolean exitsSonItem=false;
							for(Map<String,Object> sonItem:sonList){
								if(MapValueUtil.compareMapEquals(sonItem,fatherSon)){
									exitsSonItem=true;
									break;
								}
							}
							if(!exitsSonItem){
								if(!mapValueEmpty(fatherSon)){
									sonList.add(fatherSon);
								}
							}
						}
					}else{
						for(String son:sons){
							List<Object> list=new ArrayList<Object>();
							Map<String,Object> fatherSon=(Map<String, Object>)fatherTotal.get(son);
							if(!mapValueEmpty(fatherSon)){
								list.add(fatherSon);
							}
							fatherTotal.put(son,list);
						}
						ret.add(fatherTotal);
					}
				}else{
					ret.add(fatherTotal);
				}
			}
			return ret;
		}else{
			return data;
		}
	}
	
	private boolean mapValueEmpty(Map<String,Object> map){
		if(map==null){
			return true;
		}
		for(String key:map.keySet()){
			if(map.get(key)!=null){
				return false;
			}
		}
		return true;
	}
	
	private boolean compareTwoMapEquals(Map<String, Object> fatherTotal,Map<String, Object> alreadyFather, List<String> groupColumns) {
		for(String key:groupColumns){
			String tmp=key.substring(key.indexOf(".")+1);
			Object value1=fatherTotal.get(tmp);
			Object value2=alreadyFather.get(tmp);
			if(value1==null){
				if(value2!=null){
					return false;
				}
			}else{
				if(!value1.equals(value2)){
					return false; 
				}
			}
		}
		return true;
	}

	public Map<String,Object> selectOne(QueryBody q){
		return select(q).get(0);
	}
	
	public List<Map<String,Object>> select(String action,Map<String,Object> params){
		QueryBody q=querys.get(action);
		if(q==null){
			return new ArrayList<Map<String,Object>>();
		}
		QueryBody copy;
		try {
			copy = q.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException("查询参数构造失败");
		}
		if(params!=null){
			Map<String,Object> oldParams=copy.getParams();
			Map<String,Object> newParams=new HashMap<String,Object>();
			for(String key:oldParams.keySet()){
				newParams.put(key,oldParams.get(key));
			}
			newParams.putAll(params);
			copy.setParams(newParams);
		}
		return select(copy);
	}
	
	public Map<String,Object> selectOne(String action,Map<String,Object> params){
		return select(action, params).get(0);
	}

	public List<Map<String,Object>> select(String action){
		return select(action,null);
	}
	
	public Map<String,Object> selectOne(String action){
		return select(action).get(0);
	}
	
	@Override
	public void processChange(Map<String, QueryBody> change,String fileName) {
		logger.info("observ "+fileName+" changed");
		if(change!=null){
			query_tmp.put(fileName,change);
			querys.clear();
			for(String key:query_tmp.keySet()){
				querys.putAll(query_tmp.get(key));
			}
		}
	}
	
	
	public ConcurrentHashMap<String, QueryBody> getQuerys() {
		return querys;
	}

	public void setQuerys(ConcurrentHashMap<String, QueryBody> querys) {
		this.querys = querys;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

}
