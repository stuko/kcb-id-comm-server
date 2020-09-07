package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.kcb.id.comm.carrier.common.StringUtils;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;

public class MessageImpl extends DeEncoderImpl implements Message {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(MessageImpl.class);

	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
	
	Field[] header;
	Field[] body;
	Field[] tail;

	StringBuilder messageBuffer;
	String messageName;
	String forwardMessageName;
	String forwardIp;
	String forwardPort;
	String forwardTimeOut;
	String preBean;
	String postBean;
	String kindCode;
	String messageCode;

	public MessageImpl() {
		messageBuffer = new StringBuilder();
	}
	
	
	public String getMessageCode() {
		return messageCode;
	}


	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}


	public String getMessageName() {
		return messageName;
	}


	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}


	public String getForwardMessageName() {
		return forwardMessageName;
	}


	public void setForwardMessageName(String forwardMessageName) {
		this.forwardMessageName = forwardMessageName;
	}


	public String getPreBean() {
		return preBean;
	}


	public void setPreBean(String preBean) {
		this.preBean = preBean;
	}


	public String getPostBean() {
		return postBean;
	}


	public void setPostBean(String postBean) {
		this.postBean = postBean;
	}


	public String getKindCode() {
		return kindCode;
	}


	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
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
	public byte[] toByte() throws Exception {
		return this.toByte(this);
	}
	
	@Override
	public byte[] toByte(Message msg) throws Exception {
		byte[] buf = new byte[this.getLength()];
		byte[] tmp = null;
		int pos = 0;
		if(header != null) {
			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				try {
					decodeAndEncode(f,msg.toHashMap());
					f.toPadding();
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				tmp = f.getValueBytes();
				logger.debug("Header's toByte : {} -> [{}]" , f.getName(), new String(tmp));
				System.arraycopy(tmp, 0, buf, pos, tmp.length);
				pos += tmp.length;
			}
		}
		if(body != null) {
			for (int i = 0; i < body.length; i++) {
				Field f = body[i];
				try {
					decodeAndEncode(f,msg.toHashMap());
					f.toPadding();
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				tmp = f.getValueBytes();
				logger.debug("Body's toByte : {} -> [{}]" , f.getName(), new String(tmp));
				System.arraycopy(tmp, 0, buf, pos, tmp.length);
				pos += tmp.length;
			}
		}
		if(tail != null) {
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				try {
					decodeAndEncode(f,msg.toHashMap());
					f.toPadding();
				} catch (Exception e) {
					logger.error(e.toString(),e);
				}
				tmp = f.getValueBytes();
				logger.debug("Tail's toByte : {} -> [{}]" , f.getName(), new String(tmp));
				System.arraycopy(tmp, 0, buf, pos, tmp.length);
				pos += tmp.length;
			}
		}
		return buf;
	}
	
	public int getLength() throws Exception {
		int total = 0;
		total += this.getLength(header);
		total += this.getLength(body);
		total += this.getLength(tail);
		return total;
	}
	
	private int getLength(Field[] fields) throws Exception{
		int total = 0;
		try {
			for(Field f : fields) {
				total += Integer.parseInt(f.getLength());
			}
		}catch(Exception e) {
			throw new Exception("MessageLengthException",e);
		}
		return total;
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
	public Field decodeAndEncode(Field f) throws Exception {
		return this.decodeAndEncode(f, this.toHashMap());
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
		msg.setMessageName(this.getMessageName());
		msg.setForwardMessageName(this.getForwardMessageName());
		msg.setKindCode(this.getKindCode());
		msg.setMessageCode(this.getMessageCode());
		if(header != null) {
			msg.setHeader(new Field[header.length]);
			for(int i = 0; i < header.length; i++) {
				logger.debug("header : {}", header[i].toRaw());
				msg.getHeader()[i] = header[i].newInstance();
			}
		}
		if(body != null) {
			msg.setBody(new Field[body.length]);
			for(int i = 0; i < body.length; i++) {
				logger.debug("body : {}", body[i].toRaw());
				msg.getBody()[i] = body[i].newInstance();
			}
		}
		if(tail != null) {
			msg.setTail(new Field[tail.length]);
			for(int i = 0; i < tail.length; i++) {
				logger.debug("tail : {}", tail[i].toRaw());
				msg.getTail()[i] = tail[i].newInstance();
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


	@Override
	public String getBodyMessageCodeValue() {
		for(int i = 0; i < body.length; i++) {
			if(body[i].isMessageCode()) return body[i].getValue();
		}		
		return null;
	}


	public String getForwardIp() {
		return forwardIp;
	}

	public void setForwardIp(String forwardIp) {
		this.forwardIp = forwardIp;
	}

	public String getForwardPort() {
		return forwardPort;
	}

	public void setForwardPort(String forwardPort) {
		this.forwardPort = forwardPort;
	}

	public String getForwardTimeOut() {
		return forwardTimeOut;
	}

	public void setForwardTimeOut(String forwardTimeOut) {
		this.forwardTimeOut = forwardTimeOut;
	}

}
