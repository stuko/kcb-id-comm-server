package com.kcb.id.comm.carrier.loader;

import java.io.InputStream;

public interface Loader {
	enum TYPE {XML, JSON , YML , PROPERTIES};
	void setType(TYPE type);
	TYPE getType();
	void setLoaderFilePath(String file);
	String getLoaderFilePath();
	void load();
	void load(InputStream is);
}
