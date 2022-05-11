package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.adapter.AdapterLanguage;
import com.ezenit.utils.LanguageConstants;
import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;

public class FragmentLanguageSelection extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private AdapterLanguage adapter;
	private ImageView btnLeft;
	private TextView fieldTitle;
	
	private OnLanguageSelectListener languageSelectListener;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentLanguageSelection getInstance(){
		FragmentLanguageSelection fragment = new FragmentLanguageSelection();
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
		if(activity instanceof OnLanguageSelectListener)
			languageSelectListener = (OnLanguageSelectListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_language_selection, null);
		
		adapter = new AdapterLanguage(getActivity(),true);
		ListView listLanguages = (ListView) root.findViewById(R.id.listLanguages);
		listLanguages.setAdapter(adapter);
		listLanguages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				adapter.setSelection(position);
				String language = adapter.getSelectedItem();
				String shortLanguage = LanguageConstants.convertToShort(language);
				MiscUtils.changeLanguage(getActivity(), shortLanguage);
				
			}
		});
		String language = LanguageConstants.getAppLanguage(AppIsole.getAppLocaleAsStr());
		adapter.setSelection(language);
		setCustomActionBar(inflater, (ViewGroup)root);
		return root;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
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
	
	
	private void setLanguage(){
		String selectedLang = adapter.getSelectedItem();
		if(selectedLang!=null){
			String shortLang = LanguageConstants.convertToShort(selectedLang);
			AppIsole.setAppLocale(shortLang);
			AppIsole.setLanguageSelected(true);
			MiscUtils.changeLanguage(getActivity());
			if(languageSelectListener!=null)
				languageSelectListener.onLanguageSelected(shortLang);
		}
		getFragmentManager().popBackStack();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		fieldTitle.setText(getActivity().getResources().getString(R.string.select_language));
	}
	
	
	
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
		btnLeft = (ImageView) actionBarView.findViewById(R.id.btnLeft);
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setLanguage();
			}
		});
		fieldTitle.setText(R.string.select_language);
		
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
	
	public interface OnLanguageSelectListener{
		public void onLanguageSelected(String language);
	}
}
