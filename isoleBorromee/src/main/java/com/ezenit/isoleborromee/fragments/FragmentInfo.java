package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;

public class FragmentInfo extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private FragmentInfoListener fragmentInfoListener;
	private TextView			 fieldTitle;
	private View				 btnLeft;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentInfo getInstance() {
		FragmentInfo fragment = new FragmentInfo();
		return fragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try{
			fragmentInfoListener = (FragmentInfoListener) activity;
		}
		catch(ClassCastException e){
			throw new ClassCastException(activity.toString()
					+" must implemenet FragmentInfoListener");
		}
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root 	= inflater.inflate(R.layout.fr_info, container,false);
		
		View btnCredits = root.findViewById(R.id.btnCredits);
		View btnTOS 	= root.findViewById(R.id.btnTermsOfService);
		
		TextView fieldVersion = (TextView) root.findViewById(R.id.fieldVersion);
		
		fieldVersion.setText(MiscUtils.getVersion(getActivity()));
		
		btnCredits.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(fragmentInfoListener!=null)
					fragmentInfoListener.onCreditsClicked();
			}
		});
		
		btnTOS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(fragmentInfoListener!=null)
					fragmentInfoListener.onTermsAndConditionsClicked();
			}
		});
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
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	

	// ===========================================================
	// Methods
	// ===========================================================

	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root) {
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = inflater.inflate(R.layout.acbar_title_arrow, root,false);
		actionBar.setCustomView(actionBarView);

		Toolbar toolbar=(Toolbar)actionBarView.getParent();
		toolbar.setContentInsetsAbsolute(0,0);
		toolbar.setContentInsetsAbsolute(0, 0);
		toolbar.getContentInsetEnd();
		toolbar.setPadding(0, 0, 0, 0);


		fieldTitle = (TextView) actionBarView.findViewById(R.id.fieldTitle);
		btnLeft = actionBarView.findViewById(R.id.btnLeft);
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		fieldTitle.setText(R.string.infos);
		
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
	public interface FragmentInfoListener{
		public void onTermsAndConditionsClicked();
		public void onCreditsClicked();
	}
}
