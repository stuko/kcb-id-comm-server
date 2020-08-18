package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface HandlerInfo extends Forward {
	void setName(String handlerName);
	String getName();
	void setEnable(boolean enable);
	boolean isEnable();
	void setMessageName(String messageName);
	String getMessageName();
	void setHandlerClass(String handlerClass);
	String getHandlerClass();
	void setBusinessClass(String businessClass);
	String getBusinessClass();
	public Map<String, Message> getExceptionMessageMap();
	public void setExceptionMessageMap(Map<String, Message> exceptionMessageMap);
}
