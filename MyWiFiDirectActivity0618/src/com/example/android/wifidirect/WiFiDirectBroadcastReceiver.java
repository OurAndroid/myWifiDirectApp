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



import android.support.v4.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.view.View;

import com.example.android.wifidirect.R;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
            WiFiDirectActivity activity  ) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);     //检测Wi-Fi direct功能是否开启
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
        	//从Wi-Fi P2P管理器中请求可用的对等点，这是个异步的调用，并且掉用行为是通过PeerListListener.onPeersAvailable()上的回调函数来实现的。
            if (manager != null) {
            	FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) activity.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
            	DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
                manager.requestPeers(channel, (PeerListListener)  fragment);
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
            	FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) activity.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
                DeviceDetailFragment fragment = (DeviceDetailFragment) fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);  //获取一个设备的连接信息，会调用onConnectionInfoAvailable方法
            } else {
                // It's a disconnect       
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {  //当设备的详细信息改变的时候进行广播，比如设备的名称
            //DeviceListFragment fragment = (DeviceListFragment) activity.getSupportFragmentManager().findFragmentByTag("devicelistfragment");
            FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) activity.getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        	DeviceListFragment fragment = (DeviceListFragment)fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            //activity.getSupportFragmentManager().beginTransaction().add(R.id.list_fragment, fragment).commit();
        	
            //DeviceDetailFragment fragment2 = (DeviceDetailFragment) fileTransmitfragment.getFragmentManager().findFragmentById(R.id.frag_detail);
            //fragment2.getrootView().setVisibility(View.VISIBLE);
            
            /*Log.d(WiFiDirectActivity.TAG,"Fragment2:"+fragment2);
            Log.d(WiFiDirectActivity.TAG,"Fragment2.getView():"+fragment2.getView());
            Log.d(WiFiDirectActivity.TAG,"Fragment2.getrootView():"+fragment2.getrootView());
        	Log.d(WiFiDirectActivity.TAG, "Fragment:"+fileTransmitfragment);
        	Log.d(WiFiDirectActivity.TAG, "device:"+(WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/
        }
    }
}
