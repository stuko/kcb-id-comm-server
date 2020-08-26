package com.kcb.id.comm.carrier.loader;

import java.util.Map;

import com.kcb.id.comm.carrier.loader.impl.Field;

public interface DeEncoder {
	Field decodeAndEncode(Field f, Map<String,Object> msg) throws Exception;
}
