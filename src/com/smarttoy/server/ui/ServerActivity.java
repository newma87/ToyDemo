package com.smarttoy.server.ui;

import java.util.ArrayList;
import java.util.List;

import com.smarttoy.R;
import com.smarttoy.global.Constraint;
import com.smarttoy.global.SmartToyApplication;
import com.smarttoy.server.ui.ToyService.ToyBind;
import com.smarttoy.server.ui.VedioCapturer.VedioActivityInterface;
import com.smarttoy.tcp.MessageDispatcher;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ServerActivity extends FragmentActivity implements VedioActivityInterface {
	public static final int MODE_NORMAL = 1;
	public static final int MODE_GAME = 2;
	public static final int MODE_EDUCATION = 3;
	
	private CharactorFragment m_face = null;
	private GameFragment m_game = null;
	private EducationFragment m_edu = null;
	private ModeFragmentInterface m_curModeFragment = null;
	
	private MessageDispatcher m_dispatcher = null;
	private VedioCapturer m_vedio = null;

	private LinearLayout m_modeMenu = null;
	private List<View> 	 m_modeItemList = new ArrayList<View>();
	
	private ToyBind m_binder = null;
	
	public interface ModeFragmentInterface {
		public boolean onTouch(MotionEvent ev);
		public int getModeType();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.activity_server);

		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);		// full screen
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	// no screen lock

		m_face = new CharactorFragment();
		m_game = new GameFragment();
		m_edu = new EducationFragment();
		
		m_dispatcher = new MessageDispatcher(this);
		m_vedio = new VedioCapturer(this.getBaseContext(), this);
		
		initModeList();
		
		setMode(Constraint.NORMAL_MODE);
	}
	
	public VedioCapturer getVedioCapture() {
		return m_vedio;
	}
	
	@Override
	public SurfaceView getCameraView() {
		return (SurfaceView)findViewById(R.id.sfv_camera);
	}
	
	private void initModeList() {
		View v = null;
		int clr = android.graphics.Color.parseColor("#0069ae");
		// normal view
		v = findViewById(R.id.layout_normal_mode_item);
		v.setBackgroundColor(clr);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View vw) {
				switchMode(Constraint.NORMAL_MODE);
				m_modeMenu.setVisibility(View.GONE);
			}
		});
		m_modeItemList.add(v);
		// game view
		v = findViewById(R.id.layout_game_mode_item);
		v.setBackgroundColor(clr);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View vw) {
				switchMode(Constraint.GAME_MODE);
				m_modeMenu.setVisibility(View.GONE);
			}
		});
		m_modeItemList.add(v);
		// education view
		v = findViewById(R.id.layout_edu_mode_item);
		v.setBackgroundColor(clr);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View vw) {
				switchMode(Constraint.EDUCATION_MODE);
				m_modeMenu.setVisibility(View.GONE);
			}
		});
		m_modeItemList.add(v);
		
		m_modeMenu = (LinearLayout)findViewById(R.id.layout_mode);
		m_modeMenu.setVisibility(View.GONE);
	}
	
	public void setMode(int type) {
		View v = null;
		
		SmartToyApplication app = (SmartToyApplication)this.getApplication();
		app.setPlayMode(type);
		
		switch(type) {
		case Constraint.NORMAL_MODE:
			getSupportFragmentManager().beginTransaction().replace(R.id.layout_main, m_face, "face fragment").commit();		
			m_curModeFragment = (ModeFragmentInterface)m_face;
			v = findViewById(R.id.layout_normal_mode_item);
			v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			break;
		case Constraint.GAME_MODE:
			getSupportFragmentManager().beginTransaction().replace(R.id.layout_main, m_game, "game fragment").commit();		
			m_curModeFragment = (ModeFragmentInterface)m_game;
			v = findViewById(R.id.layout_game_mode_item);
			v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			break;
		case Constraint.EDUCATION_MODE:
			getSupportFragmentManager().beginTransaction().replace(R.id.layout_main, m_edu, "education fragment").commit();		
			m_curModeFragment = (ModeFragmentInterface)m_edu;
			v = findViewById(R.id.layout_edu_mode_item);
			v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			break;
		}		
	}
	
	public void switchMode(int type) {
		int curType = m_curModeFragment.getModeType();
		if (type == curType) {
			return;
		}
		
		View v = null;
		int clr = android.graphics.Color.parseColor("#0069ae");
		switch (curType) {
		case Constraint.NORMAL_MODE:
			v = findViewById(R.id.layout_normal_mode_item);
			v.setBackgroundColor(clr);			
			break;
		case Constraint.GAME_MODE:
			v = findViewById(R.id.layout_game_mode_item);
			v.setBackgroundColor(clr);
			break;
		case Constraint.EDUCATION_MODE:
			v = findViewById(R.id.layout_edu_mode_item);
			v.setBackgroundColor(clr);
			break;
		}
		
		setMode(type);
	}

	private int m_touchY = -1;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int touchY = (int)event.getY();
			if (touchY < Constraint.MENU_SLIDE_HEIGHT) {
				m_touchY = touchY;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (m_touchY >= 0 && m_touchY < (int)event.getY()) {
				m_modeMenu.setVisibility(View.VISIBLE);
			} else {
				m_modeMenu.setVisibility(View.GONE);
			}
			m_touchY = -1;
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (m_modeMenu.getVisibility() == View.GONE && m_curModeFragment != null) {
			if (! m_curModeFragment.onTouch(ev)) {
				return true;	// stop calling onTouchEvent
			}
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	protected void onStart() {
		Intent intent = new Intent(this, ToyService.class);  ;
		startService(intent);
		super.onStart();
	}

	@Override
	protected void onStop() {
		Intent intent = new Intent(this, ToyService.class);  ;
		stopService(intent);
		super.onStop();
	}

    private ServiceConnection servCon = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			m_binder = (ToyService.ToyBind)binder;
			m_binder.startServer(m_dispatcher, m_vedio, null);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			m_binder = null;
		}  
    }; 
    
	@Override
	protected void onResume() {
		Intent bindIntent = new Intent(this, ToyService.class); 
		bindService(bindIntent, servCon, BIND_AUTO_CREATE);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		m_binder.stopServer();
		unbindService(servCon);
		super.onPause();
	}
}
