package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
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
@Scope("prototype")
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
		FieldParser parser = null;
		for (int k = 0; k < subSubNodeList.getLength(); k++) {
			if (subSubNodeList.item(j) == null)
				continue;
			if (subSubNodeList.item(j).getNodeType() == Node.TEXT_NODE)
				continue;
			Field[] fields = null;
			Node subSubNode = subSubNodeList.item(k);
			parser = new FieldParser();
			parsedMsg = parser.parseFields(parsedMsg, subSubNode);
		}
		return parsedMsg;
	}


}
