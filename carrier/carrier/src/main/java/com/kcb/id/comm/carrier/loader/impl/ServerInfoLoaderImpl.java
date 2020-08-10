package com.kcb.id.comm.carrier.loader.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.ServerInfoLoader;
import com.kcb.id.comm.carrier.parser.ServerInfoParser;

@Component
public class ServerInfoLoaderImpl extends LoaderImpl  implements ServerInfoLoader {

	static Logger logger = LoggerFactory.getLogger(MessageInfoLoaderImpl.class);
	@Autowired
	ServerInfoParser parser;
	Map<String, ServerInfo> serverRepository = new HashMap<>();

	public ServerInfoParser getParser() {
		return parser;
	}

	public void setParser(ServerInfoParser parser) {
		this.parser = parser;
	}

	public Map<String, ServerInfo> getServerRepository() {
		return serverRepository;
	}

	public void setServerRepository(Map<String, ServerInfo> serverRepository) {
		this.serverRepository = serverRepository;
	}

	@Override
	public void parseMe(NodeList nodeList){
		try {
			List<ServerInfo> list = this.getParser().parse(nodeList);
			if (list != null && list.size() > 0) {
				list.forEach(server->{
					this.getServerRepository().put(server.getName(),server);
				});
			} 
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}

}