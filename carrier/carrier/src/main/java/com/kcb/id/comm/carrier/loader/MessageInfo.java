package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface MessageInfo{

	String getName();
	void setName(String name);
	
	Message getRequestMessage();
	void setRequestMessage(Message requestMessage);
	
	Message getResponseMessage();
	void setResponseMessage(Message responseMessage);
	
	Map<String,Message> getExceptionMessageMap();
	void setExceptionMessageMap(Map<String,Message> exceptionMessage);
	
	void setForward(String msgName);
	String getForward();

	MessageInfo newInstance();
	
	void setForwardIp(String ip);
	String getForwardIp();
	
	void setForwardPort(int port);
	int getForwardPort();
}