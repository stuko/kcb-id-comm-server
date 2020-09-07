package com.kcb.id.comm.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class CarrierTestClient {

	public static void main(String[] args) {
		try {
			URL u = new URL("http://localhost:8080/kcb/request");
			URLConnection con = u.openConnection();
			InputStream is = con.getInputStream();
			OutputStream os = con.getOutputStream();
			os.write("TEST".getBytes());
			os.flush();
			os.close();
			is.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
