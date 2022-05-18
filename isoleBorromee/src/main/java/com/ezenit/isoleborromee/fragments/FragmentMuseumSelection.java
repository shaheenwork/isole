package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.ezenit.isoleborromee.R;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

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
		View btnlink1 = root.findViewById(R.id.btnlink1);
		View btnlink2 = root.findViewById(R.id.btnlink2);
		View btnlink3 = root.findViewById(R.id.btnlink3);
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
		btnlink1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


			//	startActivity(new Intent(getActivity(),WebViewActivity.class));

				CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

				// below line is setting toolbar color
				// for our custom chrome tab.
				customIntent.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.app_primary_color));

				// we are calling below method after
				// setting our toolbar color.
				openCustomTab(getActivity(), customIntent.build(), Uri.parse("https://mailchi.mp/e5f32e967395/corrado-bonomi-newsletter"));



			}
		});
		btnlink2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


			//	startActivity(new Intent(getActivity(),WebViewActivity.class));

				CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

				// below line is setting toolbar color
				// for our custom chrome tab.
				customIntent.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.app_primary_color));

				// we are calling below method after
				// setting our toolbar color.
				openCustomTab(getActivity(), customIntent.build(), Uri.parse("https://mailchi.mp/b0f000c690b6/gianni-cella-newsletter"));



			}
		});

		btnlink3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {


				//	startActivity(new Intent(getActivity(),WebViewActivity.class));

				CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

				// below line is setting toolbar color
				// for our custom chrome tab.
				customIntent.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.app_primary_color));

				// we are calling below method after
				// setting our toolbar color.
				openCustomTab(getActivity(), customIntent.build(), Uri.parse("https://rk2brqwonvs.typeform.com/to/QWJLvCID"));



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

	public static void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri) {
		// package name is the default package
		// for our custom chrome tab
		String packageName = "com.android.chrome";
		if (packageName != null) {

			// we are checking if the package name is not null
			// if package name is not null then we are calling
			// that custom chrome tab with intent by passing its
			// package name.
			customTabsIntent.intent.setPackage(packageName);

			// in that custom tab intent we are passing
			// our url which we have to browse.
			customTabsIntent.launchUrl(activity, uri);
		} else {
			// if the custom tabs fails to load then we are simply
			// redirecting our user to users device default browser.
			activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
	}

}
