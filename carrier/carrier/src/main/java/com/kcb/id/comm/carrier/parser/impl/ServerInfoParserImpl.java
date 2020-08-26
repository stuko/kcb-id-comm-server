package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.impl.ServerInfoImpl;
import com.kcb.id.comm.carrier.parser.ServerInfoParser;

@Component
@Scope("prototype")
public class ServerInfoParserImpl implements ServerInfoParser {

	static Logger logger = LoggerFactory.getLogger(ServerInfoParserImpl.class);
	
	@Override
	public List<ServerInfo> parse(NodeList nodeList) throws Exception {
		List<ServerInfo> list = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = nodeList.item(i);
			NodeList subNodeList = node.getChildNodes();
			FieldParser parser = null;
			for (int j = 0; j < subNodeList.getLength(); j++) {
				if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
					continue;
				if (subNodeList.item(j).getNodeName().equals("server")) {
					ServerInfo serverInfo = new ServerInfoImpl();
					String serverName = subNodeList.item(j).getAttributes().getNamedItem("serverName").getNodeValue();
					serverInfo.setName(serverName);
					serverInfo.setIP(subNodeList.item(j).getAttributes().getNamedItem("ip").getNodeValue());
					String port = subNodeList.item(j).getAttributes().getNamedItem("port").getNodeValue();
					if(port != null) serverInfo.setPort(Integer.parseInt(port));
					logger.debug("Server info is {}:{}", serverInfo.getIP(), serverInfo.getPort());
					serverInfo.setHandlerName(subNodeList.item(j).getAttributes().getNamedItem("handlerName").getNodeValue());
					
					if(subNodeList.item(j).hasChildNodes()) {
						NodeList subSubNodeList = subNodeList.item(j).getChildNodes();
						for(int k = 0; k < subSubNodeList.getLength(); k++) {
							if (subSubNodeList.item(k).getNodeType() == Node.TEXT_NODE)
								continue;
							if (subSubNodeList.item(k).getNodeName().equals("error")) {
								logger.debug("error node exists");
								String exception = subSubNodeList.item(k).getAttributes().getNamedItem("name").getNodeValue();
								parser = new FieldParser();
								serverInfo.getExceptionMessageMap().put(exception, parser.parseFields(subSubNodeList.item(k),false));
							}
						}
					}
					
					if(serverInfo.checkMe()) {
						list.add(serverInfo);
					}
				}
			}
		}
		return list;
	}
}
