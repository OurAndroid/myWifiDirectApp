// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import java.util.ArrayList;

import mySocket.TransferClient;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.fl.database.DBManager;
import com.fl.database.Transfer;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    //public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String SERIALIZATION_VALUE = "value";
    public static final String EXTRAS_MAC = "macIP";
    public static final String EXTRAS_DEVICE_NAME = "deviceName";
    
    private Handler mHandler = null;
    private DeviceDetailFragment detailFragment ;
    private DBManager dbManager = null;
    
    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*系统服务要在oncreate中创建，不能在构造函数中？
     * (non-Javadoc)
     * @see android.app.IntentService#onCreate()
     */
    @Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		MainApplication app = (MainApplication) this.getApplication();
		//mHandler = app.getmHandler();
		mHandler = new NewFileListFragment.MyHandler();
        dbManager = new DBManager(getApplicationContext());    

	}

	/*
     * (non-Javadoc)打开socket，并将文件读入socket输入流，传递给远程主机
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            //获取文件名List
        	ArrayList<String> filelist = intent.getStringArrayListExtra(EXTRAS_FILE_PATH);
            //获取IP
        	String host = intent.getExtras().getString(EXTRAS_ADDRESS); //intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS)
            //获取设备信息
        	String macIP = intent.getExtras().getString(EXTRAS_MAC);
        	String deviceName = intent.getExtras().getString(EXTRAS_DEVICE_NAME);
        	
        	if(dbManager == null)
        	dbManager = new DBManager(getApplicationContext());    
        	Transfer t = new Transfer();
        	t.setDevice_address(macIP);
        	t.setIsclient(1);
        	
        	//创建TransferClient对象
            new TransferClient(filelist , host , detailFragment, dbManager, t, mHandler).service();
        	
            //Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

           /* try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
               // ContentResolver cr = context.getContentResolver();
                File file = new File(fileUri);
                InputStream is = null;
                try {
                	is = new FileInputStream(file);
                   // is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(WiFiDirectActivity.TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream);
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }*/

        }
    }
}
