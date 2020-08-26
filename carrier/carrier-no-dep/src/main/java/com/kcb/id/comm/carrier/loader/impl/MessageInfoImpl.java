package com.kcb.id.comm.carrier.loader.impl;

import java.io.Serializable;

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
	String messageName;
	
	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
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

	public MessageInfo newInstance() {
		MessageInfo msg = new MessageInfoImpl();
		msg.setRequestMessage(this.getRequestMessage().newInstance());		
		msg.setResponseMessage(this.getResponseMessage().newInstance());
		msg.setMessageName(this.getMessageName());
		return msg;
	}
	
}
