package com.example.filebrowser.utils;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.LruCache;

public class ImageCache {
	
	    //ͼƬ����
		private static LruCache<String,BitmapDrawable> mMemoryCache;
		//��������Ϊ��������
		private static Set<SoftReference<Bitmap>> reusableSet = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
		
		
		public ImageCache(int cacheSize){
			mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize){

				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					// TODO Auto-generated method stub
					return value.getBitmap().getByteCount()/1024;
				}

				@Override
				protected void entryRemoved(boolean evicted, String key,
						BitmapDrawable oldValue, BitmapDrawable newValue) {
					// �����������Դ�����������
					Log.i("fl----entryRemove", "���������С"+reusableSet.size());
					reusableSet.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
				}
				
				
				
			};
		}
		
		
		public void addBitmapToMemCache(String key, BitmapDrawable value){
			if(key == null||value == null){
				return;
			}
			
			Log.i("fl---addBitmap", ""+mMemoryCache.size());
			mMemoryCache.put(key, value);
		}
		
		public BitmapDrawable getBitmapFromMemCache(String key){
			Log.i("fl---getBitmap", "��ȡ����");
			return mMemoryCache.get(key);
		}
		
		public Bitmap getBitmapFromReuseableSet(BitmapFactory.Options options){
			
			Log.i("fl---getBitmapFromReusableSet", reusableSet.size()+"���������С");
			Bitmap bitmap = null;
			if(null !=reusableSet&&!reusableSet.isEmpty()){
				
				
				synchronized (reusableSet) {
					final Iterator<SoftReference<Bitmap>> iterator = reusableSet
							.iterator();
					Bitmap item;
					while(iterator.hasNext()){
						item = iterator.next().get();
						
						if(item != null && item.isMutable()){  //���item��mutable����addInbitmap�����õ�
							
							if(canUseForInbitmap(item, options)){
								bitmap = item;
								
								iterator.remove();//�������������
								break;
							}
							
						}else{
							iterator.remove();
						}
					}
				}
				
			}
			return bitmap;
		}
		
		
		
		public boolean canUseForInbitmap(Bitmap bitmap, BitmapFactory.Options options){
			//������Ҫ��4.4����ϵͳ��������
			int height = options.outHeight/options.inSampleSize;
			int width = options.outWidth/options.inSampleSize;
			int byteCount = width * height *getBytesPerPixel(bitmap.getConfig());
			
			Log.i("fl---canUseForInbitmap", ""+byteCount +"----"+bitmap.getByteCount());
			return byteCount<= bitmap.getByteCount();
		}
		
		
		 private static int getBytesPerPixel(Config config) {
		        if (config == Config.ARGB_8888) {
		            return 4;
		        } else if (config == Config.RGB_565) {
		            return 2;
		        } else if (config == Config.ARGB_4444) {
		            return 2;
		        } else if (config == Config.ALPHA_8) {
		            return 1;
		        }
		        return 1;
		    }

}
