package com.kcb.id.comm.carrier.handler.impl;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.handler.Handler;
import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.impl.Field;
import com.kcb.id.comm.carrier.service.IService;
import com.kcb.id.comm.carrier.service.IService.Type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;

@Configuration
@Component
@Scope("prototype")
public class NettyServerChannelHandlerImpl implements Handler {

	static Logger logger = LoggerFactory.getLogger(NettyServerChannelHandlerImpl.class);

	boolean sync = true;
	@Autowired
	IService service;

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	@Override
	public void onConnected(ChannelHandlerContext ctx, Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		logger.debug("[{}]-----> onConnected..... by {} ", handler.getMessageName() , this.toString());
	}
	
	@Override
	public void onCompleted(ChannelHandlerContext ctx, byte[] msg , Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		logger.debug("[{}]-----> onCompleted..... by {} ", handler.getMessageName(), this.toString());
	}

	@Override
	public void onReceived(ChannelHandlerContext ctx, byte[] msg, Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		this.receive(ctx, msg, messageRepository, handler);
	}
	
	private void receive(ChannelHandlerContext ctx, byte[] msg, Map<String, MessageInfo> messageRepository,	HandlerInfo handler) {
		MessageInfo messageInfo = null;
		try {
			logger.debug("##########################");
			logger.debug("###### RECEIVE ######");
			logger.debug("##########################");
			logger.debug("[{}] [{}]-----> onReceived..... by {} ", msg.length, handler.getMessageName(), this.toString());
			logger.debug("[{}]-----> onReceived..... by {} ", handler.getMessageName(), this.toString());
			logger.debug("##########################");
			
			logger.debug("Handler Name : {} , MessageName : {}", handler.getName(), handler.getMessageName());
			logger.debug("Handler Forward : {},{},{}", handler.getForward(), handler.getForwardIp(), handler.getForwardPort());
			messageInfo = messageRepository.get(handler.getMessageName());
			MessageInfo parsedMsg = this.parseMessage(msg, messageInfo);
			logger.debug("Parsed Message : {}", parsedMsg.getName());
			if (handler.getForward() != null && !"".equals(handler.getForward())
					&& messageRepository.get(handler.getForward()) != null) {
				logger.debug("Forward Message : {}", handler.getForward());
				forward(ctx, handler, messageRepository, messageInfo, parsedMsg);
			} else {
				logger.debug("Response Message : {}", parsedMsg.getName());
				response(ctx, handler, parsedMsg);
			}

		} catch (Exception e) {
			logger.error(e.toString());
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
		} finally {
			try {
				if (ctx != null) {
					//ctx.flush();
					//ctx.close();
				}
			} catch (Exception e) {}
		}
	}

	private void forward(ChannelHandlerContext ctx,HandlerInfo handler, Map<String, MessageInfo> messageRepository, MessageInfo messageInfo,
			MessageInfo parsedMsg) throws Exception {
		// messageInfo.getForwardServer() 와 messageInfo.getForwardPort() 로 전문을 그대로 전송한다.
		MessageInfo forwardMsg = messageRepository.get(handler.getForward());
		ByteBuf buf = unParseMessage(parsedMsg, forwardMsg);
		byte[] forwardBytes = new byte[buf.readableBytes()];
		try {
			buf.readBytes(forwardBytes);
			if (buf != null && buf.refCnt() != 0)
				buf.release();

			if (this.isSync()) {
				logger.debug("Forward is sync");
				logger.debug("Forward data is [{}]" , new String(forwardBytes));
				byte[] responseBytes = NettyUtils.send(handler.getForwardIp(), handler.getForwardPort(),5000, forwardBytes);
				logger.debug("Forward is completed");
				if(responseBytes != null) ctx.write(responseBytes);
				else ctx.close();
			} else {
				logger.debug("Forward is async");
				NettyClient client = new NettyClient();
				client.send(handler.getForwardIp(), handler.getForwardPort(), forwardBytes, (res) -> {
					try {
						final ChannelFuture f = ctx.writeAndFlush(res);
						f.addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture future) {
								assert f == future;
								if (res != null && res.refCnt() != 0)
									res.release();
								ctx.close();
							}
						});
					} catch (IllegalReferenceCountException re) {
						logger.debug("#### Already flush....######");
					}
				});
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private void response(ChannelHandlerContext ctx, HandlerInfo handler, MessageInfo parsedMsg)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, Exception {
		Map<String, Object> responseMap = new HashMap<>();
		if (service == null) {
			logger.debug("Service is Null");
			if (handler.getBusinessClass() != null) {
				Object object = Class.forName(handler.getBusinessClass()).getConstructor().newInstance();
				if (object != null)
					service = (IService) object;
			}
		}
		if (service == null) {
			throw new Exception("NoServiceException");
		}

		logger.debug("Service Type is {}", service.getType());

		if (service.getType() == Type.JSON) {
			Gson gson = new Gson();
			responseMap = gson.fromJson((String) (service.call(parsedMsg.getRequestMessage().getBodyMap())),
					responseMap.getClass());
		} else if (service.getType() == Type.MAP) {
			responseMap = (Map<String, Object>) (service.call(parsedMsg.getRequestMessage().getBodyMap()));
		} else if (service.getType() == Type.TCP) {
			responseMap = (Map<String, Object>) (service.call(parsedMsg.getRequestMessage().getBodyMap()));
		} else {
			responseMap = (Map<String, Object>) (service.call(parsedMsg.getRequestMessage().getBodyMap()));
		}

		if (responseMap != null && responseMap.size() > 0) {
			parsedMsg.getResponseMessage().setBodyValue(responseMap);
		}

		logger.debug("Service Response is {}", responseMap);

		sendMessage(ctx, parsedMsg.getResponseMessage(), responseMap);
	}

	private void sendMessage(ChannelHandlerContext ctx, Message msg, Map<String, Object> responseMap) {
		Charset charset = Charset.defaultCharset();
		try {
			logger.debug("Let's send return message ...");
			ByteBuf sendBuf = NettyUtils.getMessage2ByteBuf(msg, responseMap);
			try {
				logger.debug("ByteBuf 's length is {}", sendBuf.readableBytes());
				ctx.write(sendBuf);
				logger.debug("write and flush");
			} catch (IllegalReferenceCountException re) {
				logger.debug("#### Already flush....######");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	public MessageInfo parseMessage(byte[] in, MessageInfo messageInfo) throws Exception {

		String currentData = "";
		String currentPart = "";
		ByteArrayInputStream bais = null;
		MessageInfo msg = messageInfo.newInstance();
		try {
			logger.debug("[{}] Request Original Header's length = {} ", messageInfo.getName(),
					messageInfo.getRequestMessage().getHeader().length);
			logger.debug("[{}] Request Target Header's length = {} ", messageInfo.getName(),
					msg.getRequestMessage().getHeader().length);
			logger.debug("[{}] Request Original Body's length = {} ", messageInfo.getName(),
					messageInfo.getRequestMessage().getBody().length);
			logger.debug("[{}] Request Target Body's length = {} ", messageInfo.getName(),
					msg.getRequestMessage().getBody().length);
			logger.debug("[{}] Request Original Tail's length = {} ", messageInfo.getName(),
					messageInfo.getRequestMessage().getTail().length);
			logger.debug("[{}] Request Target Tail's length = {} ", messageInfo.getName(),
					msg.getRequestMessage().getTail().length);

			logger.debug("[{}] Response Original Header's length = {} ", messageInfo.getName(),
					messageInfo.getResponseMessage().getHeader().length);
			logger.debug("[{}] Response Target Header's length = {} ", messageInfo.getName(),
					msg.getResponseMessage().getHeader().length);
			logger.debug("[{}] Response Original Body's length = {} ", messageInfo.getName(),
					messageInfo.getResponseMessage().getBody().length);
			logger.debug("[{}] Response Target Body's length = {} ", messageInfo.getName(),
					msg.getResponseMessage().getBody().length);
			logger.debug("[{}] Response Original Tail's length = {} ", messageInfo.getName(),
					messageInfo.getResponseMessage().getTail().length);
			logger.debug("[{}] Response Target Tail's length = {} ", messageInfo.getName(),
					msg.getResponseMessage().getTail().length);

			messageInfo.getExceptionMessageMap().forEach((k, v) -> {
				if (v.getHeader() != null)
					logger.debug("[{}] Error Original Header's length = {} ", k, v.getHeader().length);
			});

			String repeat = messageInfo.getRequestMessage().getRepeat();
			String repeatVariable = messageInfo.getRequestMessage().getRepeatVariable();

			Field[] header = messageInfo.getRequestMessage().getHeader();
			Field[] body = messageInfo.getRequestMessage().getBody();
			Field[] tail = messageInfo.getRequestMessage().getTail();
			currentPart = "header";
			bais = new ByteArrayInputStream(in);

			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				currentData = f.getName();
				byte[] buf = new byte[messageInfo.getRequestMessage().getLength(f,messageInfo)];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				messageInfo.getRequestMessage().encodeOrDecode(f);
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
					byte[] buf = new byte[messageInfo.getRequestMessage().getLength(f,messageInfo)];
					bais.read(buf);
					String value = new String(buf);
					f.addValue(value);
					messageInfo.getRequestMessage().encodeOrDecode(f, i);
					msg.getRequestMessage().setBodyValue(f.getName(), value, i);
				}
			}
			currentPart = "tail";
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				currentData = f.getName();
				byte[] buf = new byte[messageInfo.getRequestMessage().getLength(f,messageInfo)];
				bais.read(buf);
				String value = new String(buf);
				f.setValue(value);
				messageInfo.getRequestMessage().encodeOrDecode(f);
				msg.getRequestMessage().getTail()[i].setValue(value);
			}
			bais.close();
			bais = null;
		} catch (Exception e) {
			logger.error(e.toString(), e);
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
		return msg;
	}

	/*
	 * source 의 전문 데이터를 target의 전문데이터로 만들어 바이트 배열로 리던해 주는 메서드
	 */
	public ByteBuf unParseMessage(MessageInfo source, MessageInfo target) {

		String currentData = "";
		String currentPart = "";
		ByteBufInputStream bbs = null;
		MessageInfo msg = target.newInstance();
		try {
			String repeat = source.getRequestMessage().getRepeat();
			String repeatVariable = source.getRequestMessage().getRepeatVariable();

			Field[] header = target.getRequestMessage().getHeader();
			Field[] body = target.getRequestMessage().getBody();
			Field[] tail = target.getRequestMessage().getTail();

			currentPart = "header";
			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				currentData = f.getName();
				String value = source.getRequestMessage().getHeader(currentData);
				logger.debug("HEADER : {} is {{}}", f.getName(), value);
				msg.getRequestMessage().getHeader()[i].setValue(value);
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
					String value = source.getRequestMessage().getBody(currentData, i);
					logger.debug("BODY : {} is {{}}", f.getName(), value);
					msg.getRequestMessage().setBodyValue(currentData, value, i);
				}
			}
			currentPart = "tail";
			for (int i = 0; i < tail.length; i++) {
				Field f = tail[i];
				currentData = f.getName();
				String value = source.getRequestMessage().getTail(currentData);
				logger.debug("TAIL : {} is {{}}", f.getName(), value);
				msg.getRequestMessage().getTail()[i].setValue(value);
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
		}
		return msg.getRequestMessage().toByteBuf();
	}

}