package com.ezenit.isoleborromee;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezenit.isoleborromee.adapter.AdapterFSImageGallery;
import com.ezenit.isoleborromee.adapter.AdapterFSImageGallery.OnImageClickListener;
import com.ezenit.isoleborromee.db.table.TableImgRel;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableImgRel.Image;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;

public class ActivityFSAudioGallery extends FragmentActivity{
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String ARG_CODE_NO  = "com.ezenit.isoleborromee.fragments.FragmentFSAudioGallery.ARG_CODE_NO";
	private static final String ARG_ITEM_NAME = "com.ezenit.isoleborromee.fragments.FragmentFSAudioGallery.ARG_ITEM_NAME";
	private static final String ARG_INDEX = "com.ezenit.isoleborromee.fragments.FragmentFSAudioGallery.ARG_INDEX";
	private static final String ARG_IS_FREE  = "com.ezenit.isoleborromee.fragments.FragmentFSAudioGallery.ARG_IS_FREE";
	private static final String ARG_MUSUEM 	 = "com.ezenit.isoleborromee.fragments.FragmentFSAudioGallery.ARG_MUSEUM"; 
	
	private static final int	DELAY_ANIM_HIDE = 3;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private View titleLayout;
	private ScheduledExecutorService hideAnimationDelay;
	
	private String codeNo ;
	private Museum museum ;
	private boolean isFree ;
	private int index ;
	private String title;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	public static void startActivity(Activity activity,int imageIndex,AudioGuide guide){
//		FragmentFSAudioGallery fragment = new FragmentFSAudioGallery();
		Intent intent = new Intent(activity, ActivityFSAudioGallery.class);
//		Bundle bundle = new Bundle();
		
		intent.putExtra(ARG_CODE_NO, guide.getCodeNo());
		intent.putExtra(ARG_ITEM_NAME, guide.getTitle());
		intent.putExtra(ARG_MUSUEM, guide.getMuseum().getShortName());
		intent.putExtra(ARG_IS_FREE, guide.isFree());
		intent.putExtra(ARG_INDEX, imageIndex);
		
		activity.startActivity(intent);
//		fragment.setArguments(bundle);
//		return fragment;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
//	@Override
//	public void onStart() {
//		// TODO Auto-generated method stub
//		super.onStart();
////		getActionBar().hide();
//	}
//	
//	@Override
//	public void onStop() {
//		// TODO Auto-generated method stub
//		super.onStop();
////		getActionBar().show();
//	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.fr_photo_gallery);
		
		if(savedInstanceState==null){
			codeNo = getIntent().getStringExtra(ARG_CODE_NO);
			museum = Museum.getMuseum(getIntent().getStringExtra(ARG_MUSUEM));
			isFree = getIntent().getBooleanExtra(ARG_IS_FREE, false);
			index  = getIntent().getIntExtra(ARG_INDEX, 0);
			title  = getIntent().getStringExtra(ARG_ITEM_NAME);
		}
		else{
			codeNo = savedInstanceState.getString(ARG_CODE_NO);
			museum = Museum.getMuseum(savedInstanceState.getString(ARG_MUSUEM));
			isFree = savedInstanceState.getBoolean(ARG_IS_FREE, false);
			index  = savedInstanceState.getInt(ARG_INDEX, 0);
			title  = savedInstanceState.getString(ARG_ITEM_NAME);
		}
		
//		String codeNo 	= getArguments().getString(ARG_CODE_NO);
//		Museum museum 	= Museum.getMuseum(getArguments().getString(ARG_MUSUEM));
//		boolean isFree  = getArguments().getBoolean(ARG_IS_FREE);
//		int index		= getArguments().getInt(ARG_INDEX);
		
		
		
	
		
		titleLayout=(RelativeLayout)findViewById(R.id.title_layout);
		
		SQLiteDatabase db = AppIsole.getDB();
		ArrayList<Image> images = TableImgRel.getAllImages(db, codeNo, museum, isFree);				
		
		ViewPager 		viewPager = (ViewPager) findViewById(R.id.pager);
		RelativeLayout	titleLayout=(RelativeLayout) findViewById(R.id.title_layout);
	    LinearLayout 	descLayout=(LinearLayout) findViewById(R.id.desc_layout);
	    
	    titleLayout.setVisibility(View.GONE);
	    descLayout.setVisibility(View.GONE);
	    
		
		final TextView fieldMusuem = (TextView) findViewById(R.id.fieldTitle);
		final TextView fieldSubtitle = (TextView) findViewById(R.id.fieldSubTitle);
		final View	   btnBack		 = findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});		
		
		fieldMusuem.setText(title);
		fieldSubtitle.setText(museum.getNameId());
	    
		  
        AdapterFSImageGallery adapter = new AdapterFSImageGallery(this, museum, images);
        
		
		viewPager.setAdapter(adapter);
		adapter.setOnImageClickListener(new OnImageClickListener() {
			
			@Override
			public void onImageClicked() {
				toggleTopViewVisiblity();
			}
		});
		// displaying selected image first
		viewPager.setCurrentItem(index);
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_CODE_NO, codeNo);
		outState.putString(ARG_MUSUEM, museum.getShortName());
		outState.putBoolean(ARG_IS_FREE, isFree);
		outState.putInt(ARG_INDEX, index);
		outState.putString(ARG_ITEM_NAME, title);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(titleLayout.getVisibility()==View.GONE){
			toggleTopViewVisiblity();
			hideAfterDelay();
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(titleLayout.getVisibility()==View.VISIBLE){
			toggleTopViewVisiblity();
		}
	}
	
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	private void toggleTopViewVisiblity(){
		if(titleLayout.getTag()==null){
			if(titleLayout.getVisibility()==View.VISIBLE)
			{
				animateAndShow(titleLayout, false,R.anim.slide_up_top);
			}
			else
			{
				animateAndShow(titleLayout, true, R.anim.slide_down_top);
			}
		}
	}
	

	
	private void animateAndShow(final View view,final boolean show, int animationId){
		Animation animation = AnimationUtils.loadAnimation(this, animationId);
		view.setTag("animating");
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(!show)
					view.setVisibility(View.GONE);
				else
					view.setVisibility(View.VISIBLE);
				view.setTag(null);
			}
		});
		
		view.startAnimation(animation);
		
	}
	
	private void hideAfterDelay() {
		if(hideAnimationDelay==null||hideAnimationDelay.isShutdown())
			hideAnimationDelay = Executors.newSingleThreadScheduledExecutor();
		hideAnimationDelay.schedule(new Runnable() {
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						toggleTopViewVisiblity();
					}
				});
				
			}
		}, DELAY_ANIM_HIDE, TimeUnit.SECONDS);
		
		
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
