package com.fl.database;

public class Transfer {
	
	private int _id;
	private String filePath;
	private int isclient;
	private int device_id;
	private String device_address;
	private int isdelete = 0;
	private String time;
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public int getIsclient() {
		return isclient;
	}
	public void setIsclient(int isclient) {
		this.isclient = isclient;
	}
	public int getDevice_id() {
		return device_id;
	}
	public void setDevice_id(int device_id) {
		this.device_id = device_id;
	}
	public int getIsdelete() {
		return isdelete;
	}
	public void setIsdelete(int isdelete) {
		this.isdelete = isdelete;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDevice_address() {
		return device_address;
	}
	public void setDevice_address(String device_address) {
		this.device_address = device_address;
	}
	

}
