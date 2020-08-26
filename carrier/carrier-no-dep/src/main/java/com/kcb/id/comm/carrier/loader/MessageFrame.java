package com.kcb.id.comm.carrier.loader;

import com.kcb.id.comm.carrier.loader.impl.Field;

public interface MessageFrame {
	Field[] getHeader();
	void setHeader(Field[] field);
	Field[] getBody();
	void setBody(Field[] field);
	Field[] getTail();
	void setTail(Field[] field);
}
