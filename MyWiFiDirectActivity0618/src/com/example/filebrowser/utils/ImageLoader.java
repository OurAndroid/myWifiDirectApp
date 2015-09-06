package com.example.filebrowser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

public class ImageLoader {

	public static final String TAG = ImageLoader.class.getSimpleName();
	
	static final String LOG_INIT_CONFIG = "Initialize ImageLoader with configuration";
	static final String LOG_DESTROY = "Destroy ImageLoader";
	static final String LOG_LOAD_IMAGE_FROM_MEMORY_CACHE = "Load image from memory cache [%s]";

	private static final String WARNING_RE_INIT_CONFIG = "Try to initialize ImageLoader which had already been initialized before. " + "To re-init ImageLoader with new configuration call ImageLoader.destroy() at first.";
	private static final String ERROR_WRONG_ARGUMENTS = "Wrong arguments were passed to displayImage() method (ImageView reference must not be null)";
	private static final String ERROR_NOT_INIT = "ImageLoader must be init with configuration before using";
	private static final String ERROR_INIT_CONFIG_WITH_NULL = "ImageLoader configuration can not be initialized with null";

	private ImageLoaderEngine engine;
	private MemoryCache memoryCache;
	private Context context;
	private ImageLoaderConfig config;
	
	private volatile static ImageLoader instance;
	
	/**单例模式获取imageloader**/
	public static ImageLoader getInstance(){
		if(instance == null){
			synchronized(ImageLoader.class){
				if(instance == null){
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}
	
	protected ImageLoader(){
		
	}
	
	/**
	 * 初始化ImageLoader
	 * @param config
	 */
	public synchronized void init(ImageLoaderConfig config){
		if(config == null){
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
		}
		if(this.config == null){
			this.config = config;
			this.engine = new ImageLoaderEngine();
			this.memoryCache = config.memoryCache;
		}else{
			L.w(WARNING_RE_INIT_CONFIG);
		}
	}
	
	public void displayImage(String uri, ImageAware imageAware, int width, int height ){
		if(imageAware == null){
			throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
		}
		if(uri == null || uri.equals("")){
			engine.cancelDisplayTaskFor(imageAware);//取消该imageAware的上一个任务
			imageAware.setImageDrawable(null);
			return;
		}
		String memoryCacheKey = uri+width+width;  //TODO:这里后续可以加上不同的图片尺寸
		engine.prepareDisplayTaskFor(imageAware, memoryCacheKey);
		//下面可以加上开始加载的监听器处理
		
		//再一次判断该图像是否已经被缓存
		Bitmap bmp = memoryCache.get(memoryCacheKey);
		if(bmp != null && !bmp.isRecycled()){
			//这里直接加载到imageView中
			imageAware.setImageBitmap(bmp);
			return;
		}else{
			imageAware.setImageBitmap(config.loadingbmp);
		}
		ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri, imageAware, memoryCacheKey, engine.getLockForUri(uri), width);
		LoadAndDisplayImageTask displayTask = new LoadAndDisplayImageTask(memoryCache, engine, imageLoadingInfo, defineHandler());
		engine.submit(displayTask);
	}
	
	public void displayImage(String uri, ImageView imageView, int width, int height){
		this.displayImage(uri, new ImageViewAware(imageView), width, height);
	}
	
	public Handler defineHandler(){
		Handler handler = null;
		if(Looper.myLooper() == Looper.getMainLooper()){
			handler = new Handler();
		}
		return handler;
		
	}
	
}
