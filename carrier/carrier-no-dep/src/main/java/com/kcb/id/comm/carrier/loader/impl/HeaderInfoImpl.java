package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import com.kcb.id.comm.carrier.loader.HeaderInfo;

public class HeaderInfoImpl extends DeEncoderImpl implements HeaderInfo {
	Field[] requestHeader;
	Field[] responseHeader;
	
	public String getRequestValue(String name) {
		for(int i = 0; i < requestHeader.length; i++) {
			if(requestHeader[i].getName().equals(name))return requestHeader[i].getValue();
		}
		return null;
	}
	public String getResponseValue(String name) {
		for(int i = 0; i < responseHeader.length; i++) {
			if(responseHeader[i].getName().equals(name))return responseHeader[i].getValue();
		}
		return null;
	}
	
	public Field[] getRequestHeader() {
		return requestHeader;
	}
	public void setRequestHeader(Field[] requestHeader) {
		this.requestHeader = requestHeader;
	}
	public Field[] getResponseHeader() {
		return responseHeader;
	}
	public void setResponseHeader(Field[] responseHeader) {
		this.responseHeader = responseHeader;
	}
	@Override
	public HeaderInfo newInstance() {
		HeaderInfo headerInfo = new HeaderInfoImpl();
		headerInfo.setRequestHeader(new Field[requestHeader.length]);
		for(int i = 0; i < requestHeader.length; i++){
			headerInfo.getRequestHeader()[i] = requestHeader[i].newInstance();
		}
		headerInfo.setResponseHeader(new Field[responseHeader.length]);
		for(int i = 0; i < responseHeader.length; i++){
			headerInfo.getResponseHeader()[i] = responseHeader[i].newInstance();
		}
		return headerInfo;
	}
	@Override
	public Map<String, Object> toHashMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("request",this.getRequestHeaderMap());
		map.put("response",this.getResponseHeaderMap());
		return map;
	}	
	
	public Map<String, Object> getRequestHeaderMap() {
		Map<String, Object> map = new HashMap<>();
		for(int i = 0; i < requestHeader.length; i++){
			map.put(requestHeader[i].getName(),requestHeader[i].getValue()) ;
		}
		return map;
	}
	public Map<String, Object> getResponseHeaderMap() {
		Map<String, Object> map = new HashMap<>();
		for(int i = 0; i < responseHeader.length; i++){
			map.put(responseHeader[i].getName(),responseHeader[i].getValue()) ;
		}
		return map;
	}
	@Override
	public String getRequestKindCodeValue() {
		for(int i = 0; i < requestHeader.length; i++) {
			if(requestHeader[i].isKindCode())return requestHeader[i].getValue();
		}
		return null;
	}
	
}
