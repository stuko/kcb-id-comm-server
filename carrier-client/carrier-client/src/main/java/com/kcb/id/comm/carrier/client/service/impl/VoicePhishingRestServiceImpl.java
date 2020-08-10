package com.kcb.id.comm.carrier.client.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.client.common.JsonUtils;
import com.kcb.id.comm.carrier.client.service.VoicePhishingService;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Configuration
@Service
public class VoicePhishingRestServiceImpl implements VoicePhishingService {

	static Logger logger = LoggerFactory.getLogger(VoicePhishingRestServiceImpl.class);
	
	@Value("${vp.server.url}")
	private String vpUrl;
	Gson gson = new Gson();
	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Override
	public String call(Map<String, Object> param, Map<String, Object> requestMap) {
		Map<String, Object> payload = new HashMap<>();
		payload = JsonUtils.getPayLoad(payload, param, requestMap);
		try {
			return this.post(vpUrl, gson.toJson(payload));
		} catch (Exception e) {
			logger.error(e.toString(),e);
			return "";
		}
	}

	private String post(String url, String json) throws IOException {
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(json, JSON);
		Request request = new Request.Builder().url(url).post(body).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

}
