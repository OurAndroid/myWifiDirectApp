package com.example.android.wifidirect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mySocket.TransferServer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.content.Context ;



public class FileListFragment extends Fragment {

	/**
	 * @param args
	 */
	
	private SharedPreferences sp ;
	private SimpleAdapter adapter ;
	private List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
	private DeviceDetailFragment detailfragment ;
	
	public SimpleAdapter getAdapter(){
		return adapter ;
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
	Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getActivity().getSharedPreferences("FilePath", Context.MODE_PRIVATE);
		//找到fileTransmitfragment
		FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getActivity().getSupportFragmentManager()
									.findFragmentByTag("android:switcher:" + R.id.pager + ":0");
		//根据fileTransmitfragment找到DeviceDetailFragment
		detailfragment = (DeviceDetailFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);	
		View rootView = inflater.inflate(R.layout.file_list,container,false);
		//SimpleAdapter adapter = new SimpleAdapter(this , getData(),R.layout.vlist,
		//		new String[]{"file_path","img"} ,
		//		new int[]{R.id.file_path , R.id.img});
		adapter = new SimpleAdapter(getActivity() , getData(),
				R.layout.vlist , new String[]{"file_path","img"} ,
				new int[]{R.id.file_path , R.id.img});
		ListView filelist = (ListView )rootView.findViewById(R.id.filelist);
		filelist.setAdapter(adapter);
		Button btn_clear= (Button)rootView.findViewById(R.id.clear);		
		btn_clear.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ClearFile();
				list.clear();
				detailfragment.clearList();
		    	adapter.notifyDataSetChanged();
			}
		});
	
	return rootView;
	}

	private List<Map<String, Object>> getData() {
		// TODO Auto-generated method stub
		//读取文件内容，有内容则添加到list中,主要是将程序之前保存的结果展示出来
		//list.clear();
		File file = new File("filelist.txt");
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			try {
				String line = in.readLine();
				while(line != null){
					Map<String , Object> map= new HashMap<String, Object>();
					map.put("file_path", line);
					map.put("img", R.drawable.wenjian);
					list.add(map);
				}
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> filelist = new ArrayList<String>();
	
    	list = detailfragment.getList();
    	//添加新信息到文件中
    	try {
    		ClearFile();
			FileWriter fw = new FileWriter(file,true);
			for(Map<String,Object> m :list){
				String filename = (String)m.get("file_path");
				fw.write(filename + "\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return list;
	}
	private void ClearFile(){
		File file = new File("filelist.txt");
		try {
			OutputStream os = new FileOutputStream(file);
			try {
				os.write("".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

