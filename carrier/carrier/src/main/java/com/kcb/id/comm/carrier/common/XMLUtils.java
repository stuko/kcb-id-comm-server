package com.kcb.id.comm.carrier.common;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XMLUtils {
	
	public static Document getDocument(InputStream is) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		return doc;
	}
	
}
