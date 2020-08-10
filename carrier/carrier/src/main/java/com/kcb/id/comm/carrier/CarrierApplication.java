package com.kcb.id.comm.carrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.kcb.id.comm.carrier.core.Carrier;

@SpringBootApplication
public class CarrierApplication {

	static Logger logger = LoggerFactory.getLogger(CarrierApplication.class);
	
	@Autowired
	Carrier carrier;
	
	public static void main(String[] args) {
		SpringApplication.run(CarrierApplication.class, args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
	    init();
	}
	
	public void init() {
		try {
			carrier.startAll();
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}
}
