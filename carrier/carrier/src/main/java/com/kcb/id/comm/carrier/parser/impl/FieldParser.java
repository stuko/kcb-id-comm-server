package com.kcb.id.comm.carrier.parser.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.loader.impl.MessageImpl;

public class FieldParser {

	static Logger logger = LoggerFactory.getLogger(FieldParser.class);

	public Message parseFields(Node subSubNode, boolean isRequest) throws Exception {
		return this.parseFields(new MessageImpl(), subSubNode , isRequest);
	}
		
	public Message parseFields(Message parsedMsg, Node subSubNode , boolean isRequest) throws Exception {
		
		Field[] fields = null;
		if (subSubNode.getNodeName().equals("header")) {
			logger.debug("header node exists");
			fields = getFields(subSubNode , isRequest);
			parsedMsg.setHeader(fields);
		} else if (subSubNode.getNodeName().equals("body")) {
			logger.debug("body node exists");
			fields = getFields(subSubNode , isRequest);
			parsedMsg.setBody(fields);
		} else if (subSubNode.getNodeName().equals("tail")) {
			logger.debug("tail node exists");
			fields = getFields(subSubNode , isRequest);
			parsedMsg.setTail(fields);
		}
		return parsedMsg;
		
	}

	private Field[] getFields(Node subSubNode, boolean isRequest) throws Exception{

		NodeList subSubSubNodeList = ((Element) subSubNode).getElementsByTagName("field");
		Field[] fields = null;
		if (subSubSubNodeList.getLength() > 0) {
			logger.debug("field node exists");
			fields = new Field[subSubSubNodeList.getLength()];
		} else {
			return null;
		}
		for (int l = 0; l < subSubSubNodeList.getLength(); l++) {
			Node subSubSubSubNode = subSubSubNodeList.item(l);
			fields[l] = new Field();
			
			if(isRequest 
			&& (subSubSubSubNode.getAttributes().getNamedItem("isResCode") != null || subSubSubSubNode.getAttributes().getNamedItem("ref") != null) )
				throw new Exception("MessageFormatException");
			
			if (subSubSubSubNode.getAttributes().getNamedItem("length") != null) {
				fields[l].setLength(subSubSubSubNode.getAttributes().getNamedItem("length").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("refLength") != null) {
				fields[l].setRefLength(subSubSubSubNode.getAttributes().getNamedItem("refLength").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("name") != null) {
				fields[l].setName(subSubSubSubNode.getAttributes().getNamedItem("name").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("padType") != null) {
				fields[l].setPadType(subSubSubSubNode.getAttributes().getNamedItem("padType").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("padChar") != null) {
				fields[l].setPadChar(subSubSubSubNode.getAttributes().getNamedItem("padChar").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("encode") != null) {
				fields[l].setEncode(subSubSubSubNode.getAttributes().getNamedItem("encode").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("decode") != null) {
				fields[l].setDecode(subSubSubSubNode.getAttributes().getNamedItem("decode").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("value") != null) {
				fields[l].setValue(subSubSubSubNode.getAttributes().getNamedItem("value").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("isResCode") != null) {
				String isResCode = subSubSubSubNode.getAttributes().getNamedItem("isResCode").getNodeValue();
				if("true".equals(isResCode))fields[l].setResCode(true);
				else fields[l].setResCode(false);
			}
			if(subSubSubSubNode.getAttributes().getNamedItem("ref") != null) {
				fields[l].setRef(subSubSubSubNode.getAttributes().getNamedItem("ref").getNodeValue());
			}
			
		}
		return fields;
		
	}
}
