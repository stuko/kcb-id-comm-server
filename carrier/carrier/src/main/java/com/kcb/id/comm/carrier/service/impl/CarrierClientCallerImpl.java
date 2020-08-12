package com.kcb.id.comm.carrier.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CarrierClientCallerImpl extends APIClientCallerImpl {

	@Value( "${carrier.client.url}" )
	private String clientUrl;
	
	@Override
	public String getClientUrl() {
		return clientUrl;
	}
}
