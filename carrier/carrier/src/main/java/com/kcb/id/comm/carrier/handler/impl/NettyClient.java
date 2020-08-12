package com.kcb.id.comm.carrier.handler.impl;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kcb.id.comm.carrier.handler.NettyClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

	static Logger logger = LoggerFactory.getLogger(NettyClient.class);

	public byte[] getByte(String ip, int port, byte[] msg, NettyClientHandler clientHandler) {
		ByteBuf buf = this.send(ip, port, msg ,clientHandler);
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		if(buf != null && buf.refCnt() != 0) buf.release();
		return bytes;
	}

	public ByteBuf send(String ip, int port, byte[] msg , NettyClientHandler clientHandler) {
		NettyClientChannelHandler handler = new NettyClientChannelHandler(msg);
		handler.setClientHandler(clientHandler);
		this.send(ip, port, handler);
		return handler.getResponseByteBuf();
	}

	public void send(String ip, int port, SimpleChannelInboundHandler handler) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap clientBootstrap = new Bootstrap();
			clientBootstrap.group(group);
			clientBootstrap.channel(NioSocketChannel.class);
			clientBootstrap.remoteAddress(new InetSocketAddress(ip, port));
			clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					socketChannel.pipeline().addLast(handler);
				}
			});
			ChannelFuture channelFuture;
			try {
				channelFuture = clientBootstrap.connect().sync();
				channelFuture.channel().closeFuture().sync();
			} catch (Exception e) {
				logger.error(e.toString(), e);
			}
		} finally {
			try {
				group.shutdownGracefully().sync();
			} catch (Exception e) {
			}
		}
	}
}
