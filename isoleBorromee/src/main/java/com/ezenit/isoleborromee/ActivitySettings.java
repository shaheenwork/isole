package com.ezenit.isoleborromee;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue.DownloadQueue;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.fragments.FragmentAlert;
import com.ezenit.isoleborromee.fragments.FragmentCredits;
import com.ezenit.isoleborromee.fragments.FragmentDownloadViewer;
import com.ezenit.isoleborromee.fragments.FragmentInfo;
import com.ezenit.isoleborromee.fragments.FragmentLanguageSelection;
import com.ezenit.isoleborromee.fragments.FragmentSettings;
import com.ezenit.isoleborromee.fragments.FragmentTOS;
import com.ezenit.utils.MiscUtils;
import com.ezenit.utils.iab.IabHelper;
import com.ezenit.utils.iab.IabResult;
import com.ezenit.utils.iab.Inventory;
import com.ezenit.utils.iab.Purchase;
import com.ezenit.utils.iab.SkuDetails;
import com.ezenit.utils.iab.IabHelper.OnIabSetupFinishedListener;
import com.ezenit.utils.iab.IabHelper.QueryInventoryFinishedListener;


/****
 * Handles and sets the application preferences
 * *******/
public class ActivitySettings extends AppCompatActivity
			implements FragmentSettings.OnSettingsClickListener,
			FragmentInfo.FragmentInfoListener,
			FragmentLanguageSelection.OnLanguageSelectListener,
			FragmentAlert.OnDialogClickListener{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG_FRAGMENT_SETTINGS 	= "com.ezenit.isoleborromee.ActivitySettings.TAG_FRAGMENT_SETTINGS";
	private static final String TAG_FRAGMENT_LANGUAGE 	= "com.ezenit.isoleborromee.ActivitySettings.TAG_FRAGMENT_LANGUAGE";
	private static final String TAG_FRAGMENT_INFO 	  	= "com.ezenit.isoleborromee.ActivitySettings.TAG_FRAGMENT_INFO";
	private static final String TAG_FRAGMENT_TOS 	  	= "com.ezenit.isoleborromee.ActivitySettings.TAG_FRAGMENT_LANGUAGE";
	private static final String TAG_FRAGMENT_CREDITS  	= "com.ezenit.isoleborromee.ActivitySettings.TAG_FRAGMENT_CREDITS";
	
	private static final String TAG_DOWNLOAD_PROGRESS	= "com.ezenit.isoleborromee.ActivitySettings.TAG_DOWNLOAD_PROGRESS";
	private static final String TAG_DOWNLOAD_ALL 		= "com.ezenit.isoleborromee.ActivitySettings.TAG_DOWNLOAD_ALL";
	
	private static final String TAG_FULLSCREEN = ActivitySettings.class.getName()+"_TAG_FULLSCREEN";
	
	private static final float  PER_HEIGHT_LS  = 0.915144f;
	
	//Aspect WIDTH/HEIGHT
	private static final float  PER_ASPECT_LS  = 0.873038f;
	//FragmentAlert fragmentDownloadAll;
	private static final String TAG = ActivitySettings.class.getName();
	
	private FragmentAlert fragmentDownloadAll;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private TextView  		fieldTitle;
	private View	  		btnLeft;
	private boolean   		fullScreen;
	private IabHelper 		mHelper;
	private ProgressDialog 	dialog;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static final void startActivity(Activity activity,boolean fullscreen){
		Intent intent = new Intent(activity, ActivitySettings.class);
		intent.putExtra(TAG_FULLSCREEN, fullscreen);
		
		activity.startActivity(intent);
		if(fullscreen)
			activity.overridePendingTransition(R.anim.slide_up, 0);
		
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(0, R.anim.slide_down);
	}
	
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
			
			MiscUtils.showAsPopup(this, R.layout.activity_settings, width, height);
		}
		else{
			setContentView(R.layout.activity_settings);
		}
				
		addSettingsFragment();
		setCustomActionBar();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TAG_FULLSCREEN, fullScreen);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	//Settings Click Listener
	@Override
	public void onLanguageClicked() {
		addLanguageSelectionFragment();
	}

	@Override
	public void onRestorePurchaseClicked() {
		dialog.show();
		if(mHelper==null){
			
			mHelper = new IabHelper(this, AppIsole.APP_KEY);
			mHelper.startSetup(new OnIabSetupFinishedListener() {
				
				@Override
				public void onIabSetupFinished(IabResult result) {
					 if (!result.isSuccess()) {
		                    // Oh noes, there was a problem.
//		                    complain("Problem setting up in-app billing: " + result);
						 	Toast.makeText(ActivitySettings.this, R.string.restore_failed, Toast.LENGTH_SHORT).show();
		                    return;
		                }

		                // Have we been disposed of in the meantime? If so, quit.
		                if (mHelper == null){
		                	Toast.makeText(ActivitySettings.this, R.string.restore_failed, Toast.LENGTH_SHORT).show();
		                	return;
		                }
		                
		                // IAB is fully set up. Now, let's get an inventory of stuff we own.
//		                Log.d(TAG, "Setup successful. Querying inventory.");
		                mHelper.queryInventoryAsync(mGotInventoryListener);
		                dialog.dismiss();
				}
			});
		}
		else{
			 mHelper.queryInventoryAsync(mGotInventoryListener);
		}
	}
	
	@Override
	public void onLanguageSelected(String language) {
		ActivityAudioPlayer.updateToLanguageChange(ActivitySettings.this);
	}

	@Override
	public void onInfoClicked() {
		addInfoFragment();
	}
	
	@Override
	public void onSettingsBackPressed() {
		// TODO Auto-generated method stub
		onBackPressed();
	}
	
	//Info Click Listener
	@Override
	public void onTermsAndConditionsClicked() {
		addTOSFragment();
	}

	@Override
	public void onCreditsClicked() {
		// TODO Auto-generated method stub
		addCreditsFragment();
	}
	
	

	
	// ===========================================================
	// Methods
	// ===========================================================
	
	private void init(Bundle savedInstanceState){
		if(savedInstanceState==null){
			fullScreen = getIntent().getBooleanExtra(TAG_FULLSCREEN, false);
			
		}
		else{
			fullScreen = savedInstanceState.getBoolean(TAG_FULLSCREEN);
		}
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.restoring_your_purchase));
	}
	
	private void addLanguageSelectionFragment(){
		FragmentManager fm = getSupportFragmentManager();
		FragmentLanguageSelection fragment = (FragmentLanguageSelection) fm.findFragmentByTag(TAG_FRAGMENT_LANGUAGE);
		if(fragment==null){
			fragment = FragmentLanguageSelection.getInstance();
			fm.beginTransaction()
			  .replace(R.id.fragContainer, fragment, TAG_FRAGMENT_LANGUAGE)
			  .addToBackStack(null)
			  .commit();
		}
		
	}
	
	private void addInfoFragment(){
		FragmentManager fm = getSupportFragmentManager();
		FragmentInfo fragment = (FragmentInfo) fm.findFragmentByTag(TAG_FRAGMENT_INFO);
		if(fragment==null){
			fragment = FragmentInfo.getInstance();
			fm.beginTransaction()
			  .replace(R.id.fragContainer, fragment, TAG_FRAGMENT_INFO)
			  .addToBackStack(null)
			  .commit();
		}
	}
	
	protected void addSettingsFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentSettings fragment = (FragmentSettings) fm.findFragmentByTag(TAG_FRAGMENT_SETTINGS);
		if(fragment==null){
			fragment = FragmentSettings.getInstance();
			fm.beginTransaction().replace(R.id.fragContainer, fragment, TAG_FRAGMENT_SETTINGS).commit();
		}
	}
	
	protected void addTOSFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTOS fragment = (FragmentTOS) fm.findFragmentByTag(TAG_FRAGMENT_TOS);
		if(fragment==null){
			fragment = FragmentTOS.getInstance();
			fm.beginTransaction()
			  .replace(R.id.fragContainer, fragment, TAG_FRAGMENT_TOS)
			  .addToBackStack(null)
			  .commit();
		}
	}
	
	protected void addCreditsFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentCredits fragment = (FragmentCredits) fm.findFragmentByTag(TAG_FRAGMENT_CREDITS);
		if(fragment==null){
			fragment = FragmentCredits.getInstance();
			fm.beginTransaction()
			  .replace(R.id.fragContainer, fragment, TAG_FRAGMENT_CREDITS)
			  .addToBackStack(null)
			  .commit();
		}
	}
	
	
	private void setCustomActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = getLayoutInflater().inflate(R.layout.acbar_title_subtitle
				, (ViewGroup) findViewById(R.id.root),false);
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
				finish();
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


	private void showDownloadProgress(){
		FragmentDownloadViewer downloadProgresser = (FragmentDownloadViewer) getSupportFragmentManager()
				.findFragmentByTag(TAG_DOWNLOAD_PROGRESS);
		if(downloadProgresser==null)
			downloadProgresser = FragmentDownloadViewer.getInstance();
		  downloadProgresser.show(getSupportFragmentManager(), TAG_DOWNLOAD_PROGRESS);
		 // downloadProgresser.setCancelable(false);
	}
	
	private void showDownloadAll(){
//here shows 4 instance of Fragment Alert 		
	if (_isAlertShowing()) return;
		 fragmentDownloadAll = (FragmentAlert) getSupportFragmentManager().findFragmentByTag(TAG_DOWNLOAD_ALL);
		String negativeButton = getResources().getString(R.string.cancel);
		String positiveButton = getResources().getString(android.R.string.ok);
		if(fragmentDownloadAll==null)
		fragmentDownloadAll = FragmentAlert.newInstance(R.string.download_all_bought, negativeButton, positiveButton);
		fragmentDownloadAll.show(getSupportFragmentManager(), TAG_DOWNLOAD_ALL); 
		
	}
	
	
//by bibin check fragment alert is held or not	
	private Boolean _isAlertShowing() {
		return (fragmentDownloadAll != null);
	}
	
	private void dismissDownloadAll(){
		/*FragmentAlert alert = (FragmentAlert) getSupportFragmentManager()
				.findFragmentByTag(TAG_DOWNLOAD_ALL);
		if(alert!=null)
			alert.dismiss();*/
		
//change by bibin		
		Log.d("AS", "onCancel");
		onDismiss();
	}

	@Override
	public void onPositiveClicked(String tag) {
		
		if(tag.equals(TAG_DOWNLOAD_ALL)){
			ArrayList<Museum> museums = AppIsole.getAllPurchases();
			TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
			for(Museum museum:museums){
				DownloadQueue queue = new DownloadQueue(museum, DownloadType.AUDIO_GUIDE
						, AppIsole.getAppLocaleAsStr());
				tableDownloadQueue.insertOrUpdate(queue);
			}
			 
			showDownloadProgress();
			dismissDownloadAll(); 
		}
		//
		fragmentDownloadAll = null; 
	}

	@Override
	public void onNegativeClicked(String tag) {
		if(tag.equals(TAG_DOWNLOAD_ALL)){
			dismissDownloadAll();
		}
		fragmentDownloadAll = null;
	}
	
	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		fragmentDownloadAll = null;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private QueryInventoryFinishedListener mGotInventoryListener = new QueryInventoryFinishedListener() {
		
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
			if(result.isSuccess()){
				Museum[] museums = Museum.getAllSkus();
				boolean museumpurchased = false;
				for(Museum museum:museums){
					 SkuDetails museumSkuDetails = inv.getSkuDetails(museum.getSku());			
					 if(museumSkuDetails!=null){
						 AppIsole.setPrice(museum,museumSkuDetails.getPrice());
						
					 }
					 Purchase purchase = inv.getPurchase(museum.getSku());
					 if(purchase!=null){
						 if(purchase.getPurchaseState()==0){
							 if(museum.equals(Museum.MUSEUM_ALL_IN_ONE)){
								 AppIsole.setPurchased(Museum.MUSEUM_ISOLA_BELLA, true);
								 AppIsole.setPurchased(Museum.MUSEUM_ISOLA_MADRE, true);
								 //anu changed
								 AppIsole.setPurchased(Museum.MUSEUM_ROCA_D_ANGERA, true);
							 }
							 AppIsole.setPurchased(museum, true);
							museumpurchased = true;
						 }
					 }
					 
					ActivityAudioPlayer.updateRestorePurchase(ActivitySettings.this);
					int msgId = R.string.restore_no_prev_purchase;
					if(museumpurchased){
						msgId = R.string.restore_successful;
						
						showDownloadAll();
                     
					}	
					Toast.makeText(ActivitySettings.this, msgId, Toast.LENGTH_SHORT).show();
				}
			}
			else{
				Toast.makeText(ActivitySettings.this, R.string.restore_failed, Toast.LENGTH_SHORT).show();
			}
			dialog.dismiss();
		}
	};  
}
