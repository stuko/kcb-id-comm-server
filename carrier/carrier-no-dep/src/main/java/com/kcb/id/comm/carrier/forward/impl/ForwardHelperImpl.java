package com.kcb.id.comm.carrier.forward.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.forward.ForwardHelper;

@Component
public class ForwardHelperImpl implements ForwardHelper {

	@Override
	public String[] findForwardInfo(Map<String, Object> bodyMap) throws Exception {
		String[] result = new String[3];
		result[0] = "IP";
		result[1] = "PORT";
		result[2] = "TIMEOUT";
		return result;
	}

}
