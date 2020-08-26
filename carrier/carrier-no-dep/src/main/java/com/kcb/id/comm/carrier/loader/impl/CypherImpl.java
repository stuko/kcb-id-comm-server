package com.kcb.id.comm.carrier.loader.impl;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.Cypher;

@Component
@Scope("prototype")
public class CypherImpl implements Cypher{
	
	@Override
	public String encode(String data , Map<String,Object> msg) {
		// 암호화
		// msg.getBody("");
		// msg.getHeader("");
		// msg.getTail("");
		return null;
	}

	@Override
	public String decode(String data , Map<String,Object> msg) {
		// 복호화
		// msg.getBody("");
		// msg.getHeader("");
		// msg.getTail("");
		return null;
	}

}
