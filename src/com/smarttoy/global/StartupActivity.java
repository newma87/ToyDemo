package com.smarttoy.global;

import com.smarttoy.R;
import com.smarttoy.client.ui.ControllerActivity;
import com.smarttoy.client.ui.DiscoverDeviceFragment;
import com.smarttoy.server.ui.ServerActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartupActivity extends FragmentActivity implements OnClickListener, DiscoverDeviceFragment.CallBack {
	public static final String IP_ADDRESS_MESSAGE = "ip address"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		Button btn = (Button)findViewById(R.id.btn_controller);
		btn.setOnClickListener(this);
		btn = (Button)findViewById(R.id.btn_server);
		btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_controller:
			findViewById(R.id.layout_main_overlay).setVisibility(View.GONE);
			DiscoverDeviceFragment frag = new DiscoverDeviceFragment();
			frag.setConnectCallBack(this);
			getSupportFragmentManager().beginTransaction().add(R.id.layout_main, frag, "discover fragment").commit();		
			break;
		case R.id.btn_server:
			Intent intent = new Intent(this, ServerActivity.class);
			startActivity(intent);
			finish();
			break;
		}		
	}

	@Override
	public void onConnectToDevice(String ip) {
		Intent intent = new Intent(this, ControllerActivity.class);
	    intent.putExtra(IP_ADDRESS_MESSAGE, ip);
	    startActivity(intent);
		finish();
	}

}
