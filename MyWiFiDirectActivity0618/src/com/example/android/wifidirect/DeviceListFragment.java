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

//import android.app.ListFragment;
import java.util.ArrayList;
import java.util.List;

import com.todddavies.components.progressbar.ProgressWheel;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;



/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    ProgressWheel mProgressWheel = null ;
    View mContentView = null;
    private WifiP2pDevice device;
    private WiFiPeerListAdapter myWifiListAdapter ;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myWifiListAdapter = new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers);
        this.setListAdapter(myWifiListAdapter);
		//创建一个适配器，里面有三个参数，第一个参数是上下文，就是当前的Activity，第二个参数是布局方式，使用的是自定义的row_devices布局，第三个参数是要显示的数据。
		//peers是一个ArrayList，peers经过处理后显示在ListView上
    }

    public WiFiPeerListAdapter getMyWifiListAdapter() {
		return myWifiListAdapter;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        Button btn_dis = (Button)mContentView.findViewById(R.id.btn_disconnect1);
        mProgressWheel = (ProgressWheel)mContentView.findViewById(R.id.progressBar1);
        btn_dis.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getActivity().stopService(new Intent(getActivity(), FileRecieveService.class));
            	getActivity().stopService(new Intent(getActivity(), FileTransferService.class));
				((DeviceActionListener) getActivity()).disconnect();
			}
		});
        mProgressWheel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        if (mProgressWheel != null && mProgressWheel.isSpinning()) {
		        	mProgressWheel.setVisibility(View.GONE);
		        }
		        //再次改变DeviceListFragment的View的高度
		        FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
		        fileTransmitfragment.ChangeFragmentHeight(200);
			}
		});
        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
        case WifiP2pDevice.AVAILABLE:
            return "可连接";
        case WifiP2pDevice.INVITED:
            return "被邀请";
        case WifiP2pDevice.CONNECTED:
            return "已连接";
        case WifiP2pDevice.FAILED:
            return "失败";
        case WifiP2pDevice.UNAVAILABLE:
            return "不可用";
        default:
            return "Unknown";

        }
    }

    /**
     * Initiate a connection with the peer.
     */
	//单击ListView中的一项，显示详情
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
	//android中的Adapter是这么理解的，是数据和视图之间的桥梁，数据在adapter中做处理，然后显示到视图上面
	//WifiPeerListAdapter继承自ArrayAdapter
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;
        private WifiP2pDevice device ;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final int deviceposition = position;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;

        }
    }

    /**
     * Update UI for this device.
     * 
     * @param device WifiP2pDevice object
     */
	//显示我的设备的信息
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText("名称："+device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText("状态："+getDeviceStatus(device.status));
    }
    //获取对等设备列表
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (mProgressWheel != null && mProgressWheel.isSpinning()) {
        	mProgressWheel.setVisibility(View.GONE);
        }
        //再次改变DeviceListFragment的View的高度
        FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
        fileTransmitfragment.ChangeFragmentHeight(200);
        peers.clear();
        peers.addAll(peerList.getDeviceList());
		//如果AdapterView可以处理该数据，则变更通知它。比如，如果你有可用对等点的ListView，那就发起一次更新。
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }

    }
    //Peers的处理
    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }
    public void showDisconnect(){
    	Button btn = (Button)mContentView.findViewById(R.id.btn_disconnect1);
    	if(btn != null)
    		btn.setVisibility(View.VISIBLE);
    }
    public void ResetDisconnect(){
    	Button btn = (Button)mContentView.findViewById(R.id.btn_disconnect1);
    	if(btn != null)
    		btn.setVisibility(View.GONE);
    }
    /**
     * 初始化发现设备的设置，主要是显示对话框的信息
     */
    public void onInitiateDiscovery() {
        /*if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "寻找设备", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        
                    }
                });*/
    	mProgressWheel.setVisibility(View.VISIBLE);
    	mProgressWheel.spin();
    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }

	
	public WifiP2pDevice getDeviceInfo(String mac) {
		for(WifiP2pDevice device:peers){
			if(device.deviceAddress.trim().equals(mac.trim())){
				return device;
			}
		}
		return null;
	}
  
	 /**
     * deviceListFragment定义的接口
     * @author Administrator
     *
     */
    public interface DeviceListListener{
    	WifiP2pDevice getDeviceInfo(String mac);
    }
	
}
