package com.ezenit.isoleborromee;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezenit.isoleborromee.adapter.FullScreenImageAdapter;
import com.ezenit.isoleborromee.adapter.FullScreenImageAdapter.OnImageClickListener;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery.GalleryImage;
import com.ezenit.isoleborromee.model.IImage;
import com.ezenit.utils.MiscUtils;

public class ActivityPhotoGallery extends AppCompatActivity {
	
	// ===========================================================
	// Constants
	// ===========================================================

	
	private static final int	DELAY_ANIM_HIDE = 3;
	
	private static final String ARG_LANGUAGE	= "com.ezenit.isoleborromee.fragments.FragmentPhotoGallery.ARG_LANGUAGE";
	private static final String ARG_POSITION 	= "com.ezenit.isoleborromee.fragments.FragmentPhotoGallery.ARG_POSITION";
	private static final String ARG_MUSUEM 		= "com.ezenit.isoleborromee.fragments.FragmentPhotoGallery.ARG_MUSUEM";
	
	// ===========================================================
	// Fields
	// ===========================================================

	

	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	private RelativeLayout titleLayout;
	private View descLayout;
	
	private ScheduledExecutorService hideAnimationDelay;
	
	private int position;
	private String language;
	private Museum musuem;


	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static void startActivity(Context context,GalleryImage image,int position){
		startActivity(context, Museum.getMuseum(image.getMuseumId()), image.getLanguage(),position);
	}
	
	public static void startActivity(Context context,Museum museum,String language){
		startActivity(context, museum, language,0);
	}
	
	public static void startActivity(Context context,Museum museum,String language,int position){
		Intent intent = new Intent(context,ActivityPhotoGallery.class);
		intent.putExtra(ARG_LANGUAGE, language);
		intent.putExtra(ARG_MUSUEM, museum.getShortName());
		intent.putExtra(ARG_POSITION, position);
		context.startActivity(intent);
	}






	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		getActionBar().hide();
		if(titleLayout.getVisibility()==View.GONE){
			toggleTopViewVisiblity();
			hideAfterDelay();
		}
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		getActionBar().show();
	}
	
	@Override
	protected void onResumeFragments() {
		// TODO Auto-generated method stub
		super.onResumeFragments();
		
	}
	
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.onCreateView(name, context, attrs);
	}
	
	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_POSITION, position);
		outState.putString(ARG_MUSUEM, musuem.getShortName());
		outState.putString(ARG_LANGUAGE, language);
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(titleLayout.getVisibility()==View.VISIBLE){
			toggleTopViewVisiblity();
		}
	}	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fr_photo_gallery);
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		titleLayout=(RelativeLayout) findViewById(R.id.title_layout);
	    descLayout= findViewById(R.id.desc_layout);
	    
	    if(savedInstanceState==null){
	    
		    position 		= getIntent().getIntExtra(ARG_POSITION,0);  
			musuem 		= Museum.getMuseum(getIntent().getStringExtra(ARG_MUSUEM));    
			language		= getIntent().getStringExtra(ARG_LANGUAGE);
	    }
	    else{
	    	position = savedInstanceState.getInt(ARG_POSITION);
	    	musuem   = Museum.getMuseum(savedInstanceState.getString(ARG_MUSUEM));
	    	language = savedInstanceState.getString(ARG_LANGUAGE);
	    }
	    MiscUtils.changeLanguage(this, language);
	    String folderPath 	= AppIsole.getGalleryPath(this, musuem);
		SQLiteDatabase db 	= AppIsole.getDB();
		
		final ArrayList<GalleryImage> images = TablePhotoGallery.getGalleryImages(db, musuem, language);
        if(images!=null){
        	adapter = new FullScreenImageAdapter(folderPath,images);
        }
        else{
        	finish();
        }
        
        adapter.setOnImageClickListener(new OnImageClickListener() {
			
			@Override
			public void onImageClicked(IImage image) {
				if(hideAnimationDelay!=null)
					hideAnimationDelay.shutdownNow();
				toggleTopViewVisiblity();
			}
		});
        
        viewPager.setAdapter(adapter);

		// displaying selected image first
		viewPager.setCurrentItem(position);
		
		final TextView fieldMusuem = (TextView) findViewById(R.id.fieldSubTitle);
		final TextView fieldTitle = (TextView) findViewById(R.id.fieldDescTitle);
		final TextView fieldDescription  = (TextView) findViewById(R.id.fieldDescription);
		final TextView fieldGalleryTitle = (TextView) findViewById(R.id.fieldTitle);
		fieldGalleryTitle.setText(R.string.photo_gallery);

		
		IImage image = images.get(position);
		String title = image.getTitle();
		if(TextUtils.isEmpty(title)){
			fieldTitle.setVisibility(View.GONE);
		}
		else{
			fieldTitle.setText(title);
		}

		ActionBar actionBar = getSupportActionBar();

		actionBar.setTitle(getResources().getString(R.string.title_new));

		fieldDescription.setText(image.getDescrition());
		fieldMusuem.setText(getResources().getString(R.string.sub_title_new));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				IImage image = images.get(position);
				fieldTitle.setText(image.getTitle());
				fieldDescription.setText(image.getDescrition());
				ActivityPhotoGallery.this.position = position;
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}
		});
		
		View btnBack = findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
	}
	
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getFragmentManager().popBackStack();
				return true;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	private void toggleTopViewVisiblity(){
		if(titleLayout.getTag()==null){
			if(titleLayout.getVisibility()==View.VISIBLE)
			{
				animateAndShow(titleLayout, false,R.anim.slide_up_top);
				animateAndShow(descLayout, false,R.anim.slide_down);
			}
			else
			{
				animateAndShow(titleLayout, true, R.anim.slide_down_top);
				animateAndShow(descLayout, true,R.anim.slide_up);
			}
		}
//		descLayout.setVisibility(View.GONE);
//		titleLayout.setVisibility(View.GONE);
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
