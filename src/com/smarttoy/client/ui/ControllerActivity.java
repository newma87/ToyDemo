package com.smarttoy.client.ui;

import java.net.DatagramPacket;

import com.smarttoy.protocol.BackwardProtocol;
import com.smarttoy.protocol.BaseProtocol;
import com.smarttoy.protocol.ForwardProtocol;
import com.smarttoy.protocol.StopMoveProtocol;
import com.smarttoy.protocol.TurnLeftProtocol;
import com.smarttoy.protocol.TurnRightProtocol;
import com.smarttoy.global.Constraint;
import com.smarttoy.global.StartupActivity;
import com.smarttoy.tcp.TCPClient;
import com.smarttoy.tcp.TCPClient.StateListener;
import com.smarttoy.udp.UDPReceiver;
import com.smarttoy.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class ControllerActivity extends Activity implements VirtualJoyStick.SingleRudderListener, 
															UDPReceiver.OnReceiveCallBack, 
															TCPClient.OnReceiveDataCallBack, 
															StateListener {
	private static final String LOG_TAG = "Controller Info";
	
	VirtualJoyStick m_joyStick = null;
	ImageView m_vedioView = null;
	
	private String m_serverIp = "192.168.1.139";
	private TCPClient m_client = null;
	private UDPReceiver m_imgServer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		m_serverIp = intent.getStringExtra(StartupActivity.IP_ADDRESS_MESSAGE);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.activity_controller);

		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);		// full screen
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	// no screen lock
		
		m_joyStick = (VirtualJoyStick)findViewById(R.id.virtual_joy_stick);
		m_joyStick.setSingleRudderListener(this);
		
		ButtonTouchListener btnListener = new ButtonTouchListener();
		Button btn = null;
		btn = (Button)findViewById(R.id.btn_speak);
		btn.setOnTouchListener(btnListener);
		btn = (Button)findViewById(R.id.btn_music);
		btn.setOnTouchListener(btnListener);
		btn = (Button)findViewById(R.id.btn_rec);
		btn.setOnTouchListener(btnListener);
		btn = (Button)findViewById(R.id.btn_dance);
		btn.setOnTouchListener(btnListener);
		
		m_vedioView = (ImageView)findViewById(R.id.img_vedio);
		m_vedioView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		m_vedioView.setOnTouchListener(new ImageViewTouchListener());
		
		m_client = new TCPClient();
		m_client.setStateListener(this);
		m_client.setReceiveCallBack(this);
		m_imgServer = new UDPReceiver(Constraint.VEDIO_PORT);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void sendProtocol(BaseProtocol pro) {
		if (pro == null) {
			return;
		}

		m_client.sendData(pro.getData());
	}
	
	int trigTime = 0;
	BaseProtocol lastProtocol = null;
	@Override
	public void onSteeringWheelChanged(int action, int angle) {
		if (action == VirtualJoyStick.ACTION_RUDDER) {
			BaseProtocol pro = null;
			if (angle >= 45 && angle < 135) {
					pro = new TurnRightProtocol();
			} else if (angle >= 135 && angle < 225) {
				pro = new ForwardProtocol();
			} else if (angle >= 225 && angle < 315) {
				pro = new TurnLeftProtocol();
			} else {
				pro = new BackwardProtocol();
			}
			
			if (lastProtocol == pro && (trigTime % 10) != 0) {
				trigTime++;
			} else {
				lastProtocol = pro;
				sendProtocol(pro);
				trigTime = 1;
			}
		} else if (action == VirtualJoyStick.ACTION_STOP) {
			trigTime = 0;
			sendProtocol(new StopMoveProtocol());
			Log.w(LOG_TAG, "action stop --------------------");
		} else {
			Log.w(LOG_TAG, "action start --------------------");
		}
	}

	class ButtonTouchListener implements OnTouchListener {		
		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			Button btn = (Button)v;
			switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				btn.setBackgroundResource(R.drawable.btn_pressed);
				break;
			case MotionEvent.ACTION_UP:
				btn.setBackgroundResource(R.drawable.btn_default);
				break;
			}
		
			return false;
		}
	}
	
	class ImageViewTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			return false;
		}
	}

	@Override
	protected void onPause() {
		m_client.disconnect();
		super.onPause();
	}

	@Override
	protected void onResume() {
		m_client.connectServer(m_serverIp, Constraint.COMMAND_PORT);
		super.onResume();
	}
	
	@Override
	public void handleState(int state, Object obj) {
		if (state == StateListener.ON_CONNECTE_TO_SERVER) {
			if ((Boolean)obj) {
				hand.sendEmptyMessage(ON_CONNECTED);
			} else {
				hand.sendEmptyMessage(ON_CONNECTE_FAILED);
			}
		} else if (state == StateListener.ON_SERVER_SIDE_CLOSED) {
			hand.sendEmptyMessage(ON_SERVER_SIDE_CLOSE);
		} else if (state == StateListener.ON_CONNECTION_CLOSE) {
			hand.sendEmptyMessage(ON_DISCONNECTED);
		}
	}

	@Override
	public void onReceive(TCPClient client, byte[] data) {
		//TODO:
	}

	@Override
	public void onReceive(DatagramPacket packet, byte[] data) {
		Message msg = hand.obtainMessage(ON_RECEIVE_IMG, data);
		hand.sendMessage(msg);
	}
	
	static final int ON_CONNECTED = 1;
	static final int ON_CONNECTE_FAILED = 2;
	static final int ON_RECEIVE_DATA = 3;
	static final int ON_DISCONNECTED = 4;
	static final int ON_RECEIVE_IMG = 5;
	static final int ON_SERVER_SIDE_CLOSE = 6;

	Handler hand = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ON_CONNECTED:
				m_imgServer.start();
				m_imgServer.setCallback(ControllerActivity.this);
				Toast.makeText(ControllerActivity.this.getBaseContext(),
						"server connected", Toast.LENGTH_SHORT).show();
				break;
			case ON_CONNECTE_FAILED:
				Toast.makeText(ControllerActivity.this.getBaseContext(),
						"server connecte failed", Toast.LENGTH_SHORT).show();
				break;
			case ON_DISCONNECTED:
				m_imgServer.stop();
				Toast.makeText(ControllerActivity.this.getBaseContext(),
						"server disconnected", Toast.LENGTH_SHORT).show();
				break;
			case ON_SERVER_SIDE_CLOSE:
				m_client.disconnect();
				Toast.makeText(ControllerActivity.this.getBaseContext(),
						"server side closed", Toast.LENGTH_SHORT).show();
				break;
			case ON_RECEIVE_IMG:
				byte[] img_data = (byte[]) (msg.obj);
				Bitmap bitmap = BitmapFactory.decodeByteArray(img_data, 0,
						img_data.length);
				m_vedioView.setImageBitmap(bitmap);
				m_vedioView.setScaleType(ScaleType.CENTER_CROP); // required

				break;
			}

			super.handleMessage(msg);
		}

	};
}
