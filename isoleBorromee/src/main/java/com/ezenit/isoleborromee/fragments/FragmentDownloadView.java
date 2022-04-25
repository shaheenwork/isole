package com.ezenit.isoleborromee.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ezenit.isoleborromee.R;
import com.todddavies.components.progressbar.ProgressWheel;

public class FragmentDownloadView extends Fragment{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String ARG_MUSEUM 	= "com.ezenit.isoleborromee.fragments.FragmentDownloadView.ARG_MUSUEM";
	private static final String ARG_LANG 	= "com.ezenit.isoleborromee.fragments.FragmentDownloadView.ARG_LANG";
	private static final String ARG_TYPE	= "com.ezenit.isoleborromee.fragments.FragmentDownloadView.ARG_TYPE";
		
	private static final String TAG         = FragmentDownloadView.class.getName();
	
	private static final float  BAR_FULL    	= 360.0f;
	private static final float  PERCENTAGE_FULL = 100.0f;
	
	// ===========================================================
	// Fields
	// ===========================================================
		
	
	private Museum 		 museum;
	private String 		 langShort;
	private DownloadType downloadType;
	
	private View		  layoutProgress;
	private View		  layoutProgressText;
	private Button		  btnPause;
	private ProgressWheel progressDownload;
	private TextView	  fieldProgress;
	private TextView	  fieldStatus;
	private TextView	  fieldType;
	
	private boolean 	  isProgressVisible;
	private boolean 	  isProgressTextVisible;
	
	private MuseumItemReceiver receiver;
	
	private TextView      fieldTitle;
//	private TextView	  fieldDescription;
	private View		  layoutBuy;
	
	private FragmentDownloadListener fragmentDownloadListener;
	private boolean 	  isPaused;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static FragmentDownloadView getInstance(Museum museum,String language,DownloadType type){
		
		FragmentDownloadView fragment = new FragmentDownloadView();
		
		Bundle args = new Bundle();
		args.putString(ARG_LANG, language);
		args.putString(ARG_MUSEUM, museum.getShortName());
		args.putString(ARG_TYPE, type.name());
		fragment.setArguments(args);
		
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
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            this.fragmentDownloadListener = (FragmentDownloadListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentDownloadListener");
        }
		
	}
	
	public void update(Museum museum, String language,
			DownloadType downloadType) {
		Bundle args = getArguments();
		if(args!=null&&getView()!=null){
//			boolean refreshDownload = args.getInt(ARG_TYPE)!=downloadType.ordinal();
			args.putString(ARG_LANG, language);
			args.putString(ARG_MUSEUM, museum.getShortName());
			args.putString(ARG_TYPE, downloadType.name());
			updateView(getView());
			
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if(getView()!=null){
			langShort = AppIsole.getAppLocaleAsStr();
			updateView(getView());
//			updateDownloadButtons();
//			updateTypeField();
//			resumeDownloadView();
		}
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		receiver = new MuseumItemReceiver();
		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_STATUS);
		filter.addAction(AppIsoleReceiver.ACTION_ISOLE_INSTALLATION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		getActivity().registerReceiver(receiver, filter);
		
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(receiver!=null)
			getActivity().unregisterReceiver(receiver);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fr_download_view, container,false);
		updateView(root);
		
		
		return root;
	}
	
	private void updateView(View root){
		museum  			= Museum.getMuseum(getArguments().getString(ARG_MUSEUM));
		langShort 			= getArguments().getString(ARG_LANG);
		downloadType		= DownloadType.valueOf(getArguments().getString(ARG_TYPE));
		
		layoutProgress 	   	= root.findViewById(R.id.layoutProgress);
		layoutProgressText 	= root.findViewById(R.id.layoutInner);
		btnPause  	 		= (Button) root.findViewById(R.id.btnPause);
		progressDownload 	= (ProgressWheel) root.findViewById(R.id.progressBar);
		fieldProgress		= (TextView) root.findViewById(R.id.fieldProgress);
		fieldStatus 	 	= (TextView) root.findViewById(R.id.fieldStatus);
		fieldType		 	= (TextView) root.findViewById(R.id.fieldType);
		
		fieldTitle = (TextView) root.findViewById(R.id.fieldTitle);
//		fieldDescription = (TextView) root.findViewById(R.id.fieldDescription);
		layoutBuy  = root.findViewById(R.id.layoutBuy);
		
		updateDownloadButtons(root);
		updateTypeField();
		resumeDownloadView();
		
//		showDownloadButton();
		final String download = getResources().getString(R.string.download);
		final String cancel   = getResources().getString(R.string.cancel);
		final String pause	= getResources().getString(R.string.pause);
		
		btnPause.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(btnPause.getText().toString().equals(pause)){
					pause(downloadType);
				}
				else if(btnPause.getText().toString().equals(cancel)){
					cancel(downloadType);
				}
				
			}
		});
	}
	
	public void updateDownloadButtons(){
		View root = getView();
		if(root!=null)
			updateDownloadButtons(root);
	}
	
	private void updateDownloadButtons(View root){
		if(downloadType.equals(DownloadType.PHOTO_GALLERY))
			updatePhotoGalleryBuyButton(root);
		else
			updateAudioGuideBuyButton(root);
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	private void updatePhotoGalleryBuyButton(View root){
		
//		String  msgDescription = getResources().getString(R.string.msg_download_gallery);
//		String  photoGallery   = getResources().getString(R.string.photo_gallery);
//		String  museumName     = getResources().getString(museum.getNameId());
		fieldTitle.setText(R.string.photo_gallery);
		
		String msgDownloadAll = getResources().getString(R.string.msg_download_gallery);
		
//		fieldDescription.setText(msgDescription+" "+museumName);
		
		TextView fieldBuyMusuem = (TextView) root.findViewById(R.id.fieldBuyMuseum);
		fieldBuyMusuem.setText(msgDownloadAll);
		
		Button btnBuyMuseum = (Button) root.findViewById(R.id.btnBuyMuseum);
		btnBuyMuseum.setText(R.string.download);
		btnBuyMuseum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadPhotoGallery();
			}
		});
		
		root.findViewById(R.id.layoutBuyAll).setVisibility(View.GONE);
		root.findViewById(R.id.divider1).setVisibility(View.GONE);
	}
	
	private void updateAudioGuideBuyButton(View root) {
		
		root.findViewById(R.id.layoutBuyAll).setVisibility(View.VISIBLE);
		root.findViewById(R.id.divider1).setVisibility(View.VISIBLE);
		Button btnBuyMuseum = (Button) root.findViewById(R.id.btnBuyMuseum);
		
				//////////////anu changed
		if(!AppIsole.isPurchased(museum)){
			String price = AppIsole.getPrice(getActivity(), museum);
			btnBuyMuseum.setText(price);
			btnBuyMuseum.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					fragmentDownloadListener.onBuyMuseumClicked(museum);
				}
			});
		}
		else{
			
			btnBuyMuseum.setText(R.string.download);
			btnBuyMuseum.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					downloadAudioGuide();
				}
			});
		}
//		String  museumName     = getResources().getString(museum.getNameId());
		/// 
////anisha code change
		String buyAudioGuide =  "";
		TextView fieldBuyMusuem = (TextView) root.findViewById(R.id.fieldBuyMuseum);
		if(museum.equals(Museum.MUSEUM_ISOLA_BELLA)){
			buyAudioGuide =  getResources().getString(R.string.msg_buy_ag_isole_bella);
			
		}
		////adddddd
		else if(museum.equals(Museum.MUSEUM_ISOLA_MADRE)){
			buyAudioGuide =  getResources().getString(R.string.msg_buy_ag_isole_madre);
			
		}
		fieldBuyMusuem.setText(buyAudioGuide);
		
//		if(!AppIsole.isSingleItemPurchsed()){
//			fieldTitle.setText(R.string.special_offer);
//			fieldDescription.setText(R.string.msg_buy_all);
//			
//			Button btnBuyAll = (Button) root.findViewById(R.id.btnBuyAll);
//			
//			String priceAll = AppIsole.getPrice(getActivity(), Museum.MUSEUM_ALL_IN_ONE);
//			btnBuyAll.setText(priceAll);
//			
//			btnBuyAll.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					fragmentDownloadListener.onBuyAllClicked();
//				}
//			});
//		}
//		else{
		
//			fieldTitle.setText(R.string.audio_guide);
			String title = getResources().getString(R.string.msg_title_palazzo);
			fieldTitle.setText(title);
			
//			fieldDescription.setText(R.string.msg_title_palazzo);
			root.findViewById(R.id.layoutBuyAll).setVisibility(View.GONE);
			root.findViewById(R.id.divider1).setVisibility(View.GONE);
//		}
	}

	protected void cancel(DownloadType downloadType) {
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		DownloadQueue queue = new DownloadQueue(museum, downloadType, langShort);
		tableDownloadQueue.delete(queue);
		showDownloadButton(false);
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
		showDownloadButton(true);
		
	
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
	
	

	private void download(DownloadType downloadType){
		switch (downloadType) {
		 	case PHOTO_GALLERY:
				downloadPhotoGallery();
				break;
			case AUDIO_GUIDE:
			case AUDIO_GUIDE_IMAGE:
			case MAP:
				downloadAudioGuide();
				break;
	
			default:
				break;
		}
		isPaused = false;
	}
	
	private void resumeDownloadView() {
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		DownloadQueue queue = new DownloadQueue(museum, downloadType, langShort);
		int status = tableDownloadQueue.getStatus(queue);	
		
		switch (status) {
			case DownloadQueue.STATUS_WAITING:
				showWaiting();
				break;
			case DownloadQueue.STATUS_INSTALLING:
				showInstalling();
				break;
			case DownloadQueue.STATUS_DOWNLOADING:
				showDownloadProgress();
				AppIsoleReceiver.resumeDownload(getActivity());
				break;
				
			case DownloadQueue.STATUS_COMPLETE:
				showDownloadButton(false);
				break;

			case DownloadQueue.STATUS_PAUSE:
				showDownloadButton(false);
				break;
			
			default:
				
				break;
		}
		
	}
	
	private void updateTypeField(){
		if(downloadType==DownloadType.PHOTO_GALLERY){
			fieldType.setText(R.string.photo_gallery);
		}
		else{
			fieldType.setText(R.string.audio_guide);
		}
	}

	private void showDownloadProgress() {
		showDownloadLayout(false);
		layoutProgress.setVisibility(View.VISIBLE);
		btnPause.setText(R.string.pause);
		progressDownload.setVisibility(View.VISIBLE);
		layoutProgressText.setVisibility(View.VISIBLE);
		isProgressVisible = true;
		btnPause.setVisibility(View.VISIBLE);
		
		
	}
	
	private void showInstalling(){
		showDownloadLayout(false);
		layoutProgress.setVisibility(View.VISIBLE);
		btnPause.setVisibility(View.GONE);
		fieldStatus.setText(R.string.installing);
		layoutProgressText.setVisibility(View.GONE);
		progressDownload.setVisibility(View.VISIBLE);
		
		progressDownload.spin();
		isProgressVisible = false;
		isProgressTextVisible = false;
	}
	
	private void showWaiting() {
		showDownloadLayout(false);
		layoutProgress.setVisibility(View.VISIBLE);
		btnPause.setVisibility(View.VISIBLE);
		btnPause.setText(R.string.cancel);
		fieldStatus.setText(R.string.queueing);
		layoutProgressText.setVisibility(View.GONE);
		progressDownload.setVisibility(View.VISIBLE);
		
		progressDownload.spin();
		isProgressVisible = false;
		isProgressTextVisible = false;
		
	}
	
	private void showDownloadingProgressText() {
		layoutProgressText.setVisibility(View.VISIBLE);
		fieldStatus.setText(R.string.downloading);
		isProgressTextVisible = true;
	}

	private void showDownloadButton(boolean isPaused){
		layoutProgress.setVisibility(View.GONE);
//		btnDownload.setText(R.string.download);
		showDownloadLayout(true);
		progressDownload.setVisibility(View.INVISIBLE);
		isProgressVisible = false;
		this.isPaused     = isPaused;
	}
	
	private void showDownloadLayout(boolean show){
		int visibility = View.VISIBLE;
		if(!show){
			visibility = View.GONE;
			btnPause.setVisibility(View.VISIBLE);
		}
		else{
			View root = getView();
			if(root!=null)
				updateDownloadButtons(root);
			btnPause.setVisibility(View.GONE);
		}
		
//		fieldDescription.setVisibility(visibility);
		fieldTitle.setVisibility(visibility);
		layoutBuy.setVisibility(visibility);
	}
	
	private void downloadPhotoGallery(){
//		AppIsoleReceiver.startPGInstallation(getActivity(), museum, langShort);
		AppIsoleReceiver.addToQueue(getActivity(), DownloadType.PHOTO_GALLERY, museum, langShort);
		showWaiting();
	}
	
	private void downloadAudioGuide(){
//		AppIsoleReceiver.startAGInstallation(getActivity(), museum, langShort);
		AppIsoleReceiver.addToQueue(getActivity(), DownloadType.AUDIO_GUIDE, museum, langShort);
		showWaiting();
	}
			
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public interface FragmentDownloadListener{
		public void onDownloadCompleted(Museum museum,String language,DownloadType type);
		public void onBuyMuseumClicked(Museum museum);
		public void onBuyAllClicked();
	}
	
	
	
	
	
	private class MuseumItemReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(AppIsoleReceiver.ACTION_ISOLE_INSTALLATION)){
				
				int status 		 = BroadCastManager.getStatus(intent);
				Museum msgMuseum = BroadCastManager.getMuseum(intent);
				String msgLang   = BroadCastManager.getLanguage(intent);
				boolean isFree   = BroadCastManager.isFree(intent);
				DownloadType type = BroadCastManager.getDownloadType(intent);
				
//				Log.e(TAG, "Receiving installation status:-"+status+", museum:-"+msgMuseum.getShortName()+", language "+msgLang
//						+", type "+downloadType);
//				
//				Log.i(TAG, "Current type "+downloadType+", receiving type "+type);
				
				if(msgMuseum.equals(museum)
						&&msgLang.equals(langShort)
						&&type==downloadType){
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
			switch(type){
				case AUDIO_GUIDE:
					if(!isFree){
						showDownloadProgress();
						progressDownload.spin();
					}
					break;
				case PHOTO_GALLERY:
					showDownloadProgress();
					progressDownload.spin();
					break;	
				default:
					break;
			}
		}

		private void handleInstallationSuccess(DownloadType type) {
			if(fragmentDownloadListener!=null)
				fragmentDownloadListener.onDownloadCompleted(museum, langShort, type);
		}

		private void handleInstallationFailure(DownloadType type) {
			showDownloadButton(false);
		}
	}

			
		private void handleDownloadIntents(Intent intent) {
			
			String url 	= intent.getStringExtra(DownloaderIntents.URL); 
			
			if(!TextUtils.isEmpty(url)){
				
				Log.e(TAG, "Receiving download url "+url);
				Museum museum 	 = AppIsole.getMusuemFromURL(url);
				String language = AppIsole.getLanguageFromURL(url);
				
				if(this.museum==museum&&language.equals(langShort)){
					
					int type 	= intent.getIntExtra(DownloaderIntents.TYPE, -1);
					DownloadType downloadType = AppIsole.getDownloadTypeForUrl(url);
//					Log.e(TAG, "Receiving download type:-"+downloadType+", museum:-"
//								+museum.getShortName()+", language "+language
//								+" url, "+url+" type of process "+type);
					if(this.downloadType==convertToBaseDownloadType(downloadType)){
						switch (type) {
							case DownloaderIntents.Types.PROCESS:
								if(!isPaused){
									int progress = Integer.parseInt(intent
											.getStringExtra(DownloaderIntents.PROCESS_PROGRESS));
									handleDownloadProgress(downloadType,progress,museum,language);
								}
								break;
							case DownloaderIntents.Types.ADD:
//								handleDownloadAddition(downloadType);						
								break;
							case DownloaderIntents.Types.STOP:
								break;
							case DownloaderIntents.Types.COMPLETE:
								progressDownload.spin();
								break;
				
							default:
								break;
						}
					}
				}
			}
		}
		
		public DownloadType convertToBaseDownloadType(DownloadType type){
			switch (type) {
				case PHOTO_GALLERY:
					return DownloadType.PHOTO_GALLERY;
				case AUDIO_GUIDE:
				case MAP:
				case AUDIO_GUIDE_IMAGE:
					return DownloadType.AUDIO_GUIDE;
			}
			return null;
		}
		
//		private void handleDownloadAddition(DownloadType downloadType) {
//			// TODO Auto-generated method stub
//			switch (downloadType) {
//				case PHOTO_GALLERY:
//					showDownloadProgress();
//					break;
//				case AUDIO_GUIDE:
//				case AUDIO_GUIDE_IMAGE:
//				case MAP:
//					showDownloadProgress();
//					break;
//		
//				default:
//					break;
//			}
//			
//		}

		private void handleDownloadProgress(DownloadType downloadType,float percentage,Museum museum ,String language) {
			float downloadedSize = 0;
			float totalSize = AppIsole.getTotalAudioSize(museum, language);
			float progress = 0;
			switch (downloadType) {
				case PHOTO_GALLERY:
					if(percentage==0||percentage==100){
						showInstalling();
						return;
					}
					if(!isProgressVisible)
						showDownloadProgress();
					
					if(!isProgressTextVisible)
						showDownloadingProgressText();
					fieldProgress.setText(String.format("%02d", (int)(percentage)));
					progressDownload.setProgress((int) (percentage/100f*360));
					fieldType.setText(R.string.photo_gallery);
					break;
				case MAP:
					if(percentage==0||progressDownload.getProgress()>359){
						showInstalling();
						return;
					}
					if(!isProgressVisible)
						showDownloadProgress();
					
					if(!isProgressTextVisible)
						showDownloadingProgressText();
					
					layoutProgressText.setVisibility(View.VISIBLE);
					downloadedSize = AppIsole.getMapZipSize(museum)*percentage/100f;
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100)));
					progressDownload.setProgress((int) (progress*360));
					fieldType.setText(R.string.audio_guide);
					break;
				case AUDIO_GUIDE_IMAGE:
					if(percentage==0||progressDownload.getProgress()>359){
						showInstalling();
						return;
					}
					if(!isProgressVisible)
						showDownloadProgress();
					
					if(!isProgressTextVisible)
						showDownloadingProgressText();
					
					layoutProgressText.setVisibility(View.VISIBLE);
					downloadedSize = AppIsole.getMapZipSize(museum)
							+(AppIsole.getAudioGalZipSize(museum)*percentage/100f);
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100)));
					progressDownload.setProgress((int) (progress*360));
					fieldType.setText(R.string.audio_guide);
					break;
				case AUDIO_GUIDE:
					if(percentage==0||percentage>99){
						showInstalling();
						return;
					}
					if(!isProgressVisible)
						showDownloadProgress();
					
					if(!isProgressTextVisible)
						showDownloadingProgressText();
						
				
					downloadedSize = AppIsole.getMapZipSize(museum)+AppIsole.getAudioGalZipSize(museum)
							+(AppIsole.getAudioZipSize(museum,language)*percentage/100f);
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100)));
					progressDownload.setProgress((int) (progress*360));
					fieldType.setText(R.string.audio_guide);
					break;
				
		
				default:
					break;
			}
		}

		
		

	
			
}
