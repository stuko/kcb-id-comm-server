package com.kcb.id.comm.carrier.parser.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ErrorInfo;
import com.kcb.id.comm.carrier.loader.impl.ErrorInfoImpl;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.parser.ErrorInfoParser;

public class ErrorInfoParserImpl implements ErrorInfoParser{

	static Logger logger = LoggerFactory.getLogger(ErrorInfoParserImpl.class);

	public ErrorInfo parse(NodeList nodeList) throws Exception{
		ErrorInfo errorInfo = null;
		NodeList setNodeList = nodeList.item(0).getChildNodes();
		for (int i = 0; i < setNodeList.getLength(); i++) {
			if (setNodeList.item(i) == null)
				continue;
			if (setNodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = setNodeList.item(i);
			if(node.hasChildNodes()) {
				NodeList subNodeList = node.getChildNodes();
				for(int j = 0; j < subNodeList.getLength(); j++) {
					if (subNodeList.item(j) == null)
						continue;
					if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
						continue;
					Node subNode = subNodeList.item(j);
					if(subNode.getNodeName().equals("headers")) {
						errorInfo = getErrorInfo(subNodeList, j ,true);				
					}else if(subNode.getNodeName().equals("messages")) {
						errorInfo = getErrorInfo(subNodeList, j ,false);
					}
					
				}
			}
		}
			
		return errorInfo;
	}

	private ErrorInfo getErrorInfo(NodeList subNodeList, int j , boolean isHeader) throws Exception{
		ErrorInfo errorInfo = new ErrorInfoImpl();
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
			errorInfo = parser.parseErrorField(errorInfo, subSubNode , isHeader);
		}
		return errorInfo;
	}

}
