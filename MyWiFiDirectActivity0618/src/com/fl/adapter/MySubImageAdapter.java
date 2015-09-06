package com.fl.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.android.wifidirect.R;
import com.example.filebrowser.utils.ImageLoader;
import com.example.filebrowser.utils.ImageWorker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySubImageAdapter extends BaseAdapter {

	ImageLoader imageWorker;
	
	 //记录每个位置是否被选择
	private Map<Integer, Boolean> itemStatus = new HashMap<Integer,Boolean>();
	
	private boolean isMultiSelect;
	
	private Bitmap selectedBitmap;
	private Bitmap unSelectedBitmap;
	private int bitmapWidth;
	
	private Context context;
	private LayoutInflater mInflater;
	private List<String> data;
	
	public MySubImageAdapter(Context context, List<String> list, Map<Integer, Boolean> map){
		mInflater = LayoutInflater.from(context);
		data = list;
		this.context = context;
		init(context);
		this.itemStatus = map;
	}
	
	public void init(Context context){
//		this.imageWorker = ImageWorker.imageWorkerFactory(context);
		this.imageWorker = ImageLoader.getInstance();
//		imageWorker.setLoadingImage(R.drawable.empty_photo);
		//为多选对勾 初始化
		selectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_on_selected);
		unSelectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_off);
		bitmapWidth = context.getResources().getDimensionPixelSize(R.dimen.image_size_big);

		
	}
	
	public void changeStatus(int position){
		
		if(position != -1){
			if(itemStatus.get(position) == null || !itemStatus.get(position)) //如果position所在位置为null或者为false
			itemStatus.put(position, true);
			else
				itemStatus.put(position, false);
		}
		
		//通知适配器进行更新
		notifyDataSetChanged();
	}
	
	/*
	 * 进入多选模式
	 */
	public void notifyIsSelect(boolean bl){
		this.isMultiSelect = bl;
		notifyDataSetChanged();
	}
	
	public void setDataSet(List<String> list){
		this.data = list;
	}
	
	
	
    
	//返回视图类型总数
	@Override
	public int getViewTypeCount() {
		return 1;
	}
    
	//返回数据总数
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}
    
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
   
	//填充视图并返回
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		String name = data.get(position);
		ViewHolder viewHolder;
		Log.i("fl---getView", name);
		
		//如果对应的视图不存在，创建
		if(convertView == null){
			viewHolder = new ViewHolder();
			
			//为contentView填充，并绑定控件
			convertView = mInflater.inflate(R.layout.image_child_list, parent, false);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_child_list_image);
			viewHolder.check_imageView = (ImageView) convertView.findViewById(R.id.image_child_list_select);
			convertView.setTag(viewHolder);
				
			
		}else{
			Log.i("fl", "复用view");
			viewHolder = (ViewHolder) convertView.getTag();
			
		}
		
		
		Bitmap isCheckImg;
		if(isMultiSelect){
		if(itemStatus.get(position) == null || !itemStatus.get(position)){
			 isCheckImg = unSelectedBitmap;
		}else{
		     isCheckImg = selectedBitmap;
		}
		
		}else{
			isCheckImg = null;
			//viewHolder.check_imageView.setVisibility(View.GONE);
		}
		//多选状态改变以后，重新设置check_imageView
		viewHolder.check_imageView.setImageBitmap(isCheckImg);
		
		
		
		//加载图片
//		imageWorker.loadImage(name, viewHolder.imageView);
		imageWorker.displayImage(name, viewHolder.imageView, bitmapWidth, bitmapWidth);

		
		return convertView;
	}
	
	private class ViewHolder{
	
		private ImageView imageView;
		private ImageView check_imageView;
	}
	
}


