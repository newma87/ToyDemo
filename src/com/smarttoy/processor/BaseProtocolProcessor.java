package com.smarttoy.processor;

import android.content.Context;
import android.util.Log;

import com.smarttoy.protocol.BaseProtocol;

public class BaseProtocolProcessor {
	final String LOG_TAG = "Base protocol processor";
	protected DispatcherInterface m_dispatcher = null;
	
	public static interface DispatcherInterface {
		public void receiveData(byte[] data);
		public void sendData(byte[] data, int offset, int count);
		
		public Context getContext();
		public void setContext(Context context);
	}

	
	public BaseProtocolProcessor(DispatcherInterface dispatcher) {
		m_dispatcher = dispatcher;
	}
	
	public void receiveProtocol(BaseProtocol data) {
		Log.d(LOG_TAG, "Not Override the receive protocol function.");
	}

	public void sendProtocol(BaseProtocol protocol) {
		byte[] data = protocol.getData();
		m_dispatcher.sendData(data, 0, data.length);
	}
}
