package com.kcb.id.comm.carrier.parser;

import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HeaderInfo;

public interface HeaderInfoParser {
	HeaderInfo parse(NodeList nodeList) throws Exception;
}
