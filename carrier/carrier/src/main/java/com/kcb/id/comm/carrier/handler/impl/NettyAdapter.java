package com.kcb.id.comm.carrier.handler.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/*
 * Netty에서 사용할 채널 어답터
 * 클라이언트에서 연결이 시도될 때 생성되어 사용되는 클래스
 */
public abstract class NettyAdapter extends SimpleChannelInboundHandler<Object> {

	int totalByte = 0;
	List<byte[]> listBuffer;
	
	static Logger logger = LoggerFactory.getLogger(NettyAdapter.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			if(listBuffer != null) {
				logger.debug("### OOPS..... Error. ###");
			}
			listBuffer = new ArrayList<>();
			totalByte = 0;
			logger.debug("channelActive by {}" , this.toString());
			this.onConnected(ctx);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) {
		try {
			ByteBuf buf = ((ByteBuf)msg);
			logger.debug("channelRead by {} , length = {},  readable = {} , writable = {} " , this.toString() , buf.readableBytes(),  buf.isReadable() , buf.isWritable());
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			listBuffer.add(bytes);
			totalByte += bytes.length;
			if(buf.isWritable()) {
				bytes = new byte[totalByte];
				int destPos = 0;
				for(byte[] b : listBuffer) {
					System.arraycopy(b, 0, bytes, destPos , b.length);
					destPos += b.length;
				}
				this.onReceived(ctx, bytes);
				logger.debug("channelRead length = {} by {} , readable = {} , writable = {} " , totalByte ,this.toString(), buf.isReadable() , buf.isWritable());
			}
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
		// logger.error(cause.toString(), cause);
		// if(ctx != null)ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		try {
			byte[] bytes = new byte[totalByte];
			int destPos = 0;
			for(byte[] b : listBuffer) {
				System.arraycopy(b, 0, bytes, destPos , b.length);
				destPos += b.length;
			}
			this.onCompleted(ctx, bytes);
			logger.debug("channelReadCompleted  by {}" , this.toString());
			logger.debug("channelReadCompleted length = {} by {}" , totalByte ,this.toString());
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			try{
				ctx.flush();
				ctx.close();
			}catch(Exception e) {}
		}
	}

	public abstract void onConnected(ChannelHandlerContext ctx);

	public abstract void onReceived(ChannelHandlerContext ctx, byte[] msg);
	
	public abstract void onCompleted(ChannelHandlerContext ctx, byte[] bytes);
}
