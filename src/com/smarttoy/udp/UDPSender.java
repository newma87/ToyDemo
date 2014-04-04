package com.smarttoy.udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.util.Log;

public class UDPSender {
	private static final String LOG_TAG = "UDP Sender";
	
	protected DatagramSocket m_socket;
	protected int m_destPort = 6650;
	protected InetAddress m_destAddress;
	
	protected boolean bRunning = false;
		
	public UDPSender(String destIp, int destPort) {
		setupSocket(destIp, destPort);
	}
	
	public void close() {
		releaseSocket();
	}
	
	public boolean isValidable() {
		if (m_socket != null) {
			return true;
		}
		
		return false;
	}
	
	protected void setupSocket(String destIp, int destPort) {
		try {
			if (m_socket == null) {
				m_socket = new DatagramSocket();
			}
			m_destAddress = InetAddress.getByName(destIp);
			m_destPort = destPort;
		} catch (Exception e) {
			e.printStackTrace();
			m_socket = null;
		}	
	}
	
	protected void releaseSocket() {
		if (m_socket != null) {
			m_socket.close();
			m_socket = null;
		}
	}
	
	public void sendWithoutThread(byte[] data) {
		if (data == null) {
			return;
		}
		try {
			DatagramPacket pack = new DatagramPacket (data , data.length , m_destAddress, m_destPort);
			m_socket.send(pack);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void send(byte[] data) {
		if (m_socket == null) {
			Log.e(LOG_TAG, "socket hasn't been initialize, can't send data!");
			return;
		}
		
		SendThread thread = new SendThread(data);
		thread.start();
		try {
			thread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(InputStream in) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[1024 * 1024 * 2];

		try {
			while ((nRead = in.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		send(buffer.toByteArray());
	}

	protected class SendThread extends Thread {
		byte[] m_data = null;
		
		public SendThread(byte[] data) {
			m_data = data;
		}
		
		@Override
		public void run() {
			if (m_data == null) {
				return;
			}
			
			DatagramPacket pack = new DatagramPacket (m_data , m_data.length , m_destAddress, m_destPort);
			try {			
				m_socket.send(pack);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	} 
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}

/* usage
 * 
 * UDPSender sender = new UDPSender(String ip, int port)
 * sender.send(byte[] data)		// will be execute in another thread
 * sender.close();
 * sender = null;
 * 
 */
