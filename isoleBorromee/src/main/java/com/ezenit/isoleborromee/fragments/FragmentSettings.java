package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ezenit.isoleborromee.R;

public class FragmentSettings extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private OnSettingsClickListener settingsClickListener;
	private TextView fieldTitle;
	private View	 btnLeft;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentSettings getInstance() {
		FragmentSettings fragment = new FragmentSettings();
		return fragment;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			settingsClickListener = (OnSettingsClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSettingsClickListener");
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root 				= inflater.inflate(R.layout.fr_settings, container,false);
		View btnLanguage 		= root.findViewById(R.id.btnLanguage);
		View btnRestorePurchase = root.findViewById(R.id.btnRestorePurchase);
		View btnInfos			= root.findViewById(R.id.btnInfos);
		
		btnLanguage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(settingsClickListener!=null)
					settingsClickListener.onLanguageClicked();
			}
		});
		
		btnRestorePurchase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(settingsClickListener!=null)
					settingsClickListener.onRestorePurchaseClicked();
			}
		});
		
		btnInfos.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(settingsClickListener!=null)
					settingsClickListener.onInfoClicked();
				
			}
		});
		setCustomActionBar(inflater, (ViewGroup)root);
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
				settingsClickListener.onSettingsBackPressed();
			}
		});
		fieldTitle.setText(R.string.settings);
		
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
	
	public interface OnSettingsClickListener{
		public void onLanguageClicked();
		public void onRestorePurchaseClicked();
		public void onInfoClicked();
		public void onSettingsBackPressed();
	}
}
