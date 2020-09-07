package com.kcb.id.comm.carrier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/*
 * 스프링 부트
 * 최초 실행 프로그램
 */
@SpringBootApplication
public class CarrierApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		System.out.println("###########################################");
        return application.sources(CarrierApplication.class);
	}

	public static void main(String[] args) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        SpringApplication.run(CarrierApplication.class, args);
	}

}