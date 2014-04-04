package com.smarttoy.udp;

import java.net.DatagramPacket;

import android.util.Log;


public class DeviceDiscover implements MulticastReceiver.OnReceiveCallBack {
	protected final String LOG_TAG = "Device Discover";
	
	protected final String REQUEST_DEVICE_HEADER = "romo_car_request_device";
	protected final String DEVICE_ADD_HEADER = "romo_car_device_add";
	protected final String DEVICE_REMOVE_HEADER = "romo_car_device_remove";
	
	private static final String BROCAST_IP = "255.255.255.255";
	
	private int m_broadcast_port = 8003;	// 广播端口
	private int m_discover_port = 8002;		// 发送或接收端口
	
	private UDPSender m_brocast = null;
	private UDPReceiver m_brocastRec = null;
	private UDPReceiver m_rec = null;
	private DeviceCallBack m_deviceCB = null;
	private RequestCallBack m_requestCB = null;
	
	public DeviceDiscover(int broadcastPort, int discoverPort) {
		m_broadcast_port = broadcastPort;
		m_discover_port = discoverPort;
		
		m_brocast = new UDPSender(BROCAST_IP, m_broadcast_port);
		
		m_brocastRec = new UDPReceiver(m_broadcast_port);
		m_brocastRec.setCallback(this);
		
		m_rec = new UDPReceiver(m_discover_port);
		m_rec.setCallback(this);
	}
	
	// Discover side
	public interface DeviceCallBack {
		// Find device success
		public void onDeviceDiscovered(String ip);
		// Remote device has been removed
		public void onRemoteDeviceRemoved(String ip);
	}
	
	// Being discovered side
	public interface RequestCallBack {
		// Keeping a list of host who had requested to discover this device, 
	 	// and send remove message to them when this device removed. 
		public void onReceiveDiscoverRequest(String ip);
	}
	
	public void setDeviceCallBack(DeviceCallBack cb) {
		m_deviceCB = cb;
	}
		
	public void setRequestCallBack(RequestCallBack cb) {
		m_requestCB = cb;
	}

	// 发送广播，并监听目标机器的回包
	public void brocast() {
		if (!m_rec.isRunning()) {
			m_rec.start();
		}
		sendRequestBrocast();
		
		Log.d(LOG_TAG, "broadcast discover messages");
	}
	
	// 接收广播消息
	public void listenBrocast() {
		if (!m_brocastRec.isRunning()) {
			m_brocastRec.start();
		}
		
		Log.d(LOG_TAG, "start listen to broadcast messages");
	}
	
	public void stop() {
		m_brocast.close();
		m_rec.stop();
		m_brocastRec.stop();
		
		Log.d(LOG_TAG, "device discover stop!");
	}
	
	//　重新发送广播消息
	private void sendRequestBrocast() {
		byte[] data = REQUEST_DEVICE_HEADER.getBytes();
		m_brocast.send(data);		
	}
	
	// 当设备接入，直接发送设备加入的消息
	private void sendAddDevice(String ip) {
		UDPSender sender = new UDPSender(ip, m_discover_port);
		sender.sendWithoutThread(DEVICE_ADD_HEADER.getBytes());
		sender.close();
	}
	
	// 当设备断开前，需要发送设备删除消息
	public void sendRemoveDevice(String ip) {
		UDPSender sender = new UDPSender(ip, m_discover_port);
		sender.sendWithoutThread(DEVICE_ADD_HEADER.getBytes());
		sender.close();
	}
	
	@Override
	public void onReceive(DatagramPacket packet, byte[] data) {
		String str = new String(data);
		String ip = packet.getAddress().getHostAddress();
		if (str.equals(DEVICE_ADD_HEADER)) {
			if (m_deviceCB != null) {
				m_deviceCB.onDeviceDiscovered(ip);
			}
		} else if (str.equals(DEVICE_REMOVE_HEADER)) {
			if (m_deviceCB != null) {
				m_deviceCB.onRemoteDeviceRemoved(ip);
			}
		} else if (str.equals(REQUEST_DEVICE_HEADER)) {
			sendAddDevice(ip);
			if (m_requestCB != null) {
				m_requestCB.onReceiveDiscoverRequest(ip);
			}
		}
	}
}

/*
 * Usage:
 * 
 * 1, The discover side
 * DeviceDiscover discover = new DeviceDiscover();
 * discover.setDeviceCallBack(new DeviceCallBack() {
 * 		public void onDeviceDiscovered(String ip) {}
 *		public void onRemoteDeviceRemoved(String ip) {}
 * })
 * discover.broadcast();
 * discover.stop();
 * 
 * 2, The being discovered side
 * DeviceDiscover discover = new DeviceDiscover();
 * discover.setRequestCallBack(new RequestCallBack() {
 * 		public void onReceiveDiscoverRequest(String ip) {}
 * })
 * discover.listenBroadcast();
 * discover.stop();
 */