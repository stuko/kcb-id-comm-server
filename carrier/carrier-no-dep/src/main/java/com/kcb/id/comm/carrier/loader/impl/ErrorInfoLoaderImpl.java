package com.kcb.id.comm.carrier.loader.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ErrorInfo;
import com.kcb.id.comm.carrier.loader.ErrorInfoLoader;
import com.kcb.id.comm.carrier.parser.ErrorInfoParser;

public class ErrorInfoLoaderImpl extends LoaderImpl  implements ErrorInfoLoader{

	static Logger logger = LoggerFactory.getLogger(ErrorInfoLoaderImpl.class);
	@Autowired
	ErrorInfoParser parser;
	
	ErrorInfo errorInfo;
	
	@Override
	public void parseMe(NodeList nodeList) {
		try {
			errorInfo = this.getParser().parse(nodeList);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}

	public ErrorInfo getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(ErrorInfo errorInfo) {
		this.errorInfo = errorInfo;
	}

	public ErrorInfoParser getParser() {
		return parser;
	}

	public void setParser(ErrorInfoParser parser) {
		this.parser = parser;
	}
	
}
