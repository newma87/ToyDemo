package com.smarttoy.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Log;

public class BaseProtocol {
	private static final String LOG_TAG = "Base protocol";
	private static final int INT_SIZE = Integer.SIZE / 8;
	
	// network instruction
	public static final int PT_NONE = 0;
	public static final int PT_ROBOT_FORWARD = 1;
	public static final int PT_ROBOT_BACKWARD = 2;
	public static final int PT_ROBOT_TURN_LEFT = 3;
	public static final int PT_ROBOT_TURN_RIGHT = 4;
	public static final int PT_ROBOT_STOP = 5;
	public static final int PT_REQUEST_VEDIO = 6;	// c 要求 s 传送视频
	// this  is for test
	public static final int PT_SIMPLE_TEXT = 0xff00;
	
	protected int m_type = PT_NONE;

	// static method
	public static byte[] intToByte(int i) {
		byte[] ret = new byte[4];
		
		ret[0] = (byte) (i >> 24);
		ret[1] = (byte) (i >> 16 );
		ret[2] = (byte) (i >> 8);
		ret[3] = (byte) (i);
		
		return ret;
	}
	// static method
	public static int byteToInt(byte[] b) {
		int num[] = new int[4]; 
		
		int ret = 0;
		
		for (int i = 0; i < 4; i++)
		{
			num[i] = (int)(b[i]);
			if (num[i] < 0)
			{
				num[i] = 256 + num[i];	//为负数时，加上２５６
			}
			
			ret = num[i] << ((3 - i) * 8);
		}

		return ret;
	}
	
	public BaseProtocol() {
		
	}
	
	public BaseProtocol(int type) {
		m_type = type;
	}
	
	public BaseProtocol(byte[] data) {
		setData(data);
	}
	
	public int getType() {
		return m_type;
	}
	
	public void setType(int type) {
		m_type = type;
	}
	
	public byte[] getData() {
		byte[] ret = null;
		byte[] typeData = intToByte(m_type);
		byte[] extraData = getContentData();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		int length = typeData.length;
		if (extraData != null) {
			length += extraData.length;
		}
		try {
			out.write(intToByte(length));
			out.write(typeData);
			if (extraData != null) {
				out.write(extraData);
			}
			out.flush();
			ret = out.toByteArray();
			out.close();
		}catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		
		return ret;
	}
	
	public void setData(byte[] data) {
		int length = byteToInt(data);

		byte[] type_data = new byte[INT_SIZE];		
		System.arraycopy(data, INT_SIZE, type_data, 0, INT_SIZE);	
		m_type = byteToInt(type_data);
		
		int content_length = length - INT_SIZE;
		byte[] content_data = new byte[content_length];
		System.arraycopy(data, INT_SIZE * 2, content_data, 0, content_length);
		setContentData(content_data);
	}
	
	// need to be override
	protected byte[] getContentData() {
		return null;
	}
	
	// need to be override
	protected void setContentData(byte[] data) {
		
	}
}
