package com.example.android.wifidirect;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class MainApplication extends Application {
	 /** 
     * 全局的上下文. 
     */  
    private static Context mContext;  
    private Handler mHandler;
      
    @Override  
    public void onCreate() {  
        super.onCreate();  
          
        mContext = getApplicationContext();  
          
    }     
      
    /**获取Context. 
     * @return 
     */  
    public static Context getContext(){  
        return mContext;  
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
