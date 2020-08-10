package com.kcb.id.comm.carrier.loader;

public interface HandlerInfo {
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
}
