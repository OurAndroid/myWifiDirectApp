package com.example.android.wifidirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.fl.adapter.ExpandAdapter;
import com.fl.database.DBManager;
import com.fl.database.Info;
import com.fl.database.TransferInfo;
import com.wifidirect.ui.MyExpandableListView;
import com.wifidirect.ui.MyExpandableListView.OnHeaderUpdateListener;

public class NewFileListFragment extends Fragment implements OnHeaderUpdateListener {

	private static ExpandAdapter adapter = null;
	private static List<List<Info>> mDatas = new ArrayList<List<Info>>();
	private static Map<String, Info> taskInfo = new HashMap<String, Info>();
	
	private static DBManager dbManager = null;
	
	public static List<Info> list1 = new ArrayList<Info>();
	public static List<Info> list0 = new ArrayList<Info>();
	String tag = "Wifidirect----";
	
	
	static{
		
		mDatas.add(list0);
		mDatas.add(list1);
	}
	
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		list1 = dbManager.query(null);    //这里list1指向了另外一个引用，与adapter中的引用不一样了
		Log.i(tag, "onResume");
		for(Info i:list1){
			Log.i(tag, i.getDevice_name());
			Log.i(tag, i.getFile_path());
		}
		
		//List<List<Info>> list = new ArrayList<List<Info>>();//这里list的引用关系？？
		//list.add(list1);
		//list.add(list1);
		mDatas.set(1, list1);
		//adapter.update(list);
		adapter.notifyDataSetChanged();

	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		dbManager = new DBManager(getActivity());
		
		ArrayList<Info> list = new ArrayList<Info>();
		Info info = new Info();
		int i = 10;
		while(i>0){
			info.setDevice_name(i+"");
			info.setFile_path("xxxxxx");
			info.setTime("2015-5-17");
			list.add(info);
			i--;
		}
		
		
		list1 = dbManager.query(null);
//		TransferInfo transferInfo = new TransferInfo();
//		list0.add(transferInfo);
		
		
		mDatas.set(1, list1);
		
		View rootView = inflater.inflate(R.layout.new_file_list, container,false);
		//ExpandableListView mListView = (ExpandableListView) rootView.findViewById(R.id.expand_file_list);
		MyExpandableListView mListView = (MyExpandableListView)rootView.findViewById(R.id.expand_file_list);
		
		adapter = new ExpandAdapter(getActivity(), mDatas, dbManager);
		mListView.setAdapter(adapter);
		mListView.setOnHeaderUpdateListener(this);
		//打开正在传输的标签内容
		mListView.expandGroup(0);
		
		return rootView;
	}
	
	
	
	
	public static class MyHandler extends Handler {
		//分别对创建传输任务和更新传输进度进行处理
		@Override
		public void  handleMessage(Message msg){
			if(msg.what == 1){
				Bundle b = msg.getData() ;
				
				String fileName = b.getString("fileName");
				String size = b.getString("size");
				String key = b.getString("key");
				int isclient = b.getInt("isclient");
				TransferInfo info = new TransferInfo();
				info.setFile_name(fileName);
				info.setSize(size);
				info.setIsclient(isclient);
				//将传输信息分别存入传输列表
				taskInfo.put(key, info);
				mDatas.get(0).add(info);
				
				//更新列表
				adapter.notifyDataSetChanged();
				
			}
			
			else if(msg.what == 2){
				Bundle b = msg.getData() ;
			
				String key = b.getString("key");
				int current = b.getInt("current");
				TransferInfo info = (TransferInfo) taskInfo.get(key);
				if(info!=null&&current < 100){//更新列表进度
					
					info.setPosition(current);
				}
				else if( 100 == current){//删除列表中的数据
					taskInfo.remove(key);
					mDatas.get(0).remove(info);
				}
				adapter.notifyDataSetChanged();
			}
			else if(msg.what == 3){
				//更新已传输
				Log.i("fl---", "收到更新请求");
				list1 = dbManager.query(null);
				mDatas.set(1, list1);
				adapter.notifyDataSetChanged();
			}
		}
	}




	@Override
	public View getHeader() {
		View headerView = this.getActivity().getLayoutInflater().inflate(R.layout.file_list_group_item, null);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		return headerView;
	}



	@Override
	public void updateHeader(View headerView, int firstVisibleGroupPos) {
		String groupString = (String) adapter.getGroup(firstVisibleGroupPos);
		TextView textView = (TextView) headerView.findViewById(R.id.file_list_group_name);
		textView.setText(groupString);
		
		
	}
	
	
	
	

}
