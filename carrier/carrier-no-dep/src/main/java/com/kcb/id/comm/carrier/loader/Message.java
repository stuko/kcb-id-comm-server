package com.kcb.id.comm.carrier.loader;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.impl.Field;

public interface Message  extends SelfChecker,MessageFrame {
	
	String getMessageName();
	void setMessageName(String messageName);
	
	String getForwardMessageName();
	void setForwardMessageName(String forwardMessageName);
	
	String getForwardIp();
	void setForwardIp(String ip);
	
	String getForwardPort();
	void setForwardPort(String port);

	String getForwardTimeOut();
	void setForwardTimeOut(String timeOut);
	
	String getKindCode();
	void setKindCode(String kindCode);
	
	String getMessageCode();
	void setMessageCode(String messageCode);
	
	String getBodyMessageCodeValue();
	
	String getPreBean();
	void setPreBean(String beanName);
	
	String getPostBean();
	void setPostBean(String beanName);
	
	Message newInstance();
	
	void addBuffer(String data);

	String getHeader(int idx);
	
	String getBody(int idx);

	String getTail(int idx);

	String getHeader(String name);
	
	String getBody(String name);

	String getTail(String name);

	byte[] toByte() throws Exception;
	
	byte[] toByte(Message msg) throws Exception;
	
	public int getLength() throws Exception;

	Map<String, Object> toHashMap();

	Map<String, Object> getBodyMap();

	Map<String, Object> getTailMap();

	String getTailValue(String name);
	void setTailValue(String name, String value);

	String getBodyValue(String name);
	void setBodyValue(String name, String value);
	
	Map<String,Object> bindValue(Map<String,Object> bodyMap);
	MessageInfo clone();
	
	Field decodeAndEncode(Field f) throws Exception;
	
	int getLength(Field field, Message message) throws Exception;
	
	Map<String, Object> getHeaderMap();
	String getHeaderValue(String name);
	void setHeaderValue(String name, String value);
	
}
