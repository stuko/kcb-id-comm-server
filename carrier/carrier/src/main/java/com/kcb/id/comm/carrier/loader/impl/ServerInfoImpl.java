package com.kcb.id.comm.carrier.loader.impl;

import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.ServerInfo;

@Component
public class ServerInfoImpl implements ServerInfo {

	int port = -1;
	String IP;
	String handlerName;
	String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	@Override
	public boolean checkMe() {
		if(this.getPort() == -1) return false;
		if(this.getIP() == null) return false;
		return true;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

}
