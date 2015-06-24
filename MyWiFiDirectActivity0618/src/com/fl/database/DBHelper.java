package com.fl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	//定义数据库的名称和版本
	private static final String DATABASE_NAME = "fileTransfer.db";
	private static final int DATABASE_VERSION = 1;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//创建设备表
		db.execSQL("CREATE TABLE IF NOT EXISTS transfer (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"file_path VARCHAR, isclient INTEGER, device_id INTEGER, device_address VARCHAR, isdelete INTEGER, time VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS device (_id INTEGER PRIMARY KEY AUTOINCREMENT, device_address " +
				"VARCHAR, device_name VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("ALTER TABLE transfer ADD COLUMN other STRING");  
		
	}

}
