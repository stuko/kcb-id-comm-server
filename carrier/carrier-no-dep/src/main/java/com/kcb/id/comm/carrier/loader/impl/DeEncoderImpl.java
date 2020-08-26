package com.kcb.id.comm.carrier.loader.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.kcb.id.comm.carrier.loader.Cypher;
import com.kcb.id.comm.carrier.loader.DeEncoder;

public class DeEncoderImpl implements DeEncoder {

	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
		
	
	@Override
	public Field decodeAndEncode(Field f, Map<String,Object> msg) throws Exception {
		try {
			if(f.getDecoder() != null && !"".equals(f.getDecoder())) {
				Object object = context.getBean(f.getDecoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					f.setValue(cypher.decode(f.getValue(),msg));
				}else {
					throw new Exception("DecoderBeanDoesNotExistException ["+f.getDecoder()+"]");
				}
			}
			if(f.getEncoder() != null && !"".equals(f.getEncoder())) {
				Object object = context.getBean(f.getEncoder());
				if(object != null) {
					Cypher cypher = (Cypher)object;
					f.setValue(cypher.encode(f.getValue(),msg));
				}else {
					throw new Exception("EncoderBeanDoesNotExistException ["+f.getEncoder()+"]");
				}
			}

		}catch(Exception e) {
			throw new Exception("NoDecoderOrEncoderException",e);
		}
		return f;
	}

}
