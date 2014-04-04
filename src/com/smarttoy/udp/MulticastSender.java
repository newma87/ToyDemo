package com.smarttoy.udp;

import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender extends UDPSender {
	private static String MULTICAST_IP = "224.0.0.1";
	
	public MulticastSender(int destPort) {
		super(MULTICAST_IP, destPort);
	}
	
	public MulticastSender(String ip, int destPort) {
		super(ip, destPort);
	}
	
	protected void setupSocket(String destIp, int destPort) {	
		try {
			if (m_socket == null) {
				m_socket = new MulticastSocket();
				((MulticastSocket)m_socket).setTimeToLive(4);		// brocast packet will live in the internet
			}
			
			m_destAddress = InetAddress.getByName(destIp);
			m_destPort = destPort;
		} catch (Exception e) {
			e.printStackTrace();
			m_socket = null;
		}
	}
}
