package com.kcb.id.comm.carrier.parser;

import java.util.List;

import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ServerInfo;

public interface ServerInfoParser {
	List<ServerInfo> parse(NodeList nodeList) throws Exception;
}
