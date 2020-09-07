package com.kcb.id.comm.carrier.loader.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class Field implements Serializable {
	
	static Logger logger = LoggerFactory.getLogger(Field.class);
	static enum TYPE {B,N,C};
	
	private static final long serialVersionUID = 1L;
	String length;
	String name;
	String value;
	String encoder;
	String decoder;
	String padType;
	String padChar;
	String charType;
	TYPE type = TYPE.C;
	boolean resCode;
	boolean resMessage;
	boolean kindCode;
	boolean messageCode;
	String ref;
	String refLength;
	
	public Field() {
	}

	public String getCharType() {
		return charType;
	}

	public void setCharType(String charType) {
		this.charType = charType;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getRefLength() {
		return refLength;
	}

	public void setRefLength(String refLength) {
		this.refLength = refLength;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public String toPadding(String data) throws Exception {
		String result = data;
		if ("RPAD".equals(this.getPadType().toUpperCase())) {
			result = rpad(toEncoding(data), Integer.parseInt(this.getLength()), this.getPadChar());
		} else if ("LPAD".equals(this.getPadType().toUpperCase())) {
			result = lpad(toEncoding(data), Integer.parseInt(this.getLength()), this.getPadChar());
		} else {
			result = rpad(toEncoding(data), Integer.parseInt(this.getLength()), this.getPadChar());
		}
		return result;
	}

	public void toPadding() throws NumberFormatException, UnsupportedEncodingException {
		if(this.getPadType() == null) return;
		if ("RPAD".equals(this.getPadType().toUpperCase())) {
			this.setValue(rpad(toEncoding(value), Integer.parseInt(this.getLength()), this.getPadChar()));
		} else if ("LPAD".equals(this.getPadType().toUpperCase())) {
			this.setValue(lpad(toEncoding(value), Integer.parseInt(this.getLength()), this.getPadChar()));
		} else {
			this.setValue(rpad(toEncoding(value), Integer.parseInt(this.getLength()), this.getPadChar()));
		}
	}

	public String toEncoding(String data) throws UnsupportedEncodingException {
		if (this.getCharType() == null || "".equals(this.getCharType()))
			return data;
		else {
			return new String(data.getBytes(), this.getCharType());
		}
	}

	public void setValue(String value) {
		this.value = value;
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

	public String getPadType() {
		return padType;
	}

	public void setPadType(String padType) {
		this.padType = padType;
	}

	public String getPadChar() {
		return padChar;
	}

	public void setPadChar(String padChar) {
		this.padChar = padChar;
	}

	public String getConvertedValue() {
		return null;
	}

	public static String lpad(String msg, int length, String repStr) {
		String ret = "";
		if (msg == null) {
			msg = "";
		}
		ret = msg;

		if (msg.length() > length) {
			return ret.substring(0, length);
		} else if (msg.length() == length) {
			return ret;
		} else {
			for (int i = 0; i < length - msg.length(); i++) {
				ret = repStr + ret;
			}
			return ret;
		}
	}

	public static String rpad(String msg, int length, String repStr) {

		String ret = "";
		if (msg == null) {
			msg = "";
		}
		ret = msg;

		if (msg.length() > length) {
			return ret.substring(0, length);

		} else if (msg.length() == length) {
			return ret;
		} else {
			for (int i = 0; i < length - msg.length(); i++) {
				ret = ret + repStr;
			}
			return ret;
		}
	}

	public static String nvl(String data, String repStr) {
		if (data == null || "null".equals(data))
			return repStr;
		else
			return data;
	}

	public String toRaw() {
		return "[" + this.getName() + "][" + this.getLength() + "][" + this.getRef() + "][" + this.getRefLength() + "][" + this.getEncoder() + "]";
	}
	
	public byte[] getBytes() {
		return new byte[Integer.parseInt(this.getLength())];
	}
	
	public byte[] getValueBytes() {
		return this.getValueBytes(value);
	}
	
	public byte[] getValueBytes(String value) {
		byte[] t = this.getBytes();
		if(value == null) return t;
		byte[] src = value.getBytes();
		int copyLen = src.length > t.length ? t.length : src.length;
		System.arraycopy(src, 0, t, 0, copyLen);
		return t;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public boolean isResCode() {
		return resCode;
	}

	public void setResCode(boolean resCode) {
		this.resCode = resCode;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public boolean isResMessage() {
		return resMessage;
	}

	public void setResMessage(boolean resMessage) {
		this.resMessage = resMessage;
	}

	public boolean isKindCode() {
		return kindCode;
	}

	public void setKindCode(boolean kindCode) {
		this.kindCode = kindCode;
	}

	public boolean isMessageCode() {
		return messageCode;
	}

	public void setMessageCode(boolean messageCode) {
		this.messageCode = messageCode;
	}

	public Field newInstance() {
		Field f = new Field();
		f.setName(this.getName());
		f.setLength(this.getLength());
		f.setPadType(this.getPadType());
		f.setPadChar(this.getPadChar());
		f.setEncoder(this.getEncoder());
		f.setDecoder(this.getDecoder());
		f.setResCode(this.isResCode());
		f.setResMessage(this.isResMessage());
		f.setKindCode(this.isKindCode());
		f.setMessageCode(this.isMessageCode());
		f.setRefLength(this.getRefLength());
		f.setRef(this.getRef());
		f.setValue(this.getValue());
		return f;
	}
}
