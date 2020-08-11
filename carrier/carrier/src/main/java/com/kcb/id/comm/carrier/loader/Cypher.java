package com.kcb.id.comm.carrier.loader;

public interface Cypher {
	void setMessage(Message message);
	Message getMessage();
	String encode(String data);
	String decode(String data);
}
