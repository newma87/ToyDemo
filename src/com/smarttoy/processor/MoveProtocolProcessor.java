package com.smarttoy.processor;

import java.util.HashMap;
import java.util.Map;

import android.media.AudioManager;
import android.media.ToneGenerator;

import com.smarttoy.protocol.BaseProtocol;

public class MoveProtocolProcessor extends BaseProtocolProcessor {
	private static ToneGenerator m_tone = new ToneGenerator(AudioManager.STREAM_DTMF,ToneGenerator.MAX_VOLUME);
	
	private static final int TONE_MS = 65;
	
	@SuppressWarnings("serial")
	private static Map<Integer, Integer> m_toneHash = new HashMap<Integer, Integer>() {
		{
			put(BaseProtocol.PT_ROBOT_FORWARD, ToneGenerator.TONE_DTMF_2);
			put(BaseProtocol.PT_ROBOT_BACKWARD, ToneGenerator.TONE_DTMF_8);
			put(BaseProtocol.PT_ROBOT_TURN_LEFT, ToneGenerator.TONE_DTMF_4);
			put(BaseProtocol.PT_ROBOT_TURN_RIGHT, ToneGenerator.TONE_DTMF_6);
			put(BaseProtocol.PT_ROBOT_STOP, ToneGenerator.TONE_DTMF_5);
		}
	};
	
	public MoveProtocolProcessor(DispatcherInterface dispatcher) {	
		super(dispatcher);
	}
	
	@Override
	public void receiveProtocol(BaseProtocol data) {
		int type = data.getType();
		int tone_type = m_toneHash.get(type);
		m_tone.startTone(tone_type, TONE_MS);
		
		if (m_dispatcher.getContext() != null) {
			//((ServerActivity)m_dispatcher.getContext()).handleState(ServerActivity.SHOW_TEXT, String.valueOf(tone_type));
		}
	}
	
	class TimeTickle implements Runnable {
		private int m_timeout = 0; // in microsecond
		private int m_countDown = 0;
		
		public TimeTickle(int timeout) {
			m_timeout = timeout;
			m_countDown = m_timeout;
		}
		
		public void refreshTime() {
			synchronized(this) {
				if (m_countDown != 0) {
					m_countDown = m_timeout;
				} else {		// if countDown == 0 means the previous thread is over
					new Thread(this).start(); // start a new thread for count down
				}
			}
		}
		
		@Override
		public void run() {
			while (m_countDown > 0) {
				synchronized(this) {
					m_countDown--;
				}
			}
		}
	}
}
