package com.kcb.id.comm.carrier.service.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.service.Service;

@Component
public class TcpClientCallerImpl implements Service{

	Type type = Type.TCP;
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public Object call(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return null;
	}

}
