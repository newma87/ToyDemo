package com.smarttoy.protocol;

public class ForwardProtocol  extends BaseProtocol {
	public ForwardProtocol() {
		super(BaseProtocol.PT_ROBOT_FORWARD);
	}
	
	public ForwardProtocol(byte[] data) {
		super(data);
	}
}
