package com.kcb.id.comm.carrier.loader;

import java.util.Map;

public interface ServerInfoLoader extends Loader{
	Map<String,ServerInfo> getServerRepository();
	void setServerRepository(Map<String,ServerInfo> repo);
}
