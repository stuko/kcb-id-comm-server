package com.kcb.id.comm.carrier.client.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.client.service.VoicePhishingService;

@RestController
@RequestMapping("/")
public class CarrierClientController {
	
	static Logger logger = LoggerFactory.getLogger(CarrierClientController.class);
	Map<String,Object> vpRequestJsonMap = new HashMap<>();
	
	@Autowired
	VoicePhishingService vpService;
	
	@PostConstruct
	public void init() {
		try {
			File file = ResourceUtils.getFile("classpath:config/vp/request.json");
			String vpRequestJson = new String(Files.readAllBytes(file.toPath()));
			Gson gson = new Gson();
			vpRequestJsonMap = gson.fromJson(vpRequestJson, vpRequestJsonMap.getClass());
		}catch(Exception e) {
			logger.error(e.toString(),e);
		}
	}
	
	@RequestMapping(value="/json/voicePhishing",  method = {RequestMethod.GET, RequestMethod.POST} )
	@ResponseBody
	public String jsonVoicePhishing(@RequestBody String payload) {
		try {
			Gson gson = new Gson();
			Map<String,Object> map = new HashMap<>();
			return callVoicePishing(gson.fromJson(payload,map.getClass()));
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	    return "";
	}

	@RequestMapping(value="/post/voicePhishing",  method = {RequestMethod.GET, RequestMethod.POST} )
	@ResponseBody
	public String postVoicePhishing(@RequestBody Map<String,Object> payload) {
		try {
			return callVoicePishing(payload);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	    return "";
	}

	private String callVoicePishing(Map<String,Object> param) {
		return vpService.call(param,this.vpRequestJsonMap);
	}
	
}
