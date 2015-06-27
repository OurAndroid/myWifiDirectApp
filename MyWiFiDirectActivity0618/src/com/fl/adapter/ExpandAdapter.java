package com.fl.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.wifidirect.R;
import com.example.filebrowser.utils.ImageWorker;
import com.fl.database.Info;
import com.fl.database.TransferInfo;
import com.fl.utils.DateUtil;
import com.numberprogressbar.NumberProgressBar;

public class ExpandAdapter extends BaseExpandableListAdapter {

	
	private Context mContext;
	private LayoutInflater mInflater;
	private String[] mGroupStrings = null;
	private List<List<Info>> mData = null;
	private String tag = "fl-----ExpandAdapter";
	
	private ImageWorker worker;
	
	public ExpandAdapter(Context ctx, List<List<Info>> list){
		mContext = ctx;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGroupStrings = new String[]{"正在传输","已完成"};
		mData = list;
		
		worker = ImageWorker.imageWorkerFactory(ctx);
	}
	
	public void setData(List<List<Info>> list){
		mData = list;
	}
	
	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return mData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mGroupStrings[groupPosition];
	}

	
	/*
	 * 这里的获取分组类型的方法有待商榷(non-Javadoc)
	 * @see android.widget.BaseExpandableListAdapter#getGroupType(int)
	 */
	@Override
	public int getGroupType(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public int getGroupTypeCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}
	
	

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public int getChildTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mData.get(groupPosition).get(childPosition);
	}

	/*如果获取的是正在传输的分组，判断是否为相应数据的索引
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupId(int)
	 */
	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
//		if(groupPosition == 0){
//			for(int i = 0; i<mData.size(); i++){
//				if(TransferInfo.class.isInstance(mData.get(i).get(0))){
//					Log.i(tag, "分组编号"+i);
//					return i;
//
//				}
//				
//						
//			}
//		}
		
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupViewHolder holder = null;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.file_list_group_item, null);
			holder = new GroupViewHolder();
			holder.mGroupName = (TextView) convertView.findViewById(R.id.file_list_group_name);
			convertView.setTag(holder);
			
		}else{
			holder = (GroupViewHolder) convertView.getTag();

		}
		holder.mGroupName.setText(mGroupStrings[groupPosition]);//设置组名
		
		return convertView;
	}

	/**
	 * 0表示正在传输的组，1表示已完成的组，根据不同的索引返回不同的子目录
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		List<Info> list = mData.get(groupPosition);
		if(list.size() == 0||list == null){
			return null;
		}
		if(!(list.get(0) instanceof TransferInfo)){
			ChildViewHolder holder = null;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.file_list_child_item, null);
				holder = new ChildViewHolder();
				holder.deviceName = (TextView) convertView.findViewById(R.id.file_list_device_name);
				holder.fileDetail = (TextView) convertView.findViewById(R.id.file_list_file_detail);
				holder.tranceferTime = (TextView) convertView.findViewById(R.id.file_list_time);
				holder.mIcon = (ImageView) convertView.findViewById(R.id.file_list_img);
				
				convertView.setTag(holder);
			}else{
				holder = (ChildViewHolder) convertView.getTag();
			}
			
			Info info = (Info)getChild(groupPosition, childPosition);
			
			Log.i(tag, "deviceName"+info.getDevice_name());
			Log.i(tag, "fileName"+info.getFile_name());
			Log.i(tag, "time"+info.getTime());
			String nameInfo;
			if(info.getIsclient() == 1){
				nameInfo = "发送给："+info.getDevice_name();
			}else
				nameInfo = "接收自："+info.getDevice_name();
			holder.deviceName.setText(nameInfo);
			holder.fileDetail.setText(((Info)getChild(groupPosition, childPosition)).getFile_path());
			holder.tranceferTime.setText(DateUtil.getDate(((Info)getChild(groupPosition, childPosition)).getTime()));//对时间数据格式化
			
	        String filePath = ((Info)getChild(groupPosition, childPosition)).getFile_path();
	        Log.i(tag, filePath);
			//加载不同的图片类型
			if(isImage(filePath.toLowerCase())){
				
				worker.loadImage(filePath, holder.mIcon);
			}else{
				

				holder.mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wenjian));
				
			}
			
			
			
		}else{
			TransferViewHolder holder = null;
			if(convertView == null){
				holder = new TransferViewHolder();
				convertView = mInflater.inflate(R.layout.file_list_transfer, null);
				holder.bar = (NumberProgressBar) convertView.findViewById(R.id.file_list_bar);
				holder.fileName = (TextView) convertView.findViewById(R.id.file_list_transfer_filename);
				holder.size = (TextView) convertView.findViewById(R.id.file_list_transfer_size);
				
				convertView.setTag(holder);
			}else{
				holder = (TransferViewHolder) convertView.getTag();
				
			}
			TransferInfo info = (TransferInfo) getChild(groupPosition, childPosition);
			
			String nameInfo; 
			if(info.getIsclient() == 1){
				nameInfo = "正在发送："+info.getDevice_name();
			}else
				nameInfo = "正在下载："+info.getDevice_name();
			holder.fileName.setText(nameInfo);
			holder.size.setText(info.getSize());
			holder.bar.setProgress(info.getPosition());
			
		}
		
		return convertView;
		
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public class GroupViewHolder{
		TextView mGroupName;
	}
	
	public class ChildViewHolder{
		ImageView mIcon;
		TextView deviceName;
		TextView tranceferTime;
		TextView fileDetail;
	}
	
	public class TransferViewHolder{
		TextView fileName;
		TextView size;
		//ProgressBar bar;
		NumberProgressBar bar ;
	}
	
	
	private boolean isImage(String name){
		if(name.contains(".jpg")||name.contains(".png"))
			return true;
		return false;
	}
	
	public void update(List<List<Info>> list){
		this.mData = list;
	}
	

}
