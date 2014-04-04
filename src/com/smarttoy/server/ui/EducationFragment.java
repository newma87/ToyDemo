package com.smarttoy.server.ui;

import com.smarttoy.R;
import com.smarttoy.global.Constraint;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class EducationFragment extends Fragment implements ServerActivity.ModeFragmentInterface  {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragmet_education, container, false);
		return v;
	}
	
	@Override
	public boolean onTouch(MotionEvent ev) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getModeType() {
		// TODO Auto-generated method stub
		return Constraint.EDUCATION_MODE;
	}

}
