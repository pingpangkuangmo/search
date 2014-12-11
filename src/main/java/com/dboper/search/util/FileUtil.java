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

import org.springframework.util.StringUtils;

import com.dboper.search.domain.ActionQueryBody;
import com.dboper.search.domain.QueryBody;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtil {

	public static Map<String,QueryBody> getQueryBodyFromFile(File file){
		return getQueryBodyFromFile(getInputStream(file));
	}
	
	public static Map<String,QueryBody> getQueryBodyFromFile(InputStream in){
		Map<String,QueryBody> ret=new HashMap<String,QueryBody>();
		if(in==null){
			return ret;
		}
		try {
			ObjectMapper mapper=new ObjectMapper();
			ActionQueryBody actionQueryBody=mapper.readValue(in,ActionQueryBody.class);
			for(String key:actionQueryBody.keySet()){
				ret.put(key,actionQueryBody.get(key));
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
