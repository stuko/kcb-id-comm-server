package com.kcb.id.comm.carrier.loader.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.loader.Cypher;
import com.kcb.id.comm.carrier.loader.Message;

@Component
@Scope("prototype")
public class CypherImpl implements Cypher{
	
	Message message;
	
	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	@Override
	public String encode(String data) {
		// 암호화
		// this.getMessage().getBody("",0);
		// this.getMessage().getHeader("");
		// this.getMessage().getTail("");
		return null;
	}

	@Override
	public String decode(String data) {
		// 복호화
		// this.getMessage().getBody("",0);
		// this.getMessage().getHeader("");
		// this.getMessage().getTail("");
		return null;
	}

}
