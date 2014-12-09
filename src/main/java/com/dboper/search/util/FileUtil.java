package com.dboper.search.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
