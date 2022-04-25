package com.ezenit.isoleborromee.fragments;

import com.ezenit.isoleborromee.R;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FragmentTOS extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
	private TextView fieldTitle;
	private View	 btnLeft;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentTOS getInstance() {
		FragmentTOS fragment = new FragmentTOS();
		return fragment;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_terms_conditions, container,false);
		setCustomActionBar(inflater, (ViewGroup) root);
		return root;
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		resetActionBar();
	}
	// ===========================================================
	// Methods
	// ===========================================================

	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root) {
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root,false);
		actionBar.setCustomView(actionBarView);
		
		fieldTitle = (TextView) actionBarView.findViewById(R.id.fieldTitle);
		btnLeft = actionBarView.findViewById(R.id.btnLeft);
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		fieldTitle.setText(R.string.terms_of_service);
		
		btnLeft.setVisibility(View.VISIBLE);
		fieldTitle.setVisibility(View.VISIBLE);
		
	}
	
	private void resetActionBar(){
		fieldTitle.setVisibility(View.GONE);
		btnLeft.setVisibility(View.GONE);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
