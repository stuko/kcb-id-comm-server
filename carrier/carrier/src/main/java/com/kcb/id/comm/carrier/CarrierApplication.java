package com.kcb.id.comm.carrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.kcb.id.comm.carrier.core.Carrier;

/*
 * 스프링 부트
 * 최초 실행 프로그램
 */
@SpringBootApplication
public class CarrierApplication {

	static Logger logger = LoggerFactory.getLogger(CarrierApplication.class);

	/*
	 * CarrierImpl 클래스를 자동으로 생성 등록 해 준다.
	 */
	@Autowired
	Carrier carrier;

	public static void main(String[] args) {
		SpringApplication.run(CarrierApplication.class, args);
	}

	/*
	 * 스프링 부트 실행후, Netty 통신 서버를 실행하기 위해 필요한 메서드
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		init();
	}

	public void init() {
		try {
			carrier.startAll();
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}
}
