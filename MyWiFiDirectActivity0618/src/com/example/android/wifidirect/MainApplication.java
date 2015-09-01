package com.example.android.wifidirect;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;

import com.example.filebrowser.utils.ImageLoader;
import com.example.filebrowser.utils.ImageLoaderConfig;
import com.example.filebrowser.utils.LruMemoryCache;
import com.example.filebrowser.utils.MemoryCache;

public class MainApplication extends Application {
	 /** 
     * 全局的上下文. 
     */  
    private static Context mContext;  
    private Handler mHandler;
      
    @Override  
    public void onCreate() {  
        super.onCreate();  
          
//        mContext = getApplicationContext();  
//        Stetho.initialize(
//        	      Stetho.newInitializerBuilder(this)
//        	        .enableDumpapp(
//        	            Stetho.defaultDumperPluginsProvider(this))
//        	        .enableWebKitInspector(
//        	            Stetho.defaultInspectorModulesProvider(this))
//        	        .build());
        initImageLoader(getApplicationContext());
        ArrayList<String> list;	  
        Object object;
        HashMap<String,String> map;
          
    }     
      
    /**获取Context. 
     * @return 
     */  
    public static Context getContext(){  
        return mContext;  
    }  
    
    
    public static void initImageLoader(Context context){
    	MemoryCache memoryCache = createMemoryCache(context, 0);
    	Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_photo);
    	ImageLoaderConfig config = new ImageLoaderConfig(memoryCache, bitmap);
    	ImageLoader.getInstance().init(config);
    }
    
    
    public static MemoryCache createMemoryCache(Context context, int memoryCacheSize) {
		if (memoryCacheSize == 0) {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			int memoryClass = am.getMemoryClass();
			if (hasHoneycomb() && isLargeHeap(context)) {
				memoryClass = getLargeMemoryClass(am);
			}
			memoryCacheSize = 1024 * 1024 * memoryClass / 8;
		}
		return new LruMemoryCache(memoryCacheSize);
	}

      
    private static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static boolean isLargeHeap(Context context) {
		return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static int getLargeMemoryClass(ActivityManager am) {
		return am.getLargeMemoryClass();
	}
    
    @Override  
    public void onLowMemory() {  
        super.onLowMemory();  
    }


	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}  
}
