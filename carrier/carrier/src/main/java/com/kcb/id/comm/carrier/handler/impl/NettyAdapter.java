package com.kcb.id.comm.carrier.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/*
 * Netty에서 사용할 채널 어답터
 * 클라이언트에서 연결이 시도될 때 생성되어 사용되는 클래스
 */
public abstract class NettyAdapter extends ChannelInboundHandlerAdapter {

	static Logger logger = LoggerFactory.getLogger(NettyAdapter.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			logger.debug("channelActive");
			this.onConnected(ctx);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			logger.debug("channelRead");
			this.onReceived(ctx, msg);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			try{
				// if(msg != null && ((ByteBuf)msg).refCnt() != 0)ReferenceCountUtil.release(msg);
			}catch(Exception e) {}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		logger.error(cause.toString(), cause);
		if(ctx != null)ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// ctx.flush();
	}

	public abstract void onConnected(ChannelHandlerContext ctx);

	public abstract void onReceived(ChannelHandlerContext ctx, Object msg);
}
