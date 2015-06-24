package com.fl.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	
	private DBHelper helper;
	private SQLiteDatabase db;
	
	public DBManager(Context context){
		//activity初始化时实例化
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}
	
	public void addDevice(List<Device> devices){
		db.beginTransaction();
		int deviceNum = 0;
		try{
			for(Device device:devices){
				Cursor c = queryTheDevice(device.getDeviceAddress());
				while(c.moveToNext())
					deviceNum = c.getInt(0);//判断device表中是否已存在相应的设备
				if(deviceNum == 0)
				db.execSQL("INSERT INTO device VALUES(null, ?, ?)", new Object[]{device.getDeviceAddress(),device.getDeviceName()});
			}
			db.setTransactionSuccessful();//设置事务成功完成
		}finally{
			db.endTransaction();
		}
	}
	
	
	public void addTransfer(List<Transfer> transfers){
		db.beginTransaction();
		try{
			for(Transfer transfer:transfers){
				db.execSQL("INSERT INTO transfer VALUES(null, ?, ?, 0, ?, ?, ?)", new Object[]{transfer.getFilePath(), transfer.getIsclient(), transfer.getDevice_address(),
						transfer.getIsdelete(), transfer.getTime()});
			}
			db.setTransactionSuccessful();//设置事务成功完成
		}finally{
			db.endTransaction();
		}
	}
	
	public void updateTransfer(int id){
		ContentValues cv = new ContentValues();
		cv.put("isdelete", 1);
		db.update("transfer", cv, "_id = ?", new String[]{String.valueOf(id)});
	}
	
	public List<Info> query(String deviceAddress){
		ArrayList<Info> infos = new ArrayList<Info>();
		Cursor c;
		if(deviceAddress==null){
			c = queryTheCurse();
		}else{
			c = queryTheCurse(deviceAddress);
		}
		while(c.moveToNext()){
			Info info = new Info();
			info.setDevice_name(c.getString(c.getColumnIndex("device_name")));
			info.setFile_path(c.getString(c.getColumnIndex("file_path")));
			info.setIsclient(c.getInt(c.getColumnIndex("isclient")));
			info.setTime(c.getString(c.getColumnIndex("time")));
		    infos.add(info);
		}
		c.close();
		return infos;
	}
	
	
	public Cursor queryTheCurse(String address){
		Cursor c = db.rawQuery("SELECT d.device_name,t._id,t.file_path,t.isclient,t.time FROM device as d,transfer as t WHERE d.device_address = ? and d.device_address = t.device_address" , new String[]{address});
		return c;
	}
	
	public Cursor queryTheDevice(String address){
		Cursor c = db.rawQuery("SELECT count(device_name) FROM device WHERE device_address = ?" , new String[]{address});
		return c;
	}
	
	public Cursor queryTheCurse(){
		Cursor c = db.rawQuery("SELECT d.device_name,t._id,t.file_path,t.isclient,t.time FROM device as d,transfer as t WHERE d.device_address = t.device_address" , null);
        return c;
	}

}
