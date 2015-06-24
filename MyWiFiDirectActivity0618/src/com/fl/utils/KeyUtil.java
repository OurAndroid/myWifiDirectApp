package com.fl.utils;

public class KeyUtil {
	
	public static String getKey(String str){
		return str+"/"+System.currentTimeMillis();
	}

}
