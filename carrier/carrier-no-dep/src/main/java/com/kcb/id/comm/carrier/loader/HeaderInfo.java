package com.kcb.id.comm.carrier.loader;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.impl.Field;

public interface HeaderInfo {
	String getRequestValue(String name);
	String getRequestKindCodeValue();
	String getResponseValue(String name);
	Field[] getRequestHeader();
	void setRequestHeader(Field[] header);
	Field[] getResponseHeader();
	void setResponseHeader(Field[] header);
	Map<String,Object> toHashMap();
	HeaderInfo newInstance();
}
