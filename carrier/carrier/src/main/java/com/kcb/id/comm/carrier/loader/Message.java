package com.kcb.id.comm.carrier.loader;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.impl.Field;

import io.netty.buffer.ByteBuf;

public interface Message  extends SelfChecker{
	
	Message newInstance();
	
	String getEncoder();

	void setEncoder(String encoder);
	
	String getDecoder();

	void setDecoder(String decoder);

	void addBuffer(String data);

	Field[] getHeader();

	void setHeader(Field[] header);

	Field[] getBody();

	void setBody(Field[] body);

	Field[] getTail();

	void setTail(Field[] tail);

	String getHeader(int idx);

	String getBody(int idx);

	String getTail(int idx);

	String getHeader(String name);

	String getBody(String name);

	String getTail(String name);

	String getHandlerName();

	void setHandlerName(String handlerName);

	String getBusinessClass();

	void setBusinessClass(String businessClass);

	String getMessageKey();

	void setMessageKey(String messageKey);

	ByteBuf toByteBuf();
	
	ByteBuf toByteBuf(Message msg);

	Map<String, Object> toHashMap();

	Map<String, Object> getBodyMap();

	Map<String, Object> getTailMap();

	Map<String, Object> getHeaderMap();

	String getHeaderValue(String name);
	void setHeaderValue(String name, String value);
	String getTailValue(String name);
	void setTailValue(String name, String value);
	String getBodyValue(String name);
	void setBodyValue(String name, String value);
	
	Map<String,Object> bindValue(Map<String,Object> bodyMap);
	MessageInfo clone();
	Field encodeOrDecode(Field f) throws Exception;
	Field encodeOrDecode(Field f, Message msg) throws Exception;
	String getDestinationIp();
	void setDestinationIp(String destinationIp);
	String getDestinationPort();
	void setDestinationPort(String destinationPort);
	int getLength(Field field, Message message) throws Exception;
}
