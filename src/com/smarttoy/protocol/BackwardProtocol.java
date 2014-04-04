package com.smarttoy.protocol;

public class BackwardProtocol  extends BaseProtocol {
	public BackwardProtocol() {
		super(BaseProtocol.PT_ROBOT_BACKWARD);
	}
	
	public BackwardProtocol(byte[] data) {
		super(data);
	}
}
