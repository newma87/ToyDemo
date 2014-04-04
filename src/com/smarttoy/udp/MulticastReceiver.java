package com.smarttoy.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.util.Log;

public class MulticastReceiver extends UDPReceiver {
	private static final String LOG_TAG = "Multicast Receiver";
	
	private String MULTICAST_HOST = "224.0.0.1";
	
	public MulticastReceiver(int port) {
		super(port);
	}
	
	public void setBrocastHost(String ip) {
		MULTICAST_HOST = ip;
	}
	
	public void start() {
		try {
			m_socket = (DatagramSocket)new MulticastSocket(m_port);
			
			InetAddress recvAddress = InetAddress.getByName(MULTICAST_HOST);
			((MulticastSocket)m_socket).joinGroup(recvAddress);
			m_bRunning = true;
			new Thread(this).start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "initial socket failed!");
			return;
		}
	}
}
