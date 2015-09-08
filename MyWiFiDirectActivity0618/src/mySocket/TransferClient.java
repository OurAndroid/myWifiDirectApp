package mySocket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.android.wifidirect.DeviceDetailFragment;
import com.example.android.wifidirect.MainApplication;
import com.example.android.wifidirect.WiFiDirectActivity;
import com.fl.database.DBManager;
import com.fl.database.Transfer;
import com.fl.utils.DateUtil;
import com.fl.utils.KeyUtil;

public class TransferClient {
	private static ArrayList<String> fileList = new ArrayList<String>();
	private String sendFilePath = "D:\\send" ;
	
	private static String ServerIP ;
	public static Context mContext ;
	private int TIME_OUT = 5000 ;
	private static NotificationBean NoBean ;
	private Handler mHandler;
	
	
	private Transfer transfer;
	
	String tag = "WIFIdirect----";
	//数据库辅助类
	private DBManager dbManager;
	
	public static ExecutorService executorService = Executors.newCachedThreadPool();
	
	/**
	 * �������Ĺ�����
	 * @param filePath
	 */
	public ArrayList<String> getFileList(){
		return fileList;
	}
	
	/*public static void main(String[] args){  
        new TransferClient().service();  
    } */ 
	/**
	 * 有参构造函数 
	 * @param filelist
	 * @param ServerIP
	 */
	 
	public TransferClient(ArrayList<String> filelist , String ServerIP,DeviceDetailFragment detailfragment, DBManager dbManager, Transfer t, Handler mHandler){
		this.fileList = filelist ;
		this.ServerIP = ServerIP ;
		//this.detailfragment = detailfragment ;
		mContext = MainApplication.getContext();
		
		this.mHandler = mHandler;
		this.transfer = t;
		this.dbManager = dbManager;
		
		//this.manager = DeviceDetailFragment.manager;
	}
	/**
	 * ���������Ĺ�����
	 */
   /* public TransferClient(){
    	getFilePath(sendFilePath);
    }*/
    
    //获取发送
	private void getFilePath(String dirPath){  
        File dir = new File(dirPath);  
        File[] files = dir.listFiles();  
        if(files == null){  
            return;  
        }  
        for(int i = 0; i < files.length; i++){  
            if(files[i].isDirectory()){  
                getFilePath(files[i].getAbsolutePath());  
            }  
            else {  
                fileList.add(files[i].getAbsolutePath());  
            }  
        }  
    }  
	
	public void service(){
		
		//Vector<Integer> vector = getRandom(fileList.size());
		File file = null;
		for(String filePath:fileList){
			//String filePath = fileList.get(integer.intValue());
			/*
			 * 发送并创建文件传输的信息
			 */
			String key = KeyUtil.getKey(filePath);//生成文件传输的编号
			file = new File(filePath);
			Message msg = Message.obtain();
			Bundle b = new Bundle();
			b.putString("size", file.length()+"");
			b.putString("fileName", file.getName());
			b.putString("key", key);
			b.putInt("isclient", 1);
			msg.setData(b);
			msg.what = 1;
			mHandler.sendMessage(msg);
			
			executorService.execute(sendFile(filePath, key));
			
				
		}
	}
	
	private Vector<Integer> getRandom(int size){  
		//Vector ֧���߳�ͬ����ĳһʱ��ֻ��һ���߳��ܹ�дVector
        Vector<Integer> v = new Vector<Integer>();  
        Random r = new Random();  
        boolean b = true;  
        while(b){  
            int i = r.nextInt(size);  
            if(!v.contains(i))  
                v.add(i);  
            if(v.size() == size)  
                b = false;  
        }  
        return v;  
    }
	
	public Runnable sendFile(final String filePath, final String key){
		Log.i(tag, "接收端的ip为"+ServerIP);
		return new Runnable(){
			private Socket socket = null ;
			private String ip = ServerIP; 
			private int port = 9099 ;
			public void run() {
				// TODO Auto-generated method stub
				//System.out.println("��ʼ�����ļ�:" + filePath);
				int no_id = (int) Thread.currentThread().getId();
				File file = new File(filePath);
				Log.d("fl---", "准备建立连接");
				if(createConnection())
				{
					Log.d("fl---", "建立连接");

					int bufferSize = 8192 ;
					byte[] buf = new byte[bufferSize];
					try{
						DataInputStream fis = new DataInputStream(new 
								BufferedInputStream(new FileInputStream(filePath)));
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						DataInputStream fdis = new DataInputStream(socket.getInputStream());
						RandomAccessFile access = new RandomAccessFile(file, "r");
						// 发送文件名
						dos.writeUTF(file.getName());
						dos.flush();
						// 接收方回应是否已经有收到过该文件，返回已收到的文件长度，未收到文件则返回0
						long start_index = 0 ;
						start_index = fdis.readLong();
						// 发送文件长度
						dos.writeLong(file.length());
						dos.flush();
						
						// 跳过已经发送过的长度
						access.skipBytes((int) start_index);
						
						int read = 0 ;
						int passedlen = (int) start_index ;
						long length = file.length();
						int times = 0 ;
						int progress = (int) start_index ;
						
						while((read = access.read(buf))!=-1){				//while((read = fis.read(buf))!=-1)
							passedlen += read ;
							dos.write(buf,0,read);
							/*
							 * times==256是什么意思？？
							 */
							if(times == 15 || (passedlen == length)){
								progress = (int )((passedlen / (float)length) * 100);
								//Message msg = new Message();
								Message msg = Message.obtain();
								Bundle b = new Bundle();
								
								b.putInt("current", progress);
								b.putString("key", key);
								msg.setData(b);
								
								msg.what = 2 ;
								mHandler.sendMessage(msg);
								times = 0;
							}
							times ++ ; 
						}
						dos.flush();
						fis.close();
						access.close();
						dos.close();
						socket.close();
						
						Log.d(WiFiDirectActivity.TAG, "File: " + filePath +"传输成功");
				        
				        /*
				         * 向数据库中写入传输信息，可以考虑将这些信息缓存起来，在程序退出时写入数据库
				         */
				        String date = DateUtil.getAllDate(new Date());

				        synchronized (transfer) {
				        	transfer.setFilePath(filePath);
							transfer.setTime(date);
							List<Transfer> list = new ArrayList<Transfer>();
							list.add(transfer);
								    
							dbManager.addTransfer(list);
						    Log.i(tag, "写入成功");
						   
						    //文件写入完毕，刷新数据库
						  
						    mHandler.sendEmptyMessage(3);
						}
						
				        
				        
				        
				        
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}
			
			private boolean createConnection(){
				try{
					Log.d("fl---","ip"+ip);
					//socket = new Socket(ip,port);
					//连接超时会出现异常
					socket = new Socket();
					socket.connect((new InetSocketAddress(ip, port)), TIME_OUT);
					return true ;
				}catch(Exception e){
					e.printStackTrace();
					return false;
				}
			}
			
		};
		
	}

}
