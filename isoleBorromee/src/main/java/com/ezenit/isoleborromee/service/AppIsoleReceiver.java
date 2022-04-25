package com.ezenit.isoleborromee.service;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ezenit.download.services.DownloadManager;
import com.ezenit.download.services.DownloadService;
import com.ezenit.download.utils.DownloaderIntents;
import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.AppIsole.AudioGuideInstallationState;
import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.AppIsole.InstallationState;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue;
import com.ezenit.isoleborromee.db.table.TableImgRel;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue.DownloadQueue;
import com.ezenit.isoleborromee.db.table.TableImgRel.Image;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.net.IsNetHelper;
import com.ezenit.utils.LanguageConstants;
import com.ezenit.zip.ZipOperation;
import com.ezenit.zip.ZipOperation.OnFileExtractedListener;
/***
 * Handles all the local broadcast associated with isole package
 * installation
 * */
public class AppIsoleReceiver extends BroadcastReceiver{


	// ===========================================================
	// Constants
	// ===========================================================
	public static final String ACTION_ISOLE_INSTALLATION  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_ISOLE_INSTALLATION";
	
	public static final String ACTION_DOWNLOADHELPER_RESPONSE = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_DOWNLOADHELPER_RESPONSE";
	
	public static final String ACTION_PARSE_XML 		  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_PARSE_XML";
	public static final String ACTION_EXTRACT_ZIP 		  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_EXTRACT_ZIP";
	public static final String ACTION_DOWNLOAD_ZIP		  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_DOWNLOAD_ZIP";
	public static final String ACTION_CALCULATE_ZIP_SIZE  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.ACTION_CALCULATE_ZIP_SIZE";
	
	public static final String EXTENDED_DATA_STATUS   	  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_DATA_STATUS";
	public static final String EXTENDED_DATA_MUSEUM   	  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_DATA_MUSEUM";
	public static final String EXTENDED_DATA_LANGUAGE 	  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_DATA_LANGUAGE";
	public static final String EXTENDED_DATA_ISFREE   	  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_DATA_ISFREE";
	public static final String EXTENDED_DATA_TYPE   	  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_DATA_TYPE";
	public static final String EXTENDED_ID   	  		  = "com.ezenit.isoleborrome.service.AppIsoleReceiver.EXTENDED_ID";
	
	public static final int STATE_INSTALLATION_SUCCESS = 121;
	public static final int STATE_INSTALLATION_FAILED  = 122;
	public static final int STATE_INSTALLATION_STARTED = 123;
	
	private static final String TAG = AppIsoleReceiver.class.getName();
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public static void addToQueue(Context context,DownloadType type,Museum museum, String language){
			
		DownloadQueue  queue = new DownloadQueue(museum, type, language);
		TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
		tableDownloadQueue.insertOrUpdate(queue);		
		
		resumeDownload(context);
	}
	
	public static void resumeDownload(Context context){
			
		DownloadQueue 	queue = TableDownloadQueue.getTopQueue();
	
		if(queue!=null){
			switch (queue.getType()) {
				case AUDIO_GUIDE:
				case AUDIO_GUIDE_IMAGE:
				case MAP:
					if(AppIsole.isInstalled(queue.getMusuem(), queue.getType(), queue.getLanguage())){
						TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
						tableDownloadQueue.delete(queue);
						resumeDownload(context);
						return;
					}
					startAGInstallation(context, queue.getMusuem(),queue.getLanguage());
					break;
				case PHOTO_GALLERY:
					if(AppIsole.isInstalled(queue.getMusuem(), queue.getType(), queue.getLanguage())){
						TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
						tableDownloadQueue.delete(queue);
						resumeDownload(context);
						return;
					}
					startPGInstallation(context, queue.getMusuem(),queue.getLanguage());
					break;
				default:
					break;
	
			}
		}
	}
	
	public static void pauseAndClearDownload(Context context) {
		
		DownloadQueue 	queue = TableDownloadQueue.getTopQueue();
		if(queue!=null){
			switch (queue.getType()) {
				case AUDIO_GUIDE:
					pauseAGInstallation(context, queue.getMusuem(), queue.getLanguage());
					break;
				case PHOTO_GALLERY:
					pausePGInstallation(context, queue.getMusuem(), queue.getLanguage());
					break;
				default:
					break;

			}
		}
		TableDownloadQueue.deleteAll();
	}
	
	public static void pausePGInstallation(Context context,Museum museum, String language){
		DownloadService.pauseDownload(context, IsNetHelper.getPhotoGalleryZipURL(museum));
	}
	
	public static void pauseAGInstallation(Context context,Museum museum, String language){
		AudioGuideInstallationState state = AppIsole.getAudioGuideInstallationState(museum, language);
		DownloadType type = state.downloadType;
		switch(type){
			case AUDIO_GUIDE:
				DownloadService.pauseDownload(context, IsNetHelper.getAudioZipURL(museum, language, false));
				break;
			case AUDIO_GUIDE_IMAGE:
				DownloadService.pauseDownload(context, IsNetHelper.getAudioGalleryZipURL(museum, false));
				break;
			case MAP:
				DownloadService.pauseDownload(context, IsNetHelper.getMapZipURL(museum));
				break;
			case PHOTO_GALLERY:
				break;
		}
		
	}
	
	/*****************************************************************************
	 * Installs the audio guide related to a museum and language. 
	 * The installation can paused and resumed.
	 * Hence the state of installation is tracked.
	 * Steps involved are in the following order
	 *  <br> 1. Parse the gallery XML
	 *  <br> 2. Download the photo gallery zip
	 *  <br> 3. Extract the photo gallery zip.
	 *  
	 *  @param context Application context
	 *  @param museum Museum whose audio guide is to be installed
	 *  @param language language of the audio guide to be used
	 * 
	 * ***************************************************************************/	  
	public static void startPGInstallation(Context context,Museum museum, String language) {
		BroadCastManager.broadcastAppIntent(context
				, DownloadType.PHOTO_GALLERY, museum
				, language, STATE_INSTALLATION_STARTED);
		switch (AppIsole.getGalleryInstallationState(museum, language)) {
				case DOWNLOADING:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_DOWNLOAD_ZIP, DownloadType.PHOTO_GALLERY, museum, language);
					break;
				case EXTRACTING:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_EXTRACT_ZIP, DownloadType.PHOTO_GALLERY, museum, language);
					break;
				case PARSING:
				case NOT_YET_STARTED:
				default:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_PARSE_XML, DownloadType.PHOTO_GALLERY, museum, language);
					break;
		};
	}
	
	public static void startAGInstallation(Context context,Museum museum, String language){
		BroadCastManager.broadcastAppIntent(context
				, DownloadType.AUDIO_GUIDE, museum
				, language, STATE_INSTALLATION_STARTED);
		AudioGuideInstallationState agState = AppIsole.getAudioGuideInstallationState(museum, language);
		DownloadType type = agState.downloadType;
		switch (agState.state) {
				case DOWNLOADING:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_DOWNLOAD_ZIP, type, museum, language,false);
					break;
				case EXTRACTING:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_EXTRACT_ZIP, DownloadType.AUDIO_GUIDE, museum, language,false);
					break;
				case PARSING:
				case NOT_YET_STARTED:
				default:
					BroadCastManager.broadcastActionIntent(context,
							ACTION_PARSE_XML, DownloadType.AUDIO_GUIDE, museum, language,false);
					break;
		};
	}
	
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(DownloadManager.ACTION_DOWNLOAD_STATUS)){
			int downloadStatus 	= intent.getIntExtra(DownloaderIntents.TYPE, -1);
			String url     		= intent.getStringExtra(DownloaderIntents.URL);
			if(!TextUtils.isEmpty(url)){
				Museum museum 	= AppIsole.getMusuemFromURL(url);
				String language = AppIsole.getLanguageFromURL(url); 
				DownloadType type = AppIsole.getDownloadTypeForUrl(url);
				
				switch (downloadStatus) {
					case DownloaderIntents.Types.COMPLETE:
						Log.d(TAG, "Completing download at m:"+museum.getShortName()+", url:"+url+", l"+language
								+" act: "+action+", s:"+downloadStatus);
						
						DownloadQueue queue1 = new DownloadQueue(museum,getParentDownload(type), language);
						queue1.setStatus(DownloadQueue.STATUS_INSTALLING);
						TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
						tableDownloadQueue.updateStatus(queue1);
						handleDownloadComplete(context,museum,language,type);
						
						break;
					case DownloaderIntents.Types.ERROR:
//						DownloadException e = (DownloadException) intent.getSerializableExtra(DownloaderIntents.ERROR_CODE);
						
						handleDownloadError(context,museum,language,type);
						break;
					case DownloaderIntents.Types.PAUSE:
						break;
					case DownloaderIntents.Types.ADD:
					case DownloaderIntents.Types.START:
					case DownloaderIntents.Types.CONTINUE:
						Log.d(TAG, "Receving at downloader receiver for m:"+museum.getShortName()+", url:"+url+", l"+language
								+" act: "+action+", s:"+downloadStatus);
						TableDownloadQueue tableDownloadQueue2 = new TableDownloadQueue();
						DownloadQueue queue = new DownloadQueue(museum,getParentDownload(type),language);
						queue.setStatus(DownloadQueue.STATUS_DOWNLOADING);
						tableDownloadQueue2.updateStatus(queue);
						break;
					
					default:
						break;
				}
			}
		}
		else {
			Museum 		 musuem 	= BroadCastManager.getMuseum(intent);
			String 		 language 	= BroadCastManager.getLanguage(intent);
			DownloadType type 		= BroadCastManager.getDownloadType(intent);
			boolean		 isFree		= BroadCastManager.isFree(intent);			
			Log.d(TAG, "Receving at app receiver for m:"+musuem.getShortName()+", l:"+language+" t:"+type+" fr:"+isFree
					+" act: "+action);
			if(action.equals(ACTION_DOWNLOADHELPER_RESPONSE)){
				int state = BroadCastManager.getState(intent);
				handleDownladHelperResponse(context,musuem,language,type,isFree,state);
			}
			else if(action.equals(ACTION_ISOLE_INSTALLATION)){
				int state =  BroadCastManager.getState(intent);
				if(state==STATE_INSTALLATION_SUCCESS){
					TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
					tableDownloadQueue.delete( new DownloadQueue(musuem, type, language));
					resumeDownload(context);
				}
				else if(state==STATE_INSTALLATION_FAILED){
					TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
					tableDownloadQueue.delete(new DownloadQueue(musuem, type, language));
					resumeDownload(context);
					String musueumStr = context.getResources().getString(musuem.getNameId());
					Toast.makeText(context, "Installation failed for "+type+" of "+musueumStr+" for language "+LanguageConstants.getAppLanguage(language), Toast.LENGTH_SHORT).show();
				}
			}
			else if(action.equals(ACTION_PARSE_XML)){
				handleParseXMLAction(context,musuem,language,type,isFree);
			}
			else if(action.equals(ACTION_EXTRACT_ZIP)){				
				
				handleExtractZipAction(context,musuem,language,type,isFree);
			}
			else if(action.equals(ACTION_DOWNLOAD_ZIP)){
				handleDownloadZipAction(context,musuem,language,type,isFree);
			}
			else if(action.equals(ACTION_CALCULATE_ZIP_SIZE)){
				handleZipSizeCalculation(context,musuem,language,type,isFree);
			}
		}
	}
	
	public DownloadType getParentDownload(DownloadType download){
		switch (download) {
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
	// ===========================================================
	// Methods
	// ===========================================================
	
	private void handleDownloadError(Context context, Museum museum,
			String language, DownloadType type) {
		DownloadType broadcastType = type;
		switch (type) {
			case PHOTO_GALLERY:
				broadcastType = DownloadType.PHOTO_GALLERY;
				break;
			case AUDIO_GUIDE:
			case AUDIO_GUIDE_IMAGE:
			case MAP:
				broadcastType = DownloadType.AUDIO_GUIDE;
				break;
			default:
				break;
		}
		BroadCastManager.broadcastAppIntent(context, broadcastType, museum, language, STATE_INSTALLATION_FAILED);
		
		
		
	}

	private void handleParseXMLAction(Context context, Museum museum,
			String language, DownloadType type, boolean isFree) {
		switch (type) {
			case PHOTO_GALLERY:
				AppIsole.setGalleryInstallationState(museum, 
						language, InstallationState.PARSING);
				DownloadHelperService.startPGXMLParsing(context, museum, language);
				break;
			case AUDIO_GUIDE:
				AudioGuideInstallationState state = new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE
						, InstallationState.PARSING);
				AppIsole.setAudioGuideInstallationState(museum, language
						, state);
				DownloadHelperService.startAGXMLParsing(context
						, museum, language, isFree);
				break;			
			default:
				break;
		}		
		
	}

	private void handleExtractZipAction(Context context, Museum museum,
			String language, DownloadType type, boolean isFree) {
		switch (type) {
			case PHOTO_GALLERY:
				AppIsole.setGalleryInstallationState(museum, 
						language, InstallationState.EXTRACTING);
				DownloadHelperService.startPGExtractZip(context, museum, language);
				break;
			case AUDIO_GUIDE:
				AudioGuideInstallationState state = new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE
						, InstallationState.EXTRACTING);
				AppIsole.setAudioGuideInstallationState(museum, language, state);
				DownloadHelperService.startAGExtractZip(context, museum, language, false);
				break;
			
			default:
				break;
		}		
	}

	private void handleZipSizeCalculation(Context context, Museum museum,
			String language, DownloadType type, boolean isFree) {
		switch (type) {
			case AUDIO_GUIDE:
				AppIsole.setGalleryInstallationState(museum, 
						language, InstallationState.CALCULATING_SIZE);
				DownloadHelperService.setAGZipSizes(context, museum, language, isFree);
				break;
	
			default:
				break;
		}
	}
	
	/********************************************************************************
	 * Controls the action to be done when zip of each download type's 
	 * download is complete
	 * <br>1.PhotoGallery -> Extract the downloaded zip 
	 * ******************************************************************************/
	private void handleDownloadComplete(Context context,Museum museum, String language,
			DownloadType type) {
		switch (type) {
			case PHOTO_GALLERY:
				Log.i(TAG, "Application download is complete: "+museum.getShortName()+", language: "+language
						+" download type: "+type);
				BroadCastManager.broadcastActionIntent(context, ACTION_EXTRACT_ZIP
						, DownloadType.PHOTO_GALLERY, museum, language);
				break;
			case MAP:
				downloadAudioImageZip(context, museum, language, false);
				break;
			case AUDIO_GUIDE_IMAGE:
				downloadAudioZip(context, museum, language, false);
				break;
			case AUDIO_GUIDE:
				BroadCastManager.broadcastActionIntent(context, ACTION_EXTRACT_ZIP
						, DownloadType.AUDIO_GUIDE, museum, language);
				break;
			
			
			
			default:
				break;
		}		
	}

	private void handleDownloadZipAction(Context context,Museum museum, String language, DownloadType type,
			boolean isFree) {
		switch (type) {
			case AUDIO_GUIDE:
			case MAP:
			case AUDIO_GUIDE_IMAGE:
				
				if(!AppIsole.isDownloaded(museum, DownloadType.MAP)
						&&!AppIsole.getMapZip(context, museum).exists()){
					downloadMapZip(context, museum, language, isFree);
				}
				else if(!AppIsole.isDownloaded(museum, DownloadType.AUDIO_GUIDE_IMAGE)
						&&!AppIsole.getAudioGalleryZip(context, museum).exists()){
					downloadAudioImageZip(context, museum, language, isFree);
				}
				else if(!AppIsole.isDownloaded(museum, DownloadType.AUDIO_GUIDE, language)){
					if(!AppIsole.getAudioGuideZip(context, museum, language).exists())
						downloadAudioZip(context, museum, language, isFree);
					else
						handleExtractZipAction(context, museum, language
								, type, isFree);
				}
				else{
					AppIsole.setInstalled(museum,DownloadType.AUDIO_GUIDE , language, true);
					BroadCastManager.broadcastAppIntent(context
							, DownloadType.AUDIO_GUIDE, museum
							, language, STATE_INSTALLATION_SUCCESS);
				}
				
				break;
			case PHOTO_GALLERY:
				if(!AppIsole.isDownloaded(museum, DownloadType.PHOTO_GALLERY)){
					downloadPhotoGalleryZip(context, museum, language);
				}
				else{
					AppIsole.setInstalled(museum,DownloadType.PHOTO_GALLERY , language, true);
					BroadCastManager.broadcastAppIntent(context
							, DownloadType.PHOTO_GALLERY, museum
							, language, STATE_INSTALLATION_SUCCESS);
				}
				break;
	
			default:
				break;
		}
		
	}
	
	
	private void handleDownladHelperResponse(Context context, Museum museum,
			String language, DownloadType type, boolean isFree, int state) {
		
		switch (type) {
			case PHOTO_GALLERY:
				handleDownloadResponsePG(context, museum, language, type, isFree, state);
				break;
				
			case AUDIO_GUIDE:
				handleDownloadResponseAG(context, museum, language, type, isFree, state);
				break;
	
			default:
				break;
		}
		
		
	}
	
	private void handleDownloadResponsePG(Context context,Museum museum,
			String language, DownloadType type, boolean isFree, int state){
		switch (state) {
			case DownloadHelperService.STATE_XML_PARSING_SUCESSS:
				BroadCastManager.broadcastActionIntent(context
						, ACTION_DOWNLOAD_ZIP, type, museum, language)	;
				break;					
			case DownloadHelperService.STATE_ZIP_CALCULATION_SUCCESS:
				
				break;
				
			case DownloadHelperService.STATE_XML_PARSING_FAILED:
				BroadCastManager.broadcastAppIntent(context, type, museum, language, STATE_INSTALLATION_FAILED);
				break;
			case DownloadHelperService.STATE_ZIP_EXTRACT_SUCCESS:
				AppIsole.setInstalled(museum, type, language, true);
				BroadCastManager.broadcastAppIntent(context, type, museum, language, STATE_INSTALLATION_SUCCESS);
				break;
				
			case DownloadHelperService.STATE_ZIP_EXTRACT_FAILED:
				File file = AppIsole.getPhotoGalleryZip(context, museum);
				if(file.exists())
					file.delete();
				AppIsole.setGalleryInstallationState(museum, language, InstallationState.DOWNLOADING);
				BroadCastManager.broadcastAppIntent(context, type, museum, language, STATE_INSTALLATION_FAILED);
				break;
			default:
				break;
		}
	}
	
	private void handleDownloadResponseAG(Context context,Museum museum,
			String language, DownloadType type, boolean isFree, int state){
		Log.i(TAG, "Receving at download response m:"+museum.getShortName()+", l:"+language+" t:"+type+" fr:"+isFree
				+" st: "+state);
		switch (state) {
			case DownloadHelperService.STATE_XML_PARSING_SUCESSS:
				BroadCastManager.broadcastActionIntent(context
						, ACTION_CALCULATE_ZIP_SIZE, type, museum, language,false);
				break;					
			case DownloadHelperService.STATE_ZIP_CALCULATION_SUCCESS:
				BroadCastManager.broadcastActionIntent(context
						, ACTION_DOWNLOAD_ZIP, DownloadType.AUDIO_GUIDE, museum, language,false)	;
				break;
			case DownloadHelperService.STATE_ZIP_EXTRACT_SUCCESS:
				AppIsole.setInstalled(museum, type, language, true);
				BroadCastManager.broadcastAppIntent(context, type, museum, language,false, STATE_INSTALLATION_SUCCESS);
				
				break;
			case DownloadHelperService.STATE_XML_PARSING_FAILED:
			case DownloadHelperService.STATE_ZIP_CALCULATION_FAILED:
				BroadCastManager.broadcastAppIntent(context, type, museum, language,false, STATE_INSTALLATION_FAILED);
				break;
						
			case DownloadHelperService.STATE_ZIP_EXTRACT_FAILED:
				AudioGuideInstallationState isState = new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE, InstallationState.DOWNLOADING);
				AppIsole.setAudioGuideInstallationState(museum, language, isState);
				BroadCastManager.broadcastAppIntent(context, type, museum, language,false, STATE_INSTALLATION_FAILED);
				break;
			default:
				break;
		}
	}

	// ===========================================================
	// Download Methods
	// ===========================================================
	private void downloadAudioImageZip(Context context,Museum museum,String language,boolean isFree) {
		String url = IsNetHelper.getAudioGalleryZipURL(museum, isFree);
		String folderPath = AppIsole.getPathForMuseum(context, museum);
		File folder = new File(folderPath);
		if(!folder.exists())
			folder.mkdirs();
		DownloadService.addDownload(context,url,folderPath);
		AppIsole.setMuseumforURL(url, museum);
		AppIsole.setLangaugeforURL(url, language);
		AppIsole.setDownloadTypeforURL(url,DownloadType.AUDIO_GUIDE_IMAGE);
		AudioGuideInstallationState state = new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE_IMAGE
				, InstallationState.DOWNLOADING);
		AppIsole.setAudioGuideInstallationState(museum, language, state);
		Log.i(TAG, "Downloading "+url);
	}
	
	private void downloadMapZip(Context context, Museum museum,
		String language, boolean isFree) {
	
		String url = IsNetHelper.getMapZipURL(museum);
		String folderPath = AppIsole.getPathForMuseum(context, museum);
		File folder = new File(folderPath);
		if(!folder.exists())
			folder.mkdirs();
		DownloadService.addDownload(context,url,folderPath);
		
		AppIsole.setMuseumforURL(url, museum);
		AppIsole.setLangaugeforURL(url, language);
		AppIsole.setDownloadTypeforURL(url,DownloadType.MAP);
		AudioGuideInstallationState state = new AudioGuideInstallationState(DownloadType.MAP
				, InstallationState.DOWNLOADING);
		AppIsole.setAudioGuideInstallationState(museum, language, state);
		Log.i(TAG, "Downloading "+url);
	}
	
	private void downloadAudioZip(Context context,Museum museum,String language,boolean isFree) {
		String url = IsNetHelper.getAudioZipURL(museum, language, isFree);
		String folderPath = AppIsole.getPathForMuseum(context, museum, language);
		File folder = new File(folderPath,language);
		if(!folder.exists())
			folder.mkdirs();
		DownloadService.addDownload(context,url,folderPath);
		AppIsole.setMuseumforURL(url, museum);
		AppIsole.setLangaugeforURL(url, language);
		AppIsole.setDownloadTypeforURL(url,DownloadType.AUDIO_GUIDE);
		AudioGuideInstallationState state = new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE
				, InstallationState.DOWNLOADING);
		AppIsole.setAudioGuideInstallationState(museum, language, state);
		Log.i(TAG, "Downloading "+url);
	}
	
	private void downloadPhotoGalleryZip(Context context,Museum museum,String language){
		String url = IsNetHelper.getPhotoGalleryZipURL(museum);
		String folderPath = AppIsole.getPathForMuseum(context, museum);
		File folder = new File(folderPath);
		if(!folder.exists())
			folder.mkdirs();
		DownloadService.addDownload(context,url,folderPath);
		AppIsole.setMuseumforURL(url, museum);
		AppIsole.setLangaugeforURL(url, language);
		AppIsole.setDownloadTypeforURL(url,DownloadType.PHOTO_GALLERY);
		AppIsole.setGalleryInstallationState(museum, language, InstallationState.DOWNLOADING);
	}
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	




	public static class BroadCastManager {
		// ===========================================================
		// Constants
		// ===========================================================
		// ===========================================================
		// Fields
		// ===========================================================

		// ===========================================================
		// Constructors
		// ===========================================================

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================

		
		public static void broadcastActionIntent(Context context, String action
				, DownloadType type, Museum museum, String language){
			broadcastActionIntent(context,action ,type, museum, language,true);
		}
		
	 	public static void broadcastActionIntent(Context context,String action
	 			,DownloadType type, Museum museum, String language,
	 			boolean isFree) {

	 		Intent localIntent = new Intent();
        
	        // The Intent contains the custom broadcast action for this app
	        localIntent.setAction(action);
	        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

	        // Puts the status into the Intent
	        
	        localIntent.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
	        localIntent.putExtra(EXTENDED_DATA_LANGUAGE, language);
	        localIntent.putExtra(EXTENDED_DATA_ISFREE, isFree);
	        localIntent.putExtra(EXTENDED_DATA_TYPE, type.toString());
	        
	        // Broadcasts the Intent
	        context.sendBroadcast(localIntent);

	    }
	 	
	 	public static void broadcastAppIntent(Context context,DownloadType type, Museum museum, String language,int state){
			broadcastAppIntent(context, type, museum, language, true, state);
		}
		
	 	public static void broadcastAppIntent(Context context,DownloadType type, Museum museum, String language,boolean isFree,int state) {

	 		Intent localIntent = new Intent();
        
	        // The Intent contains the custom broadcast action for this app
	        localIntent.setAction(ACTION_ISOLE_INSTALLATION);
	        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

	        // Puts the status into the Intent
	        localIntent.putExtra(EXTENDED_DATA_STATUS, state);
	        localIntent.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
	        localIntent.putExtra(EXTENDED_DATA_LANGUAGE, language);
	        localIntent.putExtra(EXTENDED_DATA_ISFREE, isFree);
	        localIntent.putExtra(EXTENDED_DATA_TYPE, type.toString());

	        // Broadcasts the Intent
	        context.sendBroadcast(localIntent);

	    }
		
		
		public static void broadcastDownloadHelperIntent(Context context,DownloadType type, Museum museum, String language,int state){
			broadcastDownloadHelperIntent(context, type, museum, language, true, state);
		}
		
	 	public static void broadcastDownloadHelperIntent(Context context,DownloadType type, Museum museum, String language,boolean isFree,int state) {

	 		Intent localIntent = new Intent();
        
	        // The Intent contains the custom broadcast action for this app
	        localIntent.setAction(ACTION_DOWNLOADHELPER_RESPONSE);
	        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

	        // Puts the status into the Intent
	        localIntent.putExtra(EXTENDED_DATA_STATUS, state);
	        localIntent.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
	        localIntent.putExtra(EXTENDED_DATA_LANGUAGE, language);
	        localIntent.putExtra(EXTENDED_DATA_ISFREE, isFree);
	        localIntent.putExtra(EXTENDED_DATA_TYPE, type.toString());

	        // Broadcasts the Intent
	        context.sendBroadcast(localIntent);

	    }
	 	
		public static int getStatus(Intent intent){
			return intent.getIntExtra(EXTENDED_DATA_STATUS, -1);
		}
		
	 	
	 	public static Museum getMuseum(Intent intent){
			return Museum.getMuseum(intent.getExtras().getString(EXTENDED_DATA_MUSEUM));
		}
		
		public static boolean isFree(Intent intent){
			return intent.getExtras().getBoolean(EXTENDED_DATA_ISFREE);
		}
		
		public static String getLanguage(Intent intent){
			return intent.getExtras().getString(EXTENDED_DATA_LANGUAGE);
		}
		
		public static int getState(Intent intent){
			return intent.getExtras().getInt(EXTENDED_DATA_STATUS);
		}

		public static DownloadType getDownloadType(Intent intent) {
			// TODO Auto-generated method stub
			return DownloadType.getType(intent.getExtras().getString(EXTENDED_DATA_TYPE));
		}
	 	
		public static long getId(Intent intent){
			return intent.getIntExtra(EXTENDED_ID, -1);
		}
	 		
		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}
	
	
	public static class DownloadHelperService extends IntentService{

		
		// ===========================================================
		// Constants
		// ===========================================================
		
		private static final String TAG = DownloadHelperService.class.getName();
		
		public static final String ACTION_PARSE_GALLERY_XML		= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_PARSE_PG_XML";
		public static final String ACTION_EXTRACT_GALLERY_ZIP 	= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_EXTRACT_GALLERY_ZIP";
		
		public static final String ACTION_PARSE_AUDIO_XML		= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_PARSE_AUDIO_XML";
		public static final String ACTION_EXTRACT_AUDIO_ZIP 	= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_EXTRACT_AUDIO_ZIP";
		
		public static final String ACTION_PARSE_MAP_XML			= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_PARSE_MAP_XML";
		public static final String ACTION_EXTRACT_MAP_ZIP		= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_EXTRACT_MAP_ZIP";
		
		public static final String ACTION_CALCULATE_AUDIO_ZIP 	= "com.ezenit.isoleborromee.service.DownloadHelperService.ACTION_CALCULATE_AUDIO_ZIP";
		
		
	    public static final int STATE_XML_PARSING_STARTED 		= 4;
	    public static final int STATE_XML_PARSING_SUCESSS 		= 5;
	    public static final int STATE_XML_PARSING_FAILED  		= 6;
	    
//	    public static final int STATE_ZIP_DOWNLOAD_STARTED = 7;
//	    public static final int STATE_ZIP_DOWNLOAD_SUCCESS = 8;
//	    public static final int STATE_ZIP_DOWNLOAD_FAILED  = 9;
    
	    public static final int STATE_ZIP_EXTRACT_STARTED 		= 10;
	    public static final int STATE_ZIP_EXTRACT_SUCCESS 		= 11;
	    public static final int STATE_ZIP_EXTRACT_FAILED  		= 12;
	    
	    public static final int STATE_ZIP_CALCULATION_STARTED 	 = 13;
	    public static final int STATE_ZIP_CALCULATION_SUCCESS 	 = 14;
	    public static final int STATE_ZIP_CALCULATION_FAILED 	 = 15;
		
//		public static final int CMD_START_ZIP_DOWNLOAD 		= 1;
//		public static final int CMD_START_ZIP_EXTRACTION 	= 2;
	    
//		public static final int STATE_INSTALLATION_FAILED 	= 3;
		
	       
		// ===========================================================
		// Fields
		// ===========================================================
	    
	    
		
		// ===========================================================
		// Constructors
		// ===========================================================
		

		public static void startPGXMLParsing(Context context,Museum museum, String language){
			Intent service = new Intent(context, DownloadHelperService.class);
			service.setAction(ACTION_PARSE_GALLERY_XML);
			service.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
			service.putExtra(EXTENDED_DATA_LANGUAGE, language);
			service.putExtra(EXTENDED_DATA_ISFREE, true);
			context.startService(service);
		}
		
		public static void startAGXMLParsing(Context context,Museum museum,String language,boolean isFree){
			Intent service = new Intent(context, DownloadHelperService.class);
			service.setAction(ACTION_PARSE_AUDIO_XML);
			service.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
			service.putExtra(EXTENDED_DATA_LANGUAGE, language);
			service.putExtra(EXTENDED_DATA_ISFREE, isFree);
			context.startService(service);
		}
		
		private static void startPGExtractZip(Context context,Museum museum, String language) {
			Intent service = new Intent(context, DownloadHelperService.class);
			service.setAction(ACTION_EXTRACT_GALLERY_ZIP);
			service.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
			service.putExtra(EXTENDED_DATA_LANGUAGE, language);
			service.putExtra(EXTENDED_DATA_ISFREE, true);
			context.startService(service);
			
		}
			
		private static void startAGExtractZip(Context context,Museum museum,String language,boolean isFree){
			Intent service = new Intent(context, DownloadHelperService.class);
			service.setAction(ACTION_EXTRACT_AUDIO_ZIP);
			service.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
			service.putExtra(EXTENDED_DATA_LANGUAGE, language);
			service.putExtra(EXTENDED_DATA_ISFREE, isFree);
			context.startService(service);
		}

		private static void setAGZipSizes(Context context,Museum museum, String language, boolean isFree) {
			Intent service = new Intent(context, DownloadHelperService.class);
			service.setAction(ACTION_CALCULATE_AUDIO_ZIP);
			service.putExtra(EXTENDED_DATA_MUSEUM, museum.getShortName());
			service.putExtra(EXTENDED_DATA_LANGUAGE, language);
			service.putExtra(EXTENDED_DATA_ISFREE, isFree);
			context.startService(service);
		}
		
		
		
		
		// ===========================================================
		// Getter & Setter
		// ===========================================================

		public DownloadHelperService() {
			super("isoledownloadhelper");
			// TODO Auto-generated constructor stub
			
		}
		
		@Override
		public void onStart(Intent intent, int startId) {
			// TODO Auto-generated method stub
			super.onStart(intent, startId);
			
		}
		
		@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			super.onCreate();
		}

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================
		@Override
		protected void onHandleIntent(Intent intent) {
			String action = intent.getAction();
			if(action.equals(ACTION_PARSE_GALLERY_XML)){
				parseXMLToDB(DownloadType.PHOTO_GALLERY, intent);
			}
			else if(action.equals(ACTION_EXTRACT_GALLERY_ZIP)){
				extractZip(DownloadType.PHOTO_GALLERY, intent);
			}
			else if(action.equals(ACTION_PARSE_AUDIO_XML)){
				parseXMLToDB(DownloadType.AUDIO_GUIDE, intent);
			}
			else if(action.equals(ACTION_EXTRACT_AUDIO_ZIP)){
				extractZip(DownloadType.AUDIO_GUIDE, intent);
			}
			else if(action.equals(ACTION_CALCULATE_AUDIO_ZIP)){
				calculateAndSetZipSize(DownloadType.AUDIO_GUIDE,intent);
			}
		}
		



		// ===========================================================
		// Methods
		// ===========================================================

		private void parseXMLToDB(DownloadType type,Intent intent) {
			// TODO Auto-generated method stub
			Museum museum   = BroadCastManager.getMuseum(intent);
			String language = BroadCastManager.getLanguage(intent);
			boolean isFree  = BroadCastManager.isFree(intent);
			
			BroadCastManager.broadcastDownloadHelperIntent(this, type,museum,language,isFree, STATE_XML_PARSING_STARTED);		
			boolean success = false;
			try {
				switch (type) {
					case AUDIO_GUIDE:
						IsNetHelper.parseAudioGalleryAndMapXMLToDB(museum, language,isFree);
						break;
					case PHOTO_GALLERY:
						IsNetHelper.parsePhotoGalleryXMLToDB(museum, language);
						break;
		
					default:
						break;
				}
				
				success = true;
			
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			if(success)
				BroadCastManager.broadcastDownloadHelperIntent(this, type, museum, language, STATE_XML_PARSING_SUCESSS);
			else
				BroadCastManager.broadcastDownloadHelperIntent(this, type,museum,language,isFree, STATE_XML_PARSING_FAILED);
		}
		
		private void extractZip(DownloadType type,Intent intent) {
			final Museum  museum   	= BroadCastManager.getMuseum(intent);
			String  language 		= BroadCastManager.getLanguage(intent); 
			boolean isFree   		= BroadCastManager.isFree(intent);
			
			BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_EXTRACT_STARTED);	
			boolean success = false;
			
				switch (type) {
					case AUDIO_GUIDE:
						File audioGallery = AppIsole.getAudioGalleryZip(this, museum);
						if(audioGallery.exists()){			
							final SQLiteDatabase db = AppIsole.getDB();
							OnFileExtractedListener fileExtractListener = new OnFileExtractedListener() {
								
								@Override
								public void onFileExtracted(File file) {
									if(file.getName().startsWith("._")){
										return;
									}
									TableImgRel.createOrUpdateImageRelation(db, new Image(museum,file));
								}
							};
							if(ZipOperation.extractZip(audioGallery,false,fileExtractListener)){
								AppIsole.setDownloaded(museum, DownloadType.AUDIO_GUIDE_IMAGE, true);
							}
							else{
								audioGallery.delete();
								break;
							}
							
						}
						File mapZip 	= AppIsole.getMapZip(this,museum);
						if(mapZip.exists()){			
							if(ZipOperation.extractZip(mapZip)){
								AppIsole.setDownloaded(museum, DownloadType.MAP, true);
							}
							else{
								mapZip.delete();
								break;
							}
						}
						
						File audioGuideZip = AppIsole.getAudioGuideZip(this, museum,language);
						
						if(ZipOperation.extractZip(audioGuideZip)){
							AppIsole.setDownloaded(museum, DownloadType.AUDIO_GUIDE, language,true);	
						}
						else{
							audioGuideZip.delete();
							break;
						}
						success = true;				
						break;
					case PHOTO_GALLERY:
						if(ZipOperation.extractZip(AppIsole.getPhotoGalleryZip(this, museum))){
							
							AppIsole.setDownloaded(museum, DownloadType.PHOTO_GALLERY, true);
							success = true;
						}
						
						break;
		
					default:
						break;
				}					
			if(success)
				BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_EXTRACT_SUCCESS);	
			else
				BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_EXTRACT_FAILED);
		}


		
		
		private void calculateAndSetZipSize(DownloadType type,Intent intent) {
			Museum museum 	= BroadCastManager.getMuseum(intent);
			String language = BroadCastManager.getLanguage(intent);
			boolean isFree  = BroadCastManager.isFree(intent);
			
			BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_CALCULATION_STARTED);		
			boolean success = false;
			try {
				Log.e(TAG, "Audio Zip size is it free "+isFree);
				float audioZip = IsNetHelper.getAudioZipSize(museum, language, isFree);
				float audioGalleryZip= IsNetHelper.getAudioGalleryZipSize(museum, isFree);
				float mapzip   = IsNetHelper.getMapZipSize(museum);
				
				Log.e(TAG, "Audio Zip size is "+audioZip+".."+audioGalleryZip);
				
				AppIsole.setAudioZipSize(museum, language, audioZip);
				AppIsole.setAudioGalZipSize(museum,audioGalleryZip);
				AppIsole.setMapZipSize(museum,mapzip);
				AppIsole.setTotalAudioSize(museum,language,audioZip+audioGalleryZip+mapzip);
				
				success = true;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			if(success)
				BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_CALCULATION_SUCCESS);	
			else
				BroadCastManager.broadcastDownloadHelperIntent(this,type,museum,language,isFree,STATE_ZIP_CALCULATION_FAILED);
			
		}

		
		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}


	

}
