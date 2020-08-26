package com.kcb.id.comm.carrier.parser.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HeaderInfo;
import com.kcb.id.comm.carrier.loader.impl.HeaderInfoImpl;
import com.kcb.id.comm.carrier.parser.HeaderInfoParser;

public class HeaderInfoParserImpl implements HeaderInfoParser{
	
	static Logger logger = LoggerFactory.getLogger(HeaderInfoParserImpl.class);

	public HeaderInfo parse(NodeList nodeList) throws Exception{
		HeaderInfo headerInfo = null;
		NodeList setNodeList = nodeList.item(0).getChildNodes(); // set node
		for (int i = 0; i < setNodeList.getLength(); i++) {
			if (setNodeList.item(i) == null)
				continue;
			if (setNodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = setNodeList.item(i);
			if(node.getNodeName().equals("request")) {
				headerInfo = getHeaderInfo(node.getChildNodes(),true);
			}else if(node.getNodeName().equals("response")) {
				headerInfo = getHeaderInfo(node.getChildNodes(),false);
			}
		}
			
		return headerInfo;
	}

	private HeaderInfo getHeaderInfo(NodeList subNodeList,boolean isRequest) throws Exception{
		HeaderInfo headerInfo = new HeaderInfoImpl();
		FieldParser parser = new FieldParser();
		for (int i = 0; i < subNodeList.getLength(); i++) { // header node
			if (subNodeList.item(i) == null)
				continue;
			if (subNodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = subNodeList.item(i); // request or response node
			if(node.getNodeName().equals("header")) {
				NodeList subSubNodeList = node.getChildNodes(); // field node
				for (int j = 0; j < subSubNodeList.getLength(); j++) {
					if (subSubNodeList.item(j) == null)
						continue;
					if (subSubNodeList.item(j).getNodeType() == Node.TEXT_NODE)
						continue;
					Node fieldNode = subSubNodeList.item(j); // field node
					headerInfo = parser.parseHeaderField(headerInfo, fieldNode,isRequest);
				}
			}
		}
		return headerInfo;
	}

}
