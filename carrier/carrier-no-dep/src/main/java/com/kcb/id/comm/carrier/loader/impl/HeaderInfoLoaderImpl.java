package com.kcb.id.comm.carrier.loader.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HeaderInfo;
import com.kcb.id.comm.carrier.loader.HeaderInfoLoader;
import com.kcb.id.comm.carrier.parser.HeaderInfoParser;

public class HeaderInfoLoaderImpl extends LoaderImpl  implements HeaderInfoLoader{

	static Logger logger = LoggerFactory.getLogger(HeaderInfoLoaderImpl.class);
	@Autowired
	HeaderInfoParser parser;
	
	HeaderInfo headerInfo;
	
	@Override
	public void parseMe(NodeList nodeList) {
		try {
			headerInfo = this.getParser().parse(nodeList);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}

	public HeaderInfoParser getParser() {
		return parser;
	}

	public void setParser(HeaderInfoParser parser) {
		this.parser = parser;
	}

	public HeaderInfo getHeaderInfo() {
		return headerInfo;
	}

	public void setHeaderInfo(HeaderInfo headerInfo) {
		this.headerInfo = headerInfo;
	}

	
}