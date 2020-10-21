package com.kcb.id.comm.carrier.handler.impl;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.common.StringUtils;
import com.kcb.id.comm.carrier.handler.Handler;
import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.service.IService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

@Configuration
@Component
@Scope("prototype")
public class NettyServerChannelHandlerImpl implements Handler {

	static Logger logger = LoggerFactory.getLogger(NettyServerChannelHandlerImpl.class);

	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;

	IService service;

	@Override
	public void onReceived(ChannelHandlerContext ctx, byte[] msg, Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		this.receive(ctx, msg, messageRepository, handler);
	}
	
	private void receive(ChannelHandlerContext ctx, byte[] msg, Map<String, MessageInfo> messageRepository,	HandlerInfo handler) {
		MessageInfo messageInfo = null;
		try {
			messageInfo = messageRepository.get(handler.getMessageName());
			// 요청 전문 파싱
			MessageInfo parsedMsg = this.parseMessage(msg, true, messageInfo);
			
			// 서비스가 있으면 실행 한다.
			Map<String,Object> resultMap = executeService(handler, parsedMsg);
			
			byte[] forwardResult = null;
			// 포워딩이 필요하면 포워딩 한다.
			if (StringUtils.chkNull(handler.getForward()) && messageRepository.get(handler.getForward()) != null) {
				forwardResult = forward(ctx, handler, msg);
				// 포워드 결과중 response 정보를 맵에 저장 한다.
				if(forwardResult != null) {
					// 응답 전문 파싱
					parsedMsg = this.parseMessage(forwardResult, false, parsedMsg);
				}
			}
			resultMap = putQualifiedValueToMap(parsedMsg, resultMap);
			// 서비스 실행 결과를 입력해 준다.
			if (resultMap != null && resultMap.size() > 0) {
				parsedMsg.getResponseMessage().bindValue(resultMap);
			}
			sendMessage(ctx, parsedMsg.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.toString(),e);
			try {
				if (messageInfo != null) {
					String exception = e.getClass().getSimpleName();
					logger.debug("Exception and choice {} " , exception);
					Message exMsg = messageInfo.getExceptionMessageMap().get(exception);
					if(exMsg != null) {
						ByteBuf buf = exMsg.toByteBuf();
						ctx.write(buf);
						if (buf != null && buf.refCnt() != 0)
							buf.release();
					}
				}
			} catch (Exception ee) {
				logger.error("Severe Error occurred...." + e.toString() + " in " + ee.toString() + "", ee);
			}
		}
	}

	private Map<String, Object> putQualifiedValueToMap(MessageInfo parsedMsg, Map<String, Object> resultMap) {
		parsedMsg.getRequestMessage().getHeaderMap().forEach((k,v)->{
			resultMap.put("request.header."+k,v);
		});
		parsedMsg.getRequestMessage().getBodyMap().forEach((k,v)->{
			resultMap.put("request.body."+k,v);
		});
		parsedMsg.getRequestMessage().getTailMap().forEach((k,v)->{
			resultMap.put("request.tail."+k,v);
		});
		parsedMsg.getResponseMessage().getHeaderMap().forEach((k,v)->{
			resultMap.put("response.header."+k,v);
		});
		parsedMsg.getResponseMessage().getBodyMap().forEach((k,v)->{
			resultMap.put("response.header."+k,v);
		});
		parsedMsg.getResponseMessage().getTailMap().forEach((k,v)->{
			resultMap.put("response.header."+k,v);
		});
		return resultMap;
	}

	private Map<String, Object> executeService(HandlerInfo handler, MessageInfo parsedMsg) throws Exception {
		service = (IService) context.getBean(handler.getBusinessClass());
		if (service == null) {
			throw new Exception("NoServiceException");
		}
		return (Map<String, Object>) (service.call(parsedMsg.getRequestMessage().getBodyMap()));
	}

	private byte[] forward(ChannelHandlerContext serverCtx,HandlerInfo handler, byte[] forwardBytes) throws Exception {
		try {
			int timeOut = Integer.parseInt(handler.getTimeOut());
			logger.debug("[{}][{}][{}]",handler.getForward(),handler.getForwardIp(), handler.getForwardPort());
			return NettyUtils.sendNio(handler.getForwardIp(), handler.getForwardPort(), timeOut, forwardBytes);
			// return NettyUtils.send(handler.getForwardIp(), handler.getForwardPort(), timeOut, forwardBytes);
		} catch (Exception e) {
			throw new Exception("ForwardException", e);
		}
	}

	private void sendMessage(ChannelHandlerContext ctx, Message msg) throws Exception{
		ByteBuf sendBuf = msg.toByteBuf();
		ctx.write(sendBuf);
	}

	public MessageInfo parseMessage(byte[] in, boolean isRequest, MessageInfo messageInfo) throws Exception {

		ByteArrayInputStream bais = null;
		MessageInfo newMsg = messageInfo.newInstance();
		try {
			Message sourceMsg = isRequest ? messageInfo.getRequestMessage() : messageInfo.getResponseMessage();
			Message targetMsg = isRequest ? newMsg.getRequestMessage() : newMsg.getResponseMessage();
			
			// 이제부터는 새로운 메시지 newMsg 정보를 사용해야 함.
			Field[] header = targetMsg.getHeader();
			Field[] body = targetMsg.getBody();
			Field[] tail = targetMsg.getTail();
			bais = new ByteArrayInputStream(in);

			// 하드코딩 ^^
			header[0].setValue(in.length+"");

			for (int i = 1; i < header.length; i++) {
				Field f = header[i];
				byte[] buf = new byte[targetMsg.getLength(f,targetMsg)];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				targetMsg.encodeOrDecode(f);
				logger.debug("header's name: {} , value: [{}]", f.getName() , f.getValue());
			}

			for (int i = 0; i < body.length; i++) {
				Field f = body[i];
				byte[] buf = new byte[targetMsg.getLength(f,targetMsg)];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				targetMsg.encodeOrDecode(f);
				logger.debug("body's name: {} , value: [{}]", f.getName() , f.getValue());
			}

			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				byte[] buf = new byte[targetMsg.getLength(f,targetMsg)];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				targetMsg.encodeOrDecode(f);
				logger.debug("tail's name: {} , value: [{}]", f.getName() , f.getValue());				
			}
			bais.close();
			bais = null;
		} catch (Exception e) {
			throw new Exception("MessageParseException [" + e.getStackTrace()[0].getClassName() + ":" + e.getStackTrace()[0].getLineNumber() + "]", e);
		} finally {
			try {
				if (bais != null)
					bais.close();
			} catch (Exception e) {
			}
			try {
				ReferenceCountUtil.release(in);
			} catch (Exception e) {
			}
		}
		return newMsg;
	}
	
}