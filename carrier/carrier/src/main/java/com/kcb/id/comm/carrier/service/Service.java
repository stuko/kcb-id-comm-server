package com.kcb.id.comm.carrier.service;

import java.util.Map;

public interface Service {
	enum Type{JSON, MAP, TCP}
	Type getType();
	void setType(Type type);
	Object call(Map<String,Object> param);
}
