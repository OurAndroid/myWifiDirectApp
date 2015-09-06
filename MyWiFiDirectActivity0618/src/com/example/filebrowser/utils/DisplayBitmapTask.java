package com.example.filebrowser.utils;

import android.graphics.Bitmap;
import android.os.Handler;

public class DisplayBitmapTask implements Runnable {

	private static final String LOG_DISPLAY_IMAGE_IN_IMAGEAWARE = "Display image in ImageAware (loaded from %1$s) [%2$s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";

	
	private final Bitmap bitmap;
	private final String imageUri;
	private final ImageAware imageAware;
	private final String memoryCacheKey;
	private final ImageLoaderEngine engine;
	
	
	public DisplayBitmapTask(Bitmap bitmap, ImageLoadingInfo imageLoadingInfo, ImageLoaderEngine engine){
		this.bitmap = bitmap;
		imageUri = imageLoadingInfo.uri;
		imageAware = imageLoadingInfo.imageAware;
		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		this.engine = engine;
	}
	
	@Override
	public void run() {
		if(imageAware.isCollected()){
		L.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, memoryCacheKey);	
		}else if(isViewWasReuseed()){
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, memoryCacheKey);
		}else{
			engine.cancelDisplayTaskFor(imageAware);     //显示前，把当前任务移出
			imageAware.setImageBitmap(bitmap);
			
		}

	}
	
	public boolean isViewWasReuseed(){
		String currentCacheKey = engine.getLoadingUriForView(imageAware);
		if(currentCacheKey == null || memoryCacheKey == null){  //TODO:这里为什么会出现currentCachekey为null的情况？
			return true;
		}
		return !currentCacheKey.equals(memoryCacheKey);
	}

}
