package com.smarttoy.protocol;

public class StopMoveProtocol extends BaseProtocol {
	public StopMoveProtocol() {
		super(BaseProtocol.PT_ROBOT_STOP);
	}
	
	public StopMoveProtocol(byte[] data) {
		super(data);
	}
}
