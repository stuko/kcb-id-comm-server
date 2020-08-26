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

	int msgLength = 0;
	int totalByte = 0;
	List<byte[]> listBuffer;
	static Logger logger = LoggerFactory.getLogger(NettyAdapter.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			listBuffer = new ArrayList<>();
			totalByte = 0;
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception{

		byte[] bytes = null;
		ByteBuf buf = ((ByteBuf) msg);
		// 최초 메시지의 앞 4자리를 읽어야 하는 경우
		if(msgLength == 0) {
			if (buf.readableBytes() < 4) {
				throw new Exception("MessageLengthException");
			}
			byte[] byteLength = new byte[4];
			buf.readBytes(byteLength);
			try {
				msgLength = Integer.valueOf(new String(byteLength).trim());
			}catch(Exception e) {
				throw new Exception("MessageLengthNumberFormatException",e);
			}
			
			if(buf.readableBytes() < msgLength) {
				buf.resetReaderIndex();
				return;
			}
		}
		
		if(buf.readableBytes() > 0) {
			bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			listBuffer.add(bytes);
			totalByte += bytes.length;
		}else {
			return;
		}

		logger.debug("------ channelRead0 ---------");
		logger.debug("Object ref = ", this.toString());
		logger.debug("Message Length  = ", msgLength);
		logger.debug("Read Total Byte Length  = ", totalByte);
		logger.debug("Read Length = ", bytes.length);
		logger.debug("Readable Length = ", buf.readableBytes());
		logger.debug("Readable ? = ", buf.isReadable());
		logger.debug("Writable ? = ", buf.isWritable());
		logger.debug("------ channelRead0 ---------");
		
		if(totalByte >= msgLength) {
			this.onReceived(ctx, msgLength,  getReceivedBytes());
			if(ctx != null) {
				ctx.flush();
				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		logger.error(cause.toString(), cause);
		if (ctx != null)
			ctx.close();
	}

	private byte[] getReceivedBytes() {
		byte[] bytes = new byte[totalByte];
		int destPos = 0;
		for (byte[] b : listBuffer) {
			System.arraycopy(b, 0, bytes, destPos, b.length);
			destPos += b.length;
		}
		return bytes;
	}

	public abstract void onReceived(ChannelHandlerContext ctx, int msgLength, byte[] msg);
}
