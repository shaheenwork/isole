package com.ezenit.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ezenit.isoleborromee.AppIsole;
import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.utils.HttpClientHelper;
import com.ezenit.xmlparser.AudioGuideParser;
import com.ezenit.xmlparser.MapParser;
import com.ezenit.xmlparser.PhotoParser;
import com.ezenit.isoleborromee.R;

public class IsNetHelper {
	// ===========================================================
	// Constants
	// ===========================================================
	
	//new test url
private static final String SERVER_URL = "http://www.e-zenitsviluppo.it/mobile/isoleborromee/app_content/borromee-mobile";
	//new production url
//private static final String SERVER_URL = "http://static.isoleborromee.it/borromee-mobile";

		//new one paolo send,inapp add for test
	
	//private static final String SERVER_URL = "http://www.e-zenitsviluppo.it/mobile/isoleborromee/app_content/borromee-mobile";
	//private static final String SERVER_URL = "http://static.isoleborromee.it/borromee-mobile";
	//private static final String SERVER_URL = "http://www.e-zenitsviluppo.it/ios/borromee-mobile";
//	private static final String SERVER_URL = "http://192.168.0.64/borromee-mobile";
//	private static final String SERVER_URL = "http://192.168.0.36/borromee-mobile";
//	private static final String SERVER_URL = "http://192.168.1.9/borromee-mobile";
//	private static final String SERVER_URL = "http://192.168.1.63/borromee-mobile";
//	private static final String SERVER_URL = "http://localhost/borromee-mobile/";
	
	private static final String TAG = IsNetHelper.class.getName();
	
	
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
	
//	public static DownloadManager.Request getMapZipRequest(Context context,Museum museum){
//		return getDownloadRequest(context, SERVER_URL+"/"+museum.getFolderName(), AppIsole.MAP+".zip", 
//				museum.getFolderName(), R.string.download, R.string.downloading_audio_guide);
//	}
	
//	public static String getMapZipURL( Museum museum){
//		return SERVER_URL+"/"+museum.getFolderName()+"/"+AppIsole.MAP+".zip";
//	}
	
	public static DownloadManager.Request getAudioZipRequest(Context context,Museum museum,String language,boolean free){		
			
		String fileToDownload = null;
		if(free){
			fileToDownload = AppIsole.FREE_GUIDE+".zip";
		}
		else{
			fileToDownload = AppIsole.PAID_GUIDE+".zip";
		}
				
		return getDownloadRequest(context, SERVER_URL+"/"+museum.getFolderName()+"/"+language, fileToDownload, 
				museum.getFolderName()+File.separator+language, R.string.download, R.string.downloading_audio_guide);
	}
	
	public static DownloadManager.Request getAudioImageGalleryZipRequest(Context context,Museum museum,boolean free){
		String fileToDownload = null;
		if(free){
			fileToDownload = AppIsole.FREE_GUIDE_GALLERY+".zip";
		}
		else{
			fileToDownload = AppIsole.PAID_GUIDE_GALLERY+".zip";
		}
		
		return getDownloadRequest(context, SERVER_URL+"/"+museum.getFolderName(), fileToDownload, 
				museum.getFolderName(), R.string.download, R.string.downloading_audio_guide);
	}
	

	public static DownloadManager.Request getPhotoGalleryZip(Context context,Museum museum){
		return getDownloadRequest(context, SERVER_URL+"/"+museum.getFolderName(), AppIsole.PHOTO_GALLERY+".zip", 
				museum.getFolderName(), R.string.download, R.string.downloading_audio_guide);
	}
	
	public static void parsePhotoGalleryXMLToDB(Museum museum,String languageShort) 
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException{
		String xmlUrl = SERVER_URL+"/"+museum.getFolderName()
				+"/"+languageShort+"/"+AppIsole.PHOTO_GALLERY+".xml";
		HttpClientHelper.parseXML(xmlUrl, new PhotoParser(museum, languageShort));
		Log.d("photo-url",xmlUrl);
	}
	
	public static void parseAudioGalleryAndMapXMLToDB(Museum museum,String languageShort,boolean isFree) 
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException{
	
		HttpClientHelper.parseXML(getAudioXMLURL(museum, languageShort, isFree)
				, new AudioGuideParser(isFree,museum, languageShort));
		HttpClientHelper.parseXML(getMapXMLURL(museum), new MapParser(museum));
		Log.d("audio-url",getMapXMLURL(museum));
		
	}
	
	
	
	
	
	


	public static boolean isPhotoGalleryZip(String path){
		if(path.endsWith(AppIsole.PHOTO_GALLERY+".zip")){
			return true;
		}
		return false;
	}
	
	public static boolean isPaidGuideZip(String path){
		if(path.endsWith(AppIsole.PAID_GUIDE+".zip")){
			return true;
		}
		return false;
	}
	
	public static boolean isPaidGuideGallery(String path){
		if(path.endsWith(AppIsole.PAID_GUIDE_GALLERY+".zip")){
			return true;
		}
		return false;
	}
	
	public static boolean isFreeGuideZip(String path){
		if(path.endsWith(AppIsole.FREE_GUIDE+".zip")){
			return true;
		}
		return false;
	}
	
	public static boolean isFreeGuideGallery(String path){
		if(path.endsWith(AppIsole.FREE_GUIDE_GALLERY+".zip")){
			return true;
		}
		return false;
	}
	
	public static final float getAudioZipSize(Museum museum,String language,boolean isFree) 
			throws IOException{
		return HttpClientHelper.getFileSize(getAudioZipURL(museum, language, isFree));
	}
	
	public static final float getAudioGalleryZipSize(Museum museum,boolean isFree) 
			throws IOException{
//		Log.d(TAG, "The gallery zip is "+getAudioGalleryZipURL(museum, isFree));
		return HttpClientHelper.getFileSize(getAudioGalleryZipURL(museum,  isFree));
	}
	
	public static float getMapZipSize(Museum museum) 
			throws IOException {
		// TODO Auto-generated method stub
		return HttpClientHelper.getFileSize(getMapZipURL(museum));
	}
	
	public static String getMapZipURL(Museum museum) {
		// TODO Auto-generated method stub
		return SERVER_URL+"/"+museum.getFolderName()+"/"+AppIsole.MAP+".zip";
	}
	
	public static String getPhotoGalleryZipURL(Museum museum){
		return SERVER_URL+"/"+museum.getFolderName()+"/"+AppIsole.PHOTO_GALLERY+".zip";
	}
	
	
	public static String getPGXMLURL(Museum museum,String language,boolean isFree){
		return SERVER_URL+"/"+museum.getFolderName()+"/"+language+"/"+AppIsole.PHOTO_GALLERY+".xml";
	}
	
	public static String getAudioGalleryZipURL(Museum museum,boolean isFree){
		String fileName = "";
		if(isFree){
			fileName = AppIsole.FREE_GUIDE_GALLERY;			
		}
		else{
			fileName = AppIsole.PAID_GUIDE_GALLERY;
		}
		return SERVER_URL+"/"+museum.getFolderName()+"/"+fileName+".zip";
	}
	
	public static String getAudioZipURL(Museum museum,String language,boolean isFree){
		String fileName = "";
		if(isFree){
			fileName = AppIsole.FREE_GUIDE;			
		}
		else{
			fileName = AppIsole.PAID_GUIDE;
		}
		return SERVER_URL+"/"+museum.getFolderName()+"/"+language+"/"+fileName+".zip";
	}
	
	public static String getAudioXMLURL(Museum museum,String language,boolean isFree){
		String fileName = "";
		if(isFree){
			fileName = AppIsole.FREE_GUIDE;			
		}
		else{
			fileName = AppIsole.PAID_GUIDE;
		}
		return SERVER_URL+"/"+museum.getFolderName()+"/"+language+"/"+fileName+".xml";
	}
	
	private static String getMapXMLURL(Museum museum) {
		// TODO Auto-generated method stub
		return SERVER_URL+"/"+museum.getFolderName()+"/"+AppIsole.MAP+".xml";
	}
	
	
	public static boolean isMapZip(String path){
		if(path.endsWith(AppIsole.MAP+".zip")){
			return true;
		}
		return false;
	}
	
	private static DownloadManager.Request getDownloadRequest(Context context
			,String url
			,String fileToDownload
			,String destFolder
			,int titleId,int msgId){
			
		url += "/"+fileToDownload;
		
		String title = context.getResources().getString(titleId); 
		String desc = context.getResources().getString(msgId);
		
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
				|DownloadManager.Request.NETWORK_MOBILE);
		request.setTitle(title);
		request.setDescription(desc);
		
		request.setVisibleInDownloadsUi(false);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		File folder = context.getExternalFilesDir(destFolder);
		if(!folder.exists())
			folder.mkdirs();
		File file = new File(folder.getAbsolutePath()+File.separator+fileToDownload);	
		if(file.exists()){
//			Log.d(TAG, "Deleting file"+file.getAbsolutePath());
			file.delete();
			
		}
		
//		Log.d(TAG, "Downloading to path "+file.getAbsolutePath());
		request.setDestinationInExternalFilesDir(context, 
				destFolder, fileToDownload);
		
		
		return request;
	}
	
	
	
	public static float getDownloadProgress(DownloadManager downloadManager,long downloadId){
		Query query = new Query();
		query.setFilterById(downloadId);
		float progress = 0;
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
		  int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
		  int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
		  long size = c.getInt(sizeIndex);
		  long downloaded = c.getInt(downloadedIndex);
		 
		  if (size != -1) progress = downloaded/(float)size;
		 	  
		}
	
		c.close();
		return progress;
	}
	
	public static float getDownloadAudioProgress(DownloadType type,
			Museum museum,String language
			,DownloadManager downloadManager,long downloadId){
		Query query = new Query();
		query.setFilterById(downloadId);
		float progress = 0;
		
		
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			
	    int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
		  float size = AppIsole.getTotalAudioSize(museum, language);

		  float downloaded = c.getInt(downloadedIndex);
		  
		  if(type==DownloadType.AUDIO_GUIDE_IMAGE)
			  downloaded = AppIsole.getMapZipSize(museum)+downloaded;
		  else  if(type==DownloadType.AUDIO_GUIDE){
			  downloaded = AppIsole.getMapZipSize(museum)+AppIsole.getAudioGalZipSize(museum)+downloaded;
//			  Log.d(TAG, "Audio Guide donwnload size "+downloaded);
		  }
		
		  if (size != -1) progress = downloaded/(float)size;
		  
		}
		
		c.close();
		return progress;
	}
	
	
	
	public static ArrayList<DownloadFileInfo> getDownloadingFiles(DownloadManager downloadManager){
		Query query = new Query();
		query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
		Cursor c = downloadManager.query(query);
		c.getColumnIndex(DownloadManager.COLUMN_ID);
		ArrayList<DownloadFileInfo> idFiles = new ArrayList<DownloadFileInfo>();
		if (c.moveToFirst()) {
		  	idFiles.add(new DownloadFileInfo(c));
		}
		c.close();
		return idFiles;
	}
	
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	public static class DownloadFileInfo{
		private long 	id;
		private String 	fileName;
		private DownloadType type;
		
		public DownloadFileInfo(Cursor c) {
			id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
			fileName = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
		}
		
		public DownloadFileInfo(long id,DownloadType type) {
			this.id = id;
			this.type = type;
		}
		
		public DownloadType getType() {
			return type;
		}
		
		public long getId() {
			return id;
		}
		
		public void setId(long id) {
			this.id = id;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
}
