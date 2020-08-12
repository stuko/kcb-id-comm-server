package com.kcb.id.comm.carrier.core.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.kcb.id.comm.carrier.common.NettyUtils;
import com.kcb.id.comm.carrier.core.Carrier;
import com.kcb.id.comm.carrier.handler.Handler;
import com.kcb.id.comm.carrier.handler.impl.NettyAdapter;
import com.kcb.id.comm.carrier.loader.HandlerInfoLoader;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.MessageInfo;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;
import com.kcb.id.comm.carrier.loader.ServerInfo;
import com.kcb.id.comm.carrier.loader.ServerInfoLoader;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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

	/*
	 * Monitoring 과 테스트를 위한 것.
	 */
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
	
	/*
	 * XML로 부터 여러 전문을 읽어서 MessageInfo 라는 객체에 채워주는 클래스 
	 */
	@Autowired
	MessageInfoLoader messageInfoLoader;
	/*
	 * XML로 부터 여러 서버정보를 읽어서 ServerInfo 라는 객체에 채워주는 클래스 
	 */
	@Autowired
	ServerInfoLoader serverInfoLoader;
	/*
	 * XML로 부터 여러 핸들러 정보를 읽어서 HandlerInfo 라는 객체에 채워주는 클래스 
	 */
	@Autowired
	HandlerInfoLoader handlerInfoLoader;
	
	/*
	 * ServerInfo XML 파일을 읽어올 경로 application.yml에 정의됨
	 */
	@Value( "${carrier.path.server}" )
	private String serverPath;
	/*
	 * MessageInfo XML 파일을 읽어올 경로 application.yml에 정의됨
	 */
	@Value( "${carrier.path.message}" )
	private String messagePath;
	/*
	 * HandlerInfo XML 파일을 읽어올 경로 application.yml에 정의됨
	 */
	@Value( "${carrier.path.handler}" )
	private String handlerPath;

	/*
	 * ServerInfo XML을 URL로 부터 읽어올 주소 application.yml에 정의됨
	 */
	@Value( "${carrier.url.server}" )
	private String serverUrl;
	/*
	 * MessageInfo XML을 URL로 부터 읽어올 주소 application.yml에 정의됨
	 */
	@Value( "${carrier.url.message}" )
	private String messageUrl;
	/*
	 * HandlerInfo XML을 URL로 부터 읽어올 주소 application.yml에 정의됨
	 */
	@Value( "${carrier.url.handler}" )
	private String handlerUrl;
	
	/*
	 * Netty로 실행한 서버들을 나중에 종료시키기 위해, 해당 워커를 맵에 저장하기 위한 클래스
	 */
	Map<String,EventLoopGroup> bossMap = new HashMap<>();
	Map<String,EventLoopGroup> workerMap = new HashMap<>();
	
	int boss = 1, worker = 1;
	
	/*
	 * 스프링의 어플리케이션 컨텍스트, 빈들을 참조하기 위한 용도
	 */
	@Autowired
	private ApplicationContext context;
	
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
	
	
	/*
	 * ServerInfo 정보를 가지고, Netty TCP 통신 서버를 실행 한다.
	 */
	public void start(ServerInfo server) {
		EventLoopGroup bossGroup = new NioEventLoopGroup(this.getBoss()); 
		EventLoopGroup workerGroup = new NioEventLoopGroup(this.getWorker());
		try {
			ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) 
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
								new NettyAdapter() {
									public void onConnected(ChannelHandlerContext ctx) {
										logger.debug("{}:{} is connected",server.getIP(),server.getPort());
										String handlerClass = getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()).getHandlerClass();
										try {
											Object object = context.getBean(handlerClass);
											if(object != null) {
												((Handler)object).onConnected(ctx
														, getMessageInfoLoader().getMessageRepository()
														, getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()));
											}
										} catch (Exception e) {
											logger.error(e.toString(),e);
										}
									}
									public void onReceived(ChannelHandlerContext ctx, Object msg) {
										logger.debug("{}:{} is received",server.getIP(),server.getPort());
										String handlerClass = getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()).getHandlerClass();
										try {
											Object object = context.getBean(handlerClass);
											if(object != null) {
												((Handler)object).onReceived(ctx
														, msg
														, getMessageInfoLoader().getMessageRepository()
														, getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()));
											}
										} catch (Exception e) {
											logger.error(e.toString(),e);
											try {
												MessageInfo messageInfo = getMessageInfoLoader().getMessageRepository().get(getHandlerInfoLoader().getHandlerRepository().get(server.getHandlerName()).getMessageName());
												if (messageInfo != null) {
													String exception = e.getClass().getSimpleName();
													logger.debug("[{}] Exception and choice {} " ,messageInfo.getName(), exception);
													logger.debug("Exception Map is {} " , messageInfo.getExceptionMessageMap());
													Message exMsg = messageInfo.getExceptionMessageMap().get(exception);
													if(exMsg != null) {
														logger.debug("Exception and find error message");
														ByteBuf buf = exMsg.toByteBuf();
														ctx.write(buf);
														if (buf != null && buf.refCnt() != 0)
															buf.release();
													}
												}
											} catch (Exception ee) {
												logger.error("Severe Error occurred...." + e.toString() + " in " + ee.toString() + "", ee);
											}
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
	/*
	 * ServerInfo 를 통해, Netty 통신 서버를 종료 한다.
	 */
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
	/*
	 * XML 정보를 URL을 통해 가져올 경우 아래 메서드가 호출 된다.
	 */
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
				logger.debug("Server xml path exists... {}", this.getServerPath());
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
				logger.debug("Message xml path exists... {}", this.getMessagePath());
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
				logger.debug("Handler xml path exists... {}", this.getHandlerPath());
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
		// this.monitoring();
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
	/*
	 * 서버가 정상인지 확인 하기 위해,
	 * 테스트 전문을 10초 간격으로 전송 한다.
	 */
	public void monitoring() {
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(1000);
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
						executor.submit(
						  ()->{
							try {
								NettyUtils.tcpTest(s.getIP(),s.getPort(),5000,getMessageInfoLoader().getMessageRepository().get(getHandlerInfoLoader().getHandlerRepository().get(s.getHandlerName()).getMessageName()).getRequestMessage());
							} catch (Exception e) {
								logger.error(e.toString(),e);
							}
						  });
					});
				}catch(Exception e) {
					logger.error(e.toString(),e);
				}
			}
		}).start();
	}

}
