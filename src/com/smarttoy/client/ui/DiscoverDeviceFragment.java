package com.smarttoy.client.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.smarttoy.R;
import com.smarttoy.global.Constraint;
import com.smarttoy.udp.DeviceDiscover;
import com.smarttoy.udp.DeviceDiscover.DeviceCallBack;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DiscoverDeviceFragment extends Fragment implements DeviceCallBack {

	private DeviceDiscover m_discover = null;
	 
	private Button m_btnResearch = null;
	private Button m_btnOtherWay = null;
	private ListView m_lstView = null;
	private ArrayList<String> m_devices = new ArrayList<String> ();
	private HashMap<String, Integer> m_ipMap = new HashMap<String, Integer> ();
	private CallBack m_cb = null;
	
	public interface CallBack {
		public void onConnectToDevice(String ip);
	}
		
	public DiscoverDeviceFragment() {
		m_discover = new DeviceDiscover(Constraint.BROADCAST_DISCOVER_PORT, Constraint.DISCOVER_PORT);
	}
	
	public void setConnectCallBack(CallBack cb) {
		m_cb = cb;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_device_list, container, false);
		
		m_btnResearch = (Button)v.findViewById(R.id.btn_research);
		m_btnResearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dicoverDevice();
			}
		});
		m_btnOtherWay = (Button)v.findViewById(R.id.btn_other_way);
		m_btnOtherWay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
			}
		});
		m_lstView = (ListView)v.findViewById(R.id.lst_device_ip);
		
		m_discover.setDeviceCallBack(this);
		
		return v;
	}

	private void dicoverDevice() {
		m_devices.clear();
		m_ipMap.clear();
		m_discover.brocast();
		
		// newma TODO: add progress
	}
	
	@Override
	public void onPause() {
		m_discover.stop();
		// newma TODO: disable progress
		super.onPause();
	}

	@Override
	public void onResume() {
		dicoverDevice();
		super.onResume();
	}

	private static final int UPDATE_DEVICE = 1;
	Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_DEVICE:
				m_lstView.setAdapter(new ListViewAdapter(m_devices));
				// newma TODO: disable progress
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	public void onDeviceDiscovered(String ip) {
		m_devices.add(ip);
		handler.sendEmptyMessage(UPDATE_DEVICE);
	}

	@Override
	public void onRemoteDeviceRemoved(String ip) {
		int index = m_ipMap.get(ip);
		m_devices.remove(index);
		m_ipMap.remove(ip);
		handler.sendEmptyMessage(UPDATE_DEVICE);
	}
	
	private class ListViewAdapter extends BaseAdapter {
		View[] itemViews;

		public ListViewAdapter(ArrayList<String> list) {
			initView(list);
		}

		public void initView(ArrayList<String> list) {
			itemViews = new View[list.size()];
			for (int i = 0; i < list.size(); i++){
				itemViews[i] = makeItemView(list.get(i));
			}
		}
		
		public int getCount() {
			return itemViews.length;
		}

		public View getItem(int position) {
			return itemViews[position];
		}

		public long getItemId(int position) {
			return position;
		}

		private View makeItemView(String ip) {
			Activity act = DiscoverDeviceFragment.this.getActivity();
			LayoutInflater inflater = (LayoutInflater) act
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View itemView = inflater.inflate(R.layout.device_list_item, null);

			// 通过findViewById()方法实例R.layout.item内各组件
			TextView title = (TextView) itemView.findViewById(R.id.txt_device_ip);
			title.setText(ip);
			title.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {					
					if (m_cb != null) {
						TextView txt = (TextView) v;
						String ip = txt.getText().toString();
						m_cb.onConnectToDevice(ip);
					}
				}
				
			});
			
			return itemView;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				return itemViews[position];
			return convertView;
		}
	}

}
