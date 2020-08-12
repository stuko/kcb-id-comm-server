package com.kcb.id.comm.carrier.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class NettyClientChannelHandler extends SimpleChannelInboundHandler {

	String request;
	String response;
	byte[] requestBytes;
	byte[] responseBytes;
	ByteBuf responseByteBuf;
	
	public NettyClientChannelHandler(String msg){
		request = msg;
	}
	public NettyClientChannelHandler(byte[] msg){
		requestBytes = msg;
	}
	public ByteBuf getResponseByteBuf() {
		return responseByteBuf;
	}
	public void setResponseByteBuf(ByteBuf responseByteBuf) {
		this.responseByteBuf = responseByteBuf;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public byte[] getRequestBytes() {
		return requestBytes;
	}
	public void setRequestBytes(byte[] requestBytes) {
		this.requestBytes = requestBytes;
	}

	public byte[] getResponseBytes() {
		return responseBytes;
	}

	public void setResponseBytes(byte[] responseBytes) {
		this.responseBytes = responseBytes;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ByteBuf messageBuffer = Unpooled.buffer();
		if(request != null)messageBuffer.writeBytes(request.getBytes());
		else messageBuffer.writeBytes(requestBytes);
	    ctx.writeAndFlush(messageBuffer);
	    if(messageBuffer != null && messageBuffer.refCnt() != 0) messageBuffer.release();
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
		cause.printStackTrace();
		channelHandlerContext.close();
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.setResponseByteBuf((ByteBuf)msg);
		byte[] bytes = new byte[this.getResponseByteBuf().readableBytes()];
		ByteBuf derived = this.getResponseByteBuf().readBytes(bytes);
		this.setResponseBytes(bytes);
		this.setResponse(this.getResponseByteBuf().toString(CharsetUtil.UTF_8));
	}
}
