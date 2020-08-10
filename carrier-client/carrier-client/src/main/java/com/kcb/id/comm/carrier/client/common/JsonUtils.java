package com.kcb.id.comm.carrier.client.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class JsonUtils {
	
	static Logger logger = LoggerFactory.getLogger(JsonUtils.class);
	
	public static Map<String,Object> getPayLoad(Map<String,Object> payload, Map<String, Object> param, Map<String, Object> requestMap){
		requestMap.forEach((k,v)->{
			if(v instanceof Map) {
				payload.put(k,new HashMap<String,Object>());
				getPayLoad((Map<String,Object>)payload.get(k),param,(Map<String,Object>) v);
			}else {
				String value = (String)v;
				String[] values = value.split(".");
				Object finalValue = get(param,0,values);
				payload.put(k,finalValue);
			}
		});
		return payload;
	}
	
	public static Object get(Map<String,Object> map, int pos , String[] nest) {
		if(nest == null || nest.length == 0) {
			logger.error("Oops!!!, your json tree name array is null ");
			return null;
		}
		if(nest[pos] == null) {
			logger.error("Oops!!!, your json tree value is null ");
			return null;
		}
		if(!map.containsKey(nest[pos])) {
			logger.debug("Oops!!!, does not contains key {} " , nest[pos]);
			return null;
		}
		if(map.get(nest[pos]) == null) {
			logger.debug("Oops!!!,  {}'s value is null " , nest[pos]);
			return null;
		}
		if(pos == nest.length-1) return map.get(nest[pos]); 
		else {
			map = (Map<String,Object>)map.get(nest[pos]);
			return get(map,pos++,nest);
		}
	}
}
