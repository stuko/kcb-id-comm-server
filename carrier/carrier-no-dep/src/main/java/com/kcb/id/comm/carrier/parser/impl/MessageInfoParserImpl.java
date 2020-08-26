package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageFrame;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.loader.impl.MessageImpl;
import com.kcb.id.comm.carrier.loader.impl.MessageInfoImpl;
import com.kcb.id.comm.carrier.parser.MessageInfoParser;

@Component
@Scope("prototype")
public class MessageInfoParserImpl implements MessageInfoParser {

	static Logger logger = LoggerFactory.getLogger(MessageInfoParserImpl.class);

	public List<MessageInfo> parse(NodeList nodeList) throws Exception{
		List<MessageInfo> list = new ArrayList<>();
		NodeList setNodeList = nodeList.item(0).getChildNodes();
		for (int i = 0; i < setNodeList.getLength(); i++) {
			if (setNodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			MessageInfo receiverInfo = new MessageInfoImpl();
			Node node = setNodeList.item(i);
			receiverInfo.setMessageName(node.getAttributes().getNamedItem("messageName") == null ? "" : node.getAttributes().getNamedItem("messageName").getNodeValue());
			
			if (receiverInfo.getMessageName() == null) {
				throw new Exception("MessageNameDoesNotExistException");
			}
			
			NodeList subNodeList = node.getChildNodes();
			for (int j = 0; j < subNodeList.getLength(); j++) {
				if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
					continue;
				
				if (subNodeList.item(j).getNodeName().equals("request")) {
					receiverInfo.setRequestMessage(getMessage(subNodeList, j , true));
				} else if (subNodeList.item(j).getNodeName().equals("response")) {
					receiverInfo.setResponseMessage(getMessage(subNodeList, j, false));
				}
			}
			if (receiverInfo.getRequestMessage().checkMe()) {
				list.add(receiverInfo);
			}
		}
		return list;
	}

	private Message getMessage(NodeList subNodeList, int j , boolean isRequest) throws Exception{
		Message parsedMsg = new MessageImpl();
		
		parsedMsg.setMessageName(subNodeList.item(j).getAttributes().getNamedItem("messageName") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("messageName").getNodeValue());
		parsedMsg.setForwardMessageName(subNodeList.item(j).getAttributes().getNamedItem("forwardMessageName") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("forwardMessageName").getNodeValue());
		parsedMsg.setForwardIp(subNodeList.item(j).getAttributes().getNamedItem("forwardIp") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("forwardIp").getNodeValue());
		parsedMsg.setForwardPort(subNodeList.item(j).getAttributes().getNamedItem("forwardPort") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("forwardPort").getNodeValue());		
		parsedMsg.setForwardTimeOut(subNodeList.item(j).getAttributes().getNamedItem("forwardTimeOut") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("forwardTimeOut").getNodeValue());		
		parsedMsg.setKindCode(subNodeList.item(j).getAttributes().getNamedItem("kindCode") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("kindCode").getNodeValue());
		parsedMsg.setMessageCode(subNodeList.item(j).getAttributes().getNamedItem("messageCode") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("messageCode").getNodeValue());
		parsedMsg.setPreBean(subNodeList.item(j).getAttributes().getNamedItem("preBean") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("preBean").getNodeValue());
		parsedMsg.setPostBean(subNodeList.item(j).getAttributes().getNamedItem("postBean") == null ? "" 	: subNodeList.item(j).getAttributes().getNamedItem("postBean").getNodeValue());
		
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
			parsedMsg = parser.parseMessageFields(parsedMsg, subSubNode, isRequest);
		}
		return parsedMsg;
	}


}
