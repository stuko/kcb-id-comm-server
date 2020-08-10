package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.HandlerInfoLoader;
import com.kcb.id.comm.carrier.parser.HandlerInfoParser;

@Component
public class HandlerInfoLoaderImpl extends LoaderImpl implements HandlerInfoLoader {

	@Autowired
	HandlerInfoParser parser;
	Map<String, HandlerInfo> handlerRepository = new HashMap<>();
	
	public HandlerInfoParser getParser() {
		return parser;
	}

	public void setParser(HandlerInfoParser parser) {
		this.parser = parser;
	}
	@Override
	public Map<String, HandlerInfo> getHandlerRepository() {
		return handlerRepository;
	}

	@Override
	public void setHandlerRepository(Map<String, HandlerInfo> repo) {
		handlerRepository = repo;
	}

	@Override
	public void parseMe(NodeList nodeList) {
		try {
			List<HandlerInfo> list = this.getParser().parse(nodeList);
			if (list != null && list.size() > 0) {
				list.forEach(handler->{
					this.getHandlerRepository().put(handler.getName(),handler);
				});
			} 
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}

}
