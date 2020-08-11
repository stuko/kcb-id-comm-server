package com.kcb.id.comm.carrier.common;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class XMLUtils {
	
	/*
	 * XML 문서 객체를 생성해 주는 메서드(입력한 스트림으로 부터)
	 */
	public static Document getDocument(InputStream is) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		return doc;
	}
	
}
