package com.kcb.id.comm.carrier.forward;

import java.util.Map;

public interface ForwardHelper {
	String[] findForwardInfo(Map<String,Object> bodyMap) throws Exception;
}
