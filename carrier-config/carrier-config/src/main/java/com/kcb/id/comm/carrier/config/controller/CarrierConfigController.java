package com.kcb.id.comm.carrier.config.controller;

import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class CarrierConfigController {
	static Logger logger = LoggerFactory.getLogger(CarrierConfigController.class);
	
	@RequestMapping(value="/servers", method=RequestMethod.GET)
	@ResponseBody
	public String servers() {
		try {
			File file = ResourceUtils.getFile("classpath:config/servers.xml");
			String content = new String(Files.readAllBytes(file.toPath()));
			return content;
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	    return "";
	}

	@RequestMapping(value="/messages", method=RequestMethod.GET)
	@ResponseBody
	public String messages() {
		try {
			File file = ResourceUtils.getFile("classpath:config/messages.xml");
			String content = new String(Files.readAllBytes(file.toPath()));
			return content;			
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	    return "";
	}

	@RequestMapping(value="/handlers", method=RequestMethod.GET)
	@ResponseBody
	public String handlers() {
		try {
			File file = ResourceUtils.getFile("classpath:config/handlers.xml");
			String content = new String(Files.readAllBytes(file.toPath()));
			return content;			
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	    return "";
	}
	
}
