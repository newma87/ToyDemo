package com.smarttoy.protocol;

public class TurnRightProtocol extends BaseProtocol {
	public TurnRightProtocol() {
		super(BaseProtocol.PT_ROBOT_TURN_RIGHT);
	}
	
	public TurnRightProtocol(byte[] data) {
		super(data);
	}
}
