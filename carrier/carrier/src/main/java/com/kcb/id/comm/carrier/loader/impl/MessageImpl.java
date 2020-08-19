package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

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
		return (String) this.header[idx].value.get(0);
	}

	@Override
	public String getBody(int idx, int row) {
		return (String) this.body[idx].value.get(row);
	}

	@Override
	public String getTail(int idx) {
		return (String) this.tail[idx].value.get(0);
	}

	@Override
	public String getHeader(String name) {
		String value = null;
		for (int i = 0; i < this.header.length; i++) {
			if (name.equals(this.header[i].name))
				value = (String) this.header[i].value.get(0);
		}
		return value;
	}

	@Override
	public String getBody(String name, int row) {
		String value = null;
		for (int i = 0; i < this.body.length; i++) {
			if (name.equals(this.body[i].name))
				value = (String) this.body[i].value.get(row);
		}
		return value;
	}

	@Override
	public String getTail(String name) {
		String value = null;
		for (int i = 0; i < this.tail.length; i++) {
			if (name.equals(this.tail[i].name))
				value = (String) this.tail[i].value.get(0);
		}
		return value;
	}

	@Override
	public String getRepeat() {
		return repeat;
	}

	@Override
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	@Override
	public String getRepeatVariable() {
		return repeatVariable;
	}

	@Override
	public void setRepeatVariable(String repeatVariable) {
		this.repeatVariable = repeatVariable;
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
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toRaw() throws Exception {
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			// f.toPadding();
			messageBuffer.append(f.toPadding((String) this.encodeOrDecode(f).getValue(0)));
		}
		int repeatCount = 1;
		if ("true".equals(repeat)) {
			repeatCount = Integer.parseInt(repeatVariable);
		}
		for (int i = 0; i < repeatCount; i++) {
			for (int j = 0; j < body.length; j++) {
				Field f = body[j];
				// f.toPadding(i);
				messageBuffer.append(f.toPadding((String) this.encodeOrDecode(f, i).getValue(i)));
			}
		}
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			// f.toPadding();
			messageBuffer.append(f.toPadding((String) this.encodeOrDecode(f).getValue(0)));
		}
		return messageBuffer.toString();
	}

	@Override
	public String header2Raw() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			sb.append((String) f.getValue(0));
		}
		return sb.toString();
	}

	@Override
	public String body2Raw() {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < body.length; j++) {
			Field f = body[j];
			sb.append((String) f.getValue(0));
		}
		return sb.toString();
	}

	@Override
	public String tail2Raw() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			sb.append((String) f.getValue(0));
		}
		return sb.toString();
	}

	@Override
	public ByteBuf toByteBuf() {
		ByteBuf buf = Unpooled.buffer(256);

		if(header != null) {
			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				try {
					f.toPadding();
					encodeOrDecode(f);
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				logger.debug("Header's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes()));
				buf.writeBytes(f.getValueBytes());
			}
		}
		if(body != null) {
			int repeatCount = 1;
			if ("true".equals(repeat)) {
				repeatCount = Integer.parseInt(repeatVariable);
			}
			for (int i = 0; i < repeatCount; i++) {
				for (int j = 0; j < body.length; j++) {
					Field f = body[j];
					try {
						f.toPadding();
						encodeOrDecode(f,i);
					} catch (Exception e) {
						logger.error(e.toString(),e);
					}
					logger.debug("Body's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes(i)));
					buf.writeBytes(f.getValueBytes(i));
				}
			}
		}
		if(tail != null) {
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				try {
					f.toPadding();
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				logger.debug("Tail's toByteBuf : {} -> [{}]" , f.getName(), new String(f.getValueBytes()));
				buf.writeBytes(f.getValueBytes());
			}
		}
		// byte[] b = new byte[buf.readableBytes()];
		// buf.readBytes(b);
		// logger.debug("FINAL : [{}]", new String(b));
		return buf;
	}

	@Override
	public Map<String, Object> toHashMap() {
		Map<String, Object> rootMap = new HashMap<>();
		Map<String, Object> headerMap = getHeaderMap();
		rootMap.put("header", headerMap);
		int repeatCount = 1;
		if ("true".equals(repeat)) {
			repeatCount = Integer.parseInt(repeatVariable);
		}
		rootMap.put("body", getBodyMapOfCount(repeatCount));
		Map<String, Object> tailMap = getTailMap();
		rootMap.put("tail", tailMap);
		return rootMap;
	}

	@Override
	public Object getBodyMapOfCount(int repeatCount) {
		if (repeatCount > 1) {
			Map<String,Object>[] bodyMap = getBodyMap(repeatCount);
			return bodyMap;
		} else {
			Map<String,Object> bodyMap = getBodyMap();
			return bodyMap;
		}
	}

	@Override
	public Map<String, Object> getBodyMap() {
		Map<String, Object> bodyMap = new HashMap<>();
		for (int j = 0; j < body.length; j++) {
			Field f = body[j];
			bodyMap.put(f.getName(), f.getValue(0));
		}
		return bodyMap;
	}

	@Override
	public Map[] getBodyMap(int repeatCount) {
		Map<String, Object>[] bodyMap = new HashMap[repeatCount];
		for (int i = 0; i < repeatCount; i++) {
			for (int j = 0; j < body.length; j++) {
				Field f = body[j];
				bodyMap[i] = new HashMap<>();
				bodyMap[i].put(f.getName(), f.getValue(i));
			}
		}
		return bodyMap;
	}

	@Override
	public Map<String, Object> getTailMap() {
		Map<String, Object> tailMap = new HashMap<>();
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			tailMap.put(f.getName(), f.getValue(0));
		}
		return tailMap;
	}

	@Override
	public Map<String, Object> getHeaderMap() {
		Map<String, Object> headerMap = new HashMap<>();
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			headerMap.put(f.getName(), f.getValue(0));
		}
		return headerMap;
	}

	@Override
	public StringBuilder getMessageBuffer() {
		return messageBuffer;
	}

	@Override
	public void setMessageBuffer(StringBuilder messageBuffer) {
		this.messageBuffer = messageBuffer;
	}

	@Override
	public String getHeaderValue(String name) {
		for (int i = 0; i < this.header.length; i++) {
			if (header[i].getName().equals(name))
				return (String) header[i].getValue(0);
		}
		return null;
	}

	@Override
	public void setHeaderValue(String name, String value) {
		for (int i = 0; i < this.header.length; i++) {
			if (header[i].getName().equals(name))
				header[i].setValue(value);
		}
	}

	@Override
	public String getTailValue(String name) {
		for (int i = 0; i < this.tail.length; i++) {
			if (tail[i].getName().equals(name))
				return (String) tail[i].getValue(0);
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
	public String getBodyValue(String name, int index) {
		for (int i = 0; i < this.body.length; i++) {
			if (body[i].getName().equals(name))
				return (String) body[i].getValue(index);
		}
		return null;
	}

	@Override
	public void setBodyValue(String name, String value, int index) {
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
		return this.encodeOrDecode(f, 0);
	}

	@Override
	public Field encodeOrDecode(Field f, int idx) throws Exception {
		try {
			if("true".equals(this.getEncoder())) {
				Object object = context.getBean(this.getEncoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					if ("true".equals(f.getEncode())) {
						cypher.setMessage(this);
						f.setValue(idx, cypher.encode((String) f.getValue(idx)));
					}
				}
			}
			if("true".equals(this.getDecoder())) {
				Object object = context.getBean(this.getDecoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					if ("true".equals(f.getDecode())) {
						cypher.setMessage(this);
						f.setValue(idx, cypher.decode((String) f.getValue(idx)));
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
		msg.setRepeat(this.getRepeat());
		msg.setRepeatVariable(this.getRepeatVariable());
		if(header != null) {
			msg.setHeader(new Field[header.length]);
			for(int i = 0; i < header.length; i++) {
				msg.getHeader()[i] = new Field();
				msg.getHeader()[i].setName(header[i].getName());
				msg.getHeader()[i].setLength(header[i].getLength());
				msg.getHeader()[i].setPadType(header[i].getPadType());
				msg.getHeader()[i].setPadChar(header[i].getPadChar());
				msg.getHeader()[i].setEncode(header[i].getEncode());
				msg.getHeader()[i].setDecode(header[i].getDecode());
				msg.getHeader()[i].setValue((String)header[i].getValue(0));
			}
		}
		if(body != null) {
			msg.setBody(new Field[body.length]);
			for(int i = 0; i < body.length; i++) {
				msg.getBody()[i] = new Field();
				msg.getBody()[i].setName(body[i].getName());
				msg.getBody()[i].setLength(body[i].getLength());
				msg.getBody()[i].setPadType(body[i].getPadType());
				msg.getBody()[i].setPadChar(body[i].getPadChar());
				msg.getBody()[i].setEncode(body[i].getEncode());
				msg.getBody()[i].setDecode(body[i].getDecode());
				msg.getBody()[i].setValue((String)body[i].getValue(0));
			}
		}
		if(tail != null) {
			msg.setTail(new Field[tail.length]);
			for(int i = 0; i < tail.length; i++) {
				msg.getTail()[i] = new Field();
				msg.getTail()[i].setName(tail[i].getName());
				msg.getTail()[i].setLength(tail[i].getLength());
				msg.getTail()[i].setPadType(tail[i].getPadType());
				msg.getTail()[i].setPadChar(tail[i].getPadChar());
				msg.getTail()[i].setEncode(tail[i].getEncode());
				msg.getTail()[i].setDecode(tail[i].getDecode());
				msg.getTail()[i].setValue((String)tail[i].getValue(0));
			}
		}
		return msg;
	}

	@Override
	public void setBodyValue(Map<String, Object> bodyMap) {
		for(int i = 0; i < this.body.length; i++) {
			if(bodyMap.get(this.body[i].getName()) != null) {
				if(bodyMap.get(this.body[i].getName()) instanceof String)
					this.body[i].addValue((String)bodyMap.get(this.body[i].getName()));
				else
					this.body[i].addValue(bodyMap.get(this.body[i].getName()).toString());
			}
		}
	}

	@Override
	public int getLength(Field field, MessageInfo messageInfo) throws Exception{
		if(field.getLength() != null && !"".equals(field.getLength())) {
			try {
				int len = Integer.parseInt(field.getLength());
				return len;
			}catch(Exception e) {
				throw new Exception("MessageFieldLengthException");
			}
		}else {
			if(field.getRefLength() != null && !"".equals(field.getRefLength())) {
				String[] names = field.getRefLength().split(".");
				if(names.length != 3) throw new Exception("MessageFieldLengthException");
				else {
					if("request".equals(names[0])) {
						int len = getRefLength(names,messageInfo.getRequestMessage());
						field.setLength(len+"");
						return len;
					}else if("response".equals(names[0])) {
						int len = getRefLength(names,messageInfo.getResponseMessage());
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
		if("header".equals(names[1])) {
			if(message.getHeader(names[2]) != null && !"".equals(message.getHeader(names[2]))) {
				try {
					int len = Integer.parseInt(message.getHeader(names[2]));
					return len;
				}catch(Exception e) {
					throw new Exception("MessageFieldLengthException");
				}
			}else {
				throw new Exception("MessageFieldLengthException");
			}
		}else if("body".equals(names[1])) {
			if(message.getBody(names[2], 0) != null && !"".equals(message.getBody(names[2], 0))) {
				try {
					int len = Integer.parseInt(message.getBody(names[2], 0));
					return len;
				}catch(Exception e) {
					throw new Exception("MessageFieldLengthException");
				}
			}else {
				throw new Exception("MessageFieldLengthException");
			}
		}else if("tail".equals(names[1])) {
			if(message.getTail(names[2]) != null && !"".equals(message.getTail(names[2]))) {
				try {
					int len = Integer.parseInt(message.getTail(names[2]));
					return len;
				}catch(Exception e) {
					throw new Exception("MessageFieldLengthException");
				}
			}else {
				throw new Exception("MessageFieldLengthException");
			}							
		}else {
			throw new Exception("MessageFieldLengthException");
		}
	}

}
