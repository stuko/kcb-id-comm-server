package com.kcb.id.comm.carrier.handler.impl;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
public class NettyServerChannelHandlerImpl implements Handler {

	static Logger logger = LoggerFactory.getLogger(NettyServerChannelHandlerImpl.class);

	@Autowired
	IService service;

	@Override
	public void onConnected(ChannelHandlerContext ctx, Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		logger.debug("-----> onConnected.....");
	}

	@Override
	public void onReceived(ChannelHandlerContext ctx, Object msg, Map<String, MessageInfo> messageRepository,
			HandlerInfo handler) {
		MessageInfo messageInfo = null;
		try {
			messageInfo = messageRepository.get(handler.getMessageName());
			MessageInfo parsedMsg = this.parseMessage(msg, messageInfo);

			if (messageInfo.getForward() != null && !"".equals(messageInfo.getForward())
					&& messageRepository.get(messageInfo.getForward()) != null) {
				forward(ctx, messageRepository, messageInfo, parsedMsg);
			} else {
				response(ctx, handler, parsedMsg);
			}

		} catch (Exception e) {
			logger.error(e.toString(), e);
			try {
				if (messageInfo != null) {
					String exception = e.getClass().getSimpleName();
					Message exMsg = messageInfo.getExceptionMessageMap().get(exception);
					ByteBuf buf = exMsg.toByteBuf();
					ctx.write(buf);
					if(buf != null && buf.refCnt() != 0) buf.release();
				}
			} catch (Exception ee) {
				logger.error("Severe Error occurred...." + e.toString() + " in " + ee.toString() + "", ee);
			}
		} finally {
			try {
				if (ctx != null)
					ctx.flush();
				ctx.close();
			} catch (Exception e) {
			}
		}
	}

	private void forward(ChannelHandlerContext ctx, Map<String, MessageInfo> messageRepository, MessageInfo messageInfo,
			MessageInfo parsedMsg) {
		// messageInfo.getForwardServer() 와 messageInfo.getForwardPort() 로 전문을 그대로 전송한다.
		MessageInfo forwardMsg = messageRepository.get(messageInfo.getForward());
		ByteBuf buf = unParseMessage(parsedMsg, forwardMsg);
		byte[] forwardBytes = new byte[buf.readableBytes()];
		try {
			buf.readBytes(forwardBytes);
			if(buf != null && buf.refCnt() != 0) buf.release();
			
			NettyClient client = new NettyClient();
			ByteBuf response = client.send(messageInfo.getForwardIp(), messageInfo.getForwardPort(), forwardBytes);
			try {
				final ChannelFuture f = ctx.writeAndFlush(response);
				f.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) {
						assert f == future;
						if(response != null && response.refCnt() != 0) response.release();
						ctx.close();
					}
				}); 
			} catch (IllegalReferenceCountException re) {
				logger.debug("#### Already flush....######");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void response(ChannelHandlerContext ctx, HandlerInfo handler, MessageInfo parsedMsg)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, Exception {
		Map<String, Object> responseMap = new HashMap<>();
		if (service == null) {
			if (handler.getBusinessClass() != null) {
				Object object = Class.forName(handler.getBusinessClass()).getConstructor().newInstance();
				if (object != null)
					service = (IService) object;
			}
		}
		if (service == null) {
			throw new Exception("NoServiceException");
		}

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
		sendMessage(ctx, parsedMsg.getResponseMessage(), responseMap);
	}

	private void sendMessage(ChannelHandlerContext ctx, Message msg, Map<String, Object> responseMap) {
		Charset charset = Charset.defaultCharset();
		try {
			ByteBuf sendBuf = NettyUtils.getMessage2ByteBuf(msg, responseMap);
			try {
				final ChannelFuture f = ctx.writeAndFlush(sendBuf);
				f.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) {
						assert f == future;
						if(sendBuf != null && sendBuf.refCnt() != 0) sendBuf.release();
						ctx.close();
					}
				}); 
			} catch (IllegalReferenceCountException re) {
				logger.debug("#### Already flush....######");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	public MessageInfo parseMessage(Object in, MessageInfo messageInfo) {

		ByteBuf byteBuf = (ByteBuf) in;
		String currentData = "";
		String currentPart = "";
		ByteBufInputStream bbs = null;
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
			bbs = new ByteBufInputStream(byteBuf);

			for (int i = 0; i < header.length; i++) {
				Field f = header[i];
				currentData = f.getName();
				byte[] buf = new byte[Integer.parseInt(f.getLength())];
				bbs.read(buf);
				String value = new String(buf);
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
					byte[] buf = new byte[Integer.parseInt(f.getLength())];
					bbs.read(buf);
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
				byte[] buf = new byte[Integer.parseInt(f.getLength())];
				bbs.read(buf);
				String value = new String(buf);
				f.setValue(value);
				messageInfo.getRequestMessage().encodeOrDecode(f);
				msg.getRequestMessage().getTail()[i].setValue(value);
			}
			bbs.close();
			bbs = null;
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			try {
				if (bbs != null)
					bbs.close();
			} catch (Exception e) {
			}
			try {ReferenceCountUtil.release(in);} catch (Exception e) {}
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