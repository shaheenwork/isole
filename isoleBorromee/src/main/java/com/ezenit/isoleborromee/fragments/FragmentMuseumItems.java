package com.ezenit.isoleborromee.fragments;

import java.util.concurrent.ScheduledExecutorService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezenit.download.services.DownloadManager;
import com.ezenit.download.utils.DownloaderIntents;
import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue.DownloadQueue;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.service.AppIsoleReceiver;
import com.ezenit.isoleborromee.service.AppIsoleReceiver.BroadCastManager;
import com.ezenit.utils.MiscUtils;
import com.ezenit.isoleborromee.R;
import com.todddavies.components.progressbar.ProgressWheel;

/********
 * Foolish MARIO had asked to disable click on isole madre and rocca d angera so search for this code
 * 
 * if(museum.equals(Museum.MUSEUM_ISOLA_MADRE)||museum.equals(Museum.MUSEUM_ROCA_D_ANGERA))
 *
 * and delete it when he asks to :)					
 * ***/
public class FragmentMuseumItems extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String ARG_MUSEUM 	= "com.ezenit.isoleborromee.fragments.FragmentMusuemItems.ARG_MUSUEM";
	private static final String ARG_LANG 	= "com.ezenit.isoleborromee.fragments.FragmentMusuemItems.ARG_LANG";
	private static final String ARG_TABLET  = "com.ezenit.isoleborromee.fragments.FragmentMusuemItems.ARG_TABLET";
		
	private static final String TAG         = FragmentMuseumItems.class.getName();
	
	private static final String TAG_ALERT	= "com.ezenit.isoleborromee.fragments.FragmentMusuemItems.TAG_ALERT";
	
	private static final String ARG_SELECTION = "com.ezenit.isoleborromee.fragments.FragmentMuseumItems.ARG_SELECTION";
	
	private static final int STATUS_CANCEL  = 1;
	private static final int STATUS_PAUSE   = 2;
		
	// ===========================================================
	// Fields
	// ===========================================================
	private Museum museum;
	private String langShort;
	private boolean isTablet;
	
	
	private ScheduledExecutorService photoGalleryExecutor;
	private ScheduledExecutorService audioExecutor;
		
	
	private View btnGallery;
	private View btnDownloadGallery;
	
	private View layoutProgressGallery;
	private ProgressWheel progressDownloadGallery;
	private View btnPauseGallery;
	
	private View detailGallery;
	
	private View btnAudioGuide;
	private TextView btnDownloadAudioGuide;
	
	private View layoutProgressAudioGuide;
	private ProgressWheel progressDownloadAudioGuide;
	private View btnPauseAudioGuide;
	
	private View detailAudioGuide;
	
	private MuseumItemReceiver receiver;
	
	private OnMuseumItemClickListener museumItemClickListener;
	
	private boolean isGalleryDownloadVisible;
	private boolean isAudioDowloadVisible;
	
	private int selection = -1;
	
	private boolean isPausedGallery;
	private boolean isPausedAudioGuide;
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentMuseumItems getInstance(Museum museum,String langShort){
		return getInstance(museum, langShort,false);
	}
	
	public static FragmentMuseumItems getInstance(Museum museum,String langShort,boolean tablet){
		FragmentMuseumItems fragment = new FragmentMuseumItems();
		Bundle bundle = new Bundle();
		populateBundle(bundle, museum, langShort, tablet);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	
	
	private static void populateBundle(Bundle bundle,Museum museum,String langShort,boolean tablet){
		bundle.putString(ARG_MUSEUM, museum.getShortName());
		bundle.putString(ARG_LANG, langShort);
		bundle.putBoolean(ARG_TABLET, tablet);
			
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean isTablet() {
		return isTablet;
	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            this.museumItemClickListener = (OnMuseumItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMuseumItemClickListener");
        }
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(!isTablet){
			receiver = new MuseumItemReceiver();
			IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_STATUS);
			filter.addAction(AppIsoleReceiver.ACTION_ISOLE_INSTALLATION);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			getActivity().registerReceiver(receiver, filter);
		}
		
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		getArguments().putInt(ARG_SELECTION, selection);
		
		if(photoGalleryExecutor!=null)
			photoGalleryExecutor.shutdown();
		if(audioExecutor!=null)
			audioExecutor.shutdown();
		if(receiver!=null)
			getActivity().unregisterReceiver(receiver);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			
		View root = inflater.inflate(R.layout.fr_museum_items, container, false);
		refreshView(root);
		setCustomActionBar(inflater,(ViewGroup)root,museum);
		return root;
	}
	
	private void refreshView(View root){
		setUpArguments();		
		setUpDownloadButtons(root);
		setUpView(root,museum);
		
		MiscUtils.hideKeyBoard(getActivity(), root);
		//Stupid Mario Asked to Remove this\
	//refence of bibin add bella and madre gudio gudie visible//	if(!museum.equals(Museum.MUSEUM_ISOLA_BELLA) && !museum.equals(Museum.MUSEUM_ISOLA_MADRE)){
		//anu changed
		if((!museum.equals(Museum.MUSEUM_ISOLA_BELLA)) &&! museum.equals(Museum.MUSEUM_ISOLA_MADRE)){
			root.findViewById(R.id.btnPaidGuide).setVisibility(View.GONE);
			root.findViewById(R.id.dividerPaidGuide).setVisibility(View.GONE);
		}
		else{
			root.findViewById(R.id.btnPaidGuide).setVisibility(View.VISIBLE);
			root.findViewById(R.id.dividerPaidGuide).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		View root = getView();
		if(root!=null){
			getArguments().putString(ARG_LANG, AppIsole.getAppLocaleAsStr());
			((TextView)root.findViewById(R.id.lblGallery)).setText(R.string.photo_gallery);
			((TextView)root.findViewById(R.id.lblPaidGuide)).setText(R.string.audio_guide_palace);
			if(museum!=null){
				Spanned musuemDesc = Html.fromHtml(getResources().getString(museum.getDescId()));
				((TextView)root.findViewById(R.id.fieldDescription)).setText(musuemDesc);
			}
			setUpArguments();
			setUpDownloadButtons();			
		}
	}
	
	
		
	private void setUpView(View root, Museum museum) {
		TextView fieldDescription = (TextView) root.findViewById(R.id.fieldDescription);
		String   description = getResources().getString(museum.getDescId());
		fieldDescription.setText(Html.fromHtml(description));
		
		ImageView museumCover = (ImageView) root.findViewById(R.id.imgMuseum);
		museumCover.setImageResource(museum.getCoverImageId());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
//		MiscUtils.enableHomeButton(getActivity());
		MiscUtils.setTitle((AppCompatActivity)getActivity(), museum.getNameId());
		((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(null);
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getFragmentManager().popBackStack();
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
	}
	
	

	private void setUpArguments() {
		museum		= Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
		langShort   = getArguments().getString(ARG_LANG);
		isTablet	= getArguments().getBoolean(ARG_TABLET);
	}

	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if(hidden&&!isTablet)
			((AppCompatActivity)getActivity()).getSupportActionBar().show();
	}
	
	@Override
	public void onDestroyView() {
		if(!isTablet)
			((AppCompatActivity)getActivity()).getSupportActionBar().show();
		super.onDestroyView();
	}
	
	public void setUpDownloadButtons(){
		View root = getView();
		if(root!=null){
			setUpDownloadButtons(root);
		}
	}
	
	private void setUpDownloadButtons(View root) {
		btnAudioGuide		  		= root.findViewById(R.id.btnPaidGuide);
		btnDownloadAudioGuide 		= (TextView) root.findViewById(R.id.btnDownloadPaidGuide);
		progressDownloadAudioGuide 	= (ProgressWheel) root.findViewById(R.id.progressPaidGuideDownload);
		detailAudioGuide  	  		= root.findViewById(R.id.imgPaidGuidedetail);
		layoutProgressAudioGuide	= root.findViewById(R.id.layoutProgressPaidGuide);
		
		
		btnGallery         		= root.findViewById(R.id.btnGallery);
		btnDownloadGallery 		= root.findViewById(R.id.btnDownloadGallery);
		progressDownloadGallery = (ProgressWheel) root.findViewById(R.id.progressGalleryDownload);
		detailGallery  	= root.findViewById(R.id.imgGallerydetail);
		layoutProgressGallery	= root.findViewById(R.id.layoutProgressGallery);
		
		btnPauseAudioGuide = root.findViewById(R.id.btnPausePaidGuide);
		btnPauseGallery	   = root.findViewById(R.id.btnPauseGallery);
		
		if(!isTablet){
			if(AppIsole.isInstalled(museum
					,DownloadType.AUDIO_GUIDE, langShort)){
				showDownloadComplete(DownloadType.AUDIO_GUIDE);
				
			}
			else{
				showDownloadButton(DownloadType.AUDIO_GUIDE);
			}		
					
			if(AppIsole.isInstalled(museum
					, DownloadType.PHOTO_GALLERY,langShort)){
				showDownloadComplete(DownloadType.PHOTO_GALLERY);
			}
			else{
				showDownloadButton(DownloadType.PHOTO_GALLERY);
			}
			
			if(AppIsole.isPurchased(museum)){
				btnDownloadAudioGuide.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(museum.equals(Museum.MUSEUM_ROCA_D_ANGERA))
							return;
						downloadAudioGuide();
					}
				});
			}
			else{
				btnDownloadAudioGuide.setText(AppIsole.getPrice(getActivity(), museum));
				btnDownloadAudioGuide.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(museum.equals(Museum.MUSEUM_ROCA_D_ANGERA))
							return;
						if(museumItemClickListener!=null){
							museumItemClickListener.onBuyMuseumFromMuseumItem(museum);
						}							
					}
				});
			}
			
			
			
			btnDownloadGallery.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					downloadPhotoGallery();
				}
			});
			
			btnPauseAudioGuide.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Integer tag = (Integer) btnPauseAudioGuide.getTag();
					if(tag!=null){
						if(tag==STATUS_PAUSE){
							pause(DownloadType.AUDIO_GUIDE);
						}
						else if(tag==STATUS_CANCEL){
							DownloadQueue queue = new DownloadQueue(museum, DownloadType.AUDIO_GUIDE, langShort);
							TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
							tableDownloadQueue.delete(queue);
							showDownloadButton(DownloadType.AUDIO_GUIDE);
							
						}
					}
				}
			});
			
			btnPauseGallery.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Integer tag = (Integer) btnPauseGallery.getTag();
					if(tag!=null){
						if(tag==STATUS_PAUSE)
							pause(DownloadType.PHOTO_GALLERY);
						else if(tag==STATUS_CANCEL){
							DownloadQueue queue = new DownloadQueue(museum, DownloadType.PHOTO_GALLERY, langShort);
							TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
							tableDownloadQueue.delete(queue);
							showDownloadButton(DownloadType.PHOTO_GALLERY);
							
						}
					}
				}
			});
			
			resumeDownloadView(DownloadType.AUDIO_GUIDE);
			resumeDownloadView(DownloadType.PHOTO_GALLERY);
		}
		else{
			showDownloadComplete(DownloadType.AUDIO_GUIDE);
			showDownloadComplete(DownloadType.PHOTO_GALLERY);
			int selection = getArguments().getInt(ARG_SELECTION,0);
			
			if(selection==0)
				selectGallery();
			else
				selectAudioGuide();
		}
	}
	
	private void selectGallery(){
		btnGallery.performClick();
	}
	
	private void selectAudioGuide(){		
		btnAudioGuide.performClick();
	}
	
	private void downloadPhotoGallery(){
		AppIsoleReceiver.addToQueue(getActivity(), DownloadType.PHOTO_GALLERY,
				museum, langShort);
		showWaiting(DownloadType.PHOTO_GALLERY);
	}
	
	private void downloadAudioGuide(){
		AppIsoleReceiver.addToQueue(getActivity(), DownloadType.AUDIO_GUIDE,
				museum, langShort);
		showWaiting(DownloadType.AUDIO_GUIDE);
	}
	
	public void clickAudio(){
		btnAudioGuide.performClick();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
	private void resumeDownloadView(DownloadType downloadType) {
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		DownloadQueue queue = new DownloadQueue(museum, downloadType, langShort);
		int status = tableDownloadQueue.getStatus(queue);		
//		Log.d(TAG, "Status is "+status);
		switch (status) {
			case DownloadQueue.STATUS_WAITING:
				showWaiting(downloadType);
				break;
			case DownloadQueue.STATUS_INSTALLING:
				showInstalling(downloadType);
				break;
			case DownloadQueue.STATUS_DOWNLOADING:
				showDownloadProgress(downloadType);
				AppIsoleReceiver.resumeDownload(getActivity());
				break;
				
			case DownloadQueue.STATUS_COMPLETE:
				if(AppIsole.isInstalled(museum, getParentType(downloadType), langShort)){
					showDownloadComplete(downloadType);
				}
				else{
					showDownloadButton(downloadType);
				}
				break;

			case DownloadQueue.STATUS_PAUSE:
				showDownloadButton(downloadType,false);
				break;
			
			default:
				
				break;
		}
		
	}
	
	private void showProgress(DownloadType downloadType){
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnDownloadGallery.setVisibility(View.INVISIBLE);
				progressDownloadGallery.setVisibility(View.VISIBLE);
				progressDownloadGallery.spin();
				layoutProgressGallery.setVisibility(View.VISIBLE);
				detailGallery.setVisibility(View.INVISIBLE);
				isGalleryDownloadVisible = true;
				break;
			case AUDIO_GUIDE:
				btnDownloadAudioGuide.setVisibility(View.INVISIBLE);
				progressDownloadAudioGuide.setVisibility(View.VISIBLE);
				progressDownloadAudioGuide.spin();
				layoutProgressAudioGuide.setVisibility(View.VISIBLE);
				detailAudioGuide.setVisibility(View.INVISIBLE);
				isAudioDowloadVisible = true;
				break;
	
			default:
				break;
		}
	}
	
	private void showDownloadButton(DownloadType downloadType){
		showDownloadButton(downloadType, false);
	}
	
	private void showDownloadButton(DownloadType downloadType,boolean isPaused){
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnDownloadGallery.setVisibility(View.VISIBLE);
				progressDownloadGallery.setVisibility(View.INVISIBLE);
				layoutProgressGallery.setVisibility(View.INVISIBLE);
				detailGallery.setVisibility(View.INVISIBLE);
				this.isPausedGallery = isPaused;
				isGalleryDownloadVisible = false;
				disableGalleryClick();
				break;
			case AUDIO_GUIDE:
				btnDownloadAudioGuide.setVisibility(View.VISIBLE);
				progressDownloadAudioGuide.setVisibility(View.INVISIBLE);
				layoutProgressAudioGuide.setVisibility(View.INVISIBLE);
				detailAudioGuide.setVisibility(View.INVISIBLE);
				this.isPausedAudioGuide = isPaused;
				isAudioDowloadVisible = false;
				disableAudioClick();
				break;
	
			default:
				break;
		}
	}
	
	private void showDownloadComplete(DownloadType downloadType){
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnDownloadGallery.setVisibility(View.INVISIBLE);
				progressDownloadGallery.setVisibility(View.INVISIBLE);
				layoutProgressGallery.setVisibility(View.INVISIBLE);
				detailGallery.setVisibility(View.VISIBLE);
				this.isPausedGallery = false;
				isGalleryDownloadVisible = false;
				enableGalleryClick();
				break;
			case AUDIO_GUIDE:
				btnDownloadAudioGuide.setVisibility(View.INVISIBLE);
				progressDownloadAudioGuide.setVisibility(View.INVISIBLE);
				layoutProgressAudioGuide.setVisibility(View.INVISIBLE);
				detailAudioGuide.setVisibility(View.VISIBLE);
				this.isPausedAudioGuide = false;
				isAudioDowloadVisible = false;
				enableAudioClick();
				break;
	
			default:
				break;
		}
	}
	
	private void showWaiting(DownloadType downloadType){
		showProgress(downloadType);
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnPauseGallery.setVisibility(View.VISIBLE);
				btnPauseGallery.setTag(STATUS_CANCEL);
				disableGalleryClick();
				isGalleryDownloadVisible = false;
				break;
			case AUDIO_GUIDE:
				btnPauseAudioGuide.setVisibility(View.VISIBLE);
				btnPauseAudioGuide.setTag(STATUS_CANCEL);
				disableAudioClick();
				isAudioDowloadVisible = false;
				break;
			default:
				break;
		}
		
	}
	
	
	
	

	private void showDownloadProgress(DownloadType downloadType) {
		showProgress(downloadType);
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnPauseGallery.setVisibility(View.VISIBLE);
				btnPauseGallery.setTag(STATUS_PAUSE);
				break;
			case AUDIO_GUIDE:
				btnPauseAudioGuide.setVisibility(View.VISIBLE);
				btnPauseAudioGuide.setTag(STATUS_PAUSE);
				break;
			default:
				break;
		}
	}

	private void showInstalling(DownloadType downloadType) {
		showProgress(downloadType);
		switch (downloadType) {
			case PHOTO_GALLERY:
				btnPauseGallery.setVisibility(View.GONE);
				isGalleryDownloadVisible = false;
				break;
			case AUDIO_GUIDE:
				btnPauseAudioGuide.setVisibility(View.GONE);
				isAudioDowloadVisible = false;
				break;
			default:
				break;
		}
	}

	
	protected void cancel(DownloadType downloadType) {
		
		DownloadQueue queue = new DownloadQueue(museum, downloadType, langShort);
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		tableDownloadQueue.delete(queue);
		showDownloadButton(downloadType, false);
		
	}
	



	protected void pause(DownloadType downloadType) {
		switch (downloadType) {
			case PHOTO_GALLERY:
				AppIsoleReceiver.pausePGInstallation(getActivity(), museum, langShort);
				break;
			case AUDIO_GUIDE:
			case AUDIO_GUIDE_IMAGE:
			case MAP:
				AppIsoleReceiver.pauseAGInstallation(getActivity(), museum, langShort);
				break;
			default:
				break;
		}
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		tableDownloadQueue.delete(new DownloadQueue(museum,getParentType(downloadType) , langShort));
		showDownloadButton(downloadType, true);
	
	}
	
	private DownloadType getParentType(DownloadType downloadType){
		switch (downloadType) {
			case PHOTO_GALLERY:
				return DownloadType.PHOTO_GALLERY;
			case AUDIO_GUIDE:
			case AUDIO_GUIDE_IMAGE:
			case MAP:
				return DownloadType.AUDIO_GUIDE;
			default:
				break;
		}
		return null;
	}
	
	
	public void enableAudioClick(){
		btnAudioGuide.setEnabled(true);
		btnAudioGuide.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(museumItemClickListener!=null&&(AppIsole.isInstalled(museum, DownloadType.AUDIO_GUIDE, langShort)||isTablet)){
					if(museumItemClickListener.onMuseumItemClicked(DownloadType.AUDIO_GUIDE, museum, langShort,false)){
						btnGallery.setSelected(false);
						btnAudioGuide.setSelected(true);
						selection = 1;
					}
				}
			}
		});
		
	}
	
	public void disableAudioClick(){
		btnAudioGuide.setOnClickListener(null);
		btnAudioGuide.setEnabled(false);
	}
	
	public void enableGalleryClick(){
		btnGallery.setEnabled(true);
		btnGallery.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(museumItemClickListener!=null&&(AppIsole.isInstalled(museum, DownloadType.PHOTO_GALLERY, langShort)||isTablet)){
							if(museumItemClickListener.onMuseumItemClicked(DownloadType.PHOTO_GALLERY, museum, langShort,true)){
								btnGallery.setSelected(true);
								btnAudioGuide.setSelected(false);;
								selection = 0;
							}
						}
					}
				});
		
	}
	
	public void disableGalleryClick(){
		btnGallery.setEnabled(false);
		btnGallery.setOnClickListener(null);		
	}
	
	private void setCustomActionBar(LayoutInflater inflater,ViewGroup root,Museum museum) {
		// TODO Auto-generated method stub
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View actionBarView = inflater.inflate(R.layout.acbar_title_subtitle, root,false);
		actionBar.setCustomView(actionBarView);

		Toolbar toolbar=(Toolbar)actionBarView.getParent();
		toolbar.setContentInsetsAbsolute(0,0);
		toolbar.setContentInsetsAbsolute(0, 0);
		toolbar.getContentInsetEnd();
		toolbar.setPadding(0, 0, 0, 0);


		View btnLeft = actionBarView.findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		TextView fieldTitle = (TextView) actionBarView.findViewById(R.id.fieldTitle);
		fieldTitle.setText(museum.getNameId());
		
		fieldTitle.setVisibility(View.VISIBLE);
		btnLeft.setVisibility(View.VISIBLE);
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class MuseumItemReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			Log.e(TAG, "Inside receiver");
			if(action.equals(AppIsoleReceiver.ACTION_ISOLE_INSTALLATION)){
				
				int status 		 = BroadCastManager.getStatus(intent);
				Museum msgMuseum = BroadCastManager.getMuseum(intent);
				String msgLang   = BroadCastManager.getLanguage(intent);
				boolean isFree   = BroadCastManager.isFree(intent);
				DownloadType type = BroadCastManager.getDownloadType(intent);
				
//				Log.e(TAG, "Receiving installation status:-"+status+", museum:-"+msgMuseum.getShortName()+", language "+msgLang);
				if(msgMuseum.equals(museum)&&msgLang.equals(langShort)){
					switch (status) {
						case AppIsoleReceiver.STATE_INSTALLATION_SUCCESS:
							handleInstallationSuccess(type);
							break;
						case AppIsoleReceiver.STATE_INSTALLATION_STARTED:
							handleInstallationStart(type, isFree);
							break;
						case AppIsoleReceiver.STATE_INSTALLATION_FAILED:
							handleInstallationFailure(type);						
							break;
		
						default:
							break;
					}
				}
			}
			else if(action.equals(DownloadManager.ACTION_DOWNLOAD_STATUS)){
				handleDownloadIntents(intent);
			}
		}

		

		private void handleInstallationStart(DownloadType type,boolean isFree) {
			showWaiting(type);
		}

		private void handleInstallationSuccess(DownloadType type) {
			showDownloadComplete(type);
			
		}

		private void handleInstallationFailure(DownloadType type) {
			showDownloadButton(type);
		}
	}
	
	private void handleDownloadIntents(Intent intent) {
		
		String url 	= intent.getStringExtra(DownloaderIntents.URL); 
		
		if(!TextUtils.isEmpty(url)){
			
			Museum museum 	 = AppIsole.getMusuemFromURL(url);
			String language = AppIsole.getLanguageFromURL(url);
			
			if(this.museum==museum&&language.equals(langShort)){
				
				int type 	= intent.getIntExtra(DownloaderIntents.TYPE, -1);
				DownloadType downloadType = AppIsole.getDownloadTypeForUrl(url);
				
				switch (type) {
					case DownloaderIntents.Types.PROCESS:
						DownloadType current = getParentType(downloadType);
						switch (current) {
							case PHOTO_GALLERY:
								if(isPausedGallery)
									return;
							case AUDIO_GUIDE:
								if(isPausedAudioGuide)
									return;	
							default:
								break;
						}
						
						int progress = Integer.parseInt(intent
								.getStringExtra(DownloaderIntents.PROCESS_PROGRESS));
						
						handleDownloadProgress(downloadType,progress,museum,language);
						break;
					case DownloaderIntents.Types.ADD:
						handleDownloadAddition(downloadType);						
						break;
					case DownloaderIntents.Types.STOP:
						break;
					case DownloaderIntents.Types.COMPLETE:
						progressDownloadGallery.spin();
						break;
		
					default:
						break;
				}
			}
		}
	}
	
	
	private void handleDownloadAddition(DownloadType downloadType) {
		showDownloadProgress(getParentType(downloadType));
		
	}

	private void handleDownloadProgress(DownloadType downloadType,float percentage,Museum museum ,String language) {
		float downloadedSize = 0;
		float totalSize = AppIsole.getTotalAudioSize(museum, language);
		switch (downloadType) {
			case PHOTO_GALLERY:
				if(percentage==0||percentage==100){
					showInstalling(DownloadType.PHOTO_GALLERY);
					return;
				}
				if(!isGalleryDownloadVisible){
					showDownloadProgress(DownloadType.PHOTO_GALLERY);
				}
				progressDownloadGallery.setProgress((int) (percentage/100f*360));
				break;
			case MAP:
				if(percentage==0||progressDownloadAudioGuide.getProgress()>359){
					showInstalling(DownloadType.AUDIO_GUIDE);
					return;
				}
				if(!isAudioDowloadVisible){
					showDownloadProgress(DownloadType.AUDIO_GUIDE);				
				}
				downloadedSize = AppIsole.getMapZipSize(museum)*percentage/100f;
				progressDownloadAudioGuide.setProgress((int) (downloadedSize/totalSize*360));
				break;
			case AUDIO_GUIDE_IMAGE:
				if(percentage==0||progressDownloadAudioGuide.getProgress()>359){
					showInstalling(DownloadType.AUDIO_GUIDE);
					return;
				}
				if(!isAudioDowloadVisible){
					showDownloadProgress(DownloadType.AUDIO_GUIDE);	
				}
				downloadedSize = AppIsole.getMapZipSize(museum)
						+(AppIsole.getAudioGalZipSize(museum)*percentage/100f);
				
				progressDownloadAudioGuide.setProgress((int) (downloadedSize/totalSize*360));
				break;
			case AUDIO_GUIDE:
				if(percentage==0||progressDownloadAudioGuide.getProgress()>359){
					showInstalling(DownloadType.AUDIO_GUIDE);
					return;
				}
				if(!isAudioDowloadVisible){
					showDownloadProgress(DownloadType.AUDIO_GUIDE);	
				}
				downloadedSize = AppIsole.getMapZipSize(museum)+AppIsole.getAudioGalZipSize(museum)
						+(AppIsole.getAudioZipSize(museum,language)*percentage/100f);
				progressDownloadAudioGuide.setProgress((int) (downloadedSize/totalSize*360));
				break;
			
	
			default:
				break;
		}
	}

	public interface OnMuseumItemClickListener{
		public boolean onMuseumItemClicked(DownloadType type
				,Museum museum,String language,boolean isFree);
		public void onBuyMuseumFromMuseumItem(Museum museum);
		
	}

	public void update(Museum museum,String langShort,boolean tablet) {
		View root = getView();
		if(root!=null){
		
			populateBundle(getArguments(),museum, langShort, tablet);
			refreshView(root);
		}
	}

	
	
	
	
}
