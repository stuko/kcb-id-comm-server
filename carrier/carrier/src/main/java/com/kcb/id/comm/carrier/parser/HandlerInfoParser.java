package com.kcb.id.comm.carrier.parser;

import java.util.List;

import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HandlerInfo;

public interface HandlerInfoParser {
	List<HandlerInfo> parse(NodeList nodeList);
}
