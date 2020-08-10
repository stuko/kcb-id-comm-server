package com.kcb.id.comm.carrier.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.service.Service;

@Component
public class SampleServiceImpl implements Service{

	Type type = Type.MAP;
	
	@Override
	public Object call(Map<String, Object> param) {
		Map<String,Object> m = new HashMap<>();
		m.put("GUBUN","TEST");
		m.put("SSN","asdfasdf");
		return m;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
