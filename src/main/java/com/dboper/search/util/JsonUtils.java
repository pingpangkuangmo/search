package com.dboper.search.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtils {
    public static String fastJsonDate(Object data){
    	return JSON.toJSONStringWithDateFormat(data,"yyyy-MM-dd HH:mm:ss",
    			SerializerFeature.WriteDateUseDateFormat,
    			SerializerFeature.DisableCircularReferenceDetect,
    			SerializerFeature.WriteMapNullValue);
    }
}
