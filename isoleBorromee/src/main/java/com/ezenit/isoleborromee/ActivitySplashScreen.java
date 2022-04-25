package com.ezenit.isoleborromee;

import com.ezenit.isoleborromee.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ActivitySplashScreen extends Activity{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int TIME_DELAY = 5000;
	// ===========================================================
	// Fields
	// ===========================================================
	private Handler mHandler;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_splash_screen);
		mHandler = new Handler();
		mHandler.postDelayed(runnable, TIME_DELAY);
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(runnable);
	};
	
	// ===========================================================
	// Methods
	// ===========================================================
	private Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			ActivityAudioPlayer.startActivity(ActivitySplashScreen.this);
			finish();
		}
	};
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
