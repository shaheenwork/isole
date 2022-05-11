package com.ezenit.isoleborromee;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.ezenit.customview.SwipeableViewPager;
import com.ezenit.isoleborromee.db.table.TableSection;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TableSection.Section;
import com.ezenit.isoleborromee.fragments.FragmentMap;
import com.ezenit.utils.FragmentStatePagerAdapter;

public class ActivityMapContainer extends AppCompatActivity implements FragmentMap.OnMapClickListener{
	// ===========================================================
	// Constants
	// ===========================================================
	
	public static final String  ARG_MUSEUM = "com.ezenit.isoleborromee.ActivityMapContainer.EXTRA_MUSEUM";
	private static final String ARG_AUDIO_GUIDE = "com.ezenit.isoleborromee.ActivityMapContainer.ARG_AUDIO_GUIDE";
	private static final String ARG_LANGUAGE	= "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_LANGUAGE";
	private static final String ARG_IS_FREE		= "com.ezenit.isoleborromee.fragments.FragmentMap.ARG_IS_FREE";
		
	// ===========================================================
	// Fields
	// ===========================================================
	
	private View 		btnLeft;
	private TextView 	fieldSubTitle;
	private TextView	fieldTitle;
	
	private Museum museum;
	private String  language;
	private boolean isFree;
	
	private SwipeableViewPager	pager;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static final void startActivity(Activity activity,int requestCode,Museum museum,String language, boolean isFree){
		Intent intent = new Intent(activity, ActivityMapContainer.class);
		intent.putExtra(ARG_MUSEUM, museum.getShortName());
		intent.putExtra(ARG_LANGUAGE, language);
		intent.putExtra(ARG_IS_FREE, isFree);
		activity.startActivityForResult(intent, requestCode);
	}
	
	public static final void startActivity(Context context,Museum museum,String language, boolean isFree){
		Intent intent = new Intent(context, ActivityMapContainer.class);
		intent.putExtra(ARG_MUSEUM, museum.getShortName());
		intent.putExtra(ARG_LANGUAGE, language);
		intent.putExtra(ARG_IS_FREE, isFree);
		context.startActivity(intent);
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	private static final String TAG = ActivityMapContainer.class.getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_map_container);
		
		View root		= findViewById(R.id.root);
		
		init(savedInstanceState);
		
		ArrayList<Section> sections = TableSection.getSections(museum,language);
		
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (SwipeableViewPager) findViewById(R.id.pager);
		
		final MapAdapter adapter = new MapAdapter(sections, getSupportFragmentManager());
		
		pager.setAdapter(adapter);		
		
		pager.setEnabled(false);
		
		
			
		if(sections.size()<2){
			tabs.setVisibility(View.GONE);
		}
		else{
			tabs.setViewPager(pager);
			tabs.setVisibility(View.VISIBLE);
			tabs.setIndicatorColorResource(android.R.color.black);
			
		}
		
		setCustomActionBar(getLayoutInflater(),(ViewGroup) root, museum);
	}
	
	
	@Override
	protected void onDestroy() {
		resetActionBar();
		super.onDestroy();
	
	}
	
	private void init(Bundle savedInstanceState){
		if(savedInstanceState==null){
			museum 	 = Museum.getMuseum(getIntent().getStringExtra(ARG_MUSEUM));
			language = getIntent().getStringExtra(ARG_LANGUAGE);
			isFree	 = getIntent().getBooleanExtra(ARG_IS_FREE, false);
		}
		else{
			museum = Museum.getMuseum(savedInstanceState.getString(ARG_MUSEUM));
			language = savedInstanceState.getString(ARG_LANGUAGE);
			isFree	 = savedInstanceState.getBoolean(ARG_IS_FREE, false);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_MUSEUM, museum.getShortName());
		outState.putString(ARG_LANGUAGE, language);
		outState.putBoolean(ARG_IS_FREE, isFree);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onMapAudioClicked(BaseAudioGuide guide) {
		Intent data = new Intent();
		data.putExtra(ARG_AUDIO_GUIDE, guide);
		setResult(RESULT_OK, data);
		finish();
	}

	
	
	// ===========================================================
	// Methods
	// ===========================================================

	private void resetActionBar(){
		fieldTitle.setVisibility(View.GONE);
		btnLeft.setVisibility(View.GONE);
		fieldSubTitle.setVisibility(View.GONE);
	}
	
	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root
			,Museum museum) {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		
		View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root,false);
		actionBar.setCustomView(actionBarView);

		Toolbar toolbar=(Toolbar)actionBarView.getParent();
		toolbar.setContentInsetsAbsolute(0,0);
		toolbar.setContentInsetsAbsolute(0, 0);
		toolbar.getContentInsetEnd();
		toolbar.setPadding(0, 0, 0, 0);


		btnLeft = actionBarView.findViewById(R.id.btnLeft);
		btnLeft.setClickable(true);
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		fieldTitle 		= (TextView) actionBarView.findViewById(R.id.fieldTitle);
		fieldSubTitle 	= (TextView) actionBarView.findViewById(R.id.fieldSubTitle);
		
		fieldTitle.setText(R.string.map_del_palazzo);
		fieldSubTitle.setText(museum.getNameId());
	
		
		fieldTitle.setVisibility(View.VISIBLE);
		btnLeft.setVisibility(View.VISIBLE);
		fieldSubTitle.setVisibility(View.VISIBLE);
		
		
	}
	
	public static BaseAudioGuide getAudioGuide(Intent data) {
		return data.getParcelableExtra(ARG_AUDIO_GUIDE);
	}
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private class MapAdapter extends FragmentStatePagerAdapter{

	
		// ===========================================================
		// Constants
		// ===========================================================
		private static final String TAG_MAP = "com.ezenit.isoleborromee.MapAdapter.TAG_MAP";
		// ===========================================================
		// Fields
		// ===========================================================
		private ArrayList<Section> sections;
		// ===========================================================
		// Constructors
		// ===========================================================
		public MapAdapter(ArrayList<Section> sections,FragmentManager fm) {
			super(fm);
			this.sections = sections;
			
		}
		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================
		@Override
		public Fragment getItem(int position) {
			Section section = sections.get(position);
			FragmentMap map = (FragmentMap) getSupportFragmentManager().findFragmentByTag(TAG_MAP+position);
			if(map==null)
				map = FragmentMap.getInstance(section.getId(), section.getMuseum()
						, language, isFree);
			
			return map;
		}
		
		@Override
		public String getItemTag(int position) {
			// TODO Auto-generated method stub
			return TAG_MAP+position;
		}
		
		
		
		@Override
		public int getCount() {
			return sections.size();
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return sections.get(position).getName();
		}
		// ===========================================================
		// Methods
		// ===========================================================

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}


	@Override
	public void onMapEdgeReached() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapOutOfEdge() {
		// TODO Auto-generated method stub
		
	}



	


	
}
