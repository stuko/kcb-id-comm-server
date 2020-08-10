package com.kcb.id.comm.carrier.handler;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.HandlerInfo;
import com.kcb.id.comm.carrier.loader.MessageInfo;

import io.netty.channel.ChannelHandlerContext;

public interface Handler {
	void onConnected(ChannelHandlerContext ctx, Map<String,MessageInfo> messageRepository, HandlerInfo handler);
	void onReceived(ChannelHandlerContext ctx, Object msg , Map<String,MessageInfo> messageRepository, HandlerInfo handler);
}
