package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface HandlerInfoLoader extends Loader{
	Map<String,HandlerInfo> getHandlerRepository();
	void setHandlerRepository(Map<String,HandlerInfo> repo);
}
