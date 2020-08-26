package com.kcb.id.comm.carrier.parser.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ErrorInfo;
import com.kcb.id.comm.carrier.loader.HeaderInfo;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.loader.impl.MessageImpl;

public class FieldParser {

	static Logger logger = LoggerFactory.getLogger(FieldParser.class);

	public ErrorInfo parseErrorField(ErrorInfo errorInfo, Node subSubNode , boolean isHeader) throws Exception{
		Field[] fields = null;
		if (subSubNode.getNodeName().equals("error")) {
			logger.debug("header or message node exists");
			fields = getFields(subSubNode , false);
			if(subSubNode.getAttributes().getNamedItem("name")!=null) {
				if(isHeader)errorInfo.getHeaderErrorMap().put(subSubNode.getAttributes().getNamedItem("name").getNodeValue(), fields);
				else errorInfo.getMessageErrorMap().put(subSubNode.getAttributes().getNamedItem("name").getNodeValue(), fields);
			}
		} 
		return errorInfo;
	}
	
	public HeaderInfo parseHeaderField(HeaderInfo headerInfo, Node subSubNode , boolean isRequest) throws Exception{
		Field[] fields = null;
		if (subSubNode.getNodeName().equals("error")) {
			logger.debug("request header or response header node exists");
			fields = getFields(subSubNode , false);
			if(isRequest) headerInfo.setRequestHeader(fields);
			else headerInfo.setResponseHeader(fields);
		} 
		return headerInfo;
	}	
	
	public Message parseMessageFields(Node subSubNode, boolean isRequest) throws Exception {
		return this.parseMessageFields(new MessageImpl(), subSubNode , isRequest);
	}
		
	public Message parseMessageFields(Message parsedMsg, Node subSubNode, boolean isRequest) throws Exception {
		
		Field[] fields = null;
		if (subSubNode.getNodeName().equals("body")) {
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
			
			// Request 항목에 리턴코드와 리턴메시지가 널이 아닌 경우
			if(isRequest 
			&& (subSubSubSubNode.getAttributes().getNamedItem("isResCode") != null
			|| subSubSubSubNode.getAttributes().getNamedItem("isResMessage") != null
			))throw new Exception("RequestMessageCannotContainResponseException");
			
			// Request 항목에 참조속성이 있는 경우
			if(isRequest && subSubSubSubNode.getAttributes().getNamedItem("ref") != null)
				throw new Exception("RequestMessageCannotContainRefException");
			
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
			if (subSubSubSubNode.getAttributes().getNamedItem("encoder") != null) {
				fields[l].setEncoder(subSubSubSubNode.getAttributes().getNamedItem("encoder").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("decoder") != null) {
				fields[l].setDecoder(subSubSubSubNode.getAttributes().getNamedItem("decoder").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("value") != null) {
				fields[l].setValue(subSubSubSubNode.getAttributes().getNamedItem("value").getNodeValue());
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("isResCode") != null) {
				String isResCode = subSubSubSubNode.getAttributes().getNamedItem("isResCode").getNodeValue();
				if("true".equals(isResCode))fields[l].setResCode(true);
				else fields[l].setResCode(false);
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("isResMessage") != null) {
				String isResMessage = subSubSubSubNode.getAttributes().getNamedItem("isResMessage").getNodeValue();
				if("true".equals(isResMessage))fields[l].setResMessage(true);
				else fields[l].setResMessage(false);
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("isKindCode") != null) {
				String isKindCode = subSubSubSubNode.getAttributes().getNamedItem("isKindCode").getNodeValue();
				if("true".equals(isKindCode))fields[l].setKindCode(true);
				else fields[l].setKindCode(false);
			}
			if (subSubSubSubNode.getAttributes().getNamedItem("isMessageCode") != null) {
				String isBizCode = subSubSubSubNode.getAttributes().getNamedItem("isMessageCode").getNodeValue();
				if("true".equals(isBizCode))fields[l].setMessageCode(true);
				else fields[l].setMessageCode(false);
			}
			
			if(subSubSubSubNode.getAttributes().getNamedItem("ref") != null) {
				fields[l].setRef(subSubSubSubNode.getAttributes().getNamedItem("ref").getNodeValue());
			}
			
		}
		return fields;
		
	}
}
