package com.kcb.id.comm.carrier.handler.impl;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.handler.Handler;
import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.service.Service;
import com.kcb.id.comm.carrier.service.Service.Type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

@Component
public class SampleHandlerImpl implements Handler{
	
	static Logger logger = LoggerFactory.getLogger(SampleHandlerImpl.class);
	
	@Autowired
	Service service;
	
	@Override
	public void onConnected(ChannelHandlerContext ctx, Map<String,MessageInfo> messageRepository, HandlerInfo handler) {
		logger.debug("-----> onConnected.....");
		String businessClass = handler.getBusinessClass();
		if(businessClass != null) {
			try {
				Object object = Class.forName(businessClass).getConstructor().newInstance();
				if(object != null) {
					((Service)object).call(null);
				}
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.error(e.toString(),e);
			}
		}
	}
	@Override
	public void onReceived(ChannelHandlerContext ctx, Object msg, Map<String,MessageInfo> messageRepository, HandlerInfo handler) {
		MessageInfo messageInfo = null;
		try {
			logger.debug("-----> onreceived..... from {} " , handler.getName());
			logger.debug("-----> onreceived..... message is  {} " , handler.getMessageName());
			messageInfo = messageRepository.get(handler.getMessageName());
			MessageInfo parsedMsg = this.parseMessage(msg, messageInfo);
			
			logger.debug("########## Forward Type ???? #############");
			logger.debug("Original Message Forward = {} " , messageInfo.getForward());
			logger.debug("Forward = {} " , parsedMsg.getForward());
			logger.debug("########## Forward Type ???? #############");
			
			Map<String,Object> responseMap = new HashMap<>();
			if(service == null) {
				if(handler.getBusinessClass() != null) {
					Object object = Class.forName(handler.getBusinessClass()).getConstructor().newInstance();
					if(object != null)
						service = (Service)object;
				}
			}
			if(service == null) {
				throw new Exception("NoServiceException");
			}
			
			if(service.getType() == Type.JSON) {
				Gson gson = new Gson();
				responseMap = gson.fromJson((String)(service.call(parsedMsg.getRequestMessage().getBodyMap())),responseMap.getClass());
			}else if(service.getType() == Type.MAP) {
				responseMap = (Map<String,Object>)(service.call(parsedMsg.getRequestMessage().getBodyMap()));
			}else if(service.getType() == Type.TCP) {
				responseMap = (Map<String,Object>)(service.call(parsedMsg.getRequestMessage().getBodyMap()));
			}else {
				responseMap = (Map<String,Object>)(service.call(parsedMsg.getRequestMessage().getBodyMap()));
			}
			
			if(messageInfo.getForward()!=null && !"".equals(messageInfo.getForward())) {
				// messageInfo.getForwardServer() 와 messageInfo.getForwardPort() 로 전문을 그대로 전송한다. 
			}
			
			if(responseMap != null && responseMap.size() > 0) {
				// header and tail is equal to Request Message
				parsedMsg.getResponseMessage().setBodyValue(responseMap);
			}
			
			
			
			Charset charset = Charset.defaultCharset();
		    try {
		    	ByteBuf b = NettyUtils.getMessage2ByteBuf(parsedMsg.getResponseMessage(), responseMap);
		    	logger.debug("response message is {}" ,new String(b.array()));
				ctx.write(b);
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}finally {
				try{if(ctx != null)ctx.flush();ctx.close();}catch(Exception e) {}
			}
		}catch(Exception e) {
			logger.error(e.toString(),e);
			try {
				if(messageInfo != null) {
					String exception = e.getClass().getSimpleName();
					Message exMsg = messageInfo.getExceptionMessageMap().get(exception);
					ctx.write(exMsg.toByteBuf());
				}
			}catch(Exception ee) {
				logger.error("Severe Error occurred...."+e.toString()+" in "+ee.toString()+"" , ee);
			}
		}
	}
	
	public MessageInfo parseMessage(Object in, MessageInfo messageInfo) {
		
		ByteBuf byteBuf = (ByteBuf) in;
		String currentData = "";
		String currentPart = "";
		ByteBufInputStream bbs = null;
		MessageInfo msg = messageInfo.newInstance();
		try {
			String repeat = messageInfo.getRequestMessage().getRepeat();
			String repeatVariable = messageInfo.getRequestMessage().getRepeatVariable();

			Field[] header = messageInfo.getRequestMessage().getHeader();
			Field[] body = messageInfo.getRequestMessage().getBody();
			Field[] tail = messageInfo.getRequestMessage().getTail();

			currentPart = "header";

			bbs = new ByteBufInputStream(byteBuf);

			// while (byteBuf.isReadable()) { 
			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				currentData = f.getName();
				byte[] buf = new byte[Integer.parseInt(f.getLength())];
				bbs.read(buf);
				// byteBuf.read.readBytes(buf);
				String value = new String(buf);
				messageInfo.getRequestMessage().encodeOrDecode(f);
				msg.getRequestMessage().getHeader()[i].setValue(value);
				msg.getResponseMessage().getHeader()[i].setValue(value);
			}
			
			currentPart = "body";
			int repeatCount = 1;
			if ("true".equals(repeat)) {
				repeatCount = Integer.parseInt(repeatVariable);
			}
			for (int i = 0; i < repeatCount; i++) {
				for (int j = 0; j < body.length; j++) {
					Field f = body[j];
					currentData = f.getName();
					byte[] buf = new byte[Integer.parseInt(f.getLength())];
					bbs.read(buf);
					// byteBuf.readBytes(buf);
					String value = new String(buf);
					f.addValue(value);
					messageInfo.getRequestMessage().encodeOrDecode(f, i);
					msg.getRequestMessage().getBody()[i].addValue(value);
					msg.getResponseMessage().getBody()[i].addValue(value);
				}
			}
			currentPart = "tail";
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				currentData = f.getName();
				byte[] buf = new byte[Integer.parseInt(f.getLength())];
				bbs.read(buf);
				// byteBuf.readBytes(buf);
				String value = new String(buf);
				f.setValue(value);
				messageInfo.getRequestMessage().encodeOrDecode(f);
				msg.getRequestMessage().getTail()[i].setValue(value);
				msg.getResponseMessage().getTail()[i].setValue(value);
			}
			bbs.close();
			bbs = null;
		} catch (Exception e) {
			logger.error(e.toString(),e);
		} finally {
			try {if (bbs != null)bbs.close();} catch (Exception e) {}
			try {ReferenceCountUtil.release(in);} catch (Exception e) {}
		}
		return msg;
	}


}