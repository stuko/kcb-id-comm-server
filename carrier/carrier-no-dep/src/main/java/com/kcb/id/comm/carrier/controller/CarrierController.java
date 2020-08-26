package com.kcb.id.comm.carrier.controller;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kcb.id.comm.carrier.common.ByteUtils;
import com.kcb.id.comm.carrier.core.Carrier;
import com.kcb.id.comm.carrier.forward.ForwardHelper;
import com.kcb.id.comm.carrier.loader.DeEncoder;
import com.kcb.id.comm.carrier.loader.HeaderInfo;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.service.IService;

@RestController
public class CarrierController {
	
	static Logger logger = LoggerFactory.getLogger(CarrierController.class);
	
	@Autowired
	Carrier carrier;

	@Autowired
	ForwardHelper forwardHelper;
	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
	
	@RequestMapping(value="/request" , method= RequestMethod.POST)
	public byte[] request(@RequestBody byte[] msg) throws Exception{
		byte[] response = null;
		try {
			MessageInfo messageInfo = this.parseRequestMessage(msg);
			if(messageInfo == null) throw new Exception("MessageInfoErrorException");
		
			String preBean = messageInfo.getRequestMessage().getPreBean();
			Map<String,Object> bodyMap = messageInfo.getRequestMessage().getBodyMap();
			Map<String,Object> result = this.execute(preBean,bodyMap);
			if(result != null) bodyMap.putAll(result);
			
			boolean isForward = false;
			
			String[] info = forwardHelper.findForwardInfo(bodyMap);
			if(info != null && info.length == 3) {
				messageInfo.getRequestMessage().setForwardIp(info[0]);
				messageInfo.getRequestMessage().setForwardPort(info[1]);
				messageInfo.getRequestMessage().setForwardTimeOut(info[2]);
			}
			
			if(messageInfo.getRequestMessage().getForwardIp() != null
			&& "".equals(messageInfo.getRequestMessage().getForwardIp())
			&& messageInfo.getRequestMessage().getForwardPort() != null
			&& "".equals(messageInfo.getRequestMessage().getForwardPort())
			&& messageInfo.getRequestMessage().getForwardTimeOut() != null
			&& "".equals(messageInfo.getRequestMessage().getForwardTimeOut())
			) {
				isForward = true;
			}
			
			byte[] forwardResponse = null;
			
			if(messageInfo.getRequestMessage().getForwardMessageName() != null
			&& "".equals(messageInfo.getRequestMessage().getForwardMessageName())) {
				MessageInfo forward = carrier.getMessageInfoLoader().getMessageRepository().get(messageInfo.getRequestMessage().getForwardMessageName());
				if(forward == null) throw new Exception("ForwardMessageCanNotFindException ["+messageInfo.getRequestMessage().getForwardMessageName()+"]");
				byte[] forwardBytes = ByteUtils.getMessage2Byte(forward.getRequestMessage(),bodyMap);
				if(isForward) {
					forwardResponse = ByteUtils.send(messageInfo.getRequestMessage().getForwardIp()
							, Integer.parseInt(messageInfo.getRequestMessage().getForwardPort())
							, Integer.parseInt(messageInfo.getRequestMessage().getForwardTimeOut())
							, forwardBytes);
				}
			}else {
				if(isForward) {
					forwardResponse = ByteUtils.send(messageInfo.getRequestMessage().getForwardIp()
						, Integer.parseInt(messageInfo.getRequestMessage().getForwardPort())
						, Integer.parseInt(messageInfo.getRequestMessage().getForwardTimeOut())
						, msg);
				}
			}

			if(forwardResponse != null) {
				MessageInfo forwardResult = parseResponseMessage(forwardResponse
						,carrier.getMessageInfoLoader().getMessageRepository().get(messageInfo.getRequestMessage().getForwardMessageName()));
				bodyMap.putAll(forwardResult.getResponseMessage().getBodyMap());
			}
				
			String postBean = messageInfo.getRequestMessage().getPostBean();
			result = this.execute(preBean,bodyMap);
			if(result != null) bodyMap.putAll(result);
				
			response = ByteUtils.getMessage2Byte(messageInfo.getResponseMessage(),bodyMap);
			
		}catch(Exception e) {
			response = makeErrorResponse(e);
		}
		return response;
	}
	
	private Map<String,Object> execute(String beanName,Map<String,Object> bodyMap) throws Exception {
		if(beanName != null && !"".equals(beanName)) {
			IService service = (IService) context.getBean(beanName);
			if(service == null)throw new Exception("ServiceBeanDoesNotExistException ["+beanName+"]");
			Object result = service.call(bodyMap);
			if(result != null) {
				return (Map<String,Object>)result;
			}
		}
		return null;
	}
	
	private byte[] makeErrorResponse(Exception e) {
		return null;
	}

	public MessageInfo parseRequestMessage(byte[] in) throws Exception {

		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		int msgLength = 0;
		String msgCode = "";
		// 최초 메시지의 앞 4자리를 읽어야 하는 경우
		byte[] byteLength = new byte[4];
		bais.read(byteLength);
		try {
			msgLength = Integer.valueOf(new String(byteLength).trim());
		}catch(Exception e) {
			throw new Exception("MessageLengthNumberFormatException",e);
		}
		if(bais.available() < msgLength) {
			throw new Exception("MessageLengthNotEqualException ["+bais.available()+"] != ["+msgLength+"]");
		}
		byte[] remains = bais.readAllBytes();
		
		HeaderInfo headerInfo = carrier.getHeaderInfoLoader().getHeaderInfo().newInstance();
		Field headers[] = headerInfo.getRequestHeader();
		
		// 하드코딩 ^^
		headers[0].setValue(in.length+"");  // 메시지 전체 사이트 (길이 정보 필드 안포함?) 
		for (int i = 1; i < headers.length; i++) {
			Field f = headers[i];
			byte[] buf = new byte[Integer.parseInt(f.getLength())];
			bais.read(buf);
			String value = new String(buf);
			f.setValue(value);
			f = ((DeEncoder)headerInfo).decodeAndEncode(f,headerInfo.toHashMap());
			logger.debug("header's name: {} , value: [{}]", f.getName() , f.getValue());
		}
		// 하드코딩 ^^
		String kindCode = headerInfo.getRequestKindCodeValue();
		if(kindCode == null || "".equals(kindCode)) throw new Exception("HeaderDoesNotContainsKindCodeException");
		// 하드코딩 ^^
		byte[] msgCodeBytes = new byte[4];
		bais.read(msgCodeBytes);
		String messageCode = new String(msgCodeBytes);
		MessageInfo findMessageInfo = carrier.getMessageInfoLoader().getMessageInfo(kindCode, messageCode);
		if(findMessageInfo == null) throw new Exception("CanNotFindMessageException");
		MessageInfo newMsg = findMessageInfo.newInstance();
		try {
			// 이미 읽은 헤더 부분
			newMsg.getRequestMessage().setHeader(headers);
			// 이미 읽은 메시지 코드
			newMsg.getRequestMessage().getBody()[0].setValue(messageCode);
			
			// 이제부터는 새로운 메시지 newMsg 정보를 사용해야 함.
			Field[] header = newMsg.getRequestMessage().getHeader();
			Field[] body = newMsg.getRequestMessage().getBody();
			Field[] tail = newMsg.getRequestMessage().getTail();

			for (int i = 1; i < body.length; i++) {
				Field f = body[i];
				byte[] buf = new byte[newMsg.getRequestMessage().getLength(f,newMsg.getRequestMessage())];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				newMsg.getRequestMessage().decodeAndEncode(f);
				logger.debug("body's name: {} , value: [{}]", f.getName() , f.getValue());
			}

			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				byte[] buf = new byte[newMsg.getRequestMessage().getLength(f,newMsg.getRequestMessage())];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				newMsg.getRequestMessage().decodeAndEncode(f);
				logger.debug("tail's name: {} , value: [{}]", f.getName() , f.getValue());				
			}
			bais.close();
			bais = null;
		} catch (Exception e) {
			throw new Exception("MessageParseException [" + e.getStackTrace()[0].getClassName() + ":" + e.getStackTrace()[0].getLineNumber() + "]", e);
		} finally {
			try {if (bais != null)bais.close();} catch (Exception e) {}
		}
		return newMsg;
	}
	
	public MessageInfo parseResponseMessage(byte[] in , MessageInfo responseMessageInfo) throws Exception {

		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		int msgLength = 0;
		String msgCode = "";
		// 최초 메시지의 앞 4자리를 읽어야 하는 경우
		byte[] byteLength = new byte[4];
		bais.read(byteLength);
		try {
			msgLength = Integer.valueOf(new String(byteLength).trim());
		}catch(Exception e) {
			throw new Exception("ForwardMessageLengthNumberFormatException",e);
		}
		if(bais.available() < msgLength) {
			throw new Exception("ForwardMessageLengthNotEqualException ["+bais.available()+"] != ["+msgLength+"]");
		}
		byte[] remains = bais.readAllBytes();
		
		HeaderInfo headerInfo = carrier.getHeaderInfoLoader().getHeaderInfo().newInstance();
		Field headers[] = headerInfo.getResponseHeader();
		
		// 하드코딩 ^^
		headers[0].setValue(in.length+"");  // 메시지 전체 사이트 (길이 정보 필드 안포함?) 
		for (int i = 1; i < headers.length; i++) {
			Field f = headers[i];
			byte[] buf = new byte[Integer.parseInt(f.getLength())];
			bais.read(buf);
			String value = new String(buf);
			f.setValue(value);
			f = ((DeEncoder)headerInfo).decodeAndEncode(f,headerInfo.toHashMap());
			logger.debug("header's name: {} , value: [{}]", f.getName() , f.getValue());
		}
		MessageInfo newMsg = responseMessageInfo.newInstance();
		try {
			// 이미 읽은 헤더 부분
			newMsg.getResponseMessage().setHeader(headers);
			// 이제부터는 새로운 메시지 newMsg 정보를 사용해야 함.
			Field[] header = newMsg.getResponseMessage().getHeader();
			Field[] body = newMsg.getResponseMessage().getBody();
			Field[] tail = newMsg.getResponseMessage().getTail();

			for (int i = 0; i < body.length; i++) {
				Field f = body[i];
				byte[] buf = new byte[newMsg.getResponseMessage().getLength(f,newMsg.getResponseMessage())];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				newMsg.getResponseMessage().decodeAndEncode(f);
				logger.debug("body's name: {} , value: [{}]", f.getName() , f.getValue());
			}

			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				byte[] buf = new byte[newMsg.getResponseMessage().getLength(f,newMsg.getResponseMessage())];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				newMsg.getResponseMessage().decodeAndEncode(f);
				logger.debug("tail's name: {} , value: [{}]", f.getName() , f.getValue());				
			}
			bais.close();
			bais = null;
		} catch (Exception e) {
			throw new Exception("MessageParseException [" + e.getStackTrace()[0].getClassName() + ":" + e.getStackTrace()[0].getLineNumber() + "]", e);
		} finally {
			try {if (bais != null)bais.close();} catch (Exception e) {}
		}
		return newMsg;
	}
}
