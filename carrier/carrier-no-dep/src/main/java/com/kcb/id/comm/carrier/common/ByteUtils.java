package com.kcb.id.comm.carrier.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kcb.id.comm.carrier.loader.DeEncoder;
import com.kcb.id.comm.carrier.loader.Message;
import com.kcb.id.comm.carrier.loader.impl.Field;


public class ByteUtils {

	static Logger logger = LoggerFactory.getLogger(ByteUtils.class);
	
	/*
	 * 전문의 값을 랜덤하게 자동 생성해 주기 위한 기본 배열들
	 */
	public static String[] rand = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B",
			"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z" };
	
	/*
	 * 현재 날짜를 입력한 포맷으로 보여 주는 메서드
	 */
	public static String getDate(String prefix) {
		SimpleDateFormat sdf = new SimpleDateFormat(prefix);
		return sdf.format(new java.util.Date(System.currentTimeMillis()));
	}

	
	/*
	 * ID 생성기
	 */
	public static String genId() {
		return genId("ID_");
	}

	/*
	 * 날짜와 랜덤값을 통해 ID를 생성해 주는 메서드
	 */
	public static String genId(String prefix) {
		return prefix + getDate("yyyyMMddHHmmssSSS") + ((int) (Math.random() * 1000000));
	}

	/*
	 * 현재 날짜 일시까지를 스트링으로 리턴해 주는 메서드
	 */
	public static String getDateTime() {
		return getDate("yyyyMMddHHmmss");
	}

	/*
	 * 현재 날짜를 스트링으로 리턴해 주는 메서드
	 */
	public static String getDate() {
		return getDate("yyyyMMdd");
	}

	/*
	 * 현재 시간을 스트링으로 리턴해 주는 메서드
	 */
	public static String getTime() {
		return getDate("HHmmss");
	}

	/*
	 * 입력한 길이만큼 랜덤 문자를 생성해주는 메서드
	 */
	public static String getRandomString(int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int idx = (int) (Math.random() * (rand.length - 1));
			sb.append(rand[idx]);
		}
		return sb.toString();
	}

	/*
	 * 입력한 전문 형식에 맞게 랜덤한 전문 데이터를 만들어 주는 메서드(테스트 전문용)
	 */
	public static String getTestMessage(Message message) throws Exception {
		StringBuilder sb = new StringBuilder();
		Field[] header = message.getHeader();
		Field[] body = message.getBody();
		Field[] tail = message.getTail();

		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			sb.append(getRandomString(Integer.parseInt(f.getLength())));
		}
		for (int i = 0; i < body.length; i++) {
			Field f = body[i];
			sb.append(getRandomString(Integer.parseInt(f.getLength())));
		}
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			sb.append(getRandomString(Integer.parseInt(f.getLength())));
		}
		return sb.toString();
	}
	
	/*
	 * 입력한 전문과 , 맵형태의 데이터를 가지고 
	 * 전문에 실제 데이터를 입력해서 
	 * 바이트배열로 통신이 가능한 데이터로 만들어 주는 메서드
	 */
	public static byte[] getMessage(Message message, Map<String,Object> map) throws Exception {
		return getMessage2Byte(message,map);
	}
	
	/*
	 * 입력한 문자를 입력한 사이즈 만큼 스트링으로 만들어 주는 메서드
	 */
	public static String fill(char one, int size) {
		char[] chars = new char[size];
		Arrays.fill(chars, one);
		String text = new String(chars);
		return text;
	}
	
	/*
	 * 입력한 문자를 입력한 사이즈 만큼 스트링으로 만들어 주는 메서드 JDK 11버전용
	 */
	public static String fillJ11(String one, int size) {
		return one.repeat(size);
	}
	
	public static byte[] getMessage2Byte(Message message, Map<String,Object> map) throws Exception {
		Field[] header = message.getHeader();
		Field[] body = message.getBody();
		Field[] tail = message.getTail();
		
		byte[] buf = new byte[message.getLength()];
		byte[] tmp = null;
		int pos = 0;
		
		for (int i = 0; i < header.length; i++) {
			Field f = header[i];
			String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
			f.setValue(v);
			f = ((DeEncoder)message).decodeAndEncode(f,message.toHashMap());
			f.toPadding();
			tmp = f.getValueBytes();
			logger.debug("Header's toByte : {} -> [{}]" , f.getName(), new String(tmp));
			System.arraycopy(tmp, 0, buf, pos, tmp.length);
			pos += tmp.length;
		}
		for (int i = 0; i < body.length; i++) {
			Field f = body[i];
			String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
			f.setValue(v);
			f = ((DeEncoder)message).decodeAndEncode(f,message.toHashMap());
			f.toPadding();
			tmp = f.getValueBytes();
			logger.debug("Header's toByte : {} -> [{}]" , f.getName(), new String(tmp));
			System.arraycopy(tmp, 0, buf, pos, tmp.length);
			pos += tmp.length;
		}
		for (int i = 0; i < tail.length; i++) {
			Field f = tail[i];
			String v = map.get(f.getName()) == null ?  fill(' ',Integer.parseInt(f.getLength())): (String)map.get(f.getName());
			f.setValue(v);
			f = ((DeEncoder)message).decodeAndEncode(f,message.toHashMap());
			f.toPadding();
			tmp = f.getValueBytes();
			logger.debug("Header's toByte : {} -> [{}]" , f.getName(), new String(tmp));
			System.arraycopy(tmp, 0, buf, pos, tmp.length);
			pos += tmp.length;
		}
		return buf;
	}

	public static byte[] send(String host, int port, int timeout, byte[] msg) throws Exception{
		Socket socket = null;
		OutputStream o = null;
		InputStream i = null;
		DataOutputStream os = null;
		DataInputStream is = null;
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout(timeout);
			o = socket.getOutputStream();
			i = socket.getInputStream();
			is = new DataInputStream(i);
			os = new DataOutputStream(o);
			os.write(msg);
			os.flush();
			byte[] data = new byte[256];
			int len = 0;
			List<byte[]> byteArray = new ArrayList<>();
			int totalLen = 0;
			while ((len = is.read(data)) > 0) {
				byte[] readBuffer = new byte[len];
				System.arraycopy(data, 0, readBuffer, 0, len);
				byteArray.add(readBuffer);
				totalLen += len;
				if(len <= 256)break;
				data = new byte[256];
			}
			os.close();
			is.close();
			socket.close();
			os = null;
			is = null;
			socket = null;
			byte[] response = new byte[totalLen];
			int pos = 0; 
			for(byte[] buf: byteArray){
				System.arraycopy(buf, 0, response, pos, buf.length);
				pos += buf.length;
			}
			return response;
		} catch (Exception e) {
			logger.error(e.toString(),e);
			throw e;
		} finally {
			try {if (os != null)os.close();} catch (Exception e) {}
			try {if (is != null)is.close();} catch (Exception e) {}
			try {if (socket != null)socket.close();} catch (Exception e) {}
		}
	}
	
	/*
	 * 입력한 서버의 아이피와 포트로 테스트 전문을 발송해 주는 메서드
	 */
	public static void tcpTest(String host, int port, int timeout, Message message) throws Exception {
		Socket socket = null;
		OutputStream o = null;
		InputStream i = null;
		BufferedOutputStream os = null;
		BufferedInputStream is = null;
		StringBuffer sb = new StringBuffer();
		String key = "K" + (int)(Math.random()*100000000);
		try {
			long start = System.currentTimeMillis();
			String msg = getTestMessage(message);
			logger.info("####################################################");
			logger.info("Test[{}:{}] request message : {}" ,host, port, msg);
			logger.info("####################################################");
			logger.info("TCP TEST REQUEST");
			logger.info("####################################################");
			logger.info("request message : [{}] -> [{}]" , key, msg);
			logger.info("####################################################");			
			
			socket = new Socket(host, port);
			socket.setSoTimeout(timeout);
			o = socket.getOutputStream();
			i = socket.getInputStream();
			is = new BufferedInputStream(i);
			os = new BufferedOutputStream(o);
			os.write(msg.getBytes());
			os.flush();
			byte[] data = new byte[256];
			int len = 0;
			while ((len = is.read(data)) > 0) {
				logger.info("read byte is {}", len);
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
			logger.info("####################################################");
			logger.info("TCP TEST RESULT");
			logger.info("####################################################");
			logger.info("response message : [{}] -> [{}] [{}]" ,key, (System.currentTimeMillis()-start)+" (msec)"  , sb.toString());
			logger.info("####################################################");			
		} catch (Exception e) {
			if(e.toString().indexOf("SocketTimeoutException") >= 0) {
				logger.info("error message : [{}] " ,key);
			}
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

	public static void main(String[] args) {
		try {
			System.out.println("["+new String(ByteUtils.send("192.168.57.163", 9006, 5000, "TESTASFDSFARERASER".getBytes()))+"]");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
