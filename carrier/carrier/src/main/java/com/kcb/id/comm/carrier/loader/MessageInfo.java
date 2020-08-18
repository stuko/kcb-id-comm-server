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
	

	MessageInfo newInstance();
}