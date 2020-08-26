package com.kcb.id.comm.carrier.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.mybatis.mapper.CarrierMapper;
import com.kcb.id.comm.carrier.service.IService;

@Service
@Primary
@MapperScan(basePackages="com.kcb.id.comm.carrier.mybatis.mapper")
@Scope("prototype")
public class SampleServiceImpl implements IService{

	static Logger logger = LoggerFactory.getLogger(SampleServiceImpl.class);
	
	@Autowired
	CarrierMapper carrierMapper;
	
	@PostConstruct
    public void init() {
    }
	
	@Override
	public Object call(Map<String, Object> param) {
		Map<String,Object> m = new HashMap<>();
		m.put("ID",NettyUtils.genId());
		m.put("NAME",NettyUtils.genId("NAME_"));
		// carrierMapper.insertData(m);
		m.put("GUBUN","R");
		m.put("SSN","12345678");
		System.out.println("############# Call service");
		return m;
	}

}
