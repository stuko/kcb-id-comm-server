package com.kcb.id.comm.carrier.handler;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface NettyClientHandler {
	public void handle(ByteBuf response);
}
