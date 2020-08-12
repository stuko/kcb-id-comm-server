package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.impl.HandlerInfoImpl;
import com.kcb.id.comm.carrier.parser.HandlerInfoParser;

@Component
@Scope("prototype")
public class HandlerInfoParserImpl  implements HandlerInfoParser {

	static Logger logger = LoggerFactory.getLogger(HandlerInfoParserImpl.class);
	@Override
	public List<HandlerInfo> parse(NodeList nodeList) {
		List<HandlerInfo> list = new ArrayList<>();
		HandlerInfo handlerInfo = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = nodeList.item(i);
			NodeList subNodeList = node.getChildNodes();
			for (int j = 0; j < subNodeList.getLength(); j++) {
				if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
					continue;
				if (subNodeList.item(j).getNodeName().equals("handler")) {
					String handlerName = subNodeList.item(j).getAttributes().getNamedItem("handlerName").getNodeValue();
					handlerInfo = new HandlerInfoImpl();
					handlerInfo.setName(handlerName);
					handlerInfo.setMessageName(
							subNodeList.item(j).getAttributes().getNamedItem("messageName").getNodeValue());
					handlerInfo.setHandlerClass(
							subNodeList.item(j).getAttributes().getNamedItem("handlerClass").getNodeValue());
					handlerInfo.setEnable(Boolean.getBoolean(subNodeList.item(j).getAttributes().getNamedItem("enable").getNodeValue()));
					handlerInfo.setBusinessClass(
							subNodeList.item(j).getAttributes().getNamedItem("businessClass").getNodeValue());
					list.add(handlerInfo);
				}
			}
		}
		return list;
	}
}