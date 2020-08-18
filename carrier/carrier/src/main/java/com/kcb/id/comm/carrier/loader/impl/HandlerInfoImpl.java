package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.Message;

public class HandlerInfoImpl extends ForwardImpl implements HandlerInfo {
	
	String handlerName;
	String messageName;
	boolean enable;
	String handlerClass;
	String businessClass;
	Map<String,Message> exceptionMessageMap = new HashMap<>();
	
	public String getName() {
		return handlerName;
	}
	public void setName(String handlerName) {
		this.handlerName = handlerName;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getMessageName() {
		return messageName;
	}
	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}
	public String getHandlerClass() {
		return handlerClass;
	}
	public void setHandlerClass(String handlerClass) {
		this.handlerClass = handlerClass;
	}
	public String getBusinessClass() {
		return businessClass;
	}
	public void setBusinessClass(String businessClass) {
		this.businessClass = businessClass;
	}
	public Map<String, Message> getExceptionMessageMap() {
		return exceptionMessageMap;
	}
	public void setExceptionMessageMap(Map<String, Message> exceptionMessageMap) {
		this.exceptionMessageMap = exceptionMessageMap;
	}
	
}
