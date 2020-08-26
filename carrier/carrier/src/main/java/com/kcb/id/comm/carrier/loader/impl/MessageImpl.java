package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.kcb.id.comm.carrier.common.StringUtils;
import com.kcb.id.comm.carrier.loader.Cypher;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageImpl implements Message , MessageFrame {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(MessageImpl.class);

	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
	
	String encoder;
	String decoder;

	Field[] header;
	Field[] body;
	Field[] tail;

	String repeat;
	String repeatVariable;

	StringBuilder messageBuffer;

	String handlerName;
	String businessClass;

	String messageKey;

	String path;

	String destinationIp;
	String destinationPort;

	
	public MessageImpl() {
		messageBuffer = new StringBuilder();
	}
	
	public String getEncoder() {
		return encoder;
	}

	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}

	public String getDecoder() {
		return decoder;
	}

	public void setDecoder(String decoder) {
		this.decoder = decoder;
	}

	@Override
	public void addBuffer(String data) {
		messageBuffer.append(data);
	}

	@Override
	public Field[] getHeader() {
		return header;
	}

	@Override
	public void setHeader(Field[] header) {
		this.header = header;
	}

	@Override
	public Field[] getBody() {
		return body;
	}

	@Override
	public void setBody(Field[] body) {
		this.body = body;
	}

	@Override
	public Field[] getTail() {
		return tail;
	}

	@Override
	public void setTail(Field[] tail) {
		this.tail = tail;
	}

	@Override
	public String getHeader(int idx) {
		return this.header[idx].value;
	}

	@Override
	public String getBody(int idx) {
		return this.body[idx].value;
	}

	@Override
	public String getTail(int idx) {
		return this.tail[idx].value;
	}

	@Override
	public String getHeader(String name) {
		String value = null;
		for (int i = 0; i < this.header.length; i++) {
			if (name.equals(this.header[i].name))
				value = this.header[i].value;
		}
		return value;
	}

	@Override
	public String getBody(String name) {
		String value = null;
		for (int i = 0; i < this.body.length; i++) {
			if (name.equals(this.body[i].name))
				value = this.body[i].value;
		}
		return value;
	}

	@Override
	public String getTail(String name) {
		String value = null;
		for (int i = 0; i < this.tail.length; i++) {
			if (name.equals(this.tail[i].name))
				value = this.tail[i].value;
		}
		return value;
	}

	@Override
	public String getHandlerName() {
		return handlerName;
	}

	@Override
	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	@Override
	public String getBusinessClass() {
		return businessClass;
	}

	@Override
	public void setBusinessClass(String businessClass) {
		this.businessClass = businessClass;
	}

	@Override
	public String getMessageKey() {
		return messageKey;
	}

	@Override
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public ByteBuf toByteBuf() {
		return this.toByteBuf(this);
	}
	
	@Override
	public ByteBuf toByteBuf(Message msg) {
		ByteBuf buf = Unpooled.buffer(256);

		if(header != null) {
			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				try {
					f.toPadding();
					encodeOrDecode(f,msg);
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				logger.debug("Header's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes()));
				buf.writeBytes(f.getValueBytes());
			}
		}
		if(body != null) {
			for (int i = 0; i < body.length; i++) {
				Field f = body[i];
				try {
					f.toPadding();
					encodeOrDecode(f,msg);
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				logger.debug("Body's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes()));
				buf.writeBytes(f.getValueBytes());
			}
		}
		if(tail != null) {
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				try {
					f.toPadding();
					encodeOrDecode(f,msg);
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				logger.debug("Tail's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes()));
				buf.writeBytes(f.getValueBytes());
			}
		}
		return buf;
	}

	@Override
	public Map<String, Object> toHashMap() {
		Map<String, Object> rootMap = new HashMap<>();
		rootMap.put("header", getHeaderMap());
		rootMap.put("body", getBodyMap());
		rootMap.put("tail", getTailMap());
		return rootMap;
	}

	@Override
	public Map<String, Object> getBodyMap() {
		Map<String, Object> bodyMap = new HashMap<>();
		for (int j = 0; j < body.length; j++) {
			Field f = body[j];
			bodyMap.put(f.getName(), f.getValue());
		}
		return bodyMap;
	}

	@Override
	public Map<String, Object> getTailMap() {
		Map<String, Object> tailMap = new HashMap<>();
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			tailMap.put(f.getName(), f.getValue());
		}
		return tailMap;
	}

	@Override
	public Map<String, Object> getHeaderMap() {
		Map<String, Object> headerMap = new HashMap<>();
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			headerMap.put(f.getName(), f.getValue());
		}
		return headerMap;
	}

	@Override
	public String getHeaderValue(String name) {
		for (int i = 0; i < this.header.length; i++) {
			if (header[i].getName().equals(name))
				return (String) header[i].getValue();
		}
		return null;
	}

	@Override
	public void setHeaderValue(String name, String value) {
		for (int i = 0; i < this.header.length; i++) {
			if (header[i].getName().equals(name)) {
				header[i].setValue(value);
			}
		}
	}

	@Override
	public String getTailValue(String name) {
		for (int i = 0; i < this.tail.length; i++) {
			if (tail[i].getName().equals(name))
				return (String) tail[i].getValue();
		}
		return null;
	}

	@Override
	public void setTailValue(String name, String value) {
		for (int i = 0; i < this.tail.length; i++) {
			if (tail[i].getName().equals(name))
				tail[i].setValue(value);
		}
	}

	@Override
	public String getBodyValue(String name) {
		for (int i = 0; i < this.body.length; i++) {
			if (body[i].getName().equals(name))
				return (String) body[i].getValue();
		}
		return null;
	}

	@Override
	public void setBodyValue(String name, String value) {
		for (int i = 0; i < this.body.length; i++) {
			if (body[i].getName().equals(name))
				body[i].setValue(value);
		}
	}

	@Override
	public MessageInfo clone() {
		try {
			return (MessageInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error(e.toString(),e);
			return null;
		}
	}

	@Override
	public Field encodeOrDecode(Field f) throws Exception {
		return this.encodeOrDecode(f, this);
	}
		
	
	@Override
	public Field encodeOrDecode(Field f, Message msg) throws Exception {
		try {
			if("true".equals(this.getEncoder())) {
				Object object = context.getBean(this.getEncoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					if ("true".equals(f.getEncode())) {
						f.setValue(cypher.encode((String) f.getValue(),msg));
					}
				}
			}
			if("true".equals(this.getDecoder())) {
				Object object = context.getBean(this.getDecoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					if ("true".equals(f.getDecode())) {
						f.setValue(cypher.decode((String) f.getValue(), msg));
					}
				}
			}
		}catch(Exception e) {
			throw new Exception("NoEncoderOrDecoderException",e);
		}
		return f;
	}

	@Override
	public String getDestinationIp() {
		return destinationIp;
	}

	@Override
	public void setDestinationIp(String destinationIp) {
		this.destinationIp = destinationIp;
	}

	@Override
	public String getDestinationPort() {
		return destinationPort;
	}

	@Override
	public void setDestinationPort(String destinationPort) {
		this.destinationPort = destinationPort;
	}

	@Override
	public boolean checkMe() {
		if(this.getBody() == null) return false;
		if(this.getHeader() == null) return false;
		return true;
	}
	
	@Override
	public Message newInstance() {
		Message msg = new MessageImpl();
		if(header != null) {
			msg.setHeader(new Field[header.length]);
			for(int i = 0; i < header.length; i++) {
				logger.debug("header : {}", header[i].toRaw());
				msg.getHeader()[i] = new Field();
				msg.getHeader()[i].setName(header[i].getName());
				msg.getHeader()[i].setLength(header[i].getLength());
				msg.getHeader()[i].setPadType(header[i].getPadType());
				msg.getHeader()[i].setPadChar(header[i].getPadChar());
				msg.getHeader()[i].setEncode(header[i].getEncode());
				msg.getHeader()[i].setDecode(header[i].getDecode());
				msg.getHeader()[i].setResCode(header[i].isResCode());
				msg.getHeader()[i].setRefLength(header[i].getRefLength());
				msg.getHeader()[i].setRef(header[i].getRef());
				msg.getHeader()[i].setValue((String)header[i].getValue());
			}
		}
		if(body != null) {
			msg.setBody(new Field[body.length]);
			for(int i = 0; i < body.length; i++) {
				msg.getBody()[i] = new Field();
				logger.debug("body : {}", body[i].toRaw());
				msg.getBody()[i].setName(body[i].getName());
				msg.getBody()[i].setLength(body[i].getLength());
				msg.getBody()[i].setPadType(body[i].getPadType());
				msg.getBody()[i].setPadChar(body[i].getPadChar());
				msg.getBody()[i].setEncode(body[i].getEncode());
				msg.getBody()[i].setDecode(body[i].getDecode());
				msg.getBody()[i].setResCode(body[i].isResCode());
				msg.getBody()[i].setRefLength(body[i].getRefLength());
				msg.getBody()[i].setRef(body[i].getRef());
				msg.getBody()[i].setValue((String)body[i].getValue());
			}
		}
		if(tail != null) {
			msg.setTail(new Field[tail.length]);
			for(int i = 0; i < tail.length; i++) {
				msg.getTail()[i] = new Field();
				logger.debug("tail : {}", tail[i].toRaw());
				msg.getTail()[i].setName(tail[i].getName());
				msg.getTail()[i].setLength(tail[i].getLength());
				msg.getTail()[i].setPadType(tail[i].getPadType());
				msg.getTail()[i].setPadChar(tail[i].getPadChar());
				msg.getTail()[i].setEncode(tail[i].getEncode());
				msg.getTail()[i].setDecode(tail[i].getDecode());
				msg.getTail()[i].setResCode(tail[i].isResCode());
				msg.getTail()[i].setRefLength(tail[i].getRefLength());
				msg.getTail()[i].setRef(tail[i].getRef());
				msg.getTail()[i].setValue((String)tail[i].getValue());
			}
		}
		return msg;
	}

	@Override
	public Map<String,Object> bindValue(Map<String, Object> map) {
		for(Field header : this.header) {
			bindMap(map,header);
		}
		for(Field body: this.body) {
			bindMap(map, body);
		}
		for(Field tail: this.tail) {
			bindMap(map, tail);
		}
		return map;
	}

	private void bindMap(Map<String, Object> map, Field field) {
		if(map.get(field.getName()) != null) {
			if(StringUtils.chkNull(field.getRef())) {
				field.setValue((String)map.get(field.getRef()));
			}else {
				if(map.get(field.getName()) instanceof String) {
					field.setValue((String)map.get(field.getName()));
				}else {
					field.setValue(map.get(field.getName()).toString());
				}
			}
		}
	}

	@Override
	public int getLength(Field field, Message message) throws Exception{
		if(StringUtils.chkNull(field.getLength())) {
			try {
				int len = Integer.parseInt(field.getLength());
				return len;
			}catch(Exception e) {
				throw new Exception("MessageFieldLengthException");
			}
		}else {
			if(StringUtils.chkNull(field.getRefLength())) {
				String[] names = field.getRefLength().split("\\.");
				if(names.length != 3) throw new Exception("MessageFieldLengthException ["+field.getRefLength()+"]["+names.length+"]");
				else {
					if("request".equals(names[0])) {
						int len = getRefLength(names,message);
						field.setLength(len+"");
						return len;
					}else if("response".equals(names[0])) {
						int len = getRefLength(names,message);
						field.setLength(len+"");
						return len;
					}else {
						throw new Exception("MessageFieldLengthException");
					}
				}
			}else {
				throw new Exception("MessageFieldLengthException");
			}
		}
		
	}

	private int getRefLength(String[] names , Message message) throws Exception {
		try {
			int len = Integer.parseInt(this.getRefValue(names, message).trim());
			return len;
		}catch(Exception e) {
			throw new Exception("MessageFieldLengthException");
		}
	}

	private String getRefValue(String[] names , Message message) throws Exception {
		if("header".equals(names[1])) {
			if(StringUtils.chkNull(message.getHeader(names[2]))) {
				try {
					return message.getHeader(names[2]);
				}catch(Exception e) {
					throw new Exception("MessageFieldException");
				}
			}else {
				throw new Exception("MessageFieldException");
			}
		}else if("body".equals(names[1])) {
			if(StringUtils.chkNull(message.getBody(names[2]))) {
				try {
					return message.getBody(names[2]);
				}catch(Exception e) {
					throw new Exception("MessageFieldException");
				}
			}else {
				throw new Exception("MessageFieldException");
			}
		}else if("tail".equals(names[1])) {
			if(StringUtils.chkNull(message.getTail(names[2]))) {
				try {
					return message.getTail(names[2]);
				}catch(Exception e) {
					throw new Exception("MessageFieldException");
				}
			}else {
				throw new Exception("MessageFieldException");
			}							
		}else {
			throw new Exception("MessageFieldException");
		}
	}

}
