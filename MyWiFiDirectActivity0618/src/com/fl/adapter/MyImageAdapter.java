package com.fl.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.wifidirect.R;
import com.example.filebrowser.utils.ImageLoader;
import com.example.filebrowser.utils.ImageWorker;
import com.wifidirect.entity.ImageGroup;

public class MyImageAdapter extends BaseAdapter {

	private List<ImageGroup> list;
	private GridView mGridView;
	protected LayoutInflater mInflater;
//	private ImageWorker imageWorker;
	private ImageLoader imageWorker;
	private static Bitmap selectedBitmap;
	private static Bitmap unSelectedBitmap;
	private int bitmapWidth;
	
	public MyImageAdapter(Context context, List<ImageGroup> list){
		this.list = list;
		this.mInflater = LayoutInflater.from(context);
		init(context);
	}
	
	public interface OnMultiSelectListener{
		public boolean isMultiSelect();
	}
	
	public void init(Context context){
//		this.imageWorker = ImageWorker.imageWorkerFactory(context);
//		imageWorker.setLoadingImage(R.drawable.empty_photo);
		//为多选对勾 初始化
		selectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_on_selected);
		unSelectedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_off);
		bitmapWidth = context.getResources().getDimensionPixelSize(R.dimen.image_size);
		imageWorker = ImageLoader.getInstance();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		ImageGroup mImageGroup = list.get(position);
		String path = mImageGroup.getTopImagePath();
		String parentName = mImageGroup.getParentName();
		int count = mImageGroup.getCount();
		if(convertView == null){
		    convertView = mInflater.inflate(R.layout.image_group_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.image_group_image);
			viewHolder.mTextView = (TextView)convertView.findViewById(R.id.image_group_dirname);
			viewHolder.counts = (TextView)convertView.findViewById(R.id.image_group_count);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		imageWorker.displayImage(path, viewHolder.mImageView, bitmapWidth, bitmapWidth);
		viewHolder.counts.setText(count+"张");
		viewHolder.mTextView.setText(parentName);
		return convertView;
	}
	
	public class ViewHolder{
		public ImageView mImageView;
		public TextView mTextView;
		public TextView counts;
	}
	
	public class subImageHolder{
		public ImageView mImageView;
	}

}
