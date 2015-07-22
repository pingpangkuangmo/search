package com.dboper.search.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dboper.search.domain.QueryBody;

public class FileUtil {
	
	public  static <T> T getClassFromFile(File file,Class<T> clazz) throws FileNotFoundException, IOException{
		return JSON.parseObject(getFileContent(file),clazz);
	}
	
	public static <T> T getClassFromInputStream(InputStream in,Class<T> clazz) throws IOException{
		return JSON.parseObject(getInputStreamContent(in),clazz);
	}
	
	public static String getFileContent(File file) throws FileNotFoundException, IOException{
		return getInputStreamContent(new FileInputStream(file));
	}
	
	public static String getInputStreamContent(InputStream in) throws IOException{
		return IOUtils.toString(in);
	}

	public static Map<String,QueryBody> getQueryBodyFromFile(File file){
		return getQueryBodyFromFile(getInputStream(file));
	}
	
	public static Map<String,QueryBody> getQueryBodyFromFile(InputStream in){
		return getTFromInputStream(in,new TypeReference<Map<String,QueryBody>>(){});
	}
	
	public static <T> Map<String,T> getTFromInputStream(InputStream in,TypeReference<Map<String,T>> typeReference){
		Map<String,T> ret=new HashMap<String,T>();
		if(in==null){
			return ret;
		}
		try {
			String fileContent=getInputStreamContent(in);
			Map<String,T> actionQueryBody=JSON.parseObject(fileContent,typeReference);
			for(String key:actionQueryBody.keySet()){
				ret.put(key,actionQueryBody.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Map<String,String> getTablesRelation(File file){
		Map<String, String> ret=new HashMap<String,String>();
		try {
			BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
			String lineStr=bufferedReader.readLine();
			while(StringUtils.hasLength(lineStr)){
				parseLine(lineStr,ret);
				lineStr=bufferedReader.readLine();
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private static void parseLine(String lineStr,Map<String,String> ret) {
		String[] parts=lineStr.split(",");
		if(parts.length==3){
			List<String> tables=new ArrayList<String>();
			tables.add(parts[0]);
			tables.add(parts[1]);
			Collections.sort(tables);
			ret.put(tables.get(0)+"__"+tables.get(1),parts[2]);
		}
	}
	
	private static InputStream getInputStream(File file){
		InputStream in=null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
	}
}
