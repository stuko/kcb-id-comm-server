package com.kcb.id.comm.carrier.loader;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.impl.Field;

public interface ErrorInfo {
	Map<String,Field[]> getHeaderErrorMap();
	void setHeaderErrorMap(Map<String,Field[]> errorMap);
	Map<String,Field[]> getMessageErrorMap();
	void setMessageErrorMap(Map<String,Field[]> errorMap);
	ErrorInfo newInstance();
}
