package com.kcb.id.comm.carrier.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public abstract class NettyAdapter extends ChannelInboundHandlerAdapter {

	static Logger logger = LoggerFactory.getLogger(NettyAdapter.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// When connected.... called.
		// ByteBuf messageBuffer = Unpooled.buffer();
		try {
			this.onConnected(ctx);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			this.onReceived(ctx, msg);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			try{ReferenceCountUtil.release(msg);}catch(Exception e) {}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		logger.error(cause.toString(), cause);
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	public abstract void onConnected(ChannelHandlerContext ctx);

	public abstract void onReceived(ChannelHandlerContext ctx, Object msg);
}
