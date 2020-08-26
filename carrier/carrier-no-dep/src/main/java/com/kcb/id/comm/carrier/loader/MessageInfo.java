package com.kcb.id.comm.carrier.loader;

public interface MessageInfo{

	String getMessageName();
	void setMessageName(String messageName);
	
	Message getRequestMessage();
	void setRequestMessage(Message requestMessage);
	
	Message getResponseMessage();
	void setResponseMessage(Message responseMessage);
	
	MessageInfo newInstance();
}