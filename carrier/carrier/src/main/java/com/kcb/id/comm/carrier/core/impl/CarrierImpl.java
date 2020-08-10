package com.kcb.id.comm.carrier.core.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.core.Carrier;
import com.kcb.id.comm.carrier.handler.Handler;
import com.kcb.id.comm.carrier.handler.impl.NettyAdapter;
import com.kcb.id.comm.carrier.loader.HandlerInfoLoader;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;
import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.ServerInfoLoader;
import com.kcb.id.comm.carrier.service.Service;
import com.kcb.id.comm.carrier.service.impl.APIClientCallerImpl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Configuration
@Component
public class CarrierImpl implements Carrier {

	static Logger logger = LoggerFactory.getLogger(CarrierImpl.class);
	
	@Autowired
	MessageInfoLoader messageInfoLoader;
	@Autowired
	ServerInfoLoader serverInfoLoader;
	@Autowired
	HandlerInfoLoader handlerInfoLoader;
	
	@Autowired
    @Qualifier("CarrierConfigCaller")
	Service carrierConfigCaller;
	
	@Value( "${carrier.path.server}" )
	private String serverPath;
	@Value( "${carrier.path.message}" )
	private String messagePath;
	@Value( "${carrier.path.handler}" )
	private String handlerPath;

	@Value( "${carrier.url.server}" )
	private String serverUrl;
	@Value( "${carrier.url.message}" )
	private String messageUrl;
	@Value( "${carrier.url.handler}" )
	private String handlerUrl;
	
	Map<String,EventLoopGroup> bossMap = new HashMap<>();
	Map<String,EventLoopGroup> workerMap = new HashMap<>();
	
	int boss = 1, worker = 1;
	
	@Bean(name="CarrierConfigCaller")
	public Service getCarrierConfigCaller() {
		return new APIClientCallerImpl();
	}
	public String getServerPath() {
		return serverPath;
	}
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}
	public String getMessagePath() {
		return messagePath;
	}
	public void setMessagePath(String messagePath) {
		this.messagePath = messagePath;
	}
	public String getHandlerPath() {
		return handlerPath;
	}
	public void setHandlerPath(String handlerPath) {
		this.handlerPath = handlerPath;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	public String getMessageUrl() {
		return messageUrl;
	}
	public void setMessageUrl(String messageUrl) {
		this.messageUrl = messageUrl;
	}
	public String getHandlerUrl() {
		return handlerUrl;
	}
	public void setHandlerUrl(String handlerUrl) {
		this.handlerUrl = handlerUrl;
	}
	public int getBoss() {
		return boss;
	}
	public void setBoss(int boss) {
		this.boss = boss;
	}
	public int getWorker() {
		return worker;
	}
	public void setWorker(int worker) {
		this.worker = worker;
	}
	public void start(ServerInfo server) {
		EventLoopGroup bossGroup = new NioEventLoopGroup(); 
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
								new NettyAdapter() {
									public void onConnected(ChannelHandlerContext ctx) {
										String handlerClass = getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()).getHandlerClass();
										try {
											Object object = Class.forName(handlerClass).getConstructor().newInstance();
											if(object != null) {
												((Handler)object).onConnected(ctx
														, getMessageInfoLoader().getMessageRepository()
														, getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()));
											}
										} catch (InstantiationException | IllegalAccessException
												| IllegalArgumentException | InvocationTargetException
												| NoSuchMethodException | SecurityException
												| ClassNotFoundException e) {
											logger.error(e.toString(),e);
										}
									}
									public void onReceived(ChannelHandlerContext ctx, Object msg) {
										String handlerClass = getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()).getHandlerClass();
										try {
											Object object = Class.forName(handlerClass).getConstructor().newInstance();
											if(object != null) {
												((Handler)object).onReceived(ctx
														, msg
														, getMessageInfoLoader().getMessageRepository()
														, getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()));
											}
										} catch (InstantiationException | IllegalAccessException
												| IllegalArgumentException | InvocationTargetException
												| NoSuchMethodException | SecurityException
												| ClassNotFoundException e) {
											logger.error(e.toString(),e);
										}
										
									}
								}
							);
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			this.getBossMap().put(server.getName(),bossGroup);
			this.getWorkerMap().put(server.getName(),workerGroup);
			logger.debug("Server will be listening... {}" , server.getPort());
			ChannelFuture f = b.bind(server.getPort()).sync();
			f.channel().closeFuture().sync();
			logger.debug("Server will be stopping... {}" , server.getPort());
		} catch (Exception e) {
			logger.error(e.toString(),e);
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	public void stop(ServerInfo server) {
		this.getWorkerMap().get(server.getName()).shutdownGracefully();
		this.getBossMap().get(server.getName()).shutdownGracefully();
	}
	public String status() {
		return null;
	}
	public MessageInfoLoader getMessageInfoLoader() {
		return messageInfoLoader;
	}
	public void setMessageInfoLoader(MessageInfoLoader messageInfoLoader) {
		this.messageInfoLoader = messageInfoLoader;
	}
	public ServerInfoLoader getServerInfoLoader() {
		return serverInfoLoader;
	}
	public void setServerInfoLoader(ServerInfoLoader serverInfoLoader) {
		this.serverInfoLoader = serverInfoLoader;
	}
	public HandlerInfoLoader getHandlerInfoLoader() {
		return handlerInfoLoader;
	}
	public void setHandlerInfoLoader(HandlerInfoLoader handlerInfoLoader) {
		this.handlerInfoLoader = handlerInfoLoader;
	}
	public Map<String, EventLoopGroup> getBossMap() {
		return bossMap;
	}
	public void setBossMap(Map<String, EventLoopGroup> bossMap) {
		this.bossMap = bossMap;
	}
	public Map<String, EventLoopGroup> getWorkerMap() {
		return workerMap;
	}
	public void setWorkerMap(Map<String, EventLoopGroup> workerMap) {
		this.workerMap = workerMap;
	}
	private InputStream selectInputStream(String url) {
		if(url == null) return null;
		String[] urls = url.split(",");
		InputStream is = null;
		try {
			for(String u : urls) {
				try {
					is = new URL(u).openStream();
					break;
				}catch(Exception e) {
					logger.error(e.toString(),e);
					continue;
				}
			}
		}catch(Exception ee) {
			logger.error(ee.toString(),ee);
		}
		return is;
	}
	
	public void startAll() {
		if(this.getServerPath() != null && !"".equals(this.getServerPath().trim())) {
			try {
				this.getServerInfoLoader().setLoaderFile(ResourceUtils.getFile(this.getServerPath()));
			} catch (FileNotFoundException e) {
				logger.error(e.toString(),e);
			}
			this.getServerInfoLoader().load();
		}else {
			try {
				this.getServerInfoLoader().load(selectInputStream(this.getServerUrl()));
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
		}
		if(this.getMessagePath() != null && !"".equals(this.getMessagePath().trim())) {
			try {
				this.getMessageInfoLoader().setLoaderFile(ResourceUtils.getFile(this.getMessagePath()));
			} catch (FileNotFoundException e) {
				logger.error(e.toString(),e);
			}
			this.getMessageInfoLoader().load();
		}else {
			try {
				this.getMessageInfoLoader().load(selectInputStream(this.getMessageUrl()));
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
		}
		if(this.getHandlerPath() != null && !"".equals(this.getHandlerPath().trim())) {
			try {
				this.getHandlerInfoLoader().setLoaderFile(ResourceUtils.getFile(this.getHandlerPath()));
			} catch (FileNotFoundException e) {
				logger.error(e.toString(),e);
			}
			this.getHandlerInfoLoader().load();
		}else {
			try {
				this.getHandlerInfoLoader().load(selectInputStream(this.getHandlerUrl()));
			} catch (Exception e) {
				logger.error(e.toString(),e);
			}
		}
		this.getServerInfoLoader().getServerRepository().forEach((n,s)->{
			new Thread(()->{
				start(s);
			}).start();
		});
		this.monitoring();
	}
	public void stopAll() {
		try {
			this.getWorkerMap().forEach((s,m)->{
				getWorkerMap().get(s).shutdownGracefully();
				getBossMap().get(s).shutdownGracefully();
			});
		}catch(Exception e) {
			logger.error(e.toString(),e);
		}
	}
	public void monitoring() {
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(10000);
					logger.info("Server Count : {}", getServerInfoLoader().getServerRepository().size());
					logger.info("Handler Count : {}", getHandlerInfoLoader().getHandlerRepository().size());
					logger.info("Message Count : {}", getMessageInfoLoader().getMessageRepository().size());
					getServerInfoLoader().getServerRepository().forEach((n,s)->{
						logger.info("Server Name : {}", n);
						logger.info("Server Port : {}", s.getPort());
						logger.info("Server Handler : {}", s.getHandlerName());
						logger.info("Handler Class : {}", getHandlerInfoLoader().getHandlerRepository().get(s.getHandlerName()).getHandlerClass());
						logger.info("Message Name : {}", getHandlerInfoLoader().getHandlerRepository().get(s.getHandlerName()).getMessageName());
					});
					getServerInfoLoader().getServerRepository().forEach((n,s)->{
						try {
							NettyUtils.tcpTest(s.getIP(),s.getPort(),5000,getMessageInfoLoader().getMessageRepository().get(getHandlerInfoLoader().getHandlerRepository().get(s.getHandlerName()).getMessageName()).getRequestMessage());
						} catch (Exception e) {
							logger.error(e.toString(),e);
						}
					});
				}catch(Exception e) {
					logger.error(e.toString(),e);
				}
			}
		}).start();
	}

}
