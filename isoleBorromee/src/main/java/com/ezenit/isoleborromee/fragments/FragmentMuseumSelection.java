package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.R;

public class FragmentMuseumSelection extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private OnMuseumSelectListener museumSelectListener;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static FragmentMuseumSelection getInstance() {
		FragmentMuseumSelection fragment = new FragmentMuseumSelection();
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
		try{
			this.museumSelectListener = (OnMuseumSelectListener) activity;
		}
		catch(ClassCastException e){
			throw new ClassCastException(activity.toString()
					+" must implement OnMusuemSelectListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_museum_selection, null);
		setUpMuseumButtons(root);
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
	
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		((AppCompatActivity)getActivity()).getSupportActionBar().hide();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(museumSelectListener!=null)
			museumSelectListener.onMuseumFragmentStart();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		((AppCompatActivity)getActivity()).getSupportActionBar().show();
		if(museumSelectListener!=null)
			museumSelectListener.onMuseumFragmentStop();
	}
	
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
	}
	// ===========================================================
	// Methods
	// ===========================================================

	private void setUpMuseumButtons(View root) {
		View btnIsolaBella = root.findViewById(R.id.btnIsolaBella);
		View btnIsolaMadre = root.findViewById(R.id.btnIsolaMadre);
		View btnRoccaAngera = root.findViewById(R.id.btnRoccaDAngera);
		View btnSettings	= root.findViewById(R.id.btnSettings);
		
		btnIsolaBella.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(museumSelectListener!=null)
					museumSelectListener.OnMuseumSelected(Museum.MUSEUM_ISOLA_BELLA);
			}
		});
		
		btnIsolaMadre.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(museumSelectListener!=null)
					museumSelectListener.OnMuseumSelected(Museum.MUSEUM_ISOLA_MADRE);
			}
		});
		
		btnRoccaAngera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(museumSelectListener!=null)
					museumSelectListener.OnMuseumSelected(Museum.MUSEUM_ROCA_D_ANGERA);
			}
		});
		
		btnSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(museumSelectListener!=null)
					museumSelectListener.OnSettingClicked();
			}
		});
		
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public interface OnMuseumSelectListener{
		public void OnMuseumSelected(Museum museum);
		public void OnSettingClicked();
		public void onMuseumFragmentStart();
		public void onMuseumFragmentStop();
	}


}
