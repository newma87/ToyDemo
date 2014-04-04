package com.smarttoy.server.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;
import android.widget.Toast;

import com.smarttoy.tcp.TCPServer.CommunicateSession.PeersInfo;
import com.smarttoy.util.VideoFrame;
import com.smarttoy.global.Constraint;
import com.smarttoy.tcp.TCPServer;
import com.smarttoy.tcp.TCPServer.StateListener;
import com.smarttoy.udp.UDPSender;
import com.smarttoy.util.CameraManager;

public class VedioCapturer implements CameraManager.CameraReadyCallback, TCPServer.StateListener {
	static final int MAX_YUV_DATA_SIZE = 1024 * 1024 * 8;
	static final int MAX_FRAME_SIZE = 1024 * 1024 * 2;
	
	private Context m_context;
	private CameraManager m_camera;
	private UDPSender m_picSender;
	private VideoFrame m_curFrame = new VideoFrame(MAX_FRAME_SIZE);

	public interface VedioActivityInterface {
		public SurfaceView getCameraView();
	}
	
	public VedioCapturer(Context context, VedioActivityInterface inf) {
		m_context = context;
		m_camera = new CameraManager(inf.getCameraView());
		m_camera.setCameraReadyCallback(this);
	}
	
	public CameraManager getCamera() {
		return m_camera;
	}
	
	// camera
	private PreviewCallback m_previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] frame, Camera c) {
			if (m_picSender != null && m_picSender.isValidable()) {
				int w = m_camera.width();
				int h = m_camera.height();

				ByteBuffer bbuff = ByteBuffer.wrap(frame);
				byte[] data = new byte[MAX_YUV_DATA_SIZE];
				bbuff.get(data, 0, w * h + w * h / 2);

				YuvImage newImage = new YuvImage(data, ImageFormat.NV21, w, h,
						null);
				m_curFrame.reset();
				try {
					newImage.compressToJpeg(new Rect(0, 0, w, h), 30,
							m_curFrame);
				} catch (Exception e) {
					e.printStackTrace();
				}

				InputStream in = m_curFrame.getInputStream();
				m_picSender.send(in);

				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void onCameraReady() {
		m_camera.setPreviewCallBack(m_previewCb);
		//m_camera.autoFocus();
	}
	
	Handler m_handler = new Handler(Looper.getMainLooper()) {
		com.smarttoy.tcp.TCPServer.CommunicateSession.PeersInfo info = null;
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case StateListener.ON_SERVER_START:
				boolean bResult = (Boolean) (msg.obj);
				if (bResult) {
					Toast.makeText(m_context,
							"server has started", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(m_context,
							"server starte failed", Toast.LENGTH_SHORT).show();
				}
				
				break;
			case StateListener.ON_SERVER_CLOSE:
				Toast.makeText(m_context,
						"server has stopped", Toast.LENGTH_SHORT).show();
				break;
			case StateListener.ON_SESSION_START:
				if (m_picSender != null && m_picSender.isValidable()) {
					m_picSender.close();
					m_picSender = null;
				}
				
				info = (PeersInfo)msg.obj;
				m_picSender= new UDPSender(info.remote_ip, Constraint.VEDIO_PORT);
				m_camera.startPreview();
				Toast.makeText(m_context,
						"start session " + String.valueOf(info.session_id), Toast.LENGTH_SHORT)
						.show();
				break;
			case StateListener.ON_SESSION_END:
				m_camera.stopPreview();
				if (m_picSender != null && m_picSender.isValidable()) {
					m_picSender.close();
					m_picSender = null;
				}
				info = (PeersInfo)msg.obj;
				Toast.makeText(m_context,
						"end session " + info.session_id, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
	
	@Override
	public void handleState(int state, Object obj) {
		Message msg = m_handler.obtainMessage(state, obj);
		m_handler.sendMessage(msg);
	}
}
