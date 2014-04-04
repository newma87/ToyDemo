package com.smarttoy.server.ui;

import com.smarttoy.R;
import com.smarttoy.global.Constraint;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CharactorFragment extends Fragment implements ServerActivity.ModeFragmentInterface {
	static final int SMILE = 1;
	static final int ANGRY = 2;
	static final int LAUGH = 3;
	static final int BORING = 4;
	static final int FIGHT = 5;
	static final int SHY = 6;
	static final int STARE = 7;
	static final int STUPEFIED = 8;
	static final int SURPRISE = 9;
	static final int WHY = 11;
	static final int FACE_COUNT = 11;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_face, container, false);
		//ImageView imag_face = (ImageView)v.findViewById(R.id.img_face);
		//imag_face.setOnClickListener(this);
		return v;
	}
	
	public void playEmotion(int status) {
		ImageView face = (ImageView)getActivity().findViewById(R.id.img_face);
		switch (status) {
		case SMILE:
			face.setImageResource(R.drawable.face_smile);
			break;
		case ANGRY:
			face.setImageResource(R.drawable.face_angry);
			break;
		case LAUGH:
			face.setImageResource(R.drawable.face_laugh);
			break;
		case BORING:
			face.setImageResource(R.drawable.face_boring);
			break;
		case FIGHT:
			face.setImageResource(R.drawable.face_fight);
			break;
		case SHY:
			face.setImageResource(R.drawable.face_shy);
			break;
		case STARE:
			face.setImageResource(R.drawable.face_stare);
			break;
		case STUPEFIED:
			face.setImageResource(R.drawable.face_stupefied);
			break;
		case SURPRISE:
			face.setImageResource(R.drawable.face_surprise);
			break;
		case WHY:
			face.setImageResource(R.drawable.face_why);
			break;
		default:
			break;
		}
	}
	
	public void randomPlayEmotion() {
		int status = (int)(Math.random() * ((FACE_COUNT) + 1));
		playEmotion(status);
		Log.d("test ", "random play emotion========================");
	}

	int m_touchY = -1;
	@Override
	public boolean onTouch(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int touchY = (int)event.getY();
			if (touchY < Constraint.MENU_SLIDE_HEIGHT) {
				m_touchY = touchY;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (m_touchY < 0 || m_touchY >= (int)event.getY()) {
				randomPlayEmotion();
			}
			m_touchY = -1;
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public int getModeType() {
		// TODO Auto-generated method stub
		return Constraint.NORMAL_MODE;
	}
	
	
}
