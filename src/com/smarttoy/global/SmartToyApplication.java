package com.smarttoy.global;

import android.app.Application;

public class SmartToyApplication extends Application {
	
	private int m_curMode;

	public SmartToyApplication() {
		super();
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	public int getPlayMode() {
		return m_curMode;
	}
	
	public void setPlayMode(int mode) {
		m_curMode = mode;
	}
}
