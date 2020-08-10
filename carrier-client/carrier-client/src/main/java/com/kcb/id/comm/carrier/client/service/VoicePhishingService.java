package com.kcb.id.comm.carrier.client.service;

import java.util.Map;

public interface VoicePhishingService {
	String call(Map<String,Object>  param, Map<String,Object> requestMap);
}
