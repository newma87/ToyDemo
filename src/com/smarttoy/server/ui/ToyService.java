package com.smarttoy.server.ui;

import com.smarttoy.R;
import com.smarttoy.udp.DeviceDiscover;
import com.smarttoy.udp.DeviceDiscover.RequestCallBack;
import com.smarttoy.global.Constraint;
import com.smarttoy.tcp.TCPServer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ToyService extends Service {
	private static final int NOTIFY_ID = 1;
	
	// Tcp command
	private TCPServer m_commandRec;
	
	// Discover
	private DeviceDiscover m_discover;
	
	public ToyService() {
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {		
		m_discover = new DeviceDiscover(Constraint.BROADCAST_DISCOVER_PORT, Constraint.DISCOVER_PORT);
		m_commandRec = new TCPServer(Constraint.COMMAND_PORT);
        
		 Notification notification = new Notification(R.drawable.ic_launcher,  
	                "Smart Toy", System.currentTimeMillis());  
	     Intent notificationIntent = new Intent(this, ServerActivity.class);  
	     PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,  
	                notificationIntent, 0);  
	     notification.setLatestEventInfo(this, "Smart Toy", "服务端接收程序正在运行",  
	                pendingIntent);  
	     startForeground(NOTIFY_ID, notification);
	        
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		m_discover.listenBrocast();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		if (m_commandRec != null) {
			m_commandRec.closeServer();
			m_commandRec = null;
		}
		if (m_discover != null) {
			m_discover.stop();
			m_discover = null;
		}
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new ToyBind();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public class ToyBind extends Binder {
		public void startServer(TCPServer.OnReceiveDataCallBack receivCB, 
								TCPServer.StateListener stateListener, 
								TCPServer.OnSendDataCallBack sendCB) {
			m_commandRec.setReceiveCallBack(receivCB);
			m_commandRec.setStateListener(stateListener);
			m_commandRec.setSendCallBack(sendCB);
			m_commandRec.startServer();
		}
		
		public void startServer() {
			m_commandRec.startServer();
		}
		
		public void stopServer() {
			m_commandRec.closeServer();
		}
		
		
		public void startDiscover(RequestCallBack cb) {
			setReceiveDeviceDiscoverRequestCallBack(cb);
			m_discover.listenBrocast();
		}
		
		public void stopDiscover() {
			m_discover.stop();
		}
		
		public void setReceiveDeviceDiscoverRequestCallBack(RequestCallBack cb) {
			m_discover.setRequestCallBack(cb);
		}
	}
	
}
