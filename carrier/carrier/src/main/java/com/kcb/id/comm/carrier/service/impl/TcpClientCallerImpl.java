package com.kcb.id.comm.carrier.service.impl;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.service.IService;

@Component
@Scope("prototype")
public class TcpClientCallerImpl implements IService{

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
