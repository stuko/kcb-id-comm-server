package com.kcb.id.comm.carrier.core;

import com.kcb.id.comm.carrier.loader.ErrorInfoLoader;
import com.kcb.id.comm.carrier.loader.HeaderInfoLoader;
import com.kcb.id.comm.carrier.loader.MessageInfoLoader;

public interface Carrier {
	
	void setMessageInfoLoader(MessageInfoLoader loader);
	MessageInfoLoader getMessageInfoLoader();
	void setHeaderInfoLoader(HeaderInfoLoader loader);
	HeaderInfoLoader getHeaderInfoLoader();
	void setErrorInfoLoader(ErrorInfoLoader loader);
	ErrorInfoLoader getErrorInfoLoader();
	
}
