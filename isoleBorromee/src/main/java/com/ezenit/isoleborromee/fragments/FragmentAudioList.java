package com.ezenit.isoleborromee.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.adapter.AdapterTopAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.TopBaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;

public class FragmentAudioList extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String TAG 	  					= FragmentAudioList.class.getName();
	
	private static final String ARG_MUSUEM 					= "com.ezenit.isoleborromee.fragments.FragmentAudioList.ARG_MUSUEM";
	private static final String ARG_LANG_SHORT 				= "com.ezenit.isoleborromee.fragments.FragmentAudioList.ARG_LANG_SHORT";
	private static final String ARG_IS_FREE  				= "com.ezenit.isoleborromee.fragments.FragmentAudioList.ARG_IS_FREE";
	
	private static final String EXTRA_GUIDE_SELECTION 		= "com.ezenit.isoleborromee.fragments.FragmentAudioList.EXTRA_GUIDE_SELECTION";
	private static final String EXTRA_EXPANDABLE_POSITIONS 	= "com.ezenit.isoleborromee.fragments.FragmentAudioList.EXTRA_EXPANDABLE_POSITIONS";
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private OnAudioClickListener audioClickListener;
	private AdapterTopAudioGuide adapter;
	
	private ExpandableListView list;
	
	
	private View 	  btnLeft;
	private TextView  fieldTitle;
	private TextView  fieldSubTitle;
	private ImageView  btnRight;
	

	private ArrayList<Integer> listExpandedPosition;
	
	private View btnClear;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static FragmentAudioList getInstance(Museum museum,String langShort,boolean isFree){
		FragmentAudioList fragment = new FragmentAudioList();
		Bundle arguments = new Bundle();
		arguments.putString(ARG_MUSUEM, museum.getShortName());
		arguments.putString(ARG_LANG_SHORT, langShort);
		arguments.putBoolean(ARG_IS_FREE, isFree);
		fragment.setArguments(arguments);
		return fragment;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public void setSelection(BaseAudioGuide audioGuide){
		if(adapter!=null){
			adapter.setSelection(audioGuide);
			
			if(audioGuide!=null&&!TableAudioGuide.isParent(audioGuide)){
				adapter.setParentCode(audioGuide.getParentCode());
			}
			else{
				adapter.setParentCode("");
			}
			getArguments().putLong(EXTRA_GUIDE_SELECTION, adapter.getSelectAudioGuideId());
			getArguments().putIntegerArrayList(EXTRA_EXPANDABLE_POSITIONS, listExpandedPosition);
		}
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
            audioClickListener = (OnAudioClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAudioClickListener");
        }
		
		listExpandedPosition = new ArrayList<Integer>(5);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(adapter!=null){
			outState.putLong(EXTRA_GUIDE_SELECTION, adapter.getSelectAudioGuideId());
			getArguments().putLong(EXTRA_GUIDE_SELECTION, adapter.getSelectAudioGuideId());
		}
		
		outState.putIntegerArrayList(EXTRA_EXPANDABLE_POSITIONS, listExpandedPosition);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View root = inflater.inflate(R.layout.fr_list_audio_guide, null);
		Museum museum = Museum.getMuseum(getArguments().getString(ARG_MUSUEM));
		String langShort = getArguments().getString(ARG_LANG_SHORT);
		boolean isFree = getArguments().getBoolean(ARG_IS_FREE);
		
		long id = -1;
		if(savedInstanceState!=null){
			id = savedInstanceState.getLong(EXTRA_GUIDE_SELECTION);
			listExpandedPosition = savedInstanceState.getIntegerArrayList(EXTRA_EXPANDABLE_POSITIONS);
		}
		else{
			id = getArguments().getLong(EXTRA_GUIDE_SELECTION, -1);
			ArrayList<Integer> tempList = getArguments().getIntegerArrayList(EXTRA_EXPANDABLE_POSITIONS);
			if(tempList!=null){
				listExpandedPosition = tempList; 
			}
		}
		
		populateAudioList(root,museum,langShort,isFree,id);
		setCustomActionBar(inflater, (ViewGroup)root, museum,langShort,isFree);
		MiscUtils.hideKeyBoard(getActivity(), root);
		return root;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		updateView(AppIsole.getAppLocaleAsStr());
	}
	
	public void scrollToSelection(BaseAudioGuide guide) {
		int position =  adapter.getPosition(guide);
		if(position!=-1)
			list.smoothScrollToPosition(position);
		
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		resetActionBar();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
//		MiscUtils.enableHomeButton(getActivity());
//		MiscUtils.setTitle(getActivity(), R.string.audio_guide, museum.getNameId());
		setHasOptionsMenu(true);
	
		
	
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
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
	
	private void updateView(String language){
		Bundle argument = getArguments();
		argument.putString(ARG_LANG_SHORT, language);
		
		if(argument!=null){
			View root = getView();
			
			Museum museum = Museum.getMuseum(argument.getString(ARG_MUSUEM));
			boolean isFree = argument.getBoolean(ARG_IS_FREE);
			long id = -1;
			if(adapter!=null)
				id = adapter.getSelectAudioGuideId();
			if(root!=null)
				populateAudioList(root,museum , language, isFree, id);
			
		}
	}
	
	
	private void populateAudioList(View root,final Museum museum,final String langShort,final boolean isFree,long selectedId) {
					
		list = (ExpandableListView) root.findViewById(R.id.list);
		ArrayList<TopBaseAudioGuide> audioGuides = TableAudioGuide.getTopAudioGuides(langShort, museum, isFree);
		
		adapter = new AdapterTopAudioGuide(getActivity(), museum,  audioGuides);
		list.setAdapter(adapter);
		BaseAudioGuide guide = TableAudioGuide.getBaseAudioGuide(selectedId);
		if(guide!=null&&!TableAudioGuide.isParent(guide)){
			adapter.setParentCode(guide.getParentCode());
		}
		
		adapter.setSelection(selectedId);
		
		list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				TopBaseAudioGuide parentAudio = adapter.getGroup(groupPosition);
				if(audioClickListener!=null&&	parentAudio.getChildAudioGuides().size()==0){
					audioClickListener.onAudioClicked(FragmentAudioList.this, parentAudio);
				}
				
				return false;
			}
		});
		
		list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if(audioClickListener!=null){
					audioClickListener.onAudioClicked(FragmentAudioList.this, adapter.getChild(groupPosition, childPosition));
				}
				return false;
			}
		});
		
		list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
				if(!listExpandedPosition.contains((Integer)(groupPosition))){
					listExpandedPosition.add(groupPosition);
				}
			}
		});
		
		list.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			
			@Override
			public void onGroupCollapse(int groupPosition) {
				boolean removed = listExpandedPosition.remove((Integer)groupPosition);
				
			}
		});
		
		for(int i=0;i<listExpandedPosition.size();i++){
			list.expandGroup(listExpandedPosition.get(i));
		}
		
//		list.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				if(audioClickListener!=null){
//					
//					audioClickListener.onAudioClicked(FragmentAudioList.this,adapter.getItem(position));
//				}
//			}
//		});
		final EditText fieldSearch = (EditText)(root.findViewById(R.id.fieldSearch));
		fieldSearch.removeTextChangedListener(watcher);
		fieldSearch.addTextChangedListener(watcher);
		View btnMap = root.findViewById(R.id.btnMap);
		if(btnMap!=null){
			btnMap.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(audioClickListener!=null)
						audioClickListener.onMapClicked(museum, langShort, isFree);
				}
			});
		}
		
		fieldSearch.setHint(getImageSpanText(R.drawable.ic_search, R.string.search_by_code));
		
		fieldSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus)
					fieldSearch.setCursorVisible(true);
				else{
					fieldSearch.setCursorVisible(false);
				}
			}
		});
		
		fieldSearch.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_SEARCH){
					fieldSearch.clearFocus();
				}
				fieldSearch.clearFocus();
				return false;
			}
		});
		
		btnClear = root.findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				fieldSearch.setText("");
			}
		});
		
		Editable fieldSearchText = fieldSearch.getText();
		if(fieldSearchText==null||TextUtils.isEmpty(fieldSearchText)){
			btnClear.setVisibility(View.GONE);
		}
		else{
			btnClear.setVisibility(View.VISIBLE);
		}
		
	}
	
	private SpannableStringBuilder getImageSpanText(int imgId, int fieldId){
		String fieldText = "  "+getResources().getString(fieldId);
		SpannableStringBuilder builder = new SpannableStringBuilder();
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgId);
		
		ImageSpan image = new ImageSpan(getActivity(),bitmap);
		
		// this is a string that will let you find a place, where the ImageSpan is.
		String dummyId = "[img=1]"; 
		builder.append(dummyId);
		builder.append(fieldText);

//		// current selection is replace with imageId
//		builder.replace(0, fieldText.length(), dummyId);

		// this "replaces" imageId string with image span. If you do builder.toString() - the string will contain imageIs where the imageSpan is.
		// you can yse this later - if you want to location of imageSpan in text;
		builder.setSpan(image, 0, 0 + dummyId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		return builder;
	}
	
	
	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root
			,final Museum museum,final String language,final boolean isFree) {
		// TODO Auto-generated method stub
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root,false);
		actionBar.setCustomView(actionBarView);
		btnLeft = actionBarView.findViewById(R.id.btnLeft);
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		fieldTitle 		= (TextView) actionBarView.findViewById(R.id.fieldTitle);
		fieldSubTitle 	= (TextView) actionBarView.findViewById(R.id.fieldSubTitle);
		btnRight		= (ImageView) actionBarView.findViewById(R.id.btnRight);
		btnRight.setImageResource(R.drawable.btn_map);
		
		fieldTitle.setText(R.string.audio_guide);
		fieldSubTitle.setText(museum.getNameId());
		btnRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(audioClickListener!=null)
					audioClickListener.onMapClicked(museum,language,isFree);
			}
		});
	
		
		fieldTitle.setVisibility(View.VISIBLE);
		btnLeft.setVisibility(View.VISIBLE);
		fieldSubTitle.setVisibility(View.GONE);
		btnRight.setVisibility(View.VISIBLE);
	}
	
	private void resetActionBar(){
		
		
		fieldTitle.setVisibility(View.GONE);
		btnLeft.setVisibility(View.GONE);
		fieldSubTitle.setVisibility(View.GONE);
		btnRight.setVisibility(View.GONE);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private TextWatcher watcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			adapter.getFilter().filter(s);
			if(btnClear!=null)
				btnClear.setVisibility(TextUtils.isEmpty(s)?View.GONE:View.VISIBLE);
			
		}
	};
	
	
	public interface OnAudioClickListener{
		public void onAudioClicked(FragmentAudioList fragment,BaseAudioGuide audioGuide);
		public void onMapClicked(Museum museum,String language,boolean isFree);
	}


	
	
}
