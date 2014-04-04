package com.smarttoy.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.util.Log;

public class UDPReceiver implements Runnable {
	private static final String LOG_TAG = "UDP Receiver";
	protected static int MAX_PACKET_DATA_LENGTH = 1024 * 1024 * 2;
	protected OnReceiveCallBack m_callback = null;
	protected int m_port = 6650;
	protected DatagramSocket m_socket;
	protected boolean m_bRunning = false;
	
	public interface OnReceiveCallBack {
		public void onReceive(DatagramPacket packet, byte[] data);
	}
	
	public UDPReceiver(int port) {
		m_port = port;
	}
	
	public void setCallback(OnReceiveCallBack cb) {
		m_callback = cb;
	}
	
	public boolean isRunning() {
		return m_bRunning;
	}
	
	public void start() {
		try {
/*  		// reuse the socket port
  			m_socket = new DatagramSocket(null);
			m_socket.setReuseAddress(true);			
			m_socket.bind(new InetSocketAddress(UtilHelper.getIPv4Address(), m_port));
*/
			m_socket = new DatagramSocket(m_port);
			m_bRunning = true;
			new Thread(this).start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "initial socket failed!");
			e.printStackTrace();
			return;
		}
	}
	

	
	public void stop() {
		m_bRunning = false;
		if (m_socket != null) {
			m_socket.close();
			m_socket = null;
		}
	}
	
	@Override
	public void run() {
		byte[] data = new byte[MAX_PACKET_DATA_LENGTH];
		while (m_bRunning) {
			DatagramPacket pack = new DatagramPacket(data , data.length);

			try {
				m_socket.receive(pack);
			} catch (IOException e) {
				Log.d(LOG_TAG, e.getMessage());
				continue;
			}
			if (m_callback != null) {
				byte[] ret = new byte[pack.getLength()];
				Log.d(LOG_TAG, String.valueOf(pack.getLength()));
				System.arraycopy(pack.getData(), pack.getOffset(), ret, 0, pack.getLength());
				m_callback.onReceive(pack, ret);
			}
		}
		Log.d(LOG_TAG, "receive thread end!");
	}
}


/* usage
 * 
 * 	UDPReceiver rec = new UDPReceiver(port);
 * 	rec.setCallBack( new UDPReceiver.OnReceiveCallBack() {
 * 		onReceive(byte[] data);
 * 	})
 * 	rec.start();	// will execute in another thread
 * 	rec.stop();
 * 	rec = null;
 * 
 */