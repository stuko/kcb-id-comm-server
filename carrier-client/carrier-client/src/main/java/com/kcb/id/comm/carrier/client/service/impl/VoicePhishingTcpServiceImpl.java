package com.kcb.id.comm.carrier.client.service.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.kcb.id.comm.carrier.client.common.JsonUtils;
import com.kcb.id.comm.carrier.client.service.VoicePhishingService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import okhttp3.MediaType;

@Configuration
@Service
public class VoicePhishingTcpServiceImpl implements VoicePhishingService {

	static Logger logger = LoggerFactory.getLogger(VoicePhishingTcpServiceImpl.class);
	
	@Value("${vp.server.url}")
	private String vpUrl;
	Gson gson = new Gson();
	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@Override
	public String call(Map<String, Object> param, Map<String, Object> requestMap) {
		Map<String, Object> payload = new HashMap<>();
		payload = JsonUtils.getPayLoad(payload, param, requestMap);
		try {
		 	//return this.post(vpUrl, gson.toJson(payload));
			return null;
		} catch (Exception e) {
			logger.error(e.toString(),e);
			return "";
		}
	}

	private String tcp(String ip, String port) throws IOException {
		EventLoopGroup group = new NioEventLoopGroup();
		try{
		    Bootstrap clientBootstrap = new Bootstrap();
		    clientBootstrap.group(group);
		    clientBootstrap.channel(NioSocketChannel.class);
		    clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 9999));
		    clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
		        protected void initChannel(SocketChannel socketChannel) throws Exception {
		            socketChannel.pipeline().addLast(()->{
		            	
		            });
		        }
		    });
		    ChannelFuture channelFuture = clientBootstrap.connect().sync();
		    channelFuture.channel().closeFuture().sync();
		    group.shutdownGracefully().sync();
		} catch(Exception e){
			
		}finally {
		}
		

		return null;
	}

}
