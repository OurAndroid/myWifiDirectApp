/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import mySocket.TransferClient;
import mySocket.TransferServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.android.wifidirect.DeviceDetailFragment.DeviceDetailListener;
import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;
import com.example.android.wifidirect.DeviceListFragment.DeviceListListener;
import com.example.android.wifidirect.FileBrowser.SendFileCallbackListener;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends FragmentActivity implements ChannelListener, DeviceActionListener,
SendFileCallbackListener,DeviceDetailListener,DeviceListListener{

    public static final String TAG = "wifidirectdemo";
    WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private ViewPager pager;


	//private final IntentFilter intentFilter = new IntentFilter();
    Channel channel;
    private BroadcastReceiver receiver = null;
    
    private FileTransmitFragment filetransmitfragment ;
    private FileBrowser filebrowser ;
    private NewFileListFragment filelistfragment ;//,filelistfragment2
    private PagerSlidingTabStrip tabs ;
    private DisplayMetrics dm ;
    private IntentFilter intentFilter = new IntentFilter();
    MyPagerAdapter mypagerAdapter = null ;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    public boolean getIsWifiP2pEnabled(){
    	return this.isWifiP2pEnabled ;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setOverflowShowingAlways();
        dm = getResources().getDisplayMetrics();
        pager = (ViewPager)findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        //为ViewPager实例添加定义的Adapter
        mypagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(mypagerAdapter);
        //ViewPager缓存两个Fragment
        pager.setOffscreenPageLimit(2); 
        int i = mypagerAdapter.getCount();
     // 将ViewPager的实例设置到了PagerSlidingTabStrip中
        tabs.setViewPager(pager);
        // 对PagerSlidingTabStrip的细节进行配置
        setTabsValue();
        
        // add necessary intent values to be matched.

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        filetransmitfragment = new FileTransmitFragment(manager,channel,this); //,intentFilter
        
        //初始化文件传输的fragment，因为handler需要初始化
        filelistfragment = new NewFileListFragment();      
        
    }
    public void onConfigurationChanged(Configuration newConfig){
    	Configuration newConfig1 = getResources().getConfiguration();
    	   if(newConfig1.orientation == Configuration.ORIENTATION_LANDSCAPE){
    	//横屏时
    	
    	}else if(newConfig1.orientation == Configuration.ORIENTATION_PORTRAIT){
    	//竖屏时
    	//setContentView(R.id.portrait);
    	}
    }
    
    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(Color.parseColor("#45c01a"));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
        tabs.setSelectedTextColor(Color.parseColor("#45c01a"));
        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
        
        //设置每个tab的宽度
        tabs.setTabPaddingLeftRight(36);
        
        // 设置tab适应屏幕，使得tab的宽度跟着tab数量的变化而变化
        //tabs.setShouldExpand(true);
        

    }
    public  class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        
        private final String[] titles = {"文件传输","文件管理","传输列表"};//
        
        @Override
        public CharSequence getPageTitle(int position){
        	return titles[position];
        }
        @Override
        public int getCount(){
        	return titles.length;
        }
        
        @Override
        public Fragment getItem(int position){
        	switch(position){
        	case 0:
        		if (filetransmitfragment != null)
        			return filetransmitfragment ;
        		else return null;
        	case 1:
        		if(filebrowser == null){
        			filebrowser = new FileBrowser();
        		}
        		return filebrowser;
        	case 2: if(filelistfragment == null){
        		filelistfragment = new NewFileListFragment();
        	}
        		return filelistfragment ;
        	default: return null;
        	}
        }
   }    
    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        //receiver = new WiFiDirectBroadcastReceiver(manager, channel, this );
        //registerReceiver(receiver, intentFilter);
        //Log.d(WiFiDirectActivity.TAG, "Fragment:"+getSupportFragmentManager().findFragmentByTag("devicelistfragment"));
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
    	FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
    	
    	DeviceListFragment fragmentList = (DeviceListFragment) fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
            fragmentList.ResetDisconnect();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
            
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                //final DeviceListFragment fragment = (DeviceListFragment) getSupportFragmentManager().findFragmentByTag("devicelistfragment");
                FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
            	final DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        //DeviceDetailFragment fragment = (DeviceDetailFragment) getSupportFragmentManager().findFragmentByTag("devicedetailfragment");
        FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        DeviceDetailFragment fragment = (DeviceDetailFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);        
        fragment.showDetails(device);

    }

   @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		TransferClient.executorService.shutdownNow();
		TransferServer.executorService.shutdownNow();
	}
	/* @Override
    public void connect(final WifiP2pConfig config) {
    	final String address = config.deviceAddress;  //内部类中使用要是final类型？
    	
    	//删除已经存在的分组信息
    	removeAndDeleteGroup();
    	//创建group组
    	manager.createGroup(channel, new ActionListener(){

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Log.i(TAG, "创建p2p组");
				 manager.connect(channel, config, new ActionListener() {

			            @Override
			            public void onSuccess() {
			                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
			            	Log.i(TAG, "组长是"+address);
			            }

			            @Override
			            public void onFailure(int reason) {
			                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
			                        Toast.LENGTH_SHORT).show();
			            }
			        });
				
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Log.i(TAG, "创建失败");
			}
    		
    	});
       
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
        
        removeAndDeleteGroup();
    }*/
    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                /*FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
            	DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
                Button btn_connect = (Button)fragment.mContentView.findViewById(R.id.btn_connect1);
                btn_connect.setText("断开");*/
            	//changeButtonState(true); //显示filebrowser中的按钮
            	
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        //final DeviceDetailFragment fragment = (DeviceDetailFragment) getSupportFragmentManager().findFragmentByTag("devicedetailfragment");
        FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        final DeviceDetailFragment fragment = (DeviceDetailFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);                
        fragment.resetViews();
        fragment.closeServerSocket() ;
        final DeviceListFragment fragmentlist = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
        fragmentlist.ResetDisconnect();
        
        changeButtonState(false);
        
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getrootView().setVisibility(View.GONE);
               
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            //final DeviceListFragment fragment = (DeviceListFragment)getSupportFragmentManager().findFragmentByTag("devicelistfragment");
            FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
            DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);        
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }    
    
    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    
    
    /**
     * @author fanlei
     * 实现SendFileCallbackListener回调接口，作为中介与deviceDetailFragment中的sendFile通信
     */
    @Override
	public void sendFile(ArrayList<String> list) {
		
		FileTransmitFragment fileTransmitfragment = (FileTransmitFragment)this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
     	DeviceDetailFragment fragment = (DeviceDetailFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);

		
		//DeviceDetailFragment fragment = (DeviceDetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
		try {
			fragment.sendFile(list);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    
    /**
     * DeviceListListener的回调接口
     */
    
    
    
    private void removeAndDeleteGroup(){
    	try {
			Method deletePersistentGroup = WifiP2pManager.class.getMethod("deletePersistentGroup");
			for(int netid=0; netid<32; netid++){
				deletePersistentGroup.invoke(manager, channel, netid, null);
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*
     * 滑动tab的实现
     */
	@Override
	public void slideTab(int position) {
		// TODO Auto-generated method stub
		pager.setCurrentItem(position);
	}
	@Override
	public WifiP2pDevice getDeviceInfo(String mac) {
		FileTransmitFragment fileTransmitfragment = (FileTransmitFragment)this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
     	DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
     	
		return fragment.getDeviceInfo(mac);
	}
	
	
	
	/**
	 * 改变filebrowser中的发送按钮的状态
	 * @param isconnect
	 */
	public void changeButtonState(boolean isconnect){
		//FileBrowser fb = (FileBrowser) this.getSupportFragmentManager().findFragmentById(R.id.filebrowser);
		FileBrowser fb= (FileBrowser) this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");
		fb.changeButtonState(isconnect);
	}
  
   
}
