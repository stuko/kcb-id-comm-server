package com.kcb.id.comm.carrier.loader;

public interface ServerInfo extends SelfChecker{
	void setPort(int port);
	int getPort();
	void setIP(String ip);
	String getIP();
	void setHandlerName(String handlerName);
	String getHandlerName();
	void setName(String name);
	String getName();
}
