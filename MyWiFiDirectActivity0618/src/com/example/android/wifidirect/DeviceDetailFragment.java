/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import mySocket.NotificationBean;
import mySocket.TransferClient;
import mySocket.TransferServer;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;
import com.example.android.wifidirect.DeviceListFragment.DeviceListListener;
import com.fl.database.DBManager;
import com.fl.database.Device;
//import android.support.v4.app.Activity;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	private String tag = "WifiActivity----";
	public static final String IP_SERVER = "192.168.49.1";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pDevice connectDevice;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    private View rootView = null ;
    public static List<Map<String,Object>> list = new ArrayList<Map<String , Object>>() ;
  
    
    
	private static String TransfilePath = null ;
	private static String ReceivefilePath = null ;
	private  int  port = 10000 ;
	public static TransferServer transferServer = null;
	private DeviceDetailFragment detailfragment ;
	public static NotificationManager manager ;
	public static MyHandler myHandle ;
	
	public DBManager dbManager = null;
	public Future<String> msgFuture;
	
    public  List<Map<String, Object>> getList() {
		return list;
	}
    
    public  void clearList(){
    	list.clear();
    }
	
    public DeviceDetailFragment getDetailFragment(){
    	return this ;
    }
    public NotificationManager getManager(){
    	return manager ;
    }
    
    /*public TransferServer getTransferServer(){
    	return transferServer ;
    }*/
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
       // msgFuture =  TransferServer.executorService.submit(new SendMsgServer());
    	dbManager = new DBManager(getActivity());
        mContentView = inflater.inflate(R.layout.device_detail, null);
        manager = (NotificationManager) getActivity().getSystemService(MainApplication.getContext().NOTIFICATION_SERVICE);
        myHandle = new MyHandler();
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
        	//点击连接按钮开始连接
            @Override
            public void onClick(View v) {
            	connectDevice = device;//点击连接后初始化设备信息
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;     //这里可能有问题，设备的mac地址
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	connectDevice = null;
                    	getActivity().stopService(new Intent(getActivity(), FileRecieveService.class));
                    	getActivity().stopService(new Intent(getActivity(), FileTransferService.class));
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
						
                        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");   //�����ļ���������ͼƬ
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);*/
                    	
                    	
                    	/**
                    	 * 在文件管理器中实现传输
                    	Intent intent = new Intent(getActivity(),FileBrowserActivity.class);
                    	startActivityForResult(intent,CHOOSE_FILE_RESULT_CODE);
                        */
                    	getConnectDevice();//获取发送端设备信息
                    	DeviceDetailListener listener = (DeviceDetailListener) getActivity();
                    	listener.slideTab(1);
                    	getDetailFragment().setStatusText("开始发送文件...");
                    }
                });
				
		mContentView.findViewById(R.id.btn_start_server).setOnClickListener(
		new View.OnClickListener(){
			@Override 
			public void onClick(View v){
//				
				getConnectDevice();
				String macAddress = connectDevice.deviceAddress;	
    			String deviceName = connectDevice.deviceName;
    			
    		    addDevice();

    	        Intent serviceIntent = new Intent(getActivity(), FileRecieveService.class);
    	        serviceIntent.setAction(FileRecieveService.ACTION_RECEIVE_FILE);
    	        serviceIntent.putExtra("macIP", macAddress);
    	        serviceIntent.putExtra("deviceName", deviceName);
				getDetailFragment().setStatusText("开始接收...");
//				new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text),getDetailFragment())
//					.execute();		
    	        getActivity().startService(serviceIntent);
    	        
			
			}
		}
		);

        return mContentView;
    }

    public View getrootView(){
    	return mContentView ;
    }
    
    
    /*
     * 连接成功后调用(non-Javadoc)
     */
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }else{//为接收端，从文件中读取发送端的mac信息，这里可以考虑多种策略实现
        	
        }
        
        this.info = info;
      
    	//this.getView().setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.VISIBLE);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        mContentView.findViewById(R.id.btn_start_server).setVisibility(View.VISIBLE);
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        /**
         * 为GroupOwner打开接收线程，为组员打开client线程,分别接收mac和ip信息
         * 如果从文件中获取ip和mac信息失败，则从这里获取
         */
        if (info.groupFormed && info.isGroupOwner) {
           Log.i(tag, "im  the owner,dont know the ip");
          
           msgFuture =  TransferServer.executorService.submit(new SendMsgServer());
        } 
         else if (info.groupFormed && !info.isGroupOwner) {
        	//msgFuture.cancel(true);//停止server线程
        	String ipInfo = info.groupOwnerAddress.getHostAddress();
        	Log.i(tag, "主机ip地址"+ipInfo);
        	
       	    msgFuture = TransferClient.executorService.submit(new SendMsgClient(ipInfo));
             }   
                
        // hide the connect button
    	//不隐藏连接按钮
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        //显示断开按钮
    	FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
    	DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
    	fragment.showDisconnect();
    	//不显示断开的按钮
        // mContentView.findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
    }

	
	
    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        //this.getView().setVisibility(View.VISIBLE);
        mContentView.setVisibility(View.VISIBLE);
        Log.d(WiFiDirectActivity.TAG, this.getView().toString());
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
	 ���UI
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_server).setVisibility(View.GONE);
        //this.getView().setVisibility(View.GONE);
        mContentView.setVisibility(View.GONE);
    }
    
    public void setStatusText(String status){
    	TextView statusText;
    	statusText = (TextView)mContentView.findViewById(R.id.status_text);
    	statusText.setText(status);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private DeviceDetailFragment detailFragment ;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText , DeviceDetailFragment detailFragment) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.detailFragment = detailFragment ;
        }

        @Override
        protected String doInBackground(Void... params) {
			int port = 10000 ; 
        	try {
				transferServer = new TransferServer(detailFragment);
				transferServer.service();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	finally{
        		
        	}
        	return null;
           
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
            Map<String , Object> map = new HashMap<String, Object>();
            if(TransfilePath != null)
            {
            	map.put("file_path", TransfilePath);
            	map.put("img", R.drawable.wenjian);
            	list.add(map);
            }
            TransfilePath = null ;
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    
   
    /**
     * 通过双向通信，发送自己的mac信息并接收server端mac信息，这里为什么不用AsyncTask
     * @author Administrator
     *
     */
	public class SendMsgClient implements Callable<String>{
   
		private String ip = null; 
		private final int mPort = 9023;
		
		
		
		public SendMsgClient(String ip){
			this.ip = ip;
		}
		@Override
		public String call() throws Exception {
			
			if(ip == null){
				return null;
			}
			//休眠2秒后执行，避免socketServer没有启动
			Thread.sleep(2000);
			String macAddress = Utils.getMacFromFile();
			Socket client = new Socket(ip, mPort);
			Writer writer = new OutputStreamWriter(client.getOutputStream());
			writer.write(macAddress);
			//写入结束符
			writer.write("eof");
			writer.flush();
			//写完后进行读操作
			Reader reader = new InputStreamReader(client.getInputStream());
			char[] chars = new char[64];
			int len;
			StringBuffer sb = new StringBuffer();
			String temp;
			int index;
			while((len = reader.read(chars))!= -1){
				temp = new String(chars, 0, len);
				if((index = temp.indexOf("eof"))!= -1){
					sb.append(temp.substring(0,index));
					break;
				}
				sb.append(temp);
			}
			Log.i(tag, "get from server"+sb);
			writer.close();
			reader.close();
			client.close();
			return sb.toString();
			
		}

    }
    
	/**
	 * 接收端等待发送端连接，获取发送端mac地址和ip地址，并发送mac地址
	 * @author Administrator
	 *
	 */
	public class SendMsgServer implements Callable<String>{

		private final int mPort = 9023;
		@Override
		public String call() throws Exception {
			ServerSocket server = new ServerSocket(mPort);
			Socket client = server.accept();
			//先接收client端发送的mac信息
			Reader reader = new InputStreamReader(client.getInputStream());
			char[] chars = new char[64];
			String temp;
			StringBuffer sb = new StringBuffer();
			int len;
			int index;
			while((len = reader.read(chars)) != -1){
				temp = new String(chars, 0, len);
				if((index = temp.indexOf("eof")) != -1){
					sb.append(temp.substring(0, index));
					break;
				}
				sb.append(temp);
			}
			//从socket中获取client端ip信息
			InetAddress ipAddress = client.getInetAddress();
			String ip = Utils.getDottedDecimalIP(ipAddress.getAddress());
			sb.append(",");
			sb.append(ip);
			//将本机mac信息发送给client端
			Writer writer = new OutputStreamWriter(client.getOutputStream());
			writer.write(Utils.getMacFromFile());
			writer.write("eof");
			writer.flush();
			writer.close();
			reader.close();
			client.close();
			server.close();
			Log.i(tag,"获取到的信息"+sb.toString());
			return sb.toString();
		}
		
	}
    
    /**
     * 
     * @author fanlei
     * deviceDetail fragment的回调接口，开始发送文件
     * @throws ExecutionException 
     * @throws InterruptedException 
     *
     */
    
    public void sendFile(ArrayList<String> list) throws InterruptedException, ExecutionException{
    	        // 找到本地IP
    			String localIP = Utils.getLocalIPAddress();

    			String clientIP = Utils.getIPFromMac();
    			Log.d(WiFiDirectActivity.TAG, "client IP " +clientIP);
    	    	
    			if(connectDevice == null){
    				String clientMac = null;
    				if(clientMac == null||clientMac == ""){
    					String temp = msgFuture.get();
    					String[] temps = temp.split(",");
    					if(temps.length == 1){
    						clientMac = temps[0];
    						Log.i(tag, "socket获取到mac地址为"+clientMac);
    					}else{
    						clientMac = temps[0];
    						if(clientIP == null|| clientIP.equals("")){
    							clientIP = temps[1];
    						}
    						
    						Log.i(tag, "socket获取到mac和ip分别为"+clientMac+clientIP);
    						
    					}
    				}
    				connectDevice = ((DeviceListListener)getActivity()).getDeviceInfo(clientMac);
    			}
    			
    			String macAddress = connectDevice.deviceAddress;	
    			String deviceName = connectDevice.deviceName;
    			
    		    addDevice();

    	        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
    	        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    	        serviceIntent.putStringArrayListExtra(FileTransferService.EXTRAS_FILE_PATH, list);  //uri.toString() putExtra���������ݲ����ģ�putExtra("A",B)�У���һ������Ϊ�������ڶ�������Ϊ�����õ�ֵ��
    	        serviceIntent.putExtra("macIP", macAddress);
    	        serviceIntent.putExtra("deviceName", deviceName);
    	        
    	        if(localIP.equals(IP_SERVER)){
    				serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
    			}else{
    				serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
    			}        
    	        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
    	        getActivity().startService(serviceIntent); 
    	       
    }
    
    
    
    public void getConnectDevice() {
    	try {
			if(connectDevice == null){
				//String clientMac = Utils.getMacFromFile();通过这种方式获取的mac地址不对？？
				String clientMac = null;
				if(clientMac == null||clientMac == ""){
					String temp = msgFuture.get();
					String[] temps = temp.split(",");
					if(temps.length == 1){
						clientMac = temps[0];
						Log.i(tag, "socket获取到mac地址为"+clientMac);
					}else{
						clientMac = temps[0];
						Log.i(tag, "socket获取到mac和ip分别为"+clientMac);
						
					}
				}
				connectDevice = ((DeviceListListener)getActivity()).getDeviceInfo(clientMac);
			}
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			
			e.printStackTrace();
		}
    }
    /*
     * 将相关设备信息添加到device表中
     */
    public void addDevice(){
    	String macAddress = connectDevice.deviceAddress;
		Log.i(tag, macAddress);
		
		String deviceName = connectDevice.deviceName;
		Log.i(tag, deviceName);
		
		List<Device> deviceList = new ArrayList<Device>();
		Device device = new Device();
		device.setDeviceAddress(macAddress);
		device.setDeviceName(deviceName);
		deviceList.add(device);
		
		dbManager.addDevice(deviceList);//将该设备添加到device表中
    }
    
    
    /**
     * 获取设备mac地址
     * @return
     */
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) this.getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
    
    
    
    /**
     * 
     * @author fanlei
     * deviceDetail fragment的回调接口，由activity实现
     *
     */
    public interface DeviceDetailListener{
    	/*
    	 * 滑动tab
    	 */
    	public void slideTab(int position);
    }
    
    
	
	public	class MyHandler extends Handler {
		//private NotificationManager manager ;
		
		/*public MyHander(NotificationManager manager){
			this.manager = manager ;
		}*/
		@Override
		public void  handleMessage(Message msg){
			if(msg.what == 1){
				Bundle b = msg.getData() ;
				NotificationBean NoBean = (NotificationBean)msg.obj ;
				int id = b.getInt("id");
				int i = b.getInt("pb");
				String fileName = b.getString("fileName");
				if(i < 100){
					NoBean.contentView.setProgressBar(R.id.pb, 100, i, false);
					NoBean.contentView.setTextViewText(R.id.tv_progress, 
									"文件：" +fileName + "接收进度："+i + "%");
					manager.notify(id, NoBean);
				}
				else if( 100 == i){
					NoBean.contentView.setTextViewText(R.id.tv_progress, "文件：" +fileName + "     "  + "接收完成");
					NoBean.contentView.setProgressBar(R.id.pb, 100, 100, true);
	    		    //noB.contentView.setImageViewBitmap(R.id.iv_show, result);
	    		    manager.notify(id, NoBean);
                    Toast.makeText(getActivity(),
                            "文件："+ fileName + "接收完成" ,
                            Toast.LENGTH_SHORT).show();
                    manager.cancel(id);
				}
			}
			
			else if(msg.what == 2){
				Bundle b = msg.getData() ;
				NotificationBean NoBean = (NotificationBean)msg.obj ;
				int id = b.getInt("id");
				int i = b.getInt("pb");
				String fileName = b.getString("fileName");
				if(i < 100){
					NoBean.contentView.setProgressBar(R.id.pb, 100, i, false);
					NoBean.contentView.setTextViewText(R.id.tv_progress, "文件：" + fileName + "     " +"发送进度："+i + "%");
					manager.notify(id, NoBean);
				}
				else if( 100 == i){
					NoBean.contentView.setTextViewText(R.id.tv_progress, "文件：" + fileName +"发送完成");
					NoBean.contentView.setProgressBar(R.id.pb, 100, 100, true);
	    		    //noB.contentView.setImageViewBitmap(R.id.iv_show, result);
	    		    manager.notify(id, NoBean);
                    Toast.makeText(getActivity(),
                            "文件：" +fileName+"发送完成" ,
                            Toast.LENGTH_SHORT).show();
                    manager.cancel(id);
				}
			}
		}
	}
    
}
