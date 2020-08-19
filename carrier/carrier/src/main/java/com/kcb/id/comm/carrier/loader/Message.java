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

	String getBody(int idx, int row);

	String getTail(int idx);

	String getHeader(String name);

	String getBody(String name, int row);

	String getTail(String name);

	String getRepeat();

	void setRepeat(String repeat);

	String getRepeatVariable();

	void setRepeatVariable(String repeatVariable);

	String getHandlerName();

	void setHandlerName(String handlerName);

	String getBusinessClass();

	void setBusinessClass(String businessClass);

	String getMessageKey();

	void setMessageKey(String messageKey);

	String getPath();

	void setPath(String path);

	String toRaw() throws Exception;

	String header2Raw();

	String body2Raw();

	String tail2Raw();

	ByteBuf toByteBuf();

	Map<String, Object> toHashMap();

	Object getBodyMapOfCount(int repeatCount);

	Map<String, Object> getBodyMap();

	Map[] getBodyMap(int repeatCount);

	Map<String, Object> getTailMap();

	Map<String, Object> getHeaderMap();

	StringBuilder getMessageBuffer();

	void setMessageBuffer(StringBuilder messageBuffer);

	String getHeaderValue(String name);

	void setHeaderValue(String name, String value);

	String getTailValue(String name);

	void setTailValue(String name, String value);

	String getBodyValue(String name, int index);

	void setBodyValue(String name, String value, int index);
	
	void setBodyValue(Map<String,Object> bodyMap);

	MessageInfo clone();

	Field encodeOrDecode(Field f) throws Exception;

	Field encodeOrDecode(Field f, int idx) throws Exception;

	String getDestinationIp();

	void setDestinationIp(String destinationIp);

	String getDestinationPort();

	void setDestinationPort(String destinationPort);
	
	int getLength(Field field, MessageInfo messageInfo) throws Exception;
}
