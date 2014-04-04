package com.smarttoy.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.smarttoy.global.Constraint;
import com.smarttoy.util.UtilHelper;


import android.util.Log;

public class TCPServer implements Runnable {
	static final String LOG_TAG = "TCP Server";
	
	public interface StateListener {
		static final int ON_SERVER_START = 1;
		static final int ON_SERVER_CLOSE = 2;
		static final int ON_SESSION_START = 3;
		static final int ON_SESSION_END = 4;
		
		public void handleState(int state, Object obj);
	}
	
	public interface OnReceiveDataCallBack {
		public void onReceive(CommunicateSession session, byte[] data);
	}
	
	public interface OnSendDataCallBack {
		public void onSend(CommunicateSession session, byte[] data, int offset, int count);
	}
	
	private ServerSocket m_socket = null;
	private Map<Integer, CommunicateSession> m_sessions = new HashMap<Integer, CommunicateSession>();	//TODO: violate or not?
	private int m_port = 0;
	private boolean bRunning = false;
	
	private StateListener m_stateListener = null;
	private OnReceiveDataCallBack m_recCB = null;
	private OnSendDataCallBack m_sendCB = null;
	
	public TCPServer(int port) {
		m_port = port;
	}	
	
	public boolean isRunning() {
		if (m_socket != null && bRunning) {
			return true;
		}
		return false;
	}
	
	public StateListener getStateListener() {
		return m_stateListener;
	}

	public void setStateListener(StateListener m_stateListener) {
		this.m_stateListener = m_stateListener;
	}

	public OnReceiveDataCallBack getReceiveCallBack() {
		return m_recCB;
	}

	public void setReceiveCallBack(OnReceiveDataCallBack receiveCallBack) {
		this.m_recCB = receiveCallBack;
	}

	public OnSendDataCallBack getSendCallBack() {
		return m_sendCB;
	}

	public void setSendCallBack(OnSendDataCallBack sendCB) {
		this.m_sendCB = sendCB;
	}
	
	public void startServer() {
		bRunning = true;
		
		if (m_socket == null) {
			new Thread(this).start();
			Log.d(LOG_TAG, "TCP port " + m_port + " start!");
		}
	}
	
	public void closeServer() {
		if (m_stateListener != null && isRunning()) {
			m_stateListener.handleState(StateListener.ON_SERVER_CLOSE, null);
		}
		
		bRunning = false;
		
		//endAllSessions();  //  Actually here is not need to end the sessions. sessions will be created in another thread, and also will be ended in that thread!
		
		if (m_socket != null) {
			try {
				m_socket.close();
			} catch (Exception e) {
				Log.w(LOG_TAG, e.getMessage());
			}
			m_socket = null;
		}
		
		Log.d(LOG_TAG, "TCP port " + m_port + " closed!");
	}
	
	private void startSession(Socket socket) {
		CommunicateSession session = new CommunicateSession(socket);
		synchronized(m_sessions) {
			m_sessions.put(session.getId(), session);
		}
		new Thread(session).start();
	}
	
	private void endSession(int id) {
		synchronized(m_sessions) {
			CommunicateSession session = m_sessions.get(id);
			if (session != null) {
				session.close();
				m_sessions.remove(id);
			}
		}
	}	

	private synchronized void endAllSessions() {
		synchronized(m_sessions) {
			for (Map.Entry<Integer, CommunicateSession> entry : m_sessions.entrySet()) {
				CommunicateSession session = (CommunicateSession)entry.getValue();
				session.close();
			}
			
			m_sessions.clear();
		}
	}
	
	public int getSessionsCount() {
		return m_sessions.size();
	}
	
	public CommunicateSession getSessionById(int id) {
		return m_sessions.get(id);
	}
	
	@Override
	public void run() {	
		try {
			m_socket = new ServerSocket(m_port);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			m_socket = null;
			if (m_stateListener != null) {
				m_stateListener.handleState(StateListener.ON_SERVER_START, false);
			}
			return;
		}
		
		if (m_stateListener != null) {
			m_stateListener.handleState(StateListener.ON_SERVER_START, true);
		}
		
		Socket session_sock = null;
		while (bRunning) {
			try {
				session_sock = m_socket.accept();
				
				// start a new session thread
				if (getSessionsCount() < Constraint.MAX_SESSION_NUMBER) {
					startSession(session_sock);
				} else {
					session_sock.close();
				}
			} catch (Exception e) {
				Log.d(LOG_TAG, e.getMessage());
				break;
			}
		}
		endAllSessions();
	}
	
	static int m_index = 0;
	public class CommunicateSession implements Runnable {
		private Socket m_socket;
		private int m_id = 0;
		
		private InputStream m_input;
		private OutputStream m_output;
		
		private StateListener m_listener = null;
		
		public class PeersInfo {
			public int session_id;
			public String remote_ip;
			public int remote_port;
			public String local_ip;
			public int local_port;
		}
		
		public CommunicateSession() {
			m_id = ++m_index;
		}
		
		public CommunicateSession(Socket socket) {
			m_id = ++m_index;
			setup(socket);
		}
		
		public int getId() {
			return m_id;
		}
		
		public PeersInfo getPeersInfos() {
			PeersInfo info = new PeersInfo();
			info.session_id = m_id;
			info.remote_ip = m_socket.getInetAddress().getHostAddress();
			info.remote_port = m_socket.getPort();
			info.local_ip = m_socket.getLocalAddress().getHostAddress();
			info.local_port = m_socket.getLocalPort();
			
			return info;
		}
		
		public void setup(Socket socket) {
			m_socket = socket;
			m_listener = m_stateListener;
					
			try {
				m_input = m_socket.getInputStream();
				m_output = m_socket.getOutputStream();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			
			if (m_listener != null) {
				m_listener.handleState(StateListener.ON_SESSION_START, this.getPeersInfos());
			}
		}
		
		public void close() {	
			if (m_listener != null) {
				m_listener.handleState(StateListener.ON_SESSION_END, this.getPeersInfos());
				m_listener = null;
			}
			
			if (m_input != null) {
				try {
					m_input.close();
				} catch (Exception e) {
					Log.w(LOG_TAG, e.getMessage());
				}
				m_input = null;
			}
			
			if (m_output != null) {
				try {
					m_output.close();
				} catch (Exception e) {
					Log.w(LOG_TAG, e.getMessage());
				}
				m_output = null;
			}
			
			if (m_socket != null) {
				try {
					m_socket.close();
				} catch (Exception e) {
					Log.w(LOG_TAG, e.getMessage());
				}
				m_socket = null;
			}
			m_id = 0;
		}

		public void sendData(byte[] data, int offset, int count) {
			if (m_output == null) {
				return;
			}
			
			try {
				m_output.write(data, offset, count);
				
				if (m_sendCB != null) {
					m_sendCB.onSend(this, data, offset, count);
				}
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
		
		public void run() {
			while (!Thread.currentThread().isInterrupted() && m_socket != null) {

				do {
					byte[] data = null;
					try {
						data = UtilHelper.readNetInstructionData(m_input);
					} catch (EOFException e) {
						Log.w(LOG_TAG, "client close the connection, this session will be ended");
						endSession(this.getId());	// end this session, thus it will call this.close
						break;
					} catch (IOException e) {
						Log.e(LOG_TAG, e.getMessage());
						break;
					}
					
					if (m_recCB != null) {
						m_recCB.onReceive(this, data);
					}
				} while(true);
			}
		}
	}
}

/* Usage
 * 
 * { Threads : MainThread -> listenThread -> sessionThread }
 * 
 * TCPServer server = new TCPServer(int port);
 * server.setStateListener(new StatetListener() {
 * 		startServer				// running in listen thread;
 * 			startSession		// running in listen thread;
 * 			endSession			// running in listen thread(close active by server) or sessionThread(close passive by client);
 * 		closeSession			// running in main thread;
 * 		handleState(int state, Object obj)			// this is running in the listen thread
 * })
 * server.setReceiveCallBack(new OnReceiveDataCallBack() {
 * 		onReceive(CommunicateSession session, byte[] data);		// this is running in the session thread
 * })
 * server.setSendCallBack(new OnSendDataCallBack() {
 * 		onSend(CommunicateSession session, byte[] data, int offset, int count);	// this is runnnig in the session thread
 * })
 * server.startServer();
 * 		startSession();
 * 			onReceiveCallBack();
 * 		endSession();
 * server.closeServer();
 * server = null;
 */

