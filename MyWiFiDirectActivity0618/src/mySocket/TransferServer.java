package mySocket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.example.android.wifidirect.DeviceDetailFragment;
import com.example.android.wifidirect.MainApplication;
import com.example.android.wifidirect.WiFiDirectActivity;
import com.fl.database.DBManager;
import com.fl.database.Transfer;
import com.fl.utils.DateUtil;
import com.fl.utils.KeyUtil;

public class TransferServer {
	
	private Handler mHandler;
	private int defaultBindPort = 9099 ;
	private int tryBindTimes = 0 ;
	//文件传输对象
	private Transfer transfer = null;
	
	private DBManager dbManager;
	//�������׽��ֵȴ��Է��������Լ��ļ�����
	private ServerSocket serverSocket ;
	//�̳߳�
	
	//����CPU���̳߳ش�С
	private final static int POOL_SIZE = 5 ;
	//传输完成的文件的列表
	private List<Map<String,Object>> filelist = new ArrayList<Map<String,Object>>();
	
	private DeviceDetailFragment detailfragment ;
	
	private static final int NOTICATION_ID = 0x12 ;
	
	private static String tag = "TransferServer";
	
	private static int Count = 0 ;
	
	private Context mContext ;
	
	//这些线程池的初始化都应该放在程序的开始处
	public static ExecutorService executorService = Executors.newCachedThreadPool();
	//private MyHander handler ;
	
	
	public List<Map<String,Object>> getFileList(){
		return filelist ;
	}
	/**
	 * ���������Ĺ�������ѡ��Ĭ�϶˿ں�
	 */
	public TransferServer(DeviceDetailFragment detailfragment) throws Exception{
		this.detailfragment = detailfragment ;
		filelist = detailfragment.getList();
		this.mContext = MainApplication.getContext();
		detailfragment.getManager();
		try{
			this.bindToServerPort(defaultBindPort);
			//executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			//System.out.println("�����߳�����" + Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			
		}
		catch(Exception e){
			throw new Exception("端口不能绑定");
		}
	}
	
	
	/**
	 * 构造函数，传入消息处理的handler
	 * @param handler
	 * @throws Exception
	 */
	public TransferServer(Handler handler, Transfer t, DBManager manager) throws Exception{
		this.transfer = t;
		this.dbManager = manager;
		this.mContext = MainApplication.getContext();
		this.mHandler = handler;
		try{
			this.bindToServerPort(defaultBindPort);
			//executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
		}
		catch(Exception e){
			throw new Exception("端口不能绑定");
		}
	}
	
	/**
	 * �������Ĺ�������ѡ���û�ָ���Ķ˿ں�
	 * @param port
	 * @throws Exception
	 */
	public TransferServer(int port , DeviceDetailFragment detailfragment) throws Exception{
		this.detailfragment = detailfragment ;
		filelist = detailfragment.getList();
		try{
			this.bindToServerPort(port);
			//executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			//System.out.println("�����߳�����" + Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			
		}
		catch(Exception e){
			throw new Exception("端口不能绑定");
		}
	}
	
	/*public void beginServer() throws Exception{
		try{
			this.bindToServerPort(port);
			executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			//System.out.println("�����߳�����" + Runtime.getRuntime().availableProcessors()*POOL_SIZE);
			
		}
		catch(Exception e){
			throw new Exception("端口不能绑定");
		}
	}*/
	private void bindToServerPort(int port) throws Exception{
		try{
			serverSocket = new ServerSocket(port);
			Log.d(WiFiDirectActivity.TAG, "Server: Socket opened , port " + port);
			//打印状态信息
			detailfragment.setStatusText("开始接收...");
			//System.out.println(port);
			//System.out.println("����������");
			
		}catch(Exception e){
			/*this.tryBindTimes = this.tryBindTimes + 1 ;
			port = port + this.tryBindTimes ;
			if(this.tryBindTimes >= 20){
				throw new Exception("���Ѿ����Ժܶ���ˣ�������Ȼ�޷��󶨵�ָ���Ķ˿ڣ�������ѡ��󶨵�Ĭ�϶˿ں�");
			}*/
			e.printStackTrace();
			//�ݹ�󶨶˿�
			//this.bindToServerPort(port);
		}
	}
	
	public void service() throws Exception{
		//beginServer();
		Socket socket = null ;
		//
		while(true){
			try{
				Log.d("fl---","准备接收socket");
				socket = serverSocket.accept();
				Log.d("fl---", "接收到socket");
				executorService.execute(new SocketHandler(socket));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	class SocketHandler implements Runnable{
		
		private Socket socket ;
		
		public SocketHandler(Socket socket){
			this.socket = socket ;
			Count ++ ;
		}
		
		public void run() {
			Log.d(WiFiDirectActivity.TAG , "New connection accepted "+ 
					socket.getInetAddress()+":" + socket.getPort());
			DataInputStream dis = null ;
			DataOutputStream dos = null ;
			int bufferSize = 8192 ;
			byte[] buf = new byte[bufferSize];
//			noB = new NotificationBean(mContext, R.drawable.wenjian, 
//					"开始接收文件",System.currentTimeMillis() , no_id);
//		    manager.notify(no_id, noB);			
			try{
				dis = new DataInputStream(new 
							BufferedInputStream(socket.getInputStream()));
				//保存文件的路径，需要改
				String fileName = dis.readUTF();
				String savePath = Environment.getExternalStorageDirectory() + "/"
                           + "wifiDirect/" +fileName;
				long length = dis.readLong();
				//获取传输唯一标识key
				String key = KeyUtil.getKey(savePath);
				
				//创建传输信息
				Message msgCreate = Message.obtain();
				Bundle b = new Bundle();
				b.putString("size", length+"");
				b.putString("fileName", fileName);
				b.putString("key", key);
				b.putInt("isclient", 0);
				msgCreate.setData(b);
				msgCreate.what = 1;
				mHandler.sendMessage(msgCreate);
				
				final File f = new File(savePath);
				File dirs = new File(f.getParent());
				if(!dirs.exists())
					dirs.mkdirs();
				f.createNewFile();
				
				dos = new DataOutputStream(new 
						BufferedOutputStream(new FileOutputStream(f)));
				int read = 0 ;
				long passedlen = 0 ;
				int progress = 0 ; //当前下载进度
				int times = 0 ;
				//记录当前时间
				long start = System.currentTimeMillis();
				
				while((read = dis.read(buf))!=-1){
					passedlen +=read ;
					dos.write(buf,0,read);
					//System.out.println("�ļ�["+savePath +"]�Ѿ�����:"+passedlen * 100L/length + "%");
					if(times == 15 || (passedlen == length)){
						if(passedlen == length){
							progress = 100;
						}
						progress = (int )((passedlen / (float)length) * 100);
						//Message msg = new Message();
						Message msg = Message.obtain();
						Bundle updateBundle = new Bundle();
						
						updateBundle.putInt("current", progress);
						updateBundle.putString("key", key);
						msg.setData(updateBundle);
						//msg.obj = noB ;
						msg.what = 2 ;
						mHandler.sendMessage(msg);
						times = 0;
					}
					times ++ ; 
				}
				dos.flush();
				 Log.d(WiFiDirectActivity.TAG, "文件：" + savePath + "传输完成");
				 //传输完成的时间
				 long end = System.currentTimeMillis();
				 long time_run = end - start ;
				 Log.d("zhangxl","传输时间:"+time_run);
				 String date = DateUtil.getAllDate(new Date());

			        synchronized (transfer) {
			        	transfer.setFilePath(savePath);
						transfer.setTime(date);
						List<Transfer> list = new ArrayList<Transfer>();
						list.add(transfer);
							    
						dbManager.addTransfer(list);
					    Log.i(tag, "写入成功");
					}
				 
//				 Map<String , Object> map = new HashMap<String, Object>();
//				 map.put("file_path", savePath);
//				 map.put("img", R.drawable.wenjian);
//				 filelist.add(map);
				 /*for(int i = 0 ; i < filelist.size() ; i++){
					 Log.d(WiFiDirectActivity.TAG, "filelist[i]"+filelist.get(i));
				 }*/
				
				 //System.out.println("�ļ�:"+savePath+ "�������!");
			}catch(Exception e){
				e.printStackTrace();
//				noB.contentView.setProgressBar(R.id.pb, 100, 0, true);
//    		    noB.contentView.setTextViewText(R.id.tv_progress, "接收失败");
//    		    manager.notify(no_id, noB);    		    
                Toast.makeText(mContext,"接收失败",0).show();	
				//System.out.println("�����ļ�ʧ��");
			}
			finally {	
    		    
				try {
					if(dos != null){
						dos.close();
					}
					if(dis != null){
						dis.close();
					}
					if(socket != null){
						socket.close();
					}
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}
	/*public static void main(String[] args) throws Exception {
		new TransferServer().service();
	}*/
	
}

