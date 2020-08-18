package com.kcb.id.comm.carrier.loader.impl;

import com.kcb.id.comm.carrier.loader.Forward;

public class ForwardImpl implements Forward{
	
	String forward;
	String forwardIp;
	int  forwardPort;
	
	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}

	public String getForwardIp() {
		return forwardIp;
	}

	public void setForwardIp(String forwardIp) {
		this.forwardIp = forwardIp;
	}

	public int getForwardPort() {
		return forwardPort;
	}

	public void setForwardPort(int forwardPort) {
		this.forwardPort = forwardPort;
	}

}
