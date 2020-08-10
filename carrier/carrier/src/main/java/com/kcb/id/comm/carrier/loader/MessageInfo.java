package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface MessageInfo{

	String getName();
	void setName(String name);
	
	void setForwardServer(String serverIp);
	String getForwardServer();
	
	void setForwardPort(int port);
	int getForwardPort();

	Message getRequestMessage();
	void setRequestMessage(Message requestMessage);
	
	Message getResponseMessage();
	void setResponseMessage(Message responseMessage);
	
	Map<String,Message> getExceptionMessageMap();
	void setExceptionMessageMap(Map<String,Message> exceptionMessage);
	
	void setForward(String msgName);
	String getForward();

	MessageInfo newInstance();
}