package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.Message;

public class HandlerInfoImpl extends ForwardImpl implements HandlerInfo {
	
	String handlerName;
	String messageName;
	String handlerClass;
	String businessClass;
	String timeOut;
	Map<String,Message> exceptionMessageMap = new HashMap<>();
	
	public String getName() {
		return handlerName;
	}
	public void setName(String handlerName) {
		this.handlerName = handlerName;
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
	public String getTimeOut() {
		return timeOut;
	}
	public void setTimeOut(String timeOut) {
		this.timeOut = timeOut;
	}
	
}
