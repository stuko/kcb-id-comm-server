package com.kcb.id.comm.carrier.service.impl;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.service.Service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Configuration
@Component
public class APIClientCallerImpl implements Service{

	static Logger logger = LoggerFactory.getLogger(APIClientCallerImpl.class);
	
	private String clientUrl;
	
	Type type = Type.JSON;
	
	Gson gson = new Gson();
	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Override
	public Object call(Map<String, Object> param) {
		try {
			return this.post(getClientUrl(), gson.toJson(param));
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

	public String getClientUrl() {
		return clientUrl;
	}

	public void setClientUrl(String clientUrl) {
		this.clientUrl = clientUrl;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
}
