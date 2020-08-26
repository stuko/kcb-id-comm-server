package com.kcb.id.comm.carrier.parser;

import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ErrorInfo;

public interface ErrorInfoParser {
	ErrorInfo parse(NodeList nodeList) throws Exception;
}
