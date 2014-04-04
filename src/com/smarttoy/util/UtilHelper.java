package com.smarttoy.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.graphics.Point;

import com.smarttoy.protocol.BaseProtocol;

public class UtilHelper {
	static final int MAX_BUFFER_LENGTH = 1024;
	static final int INT_SIZE = 4;
	
	// return value includes a length header
	static public byte[] readNetInstructionData(InputStream in) throws IOException, EOFException {
		byte[] temp = new byte[MAX_BUFFER_LENGTH];
		byte[] ret = null;
		int length = 0;
		
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		length = in.read(temp, 0, INT_SIZE); // begin receive, read the 4 length header
		if (length == -1) {
			throw(new EOFException("Had reached the end of file!"));
		}
		if (length != INT_SIZE) {
			throw (new IOException("Read length header error, no header!"));
		}
		
		writer.write(temp, 0, length);
		int left_length = BaseProtocol.byteToInt(temp); // get data total length

		if (left_length == 0) {
			return null;
		}

		while (left_length > 0) {
			int temp_length = left_length;
			if (temp_length > temp.length) {
				temp_length = temp.length;
			}

			int nRead = in.read(temp, 0, temp_length);
			if (nRead == -1) {
				throw (new IOException(
						"Read length header error, wrong header length!"));
			}

			writer.write(temp, 0, nRead);
			left_length = left_length - nRead;
		}

		ret = writer.toByteArray();

		writer.flush();
		writer.close();
		
		return ret;
	}
	
	// data set in without length header
	static public void writeNetInstruction(OutputStream out, byte[] data, int count) throws IOException {
		out.write(BaseProtocol.intToByte(count));
		out.write(data, 0, count);
		out.flush();
	}
	
	// data set in without length header
	static public void writeNetInstruction(OutputStream out, byte[] data) throws IOException {
		writeNetInstruction(out, data, data.length);
	}
	
	// get ip address
    public static String getIPv4Address() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (isIPv4) 
                            return sAddr;
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "0.0.0.0";
    }
    
    public static int getLength(int x1, int y1, int x2, int y2) {  
    	int i = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
       	return (int)Math.sqrt(i);
    }
    
    public static float getAngle(Point ptStart, Point ptEnd) {
    	float angle = (float) Math.toDegrees(Math.atan2(ptEnd.x - ptStart.x, ptEnd.y - ptStart.y));
    	if (angle < 0) {
    		angle += 360;
    	}
    	return angle;
    }
    
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
}
