package com.kcb.id.comm.carrier.loader;

import java.io.File;
import java.io.InputStream;

public interface Loader {
	enum TYPE {XML, JSON , YML , PROPERTIES};
	void setType(TYPE type);
	TYPE getType();
	void setLoaderFile(File file);
	File getLoaderFile();
	void load();
	void load(InputStream is);
}
