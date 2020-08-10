package com.kcb.id.comm.carrier.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
@RequestMapping("/")
public class CarrierTestController {
	static Logger logger = LoggerFactory.getLogger(CarrierTestController.class);
	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
		try {
			

		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		return "";
	}
}
