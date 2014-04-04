package com.smarttoy.tcp;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.Log;

import com.smarttoy.processor.BaseProtocolProcessor;
import com.smarttoy.processor.BaseProtocolProcessor.DispatcherInterface;
import com.smarttoy.processor.MoveProtocolProcessor;
import com.smarttoy.processor.SimpleTextProcessor;
import com.smarttoy.protocol.BackwardProtocol;
import com.smarttoy.protocol.BaseProtocol;
import com.smarttoy.protocol.ForwardProtocol;
import com.smarttoy.protocol.SimpleTextProtocol;
import com.smarttoy.protocol.StopMoveProtocol;
import com.smarttoy.protocol.TurnLeftProtocol;
import com.smarttoy.protocol.TurnRightProtocol;

public class MessageDispatcher implements DispatcherInterface, TCPServer.OnReceiveDataCallBack{

	private static final String LOG_TAG = "Message Dispatcher";
	private TCPServer.CommunicateSession m_session = null;
	
	private WeakReference<Context> m_context = null;	// weak reference, that means the context may invalid at any time
	
	public MessageDispatcher(Context context) {
		m_context = new WeakReference<Context>(context);
	}
	
	@Override
	public void receiveData(byte[] data) {
		BaseProtocol protocol = new BaseProtocol();
		protocol.setData(data);
		
		BaseProtocolProcessor processor = null;
		
		switch(protocol.getType()) {
		case BaseProtocol.PT_REQUEST_VEDIO:
			break;
		// move protocol
		case BaseProtocol.PT_ROBOT_BACKWARD:
			protocol = new BackwardProtocol();
			processor = (new MoveProtocolProcessor(this));
			break;
		case BaseProtocol.PT_ROBOT_FORWARD:
			protocol = new ForwardProtocol();
			processor = (new MoveProtocolProcessor(this));
			break;
		case BaseProtocol.PT_ROBOT_TURN_LEFT:
			protocol = new TurnLeftProtocol();
			processor = (new MoveProtocolProcessor(this));
			break;
		case BaseProtocol.PT_ROBOT_TURN_RIGHT:
			protocol = new TurnRightProtocol();
			processor = (new MoveProtocolProcessor(this));
			break;
		case BaseProtocol.PT_ROBOT_STOP:
			protocol = new StopMoveProtocol();
			processor = (new MoveProtocolProcessor(this));
			break;			
	    // this is for test
		case BaseProtocol.PT_SIMPLE_TEXT:
			SimpleTextProcessor simple = new SimpleTextProcessor(this);
			processor = simple;
			protocol = new SimpleTextProtocol(data);
			break;
		default:
			processor = new BaseProtocolProcessor(this);
			break;
		}
		
		processor.receiveProtocol(protocol);
	}
	
	@Override
	public void sendData(byte[] data, int offset, int count) {
		if (m_session == null) {
			Log.e(LOG_TAG, "Outputstream is unvalidate! Can't send data.");
			return;
		}
		m_session.sendData(data, offset, count);
	}
	
	@Override
	public Context getContext() {
		return m_context.get();
	}
	
	@Override
	public void setContext(Context context) {
		m_context = new WeakReference<Context>(context);
	}

	@Override
	public void onReceive(TCPServer.CommunicateSession session, byte[] data) {
		// TODO Auto-generated method stub
		m_session = session;
		receiveData(data);
	}
}
 