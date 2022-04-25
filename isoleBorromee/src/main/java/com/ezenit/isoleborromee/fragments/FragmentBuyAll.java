package com.ezenit.isoleborromee.fragments;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentBuyAll extends DialogFragment{
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String ARG_MUSEUM = "com.ezenit.isoleborromee.fragments.FragmentBuyAll.ARG_MUSEUM";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private FragmentBuyAllListener listener;
	// ===========================================================
	// Constructors
	// ===========================================================

	public static FragmentBuyAll getInstance(Museum museum){
		FragmentBuyAll fragment = new FragmentBuyAll();
		Bundle bundle = new Bundle();
		bundle.putString(ARG_MUSEUM, museum.getShortName());
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		fragment.setArguments(bundle);
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
			listener = (FragmentBuyAllListener) activity;
		}
		catch(ClassCastException e){
			throw new ClassCastException(activity.toString()+" must implement FragmentBuyAllListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_buy_all, container,false);
		
		Museum museum = Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
		update(root, museum);
		return root;
	}
	
	public void update(View root,final Museum museum){
		Button btnBuyMusuem =  (Button) root.findViewById(R.id.btnBuyMuseum);
		Button btnBuyAll  	 = 	(Button) root.findViewById(R.id.btnBuyAll);
		
		btnBuyAll.setText(AppIsole.getPrice(getActivity(), Museum.MUSEUM_ALL_IN_ONE));
		btnBuyMusuem.setText(AppIsole.getPrice(getActivity(), museum));
		
			
		btnBuyMusuem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener!=null)
					listener.onBuyMuseumFromFragmentBuyAll(museum);
			}
		});
		
		btnBuyAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener!=null)
					listener.onBuyAllFromFragmentBuyAll();
			}
		});
		
		String strAudioGuide = getResources().getString(R.string.audio_guide);
		String strMuseum	 = getResources().getString(museum.getNameId());
		
		TextView fieldBuyMusuem = (TextView) root.findViewById(R.id.fieldBuyMuseum);
		fieldBuyMusuem.setText(strMuseum+" "+strAudioGuide);
	}
	
	@Override
	public Dialog getDialog() {
		// TODO Auto-generated method stub
		Dialog dialog =  super.getDialog();
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	public void update(Museum museum){
		if(getView()!=null){
			Bundle argument = getArguments();
			argument.putString(ARG_MUSEUM, museum.getShortName());
			update(getView(), museum);
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public interface FragmentBuyAllListener{
		public void onBuyMuseumFromFragmentBuyAll(Museum museum);
		public void onBuyAllFromFragmentBuyAll();
	}
}
