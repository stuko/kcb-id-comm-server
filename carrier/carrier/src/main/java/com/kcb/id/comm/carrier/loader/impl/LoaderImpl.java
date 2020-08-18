package com.kcb.id.comm.carrier.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.kcb.id.comm.carrier.common.XMLUtils;
import com.kcb.id.comm.carrier.loader.Loader;

public abstract class LoaderImpl implements Loader {
	
	static Logger logger = LoggerFactory.getLogger(LoaderImpl.class);
	TYPE type;
	String loaderFilePath;
	
	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public String getLoaderFilePath() {
		return loaderFilePath;
	}

	public void setLoaderFilePath(String loaderFilePath) {
		this.loaderFilePath = loaderFilePath;
	}
	
	private void loadResourceDir() {
		PathMatchingResourcePatternResolver  patternResolver = new PathMatchingResourcePatternResolver();
		logger.debug("####### load resource pattern #######");
		logger.debug("Pattern is {}" , "" + this.getLoaderFilePath() + "*.xml");
		logger.debug("####### load resource pattern #######");
		this.getFilesFromClasspath(this.getLoaderFilePath(), patternResolver).forEach(f->{
			try {
				logger.debug("####### load resource files #######");
				logger.debug("File is {}" , f.getAbsolutePath());
				logger.debug("####### load resource files #######");
				this.load(new FileInputStream(f));
			} catch (FileNotFoundException e) {
				logger.error(e.toString(),e);
			}
		});
	}
	
	private Stream<File> getFilesFromClasspath(String path,PathMatchingResourcePatternResolver resolver) {
	    try {
	        return Arrays.stream(resolver.getResources("" + path + "*.xml"))
	                .filter(Resource::exists)
	                .map(resource -> {
	                    try {
	                        return resource.getFile();
	                    } catch (IOException e) {
	                        return null;
	                    }
	                })
	                .filter(Objects::nonNull);
	    } catch (IOException e) {
	        logger.error("Failed to get definitions from directory {}", path, e);
	        return Stream.of();
	    }
	}
	
	private File getResourceFile() throws FileNotFoundException {
		return ResourceUtils.getFile("classpath*:config/servers/servers.xml"); // this.getLoaderFilePath());
	}
	
	private void loadResourceFileList() throws FileNotFoundException {
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
		File[] files = this.getResourceFile().listFiles(filter);
		if (files == null || files.length == 0) {
			logger.debug("does not exist meta file. {} ", this.getResourceFile().getAbsolutePath());
			return;
		}
		for (int i = 0; i < files.length; i++) {
			String file = files[i].getName();
			String fileName = file.substring(0, file.lastIndexOf('.'));
			logger.debug("File Name= {} ", fileName);
			this.load(new FileInputStream(files[i]));
		}
	}
	
	@Override
	public void load() {
		try {
			if (this.getLoaderFilePath() == null)
				return;
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(this.getLoaderFilePath());
			for(Resource res : resources) {
				this.load(res.getInputStream());
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
