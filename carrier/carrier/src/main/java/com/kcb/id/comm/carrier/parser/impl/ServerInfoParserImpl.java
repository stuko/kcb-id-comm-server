package com.kcb.id.comm.carrier.parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.impl.ServerInfoImpl;
import com.kcb.id.comm.carrier.parser.ServerInfoParser;

@Component
public class ServerInfoParserImpl implements ServerInfoParser {

	@Override
	public List<ServerInfo> parse(NodeList nodeList) {
		List<ServerInfo> list = new ArrayList<>();
		ServerInfo serverInfo = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE)
				continue;
			Node node = nodeList.item(i);
			NodeList subNodeList = node.getChildNodes();
			for (int j = 0; j < subNodeList.getLength(); j++) {
				if (subNodeList.item(j).getNodeType() == Node.TEXT_NODE)
					continue;
				if (subNodeList.item(j).getNodeName().equals("server")) {
					String serverName = subNodeList.item(j).getAttributes().getNamedItem("serverName").getNodeValue();
					serverInfo = new ServerInfoImpl();
					serverInfo.setName(serverName);
					serverInfo.setIP(subNodeList.item(j).getAttributes().getNamedItem("ip").getNodeValue());
					String port = subNodeList.item(j).getAttributes().getNamedItem("port").getNodeValue();
					if(port != null) serverInfo.setPort(Integer.parseInt(port));
					serverInfo.setHandlerName(subNodeList.item(j).getAttributes().getNamedItem("handlerName").getNodeValue());
					if(serverInfo.checkMe()) {
						list.add(serverInfo);
					}
				}
			}
		}
		return list;
	}
}
