package com.ezenit.isoleborromee;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ezenit.isoleborromee.adapter.AdapterLanguage;
import com.ezenit.utils.LanguageConstants;
import com.ezenit.utils.MiscUtils;

public class ActivityLanguageSelection extends AppCompatActivity {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG_FULLSCREEN = ActivityLanguageSelection.class.getName()+"_TAG_FULLSCREEN";
	private static final String TAG_SELECTION  = ActivityLanguageSelection.class.getName()+"_TAG_SELECTION";
	
	private static final float  PER_HEIGHT_LS  = 0.915144f;
	
	//Aspect WIDTH/HEIGHT
	private static final float  PER_ASPECT_LS  = 0.873038f;
	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean			fullScreen;
	private AdapterLanguage adapterLanguage;
	
	private TextView		lblSelectLanguage;

	private TextView		lblWelcome;
	private TextView		lblLanguageDesc;
	private TextView		btnFine;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	// ===========================================================
	// Initializer
	// ===========================================================
	
	public static void startActivity(Activity activity,boolean fullscreen){
		Intent intent = new Intent(activity, ActivityLanguageSelection.class);
		intent.putExtra(TAG_FULLSCREEN, fullscreen);
		activity.startActivity(intent);
	}
	
	public static void startActivity(Activity activity,int requestCode,boolean fullscreen){
		Intent intent = new Intent(activity, ActivityLanguageSelection.class);
		intent.putExtra(TAG_FULLSCREEN, fullscreen);
		activity.startActivityForResult(intent, requestCode);
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		init(savedInstanceState);
		if(!fullScreen){
			setTheme(R.style.PopTheme_Settings);
		}
		super.onCreate(savedInstanceState);
		if(!fullScreen){
			DisplayMetrics outMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
			int width = (int) (outMetrics.heightPixels*PER_HEIGHT_LS);
			int height  = (int) (width * PER_ASPECT_LS);
			
			if(width>outMetrics.widthPixels)
				width = outMetrics.widthPixels;
			if(height>outMetrics.heightPixels)
				height = outMetrics.heightPixels;
			
			MiscUtils.showAsPopup(this, R.layout.activity_language_selection, width, height);
		}
		else
			setContentView(R.layout.activity_language_selection);
		
		lblWelcome		= (TextView) findViewById(R.id.lblWelcome);
		lblLanguageDesc = (TextView) findViewById(R.id.lblLanguageDesc);
		setCustomActionBar();	
		initLanguageList(savedInstanceState);
		
		
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		super.onBackPressed();
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putBoolean(TAG_FULLSCREEN, fullScreen);
		outState.putInt(TAG_SELECTION, adapterLanguage.getSelection());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_fine:
				
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
		
	}

	private void selectLanguage(){
		String selectedLang = adapterLanguage.getSelectedItem();
		if(selectedLang!=null){
			String shortLang = LanguageConstants.convertToShort(selectedLang);
			AppIsole.setAppLocale(shortLang);
			AppIsole.setLanguageSelected(true);
		}
		
		setResult(RESULT_OK);
		finish();
	}
	// ===========================================================
	// Methods
	// ===========================================================
	private void initLanguageList(Bundle savedStateInstance) {
		ListView list = (ListView) findViewById(R.id.listLanguages);
		adapterLanguage = new AdapterLanguage(this);
		
		
		list.setAdapter(adapterLanguage);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String selectedLanguage = adapterLanguage.getItem(position);
				adapterLanguage.setSelection(position);
				MiscUtils.changeLanguage(ActivityLanguageSelection.this, LanguageConstants.convertToShort(selectedLanguage));
			}
		});
		
		if(savedStateInstance!=null){
			adapterLanguage.setSelection(savedStateInstance.getInt(TAG_SELECTION));
		}
		else{
			String language = LanguageConstants.getAppLanguage(AppIsole.getAppLocaleAsStr());
			adapterLanguage.setSelection(language);
		}
		
	}
	
	public void refreshLanguage(){
		lblSelectLanguage.setText(getResources().getString(R.string.select_language));
		lblWelcome.setText(getResources().getString(R.string.welcome));
		lblLanguageDesc.setText(getResources().getString(R.string.select_fav_language));
		btnFine.setText(getResources().getString(R.string.end));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	Log.d("Log", "Config is "+newConfig.locale.getLanguage());
		refreshLanguage();
	
		
	}
	
	
	private void init(Bundle savedInstanceState){
		if(savedInstanceState==null){
			fullScreen = getIntent().getBooleanExtra(TAG_FULLSCREEN, false);
			
		}
		else{
			fullScreen = savedInstanceState.getBoolean(TAG_FULLSCREEN);
		}
	}
	
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = getLayoutInflater().inflate(R.layout.acbar_lang_selection, null);
		actionBar.setCustomView(actionBarView);

		Toolbar toolbar=(Toolbar)actionBarView.getParent();
		toolbar.setContentInsetsAbsolute(0,0);
		toolbar.setContentInsetsAbsolute(0, 0);
		toolbar.getContentInsetEnd();
		toolbar.setPadding(0, 0, 0, 0);



		lblSelectLanguage = (TextView) actionBarView.findViewById(R.id.lblSelectLanguage);
		btnFine 		  = (TextView) actionBarView.findViewById(R.id.btnFine);
		btnFine.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectLanguage();
			}
		});
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	// ===========================================================
	// Interfaces
	// ===========================================================
	
}
