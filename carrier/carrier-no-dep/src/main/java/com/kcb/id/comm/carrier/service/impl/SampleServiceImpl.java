package com.kcb.id.comm.carrier.service.impl;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kcb.id.comm.carrier.common.ByteUtils;
import com.kcb.id.comm.carrier.service.IService;

@Service
@Primary
@Scope("prototype")
public class SampleServiceImpl implements IService{

	static Logger logger = LoggerFactory.getLogger(SampleServiceImpl.class);
	
	@Override
	public Object call(Map<String, Object> param) {
		Map<String,Object> m = new HashMap<>();
		m.put("ID",ByteUtils.genId());
		m.put("NAME",ByteUtils.genId("NAME_"));
		m.put("GUBUN","R");
		m.put("SSN","12345678");
		System.out.println("############# Call service");
		return m;
	}

}
