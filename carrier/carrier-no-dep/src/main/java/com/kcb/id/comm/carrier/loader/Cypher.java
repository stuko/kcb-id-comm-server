package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface Cypher {
	String encode(String data , Map<String,Object> msg);
	String decode(String data , Map<String,Object> msg);
}
