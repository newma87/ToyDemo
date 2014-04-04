package com.smarttoy.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.smarttoy.util.UtilHelper;

import android.util.Log;

public class TCPClient implements Runnable {
	private static final String LOG_TAG = "TCP Client";
	
	private Socket m_session = null;
	private InputStream m_input = null;
	private OutputStream m_output = null;
	private OnReceiveDataCallBack m_recCB = null;
	private StateListener m_stateListener = null;
	private ReadThread m_readThread = null;
	
	private int m_serverPort = 0;
	private String m_serverIp = "0.0.0.0";
		
	public interface OnReceiveDataCallBack {	
		public void onReceive(TCPClient client, byte[] data);
	}
	
	public interface StateListener {
		static final int ON_CONNECTE_TO_SERVER = 1;
		static final int ON_SERVER_SIDE_CLOSED = 2;
		static final int ON_CONNECTION_CLOSE = 3;
		
		void handleState(int state, Object obj);
	}
	
	public TCPClient() {
	}
	
	public boolean isConnected() {
		if (m_session == null) {
			return false;
		}
		return true;
	}
	
	public String getServerIp() {
		return m_serverIp;
	}
	
	public int getServerPort() {
		return m_serverPort;
	}
	
	public OnReceiveDataCallBack getReceiveCallBack() {
		return m_recCB;
	}

	public void setReceiveCallBack(OnReceiveDataCallBack recCB) {
		this.m_recCB = recCB;
	}

	public StateListener getStateListener() {
		return m_stateListener;
	}

	public void setStateListener(StateListener stateListener) {
		this.m_stateListener = stateListener;
	}
	
	public void connectServer(String ip, int port) {
		m_serverIp = ip;
		m_serverPort = port;
		
		if (!isConnected()) {
			Thread th = new Thread(this);
			th.start();
			try {
				th.join();
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			Log.d(LOG_TAG, "TCP client ip " + m_serverIp + " port " + m_serverPort + " start!");
		}
	}
	
	// send data
	public boolean sendData(byte[] data) {
		if (!isConnected()) {
			return false;
		}
		
		try {
			m_output.write(data);
			m_output.flush();
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			return false;
		}
		return true;
	}
	
	// stop client
	public void disconnect() {
		if (m_stateListener != null && isConnected()) {
			m_stateListener.handleState(StateListener.ON_CONNECTION_CLOSE, null);
		} 
		
		if (m_readThread != null && m_readThread.isRunning()) {
			m_readThread.stop();
			m_readThread = null;
		}
		closeSocket();
		
		Log.d(LOG_TAG, "TCP client ip " + m_serverIp + " port " + m_serverPort + " closed!");
	}
	
	private void closeSocket() {
		if (m_input != null) {
			try {
				m_input.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			m_input = null;
		}
		
		if (m_output != null) {
			try {
				m_output.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			m_output = null;
		}
		
		if (m_session != null) {
			try {
				m_session.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			m_session = null;
		}
	}
	
	@Override
	public void run() {
		boolean bResult = false;
		try {
			InetAddress serverAddr = InetAddress.getByName(m_serverIp);
			m_session = new Socket(serverAddr, m_serverPort);
			m_input = m_session.getInputStream();
			m_output = m_session.getOutputStream();
					
			m_readThread = new ReadThread();
			new Thread(m_readThread).start();
			bResult = true;
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			closeSocket();
		}
		
		if (m_stateListener != null) {
			m_stateListener.handleState(StateListener.ON_CONNECTE_TO_SERVER, bResult);
		}
	}
	
	class ReadThread implements Runnable {
		private boolean bRunning = true;
		
		public boolean isRunning() {
			return bRunning;
		}
		
		public ReadThread() {
			bRunning = true;
		}
		
		public void stop() {
			bRunning = false;
		}
		
		@Override
		public void run() {			
			while (bRunning) {
				if (m_input == null) {
					break;
				}
				byte[] data;
				try {
					data = UtilHelper.readNetInstructionData(m_input);
					if (m_recCB != null) {
						m_recCB.onReceive(TCPClient.this, data);
					}
				} catch (EOFException e) {
					m_stateListener.handleState(StateListener.ON_SERVER_SIDE_CLOSED, null);
					break;
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
					break;
				}
			}
		}
	}

}

/* usage
 * 
 * TCPClient client = new TCPClient();
 * client.connectServer(ip, port)
 * client.setReceiveCallBack(new OnReceiveDataCallBack() {
 * 		onReceive();	// will be in another thread
 * });
 * client.setStateListener( new StatetListener() {
 * 		ON_CONNECTE_TO_SERVER				// run in the main thread
 * 			ON_SERVER_SIDE_CLOSED			// run in the receive thread
 * 		ON_CONNECTION_CLOSE					// run in the main thread
 * 		handleState(state, obj)
 * })
 * client.sendData(byte[] data);
 * client.disconnect();
 * client = null;
 */
