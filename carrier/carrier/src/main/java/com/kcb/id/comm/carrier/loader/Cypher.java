package com.kcb.id.comm.carrier.loader;

public interface Cypher {
	String encode(String data , Message msg);
	String decode(String data , Message msg);
}
