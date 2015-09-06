package com.example.filebrowser.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.example.android.wifidirect.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

public class LoadAndDisplayImageTask implements Runnable {

	private static final String LOG_WAITING_FOR_RESUME = "ImageLoader is paused. Waiting...  [%s]";
	private static final String LOG_RESUME_AFTER_PAUSE = ".. Resume loading [%s]";
	private static final String LOG_DELAY_BEFORE_LOADING = "Delay %d ms before loading...  [%s]";
	private static final String LOG_START_DISPLAY_IMAGE_TASK = "Start display image task [%s]";
	private static final String LOG_WAITING_FOR_IMAGE_LOADED = "Image already is loading. Waiting... [%s]";
	private static final String LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING = "...Get cached bitmap from memory after waiting. [%s]";
	private static final String LOG_LOAD_IMAGE_FROM_NETWORK = "Load image from network [%s]";
	private static final String LOG_LOAD_IMAGE_FROM_DISK_CACHE = "Load image from disk cache [%s]";
	private static final String LOG_RESIZE_CACHED_IMAGE_FILE = "Resize image in disk cache [%s]";
	private static final String LOG_PREPROCESS_IMAGE = "PreProcess image before caching in memory [%s]";
	private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";
	private static final String LOG_CACHE_IMAGE_IN_MEMORY = "Cache image in memory [%s]";
	private static final String LOG_CACHE_IMAGE_ON_DISK = "Cache image on disk [%s]";
	private static final String LOG_PROCESS_IMAGE_BEFORE_CACHE_ON_DISK = "Process image before cache on disk [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
	private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";
	private static final String LOG_TASK_INTERRUPTED = "Task was interrupted [%s]";

	private static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
	private static final String ERROR_PRE_PROCESSOR_NULL = "Pre-processor returned null [%s]";
	private static final String ERROR_POST_PROCESSOR_NULL = "Post-processor returned null [%s]";
	private static final String ERROR_PROCESSOR_FOR_DISK_CACHE_NULL = "Bitmap processor for disk cache returned null [%s]";
	
	private final ImageLoaderEngine engine;
	private final ImageLoadingInfo imageLoadingInfo;
	private final Handler handler;
	
	final String uri;
	private final String memoryCacheKey;
	private final ImageAware imageAware;
	private MemoryCache memoryCache;
	
	public LoadAndDisplayImageTask(MemoryCache memoryCache, ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler){
		this.engine = engine;
		this.handler = handler;
		this.imageLoadingInfo = imageLoadingInfo;
		this.memoryCache = memoryCache;
		
		uri = imageLoadingInfo.uri;
		memoryCacheKey = imageLoadingInfo.memoryCacheKey;
		imageAware = imageLoadingInfo.imageAware;
	}
	
	
	@Override
	public void run() {
		//同一个uri的图像咋同一时间只能加载一次
		ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
		if(loadFromUriLock.isLocked()){
			L.d(LOG_WAITING_FOR_IMAGE_LOADED, memoryCacheKey);
		}
		loadFromUriLock.lock();//锁定uri对应的锁
		Bitmap bmp;
		try{
			checkTaskNotActual();  //1.从缓存中加载图片前  检查view是否可用
			
			bmp = memoryCache.get(memoryCacheKey);
			if(bmp == null || bmp.isRecycled()){
				bmp = tryLoadBitmap();  //从硬盘读取过程中，也会检查view
				checkTaskNotActual();  //2.从硬盘中加载玩图片后  再次检查view是否可用
				checkTaskInterrupted();
				if(bmp != null){
					memoryCache.put(memoryCacheKey, bmp);
				}
			}else{
				//从缓存中读取bitmap   这里对图像的尺寸都没有做处理，后面需要更新
			}
			checkTaskInterrupted();   //3.执行显示任务前再次检查view是否可用
			checkTaskNotActual();
		}catch(TaskCancelledException e){
			//如果任务被中断，直接退出
			return;
		}finally{
			loadFromUriLock.unlock();//最终 取消uri对应的锁
		}
		DisplayBitmapTask displayBitmapTask = new DisplayBitmapTask(bmp, imageLoadingInfo, engine);//显示过程中，还会再次检查view
		runTask(displayBitmapTask, false, handler, engine);

	}
	
	
	static void runTask(Runnable r, boolean sync, Handler handler, ImageLoaderEngine engine) {
		if (sync) {
			r.run();
		} else if (handler == null) {
			engine.fireCallback(r);
		} else {
			handler.post(r);
		}
	}
	
	//从硬盘中加载bitmap
	private Bitmap tryLoadBitmap() throws TaskCancelledException{
		int width,height;
		width = height = imageLoadingInfo.width;
		try {
			checkTaskNotActual();
		} catch (TaskCancelledException e) {
			
			throw e;
		}

		return processBitmap(uri, width, height);
	}
	
private Bitmap processBitmap(String filePath, int height, int width){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		
		
		options.inJustDecodeBounds = false;
		
		options.inSampleSize = calculateSampleSize(options, height, width );
		
		
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
		
	
		return bitmap;
	}


	

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
		
		/*long totalPixal = height * width /sampleSize;
		long totalreqPixal = reqHeight * reqWidth *2;
		
		while(totalPixal> totalreqPixal){
			sampleSize *= 2;
			totalPixal /= 2;
		}*/
		return sampleSize;
	}
		
		//判断该imageView是否被重用或者回收
		private void checkTaskNotActual() throws TaskCancelledException {
			checkViewCollected();
			checkViewReused();
		}
		
		private void checkViewReused() throws TaskCancelledException {
			if (isViewReused()) {
				throw new TaskCancelledException();
			}
		}
	
	//通过比对当前任务中的key和engine中imageAware对应的key判断该view是否被重用过
	private boolean isViewReused() {
		String currentCacheKey = engine.getLoadingUriForView(imageAware);
		// Check whether memory cache key (image URI) for current ImageAware is actual.
		// If ImageAware is reused for another task then current task should be cancelled.
		boolean imageAwareWasReused = !memoryCacheKey.equals(currentCacheKey);
		if (imageAwareWasReused) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, memoryCacheKey);
			return true;
		}
		return false;
	}
	
	private void checkViewCollected() throws TaskCancelledException {
		if (isViewCollected()) {
			throw new TaskCancelledException();
		}
	}
	
	private boolean isViewCollected() {
		if (imageAware.isCollected()) {
			L.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, memoryCacheKey);
			return true;
		}
		return false;
	}

	/**
	 * 检查任务是否被中断
	 * @throws TaskCancelledException
	 */
	private void checkTaskInterrupted() throws TaskCancelledException {
		if (isTaskInterrupted()) {
			throw new TaskCancelledException();
		}
	}

	/** @return <b>true</b> - if current task was interrupted; <b>false</b> - otherwise */
	private boolean isTaskInterrupted() {
		if (Thread.interrupted()) {
			L.d(LOG_TASK_INTERRUPTED, memoryCacheKey);
			return true;
		}
		return false;
	}
	
	class TaskCancelledException extends Exception {
	}
}
