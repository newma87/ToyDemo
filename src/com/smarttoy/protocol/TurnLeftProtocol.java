package com.smarttoy.protocol;

public class TurnLeftProtocol extends BaseProtocol {
	public TurnLeftProtocol() {
		super(BaseProtocol.PT_ROBOT_TURN_LEFT);
	}
	
	public TurnLeftProtocol(byte[] data) {
		super(data);
	}
}
