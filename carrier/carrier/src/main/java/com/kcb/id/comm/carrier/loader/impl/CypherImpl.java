package com.kcb.id.comm.carrier.loader.impl;

import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.Cypher;

@Component
public class CypherImpl implements Cypher{

	@Override
	public String encode(String data) {
		// 암호화
		return null;
	}

	@Override
	public String decode(String data) {
		// 복호화
		return null;
	}

}
