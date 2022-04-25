package com.ezenit.isoleborromee.fragments;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class FragmentDownloadViewer extends DialogFragment{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	
	private ArrayList<DownloadQueue> queue;
	private ProgressBar				 progressBar;
	private Button					 btnCancel;
	
	private TextView				fieldQueue;
	private View					layoutQueue;
	private TextView				fieldProgress;
	private TextView				fieldStatus;
	
	private String					installing;
	private String					downloading;
	
	private String					audioGuide;
	private String					photoGallery;
	
	private DownloadQueueReceiver   receiver;
	
	
		
	// ===========================================================
	// Constructors
	// ===========================================================
	public static FragmentDownloadViewer getInstance(){
		FragmentDownloadViewer fragment = new FragmentDownloadViewer();
		return fragment;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View root = inflater.inflate(R.layout.fr_downloader, container,false);
		
		queue = new ArrayList<TableDownloadQueue.DownloadQueue>();
		progressBar = (ProgressBar) root.findViewById(R.id.progressbar);
		progressBar.setMax(100);
		
		
		btnCancel = (Button) root.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppIsoleReceiver.pauseAndClearDownload(getActivity());
				dismiss();				
			}
		});
		
		installing = getResources().getString(R.string.installing);
		downloading = getResources().getString(R.string.downloading);
		
		audioGuide = getResources().getString(R.string.audio_guide);
		photoGallery = getResources().getString(R.string.photo_gallery);
		
		layoutQueue = root.findViewById(R.id.layoutQueue);
		fieldStatus = (TextView) root.findViewById(R.id.fieldStatus);
		fieldQueue  = (TextView) root.findViewById(R.id.fieldQueue);
		fieldProgress = (TextView) root.findViewById(R.id.fieldProgress);
		
		return root;
	
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateQueueInfo();
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		receiver = new DownloadQueueReceiver();
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
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void updateQueueInfo(){
		
		DownloadQueue queue = TableDownloadQueue.getTopQueue();
		if(queue==null){
			dismiss();
			return;
		}
		
		AppIsoleReceiver.resumeDownload(getActivity());
		
	}
	
	private void updateNextDownload(){
		TableDownloadQueue	tableDownloadQueue = new TableDownloadQueue();
		DownloadQueue nextQueue = tableDownloadQueue.getNextInQueue();
		if(nextQueue!=null){
			layoutQueue.setVisibility(View.VISIBLE);
			String inQueue = getResources().getString(R.string.in_queue).toLowerCase(Locale.UK);
			
			String typeStr = getTypeStr(nextQueue.getType());
			String musuem   = getResources().getString(nextQueue.getMusuem().getNameId());
			fieldQueue.setText(musuem+" "+typeStr+" "+inQueue);
		}
		else{
			layoutQueue.setVisibility(View.GONE);
		}
	}
	
	private String getTypeStr(DownloadType type){
		switch (type) {
			case AUDIO_GUIDE:
				return getResources().getString(R.string.audio_guide);
			case PHOTO_GALLERY:
				return getResources().getString(R.string.photo_gallery);
	
			default:
				break;
		}
		return "";
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private class DownloadQueueReceiver extends BroadcastReceiver{

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
				
				
				switch (status) {
					case AppIsoleReceiver.STATE_INSTALLATION_SUCCESS:
						handleInstallationSuccess(type);
						break;
					case AppIsoleReceiver.STATE_INSTALLATION_STARTED:
						handleInstallationStart(type, isFree);
						break;
					case AppIsoleReceiver.STATE_INSTALLATION_FAILED:
						handleInstallationFailure(type,msgMuseum,msgLang);						
						break;

					default:
						break;
				}
				
			}
			else if(action.equals(DownloadManager.ACTION_DOWNLOAD_STATUS)){
				handleDownloadIntents(intent);
			}
		}
		
		private void handleDownloadIntents(Intent intent) {
			
			String url 	= intent.getStringExtra(DownloaderIntents.URL); 
			
			if(!TextUtils.isEmpty(url)){
				
				Log.e("url", "Receiving download url "+url);
				Museum museum 	 = AppIsole.getMusuemFromURL(url);
				String language = AppIsole.getLanguageFromURL(url);
				
					
				int type 	= intent.getIntExtra(DownloaderIntents.TYPE, -1);
				DownloadType downloadType = AppIsole.getDownloadTypeForUrl(url);
//					Log.e(TAG, "Receiving download type:-"+downloadType+", museum:-"
//								+museum.getShortName()+", language "+language
//								+" url, "+url+" type of process "+type);
				switch (type) {
					case DownloaderIntents.Types.PROCESS:
//						if(!isPaused){
						int progress = Integer.parseInt(intent
								.getStringExtra(DownloaderIntents.PROCESS_PROGRESS));
						handleDownloadProgress(downloadType,progress,museum,language);
//						}
						break;
					case DownloaderIntents.Types.ADD:
					
//								handleDownloadAddition(downloadType);	
						updateNextDownload();
						break;
					case DownloaderIntents.Types.STOP:
						break;
					case DownloaderIntents.Types.COMPLETE:
						progressBar.setIndeterminate(true);
						break;
		
					default:
						break;
				}
				}
			}
		}

		

		private void handleInstallationStart(DownloadType type,boolean isFree) {
			switch(type){
				case AUDIO_GUIDE:
					if(!isFree){
						progressBar.setIndeterminate(true);
					}
					break;
				case PHOTO_GALLERY:
					progressBar.setIndeterminate(true);
					break;	
				default:
					break;
			}
		}

		private void handleInstallationSuccess(DownloadType type) {
			updateQueueInfo();
		}

		private void handleInstallationFailure(DownloadType type, Museum msgMuseum, String msgLang) {
			String installationFailed = getResources().getString(R.string.installation_failed);
			Toast.makeText(getActivity(),installationFailed+" "+msgMuseum , Toast.LENGTH_SHORT).show();
			DownloadQueue queue = new DownloadQueue(msgMuseum, type, msgLang);
			TableDownloadQueue		tableDownloadQueue = new TableDownloadQueue();
			tableDownloadQueue.delete(queue);
			updateQueueInfo();
			
		}
		
		private void handleDownloadProgress(DownloadType downloadType,float percentage,Museum museum ,String language) {
			float downloadedSize = 0;
			float totalSize = AppIsole.getTotalAudioSize(museum, language);
			float progress = 0;
			switch (downloadType) {
				case PHOTO_GALLERY:
					if(percentage==0||percentage==100){
						progressBar.setIndeterminate(true);
						fieldStatus.setText(installing+" "+photoGallery);
						fieldProgress.setVisibility(View.INVISIBLE);
						return;
					}
					
					fieldProgress.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(false);
					fieldProgress.setText(String.format("%02d", (int)(percentage))+"%");
					progressBar.setProgress((int) (percentage));
					fieldStatus.setText(downloading+" "+photoGallery);
					break;
				case MAP:
					if(percentage==0||percentage>99){
						progressBar.setIndeterminate(true);
						fieldStatus.setText(installing+" "+audioGuide);
						fieldProgress.setVisibility(View.INVISIBLE);
						return;
					}
					
					fieldProgress.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(false);
					downloadedSize = AppIsole.getMapZipSize(museum)*percentage/100f;
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100))+"%");
					progressBar.setProgress((int) (progress*100));
					fieldStatus.setText(downloading+" "+audioGuide);
					break;
				case AUDIO_GUIDE_IMAGE:
					if(percentage==0||percentage>99){
						progressBar.setIndeterminate(true);
						fieldStatus.setText(installing+" "+audioGuide);
						fieldProgress.setVisibility(View.INVISIBLE);
						return;
					}
					
					fieldProgress.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(false);
					downloadedSize = AppIsole.getMapZipSize(museum)
							+(AppIsole.getAudioGalZipSize(museum)*percentage/100f);
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100))+"%");
					progressBar.setProgress((int) (progress*100));
					fieldStatus.setText(downloading+" "+audioGuide);
					
					break;
				case AUDIO_GUIDE:
					if(percentage==0||percentage>99){
						progressBar.setIndeterminate(true);
						fieldStatus.setText(installing+" "+audioGuide);
						fieldProgress.setVisibility(View.INVISIBLE);
						Log.d("percentage", "Bibin"+percentage);
						Log.d("install", "Bibin");
						if(percentage==100){
							dismiss();
							Log.d("Dissmiss", "Bibin");
						}else
				
						return;
					}
				
					fieldProgress.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(false);
					downloadedSize = AppIsole.getMapZipSize(museum)+AppIsole.getAudioGalZipSize(museum)
							+(AppIsole.getAudioZipSize(museum,language)*percentage/100f);
					progress = downloadedSize/totalSize;
					fieldProgress.setText(String.format("%02d", (int)(progress*100))+"%");
					progressBar.setProgress((int) (progress*100));
					fieldStatus.setText(downloading+" "+audioGuide);
			
					break;
				
		
				default:
					break;
			}
		}
	
}
