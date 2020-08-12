package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.loader.impl.MessageImpl;
import com.kcb.id.comm.carrier.loader.impl.MessageInfoImpl;
import com.kcb.id.comm.carrier.parser.MessageInfoParser;

@Component
public class MessageInfoParserImpl implements MessageInfoParser {

	static Logger logger = LoggerFactory.getLogger(MessageInfoParserImpl.class);

	public List<MessageInfo> parse(NodeList nodeList) {
		List<MessageInfo> list = new ArrayList<>();
		NodeList setNodeList = nodeList.item(0).getChildNodes();
		for (int i = 0; i < setNodeList.getLength(); i++) {
			if (setNodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			MessageInfo receiverInfo = new MessageInfoImpl();
			Node node = setNodeList.item(i);
			receiverInfo.setName(node.getAttributes().getNamedItem("name") == null ? "" : node.getAttributes().getNamedItem("name").getNodeValue());
			
			if (receiverInfo.getName() == null) {
				logger.error("message name is null");
				return null;
			}
			
			receiverInfo.setForward(node.getAttributes().getNamedItem("forward") == null ? "" : node.getAttributes().getNamedItem("forward").getNodeValue());
			receiverInfo.setForwardIp(node.getAttributes().getNamedItem("forwardIp") == null ? "" : node.getAttributes().getNamedItem("forwardIp").getNodeValue());
			String sPort = node.getAttributes().getNamedItem("forwardPort") == null ? "" : node.getAttributes().getNamedItem("forwardPort").getNodeValue();
			if(sPort != null && !"".equals(sPort)) receiverInfo.setForwardPort(Integer.parseInt(sPort));
			
			NodeList subNodeList = node.getChildNodes();
			for (int j = 0; j < subNodeList.getLength(); j++) {
				if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
					continue;
				
				if (subNodeList.item(j).getNodeName().equals("request")) {
					logger.debug("request node exists");
					receiverInfo.setRequestMessage(getRequestMessage(subNodeList, j));
				} else if (subNodeList.item(j).getNodeName().equals("response")) {
					logger.debug("response node exists");
					receiverInfo.setResponseMessage(getRequestMessage(subNodeList, j));
				} else if (subNodeList.item(j).getNodeName().equals("error")) {
					logger.debug("error node exists");
					String exception = subNodeList.item(j).getAttributes().getNamedItem("name").getNodeValue();
					receiverInfo.getExceptionMessageMap().put(exception, getRequestMessage(subNodeList, j));
				}
			}
			if (receiverInfo.getRequestMessage().checkMe()) {
				list.add(receiverInfo);
			}
		}
		return list;
	}

	private Message getRequestMessage(NodeList subNodeList, int j) {
		
		Message parsedMsg = new MessageImpl();
		
		String repeat = subNodeList.item(j).getAttributes().getNamedItem("repeat") == null ? "false"
				: subNodeList.item(j).getAttributes().getNamedItem("repeat").getNodeValue();
		String repeat_variable = subNodeList.item(j).getAttributes().getNamedItem("repeatVariable") == null ? ""
				: subNodeList.item(j).getAttributes().getNamedItem("repeatVariable").getNodeValue();
		String encoder = subNodeList.item(j).getAttributes().getNamedItem("encoder") == null ? "false"
				: subNodeList.item(j).getAttributes().getNamedItem("encoder").getNodeValue();
		String decoder = subNodeList.item(j).getAttributes().getNamedItem("decoder") == null ? "false"
				: subNodeList.item(j).getAttributes().getNamedItem("decoder").getNodeValue();
		String path = subNodeList.item(j).getAttributes().getNamedItem("path") == null ? ""
				: subNodeList.item(j).getAttributes().getNamedItem("path").getNodeValue();
		if (subNodeList.item(j).getAttributes().getNamedItem("destinationIp") != null
				&& subNodeList.item(j).getAttributes().getNamedItem("destinationPort") != null) {
			parsedMsg
					.setDestinationIp(subNodeList.item(j).getAttributes().getNamedItem("destinationIp").getNodeValue());
			parsedMsg.setDestinationPort(
					subNodeList.item(j).getAttributes().getNamedItem("destinationPort").getNodeValue());
		}
		parsedMsg.setRepeat(repeat);
		parsedMsg.setRepeatVariable(repeat_variable);

		if (encoder != null && !"false".equals(encoder)) {
			parsedMsg.setEncoder(encoder);
		}
		if (decoder != null && !"false".equals(decoder)) {
			parsedMsg.setDecoder(decoder);
		}
		parsedMsg.setPath(path);

		NodeList subSubNodeList = subNodeList.item(j).getChildNodes();
		for (int k = 0; k < subSubNodeList.getLength(); k++) {
			if (subSubNodeList.item(j) == null)
				continue;
			if (subSubNodeList.item(j).getNodeType() == Node.TEXT_NODE)
				continue;
			Field[] fields = null;
			Node subSubNode = subSubNodeList.item(k);
			if (subSubNode.getNodeName().equals("header")) {
				logger.debug("header node exists");
				fields = getFields(subSubNode);
				parsedMsg.setHeader(fields);
			} else if (subSubNode.getNodeName().equals("body")) {
				logger.debug("body node exists");
				fields = getFields(subSubNode);
				parsedMsg.setBody(fields);
			} else if (subSubNode.getNodeName().equals("tail")) {
				logger.debug("tail node exists");
				fields = getFields(subSubNode);
				parsedMsg.setTail(fields);
			}
		}
		return parsedMsg;
	}

	private Field[] getFields(Node subSubNode) {
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
			fields[l].setLength(subSubSubSubNode.getAttributes().getNamedItem("length").getNodeValue());
			fields[l].setName(subSubSubSubNode.getAttributes().getNamedItem("name").getNodeValue());
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

			logger.debug("field data : " + fields[l].toRaw());
		}
		return fields;
	}
}
