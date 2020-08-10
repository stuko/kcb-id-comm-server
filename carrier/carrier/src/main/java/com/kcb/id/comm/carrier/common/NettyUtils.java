package com.kcb.id.comm.carrier.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.impl.Field;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NettyUtils {

	static Logger logger = LoggerFactory.getLogger(NettyUtils.class);
	
	public static String[] rand = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B",
			"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z" };

	public static String getDate(String prefix) {
		SimpleDateFormat sdf = new SimpleDateFormat(prefix);
		return sdf.format(new java.util.Date(System.currentTimeMillis()));
	}

	public static String genId() {
		return genId("ID_");
	}

	public static String genId(String prefix) {
		return prefix + getDate("yyyyMMddHHmmssSSS") + ((int) (Math.random() * 1000000));
	}

	public static String getDateTime() {
		return getDate("yyyyMMddHHmmss");
	}

	public static String getDate() {
		return getDate("yyyyMMdd");
	}

	public static String getTime() {
		return getDate("HHmmss");
	}

	public static String getRandomString(int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int idx = (int) (Math.random() * (rand.length - 1));
			sb.append(rand[idx]);
		}
		return sb.toString();
	}

	public static String getTestMessage(Message message) throws Exception {
		StringBuilder sb = new StringBuilder();
		Field[] header = message.getHeader();
		Field[] body = message.getBody();
		Field[] tail = message.getTail();

		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			sb.append(getRandomString(Integer.parseInt(f.getLength())));
		}
		int repeatCount = 1;
		if ("true".equals(message.getRepeat())) {
			repeatCount = Integer.parseInt(message.getRepeatVariable());
		}
		for (int i = 0; i < repeatCount; i++) {
			for (int j = 0; j < body.length; j++) {
				Field f = body[j];
				sb.append(getRandomString(Integer.parseInt(f.getLength())));
			}
		}
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			sb.append(getRandomString(Integer.parseInt(f.getLength())));
		}
		return sb.toString();
	}
	
	public static byte[] getMessage(Message message, Map<String,Object> map) throws Exception {
		return getMessage2ByteBuf(message,map).array();
	}
	
	public static String fill(char one, int size) {
		char[] chars = new char[size];
		Arrays.fill(chars, one);
		String text = new String(chars);
		return text;
	}
	
	public static String fillJ11(String one, int size) {
		return one.repeat(size);
	}
	
	public static ByteBuf getMessage2ByteBuf(Message message, Map<String,Object> map) throws Exception {
		Field[] header = message.getHeader();
		Field[] body = message.getBody();
		Field[] tail = message.getTail();
		ByteBuf buf = Unpooled.buffer(256);
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			// logger.debug("{} 's length is {}", f.getName(), f.getLength());
			String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
			f.setValue(v);
			f.toPadding();
			f = message.encodeOrDecode(f);
			buf.writeBytes(f.getValueBytes());
		}
		int repeatCount = 1;
		if ("true".equals(message.getRepeat())) {
			repeatCount = Integer.parseInt(message.getRepeatVariable());
		}
		for (int i = 0; i < repeatCount; i++) {
			for (int j = 0; j < body.length; j++) {
				Field f = body[j];
				String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
				f.setValue(v);
				f.toPadding();
				f = message.encodeOrDecode(f);
				buf.writeBytes(f.getValueBytes());
			}
		}
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
			f.setValue(v);
			f.toPadding();
			f = message.encodeOrDecode(f);
			buf.writeBytes(f.getValueBytes());
		}
		return buf;
	}

	public static void tcpTest(String host, int port, int timeout, Message message) throws Exception {
		Socket socket = null;
		OutputStream os = null;
		InputStream is = null;
		StringBuffer sb = new StringBuffer();

		try {
			String msg = getTestMessage(message);
			logger.debug("Test[{}:{}] message : {}" ,host, port, msg);
			socket = new Socket(host, port);
			socket.setSoTimeout(timeout);
			os = socket.getOutputStream();
			is = socket.getInputStream();
			os.write(msg.getBytes());
			os.flush();
			byte[] data = new byte[256];
			int len = 0;
			while ((len = is.read(data)) > 0) {
				logger.debug("read byte is {}", len);
				byte[] readBuffer = new byte[len];
				System.arraycopy(data, 0, readBuffer, 0, len);
				sb.append(new String(readBuffer));
			}
			os.close();
			is.close();
			socket.close();
			os = null;
			is = null;
			socket = null;
			logger.debug("Result message : {}" , sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (Exception e) {
			}
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
			}
		}

	}
}
