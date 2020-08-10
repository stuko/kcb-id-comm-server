package com.kcb.id.comm.carrier.core;

import com.kcb.id.comm.carrier.loader.HandlerInfoLoader;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;
import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.ServerInfoLoader;

public interface Carrier {
	
	void setBoss(int count);
	int getBoss();
	
	void setWorker(int count);
	int getWorker();
	
	void setMessageInfoLoader(MessageInfoLoader loader);
	MessageInfoLoader getMessageInfoLoader();
	
	void setServerInfoLoader(ServerInfoLoader loader);
	ServerInfoLoader getServerInfoLoader();
	
	void setHandlerInfoLoader(HandlerInfoLoader handler);
	HandlerInfoLoader getHandlerInfoLoader();
	
	void start(ServerInfo server);
	void stop(ServerInfo server);
	String status();
	
	void startAll();
	void stopAll();
	
}
