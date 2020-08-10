package com.kcb.id.comm.carrier.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CarrierClientCallerImpl extends APIClientCallerImpl {

	@Value( "${carrier.client.url}" )
	private String clientUrl;
	
	@Override
	public String getClientUrl() {
		return clientUrl;
	}
}
