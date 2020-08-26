package com.kcb.id.comm.carrier.common;

public class StringUtils {
	public static boolean chkNull(Object src) {
		if(src != null && !"".equals(((String)src).trim()) && !"null".equals(((String)src).trim())) return true;
		else return false;
	}
}
