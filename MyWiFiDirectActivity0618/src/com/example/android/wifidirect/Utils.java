package com.example.android.wifidirect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Utils {

	private final static String p2pInt = "p2p";

	public static String getIPFromMac() {
		/*
		 * method modified from:
		 * 
		 * http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
		 * 
		 * */
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				Log.d(WiFiDirectActivity.TAG,"arp内容:"+line ); //打印出arp的信息
				String[] splitted = line.split("\\s+");
				/*Log.d(WiFiDirectActivity.TAG,"splitted[0]:"+splitted[0]);
				Log.d(WiFiDirectActivity.TAG,"splitted[1]:"+splitted[1]);
				Log.d(WiFiDirectActivity.TAG,"splitted[2]:"+splitted[2]);
				Log.d(WiFiDirectActivity.TAG,"splitted[3]:"+splitted[3]);
				Log.d(WiFiDirectActivity.TAG,"splitted[4]:"+splitted[4]);
				Log.d(WiFiDirectActivity.TAG,"splitted[5]:"+splitted[5]);*/
				if (splitted != null && splitted.length >= 4) {
					// Basic sanity check
					String p2p_device = splitted[5];
					//如果是P2P设备连接的，则返回IP地址
					//可以使用匹配规则.*p2p-p2p0.*
					if (p2p_device.contains(p2pInt)){
						
						//String mac = splitted[3];
						//if (mac.matches(MAC)) {
						
							return splitted[0];
						//}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	
	public static String getMacFromFile() {
		/*
		 * method modified from:
		 * 
		 * http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
		 * 
		 * */
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/sys/class/net/p2p0/address"));
			String line;
			while ((line = br.readLine()) != null) {
				Log.d(WiFiDirectActivity.TAG,"p2p0内容:"+line ); //打印出arp的信息
				return line;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}


	public static String getLocalIPAddress() {
		/*
		 * modified from:
		 * 
		 * http://thinkandroid.wordpress.com/2010/03/27/incorporating-socket-programming-into-your-applications/
		 * 
		 * */
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					String iface = intf.getName();
					if(iface.matches(".*" +p2pInt+ ".*")){
						if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
							return getDottedDecimalIP(inetAddress.getAddress());
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		} catch (NullPointerException ex) {
			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		}
		return null;
	}

	public static String getDottedDecimalIP(byte[] ipAddr) {
		/*
		 * ripped from:
		 * 
		 * http://stackoverflow.com/questions/10053385/how-to-get-each-devices-ip-address-in-wifi-direct-scenario
		 * 
		 * */
		String ipAddrStr = "";
		for (int i=0; i<ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i]&0xFF;
		}
		return ipAddrStr;
	}
	
	
	
}
