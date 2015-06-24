package com.fl.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;


public class DateUtil {
	
	public static SimpleDateFormat  sf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static SimpleDateFormat  sfDate = new SimpleDateFormat("yyyy/MM/dd");
	public static SimpleDateFormat  sfTime = new SimpleDateFormat("HH:mm:ss");
	
	public static String getAllDate(Date date){
		return sf.format(date);
	}
	
	public static String getDate(String oldDate){
		Log.i("Wifidirect---date", oldDate);
		
		String current = sfDate.format(new Date());
		String date = oldDate.split(" ")[0];
		String time = oldDate.split(" ")[1];
		if(date.compareTo(current)<0){
			return date;
		}
		return time;
	}

}
