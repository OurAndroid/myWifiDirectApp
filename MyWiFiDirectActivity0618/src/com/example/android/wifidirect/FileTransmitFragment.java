package com.example.android.wifidirect;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.content.ContextWrapper;
//import android.widget.FrameLayout;
//import android.widget.TextView;
import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

public class FileTransmitFragment extends Fragment{

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;
    private boolean retryChannel = false;
    private IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver receiver = null;
    private View rootView = null;

    
    public FileTransmitFragment(WifiP2pManager manager, Channel channel,
			WiFiDirectActivity activity ) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.activity = activity;
		//this.intentFilter = intentFilter ;, IntentFilter intentFilter
	}
    
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
	/**
	 * @param args
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	Bundle savedInstanceState) {
		
	rootView = inflater.inflate(R.layout.filetransmit,container,false);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
  //��ʾWi-Fi�Ե�����״̬�����˸ı�  
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
  //��ʾ���õĶԵȵ���б?���˸ı�
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
  //��ʾWi-Fi�Ե����������״̬�����˸ı� 
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
  //�豸������Ϣ�����˸ı� 
    DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
            .findFragmentById(R.id.frag_list);
    
    
		ImageButton btn_discover = (ImageButton)fragment.mContentView.findViewById(R.id.Imagebtn_discover);
		btn_discover.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if (!(activity.getIsWifiP2pEnabled())) {
	                    Toast.makeText(getActivity(), R.string.p2p_off_warning,
	                            Toast.LENGTH_SHORT).show();
	                }
	                //final DeviceListFragment fragment = (DeviceListFragment) getSupportFragmentManager().findFragmentByTag("devicelistfragment");
	                //FileTransmitFragment fileTransmitfragment = (FileTransmitFragment) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
	            	final DeviceListFragment fragment = (DeviceListFragment)getFragmentManager().findFragmentById(R.id.frag_list);
	            	ChangeFragmentHeight(300);
	            	fragment.mContentView.findViewById(R.id.tv_temp).setVisibility(View.GONE);	
	            	fragment.onInitiateDiscovery();
	                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

	                    @Override
	                    public void onSuccess() {
	                        Toast.makeText(getActivity(), "Discovery Initiated",
	                                Toast.LENGTH_SHORT).show();
	                    }

	                    @Override
	                    public void onFailure(int reasonCode) {
	                        Toast.makeText(getActivity(), "Discovery Failed : " + reasonCode,
	                                Toast.LENGTH_SHORT).show();
	                    }
	                });
			}
		});
	return rootView;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, activity );
        getActivity().registerReceiver(receiver, intentFilter);
        
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }
   /* @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceListFragment f1 = (DeviceListFragment) getFragmentManager()
                                             .findFragmentById(R.id.frag_list);
        if (f1 != null) 
            getFragmentManager().beginTransaction().remove(f1).commitAllowingStateLoss();
        DeviceDetailFragment f2 = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if(f2 != null)
        	getFragmentManager().beginTransaction().remove(f2).commitAllowingStateLoss();
    }*/
	public void show_me() {
		// TODO Auto-generated method stub
		/*DeviceListFragment fragment = (DeviceListFragment) getChildFragmentManager()
                .findFragmentById(R.id.frag_list);*/
		Log.d(WiFiDirectActivity.TAG, "Fragment:"+getFragmentManager().findFragmentByTag("devicelistfragment"));
	}
	public void ChangeFragmentHeight(int height){
		DisplayMetrics dm = getResources().getDisplayMetrics();
    	int fragmentHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, dm);    	
		rootView.findViewById(R.id.frag_list).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,fragmentHeight));		
		rootView.findViewById(R.id.frag_detail).setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	}
}
