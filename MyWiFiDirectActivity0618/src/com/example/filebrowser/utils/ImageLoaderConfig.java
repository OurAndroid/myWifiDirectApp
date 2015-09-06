package com.example.filebrowser.utils;

import android.graphics.Bitmap;

public class ImageLoaderConfig {
	
	public MemoryCache memoryCache;
	public Bitmap loadingbmp;
	
	public ImageLoaderConfig(MemoryCache memoryCache, Bitmap loadingbmp){
		this.loadingbmp = loadingbmp;
		this.memoryCache = memoryCache;
	}
	

}
