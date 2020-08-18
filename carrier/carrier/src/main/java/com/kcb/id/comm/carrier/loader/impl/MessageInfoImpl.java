package com.kcb.id.comm.carrier.loader.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;

@Component
public class MessageInfoImpl implements Serializable, Cloneable, MessageInfo {

	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(MessageInfoImpl.class);

	Message requestMessage;
	Message responseMessage;
	Map<String,Message> exceptionMessageMap = new HashMap<>();

	String name;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}


	public Message getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(Message requestMessage) {
		this.requestMessage = requestMessage;
	}

	public Message getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(Message responseMessage) {
		this.responseMessage = responseMessage;
	}

	public Map<String, Message> getExceptionMessageMap() {
		return exceptionMessageMap;
	}

	public void setExceptionMessageMap(Map<String, Message> exceptionMessageMap) {
		this.exceptionMessageMap = exceptionMessageMap;
	}

	public MessageInfo newInstance() {
		MessageInfo msg = new MessageInfoImpl();
		msg.setRequestMessage(this.getRequestMessage().newInstance());		
		msg.setResponseMessage(this.getResponseMessage().newInstance());
		this.getExceptionMessageMap().forEach((k,v)->{
			msg.getExceptionMessageMap().put(k,v.newInstance());
		});
		msg.setName(this.getName());
		return msg;
	}


	
}
