package com.kcb.id.comm.carrier.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.common.XMLUtils;
import com.kcb.id.comm.carrier.loader.Loader;

public abstract class LoaderImpl implements Loader {
	
	static Logger logger = LoggerFactory.getLogger(LoaderImpl.class);
	TYPE type;
	File loaderFile;
	
	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public File getLoaderFile() {
		return loaderFile;
	}

	public void setLoaderFile(File loaderFile) {
		this.loaderFile = loaderFile;
	}

	@Override
	public void load() {
		try {
			if (this.getLoaderFile() == null)
				return;
			TYPE fileType = this.getType();
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File directory, String fileName) {
					if (fileType == TYPE.XML)
						return fileName.endsWith(".xml");
					else if (fileType == TYPE.JSON)
						return fileName.endsWith(".json");
					else if (fileType == TYPE.YML)
						return fileName.endsWith(".yml");
					else if (fileType == TYPE.PROPERTIES)
						return fileName.endsWith(".properties");
					else
						return fileName.endsWith(".xml");
				}
			};
			File[] files = this.getLoaderFile().listFiles(filter);
			if (files == null || files.length == 0) {
				logger.debug("does not exist meta file. {} ", this.getLoaderFile().getAbsolutePath());
				return;
			}
			for (int i = 0; i < files.length; i++) {
				String file = files[i].getName();
				String fileName = file.substring(0, file.lastIndexOf('.'));
				logger.debug("File Name= {} ", fileName);
				this.load(new FileInputStream(files[i]));
			}
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}
	
	public void load(InputStream is) {
		try {
			Document doc = XMLUtils.getDocument(is);
			if (doc == null) {
				logger.error("xml file error");
				return;
			}
			NodeList nodeList = doc.getElementsByTagName("SET");
			if (nodeList == null) {
				logger.error("xml does not contains SET element");
				return;
			}
			int LoopCount = 1;
			this.parseMe(nodeList);
		} catch (Exception e) {
			logger.error(e.toString(),e);
		} finally {
			try {if (is != null)is.close();} catch (Exception e) {}
		}
	}
	
	public abstract void parseMe(NodeList nodeList);
}
