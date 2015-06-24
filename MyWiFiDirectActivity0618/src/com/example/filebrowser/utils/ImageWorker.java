package com.example.filebrowser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.android.wifidirect.R;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;



public class ImageWorker {
	
	private static final String Tag = "ImageWorker";
	
	
	private static int i=0;
	private Bitmap mLoadingBitmap;
	
	private final int width,height;
	
	private static ImageWorker worker = null;
	
	protected Resources mResources;
	
	//ͼƬ����
	private static ImageCache mImageCache;
	//private static LruCache<String,BitmapDrawable> mMemoryCache;
	//�µ��̳߳�
	private static final ThreadFactory  sThreadFactory = new ThreadFactory() {
	        private final AtomicInteger mCount = new AtomicInteger(1);

	        public Thread newThread(Runnable r) {
	            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
	        }
	    };
	    
	public final Executor DUAL_THREAD_EXECUTOR =
            Executors.newFixedThreadPool(2, sThreadFactory);
	
	
	public static ImageWorker imageWorkerFactory(Context context){
		if(worker!=null)
			return worker;
		worker = new ImageWorker(context);
		return worker;
	}
	
	public ImageWorker(Context context){
		mResources = context.getResources();
		int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024;
		int cacheSize = Math.round(maxMemory*0.7f);
		width = height = mResources.getDimensionPixelSize(R.dimen.image_size);
		
		Log.i("fl---ImageWorker", cacheSize+"");
		mImageCache = new ImageCache(cacheSize);
	}
	
	public void addBitmapToMemory(String key, BitmapDrawable bitmap){
		if(getBitmapFromCache(key) == null){
			mImageCache.addBitmapToMemCache(key, bitmap);
		}
	}
	
	public BitmapDrawable getBitmapFromCache(String key){
		return mImageCache.getBitmapFromMemCache(key);
	}
	
	
	public void setLoadingImage(Bitmap bitmap){
		mLoadingBitmap = bitmap;
	}
	
	public void setLoadingImage(int resId){
		mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
	}
	
	public void loadImage(Object data, ImageView imageView){
		if(data == null){
			return;
		}
		
		BitmapDrawable bitmap = mImageCache.getBitmapFromMemCache((String)data);
		if(bitmap != null){
			imageView.setImageDrawable(bitmap);//BitmapDrawable value = null;
		//��������뻺��
		}else if(cancelPotentialWork(data, imageView)){//�������´���task�����󶨵�imageView
			final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
			
			imageView.setImageDrawable(asyncDrawable);
			
			task.executeOnExecutor(this.DUAL_THREAD_EXECUTOR);
			//task.execute();     //������ڲ�ִ��˳��
		}
	}
	
	
	
	@SuppressWarnings("resource")
	private Bitmap processBitmap(String filePath){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		
		
		options.inJustDecodeBounds = false;
		
		Log.i("fl---processBitmap", filePath+"---"+i++);
		options.inSampleSize = calculateSampleSize(options, height, width );
		
		//addInbitmap(options);//���������е�bitmap��Դ�ܷ�����
		//bitmap = BitmapFactory.decodeFile(filePath, options);
		FileInputStream fo = null;
		try {
			fo = new FileInputStream(filePath);
			
			if(fo!=null&&options.outHeight!=-1)
			bitmap = BitmapFactory.decodeFileDescriptor(fo.getFD(),null,options);
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bitmap!=null)
		Log.i("fl_processbitmap", ""+bitmap.getByteCount());
		
		/*Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
		
		//���һ�����bitmap
		if(scaledBitmap != bitmap){
			bitmap.recycle();
		}
	
		return scaledBitmap;*/
		return bitmap;
	}
	
	public void addInbitmap(BitmapFactory.Options options){
		
		Log.i("fl---addInbitmap", "����Ƿ����ܱ����õ�bitmap");
		options.inMutable = true;
		Bitmap bitmap = mImageCache.getBitmapFromReuseableSet(options);
		if(bitmap != null&&bitmap.getConfig() != null){
			
			options.inBitmap = bitmap;
			Log.i("fl----addInbitmap", "bitMap������");
		}
	}
	
	//�жϵ�ǰimageView��ִ�е������Ƿ�ΪĿ����������ǣ�������true
	public static boolean cancelPotentialWork(Object data, ImageView imageView){
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		
		if(bitmapWorkerTask!=null){
			final Object bitmapData = bitmapWorkerTask.mData;
			
			if(bitmapData ==null||!bitmapData.equals(data)){
				bitmapWorkerTask.cancel(true);//�����cancel�ڶԶ�ȡͼƬ��IO����ʱ����һ����ȡ��
				Log.i("fl","cancelPotentialWorkǰ�󲻵�");
			}else{
				Log.i("fl","����ǰ�����");
				return false;
			}
			
		}return true;
	}
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
		if(imageView!=null){
			final Drawable drawable = imageView.getDrawable();
			if(drawable instanceof AsyncDrawable){
				AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
	
	
	private class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapDrawable>{
		
		private Object mData;
		private final WeakReference<ImageView> imageViewReference;
		
		public BitmapWorkerTask(Object data, ImageView imageView){
			mData = data;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected BitmapDrawable doInBackground(Void... params) {
			Bitmap bitmap;
			BitmapDrawable drawable =null;
			
			String filePath = String.valueOf(mData);
			bitmap = processBitmap(filePath);
			if(bitmap!=null){
				drawable = new BitmapDrawable(mResources, bitmap);
			}
			mImageCache.addBitmapToMemCache(filePath, drawable);
			
			return drawable;
		}

		@Override
		protected void onPostExecute(BitmapDrawable value) {
			// TODO Auto-generated method stub
			
			final ImageView imageView = getAttachedImageView();  //������ִ��һ�μ�⣬���ý���ѹ��ڵ�δ��ȡ��ɹ�������ʾͼƬ
			if(value!=null&&imageView!=null){
			    
				imageView.setImageDrawable(value);
			}
		}
		
		
		private ImageView getAttachedImageView(){
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			
			
			Log.i("fl", bitmapWorkerTask==this?"1":"0");
			if(this == bitmapWorkerTask){
				return imageView;
			
			}
			
			return null;
		}
		
		
	}
	
	//������ʾͼƬ�Ĵ�С
	public static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		final int height = options.outHeight;
		final int width = options.outWidth;
		int sampleSize = 1;
		
		final int halfHeight = height/2;
		final int halfWidth = width/2;
		if(halfHeight>reqHeight || halfWidth>reqWidth){
			sampleSize*=2;
		}
		
		while((halfHeight / sampleSize > reqHeight) && 
				(halfWidth/sampleSize>reqWidth)){
			sampleSize *=2;
		}
		
		Log.i("fl_calculatesize", "req"+reqHeight+"  "+reqWidth);
		Log.i("fl_calculateSize", height/sampleSize+"||"+width/sampleSize+"||size "+sampleSize+"---"+i);
		
		/*long totalPixal = height * width /sampleSize;
		long totalreqPixal = reqHeight * reqWidth *2;
		
		while(totalPixal> totalreqPixal){
			sampleSize *= 2;
			totalPixal /= 2;
		}*/
		return sampleSize;
	}
	
	
	private static class AsyncDrawable extends BitmapDrawable{
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
		
		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}
		
		public BitmapWorkerTask getBitmapWorkerTask(){
			return bitmapWorkerTaskReference.get();
		}
	}

}
