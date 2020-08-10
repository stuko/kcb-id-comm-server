package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface MessageInfoLoader extends Loader{
	Map<String,MessageInfo> getMessageRepository();
	void setMessageRepository(Map<String,MessageInfo> repo);
}
