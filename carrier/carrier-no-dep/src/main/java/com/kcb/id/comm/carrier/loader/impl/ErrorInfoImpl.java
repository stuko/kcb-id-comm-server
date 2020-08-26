package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import com.kcb.id.comm.carrier.loader.ErrorInfo;

public class ErrorInfoImpl implements ErrorInfo{
	Map<String,Field[]> headerErrorMap = new HashMap<>();
	Map<String,Field[]> messageErrorMap = new HashMap<>();
	public Map<String, Field[]> getHeaderErrorMap() {
		return headerErrorMap;
	}
	public void setHeaderErrorMap(Map<String, Field[]> headerErrorMap) {
		this.headerErrorMap = headerErrorMap;
	}
	public Map<String, Field[]> getMessageErrorMap() {
		return messageErrorMap;
	}
	public void setMessageErrorMap(Map<String, Field[]> messageErrorMap) {
		this.messageErrorMap = messageErrorMap;
	}
	@Override
	public ErrorInfo newInstance() {
		ErrorInfo errorInfo = new ErrorInfoImpl();
		for(String k : headerErrorMap.keySet()) {
			errorInfo.getHeaderErrorMap().put(k,new Field[headerErrorMap.get(k).length]);
			for(int i = 0; i < headerErrorMap.get(k).length; i++) {
				errorInfo.getHeaderErrorMap().get(k)[i] = headerErrorMap.get(k)[i].newInstance();
			}
		}
		for(String k : messageErrorMap.keySet()) {
			errorInfo.getMessageErrorMap().put(k,new Field[messageErrorMap.get(k).length]);
			for(int i = 0; i < messageErrorMap.get(k).length; i++) {
				errorInfo.getMessageErrorMap().get(k)[i] = messageErrorMap.get(k)[i].newInstance();
			}
		}
		return errorInfo;
	}
}
