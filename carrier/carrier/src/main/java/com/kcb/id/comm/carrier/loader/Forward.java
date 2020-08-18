package com.kcb.id.comm.carrier.loader;

public interface Forward {
	void setForward(String msgName);
	String getForward();
	
	void setForwardIp(String ip);
	String getForwardIp();
	
	void setForwardPort(int port);
	int getForwardPort();
}
