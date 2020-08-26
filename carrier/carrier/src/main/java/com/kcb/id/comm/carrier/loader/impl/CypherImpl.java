package com.kcb.id.comm.carrier.loader.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.Cypher;
import com.kcb.id.comm.carrier.loader.Message;

@Component
@Scope("prototype")
public class CypherImpl implements Cypher{
	
	@Override
	public String encode(String data , Message msg) {
		// 암호화
		// msg.getBody("");
		// msg.getHeader("");
		// msg.getTail("");
		return null;
	}

	@Override
	public String decode(String data , Message msg) {
		// 복호화
		// msg.getBody("");
		// msg.getHeader("");
		// msg.getTail("");
		return null;
	}

}
