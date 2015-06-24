package com.example.android.wifidirect;

import com.fl.database.DBManager;
import com.fl.database.Transfer;

import mySocket.TransferServer;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class FileRecieveService extends IntentService {

  

	public static final String ACTION_RECEIVE_FILE = "com.example.android.wifidirect.RECEIVE_FILE";
	public static final String EXTRAS_MAC = "macIP";
	public static final String EXTRAS_DEVICE_NAME = "deviceName";
	
	public static TransferServer transferServer = null;
	public Handler mHandler;
	private DBManager dbManager = null;
	
	
	  public FileRecieveService(String name) {
			super(name);
			
		}
	  
	  public FileRecieveService(){
		  super("FileRecieveService");
	  }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		MainApplication app = (MainApplication) this.getApplication();
		//mHandler = app.getmHandler();
		dbManager = new DBManager(getApplicationContext());
		mHandler = new NewFileListFragment.MyHandler();
	}


//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		// TODO Auto-generated method stub
//		
//		
//			Log.i("fl----", "fileRecieveService进入");
//			//获取设备信息
//        	String macIP = intent.getExtras().getString(EXTRAS_MAC);
//        	String deviceName = intent.getExtras().getString(EXTRAS_DEVICE_NAME);
//        	
//        	if(dbManager == null)
//            	dbManager = new DBManager(getApplicationContext());    
//            	Transfer t = new Transfer();
//            	t.setDevice_address(macIP);
//            	t.setIsclient(0);//设置为接收端
//            	
//			try {
//				transferServer = new TransferServer(mHandler, t, dbManager);
//				transferServer.service();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		
//		
//		return START_NOT_STICKY;
//		//return super.onStartCommand(intent, flags, startId);
//	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopSelf();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("fl----", "fileRecieveService进入");
		//获取设备信息
    	String macIP = intent.getExtras().getString(EXTRAS_MAC);
    	String deviceName = intent.getExtras().getString(EXTRAS_DEVICE_NAME);
    	
    	if(dbManager == null)
        	dbManager = new DBManager(getApplicationContext());    
        	Transfer t = new Transfer();
        	t.setDevice_address(macIP);
        	t.setIsclient(0);//设置为接收端
        	
		try {
			transferServer = new TransferServer(mHandler, t, dbManager);
			transferServer.service();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
	}
	
	

}
