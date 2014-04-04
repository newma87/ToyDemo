package com.smarttoy.processor;

import android.content.Context;
import android.util.Log;

import com.smarttoy.protocol.BaseProtocol;
import com.smarttoy.protocol.SimpleTextProtocol;
import com.smarttoy.tcp.MessageDispatcher;

public class SimpleTextProcessor extends BaseProtocolProcessor {
	private Context m_context;
	
	public SimpleTextProcessor(MessageDispatcher dispatcher) {
		super(dispatcher);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void receiveProtocol(BaseProtocol data) {
		SimpleTextProtocol protocol = (SimpleTextProtocol)data;
		String text = protocol.getText();
		
		Log.d("Simple text say: ", text);
		if (m_context != null) {
			//((ServerActivity)m_dispatcher.getContext()).handleState(ServerActivity.SHOW_TEXT, String.valueOf(text));
		}
		
		protocol.setText("I'm server, I have received your message!");
		super.sendProtocol(protocol);
	}
}
