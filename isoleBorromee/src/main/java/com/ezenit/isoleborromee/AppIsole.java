package com.ezenit.isoleborromee;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ezenit.db.DBApp;
import com.ezenit.isoleborromee.db.DBHelper;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.net.IsNetHelper.DownloadFileInfo;
import com.ezenit.utils.LanguageConstants;
import com.ezenit.isoleborromee.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class AppIsole extends DBApp{
	// ===========================================================
	// Constants
	// ===========================================================
	
	public static final String TAG = AppIsole.class.getName();
	
	public static enum DownloadType {
		AUDIO_GUIDE,
		AUDIO_GUIDE_IMAGE,
		PHOTO_GALLERY,
		MAP;
		
		public static DownloadType getType(String type){
			if(type.equals(AUDIO_GUIDE.toString())){
				return AUDIO_GUIDE;
			}
			else if(type.equals(AUDIO_GUIDE_IMAGE.toString())){
				return AUDIO_GUIDE_IMAGE;
			}
			else if(type.equals(PHOTO_GALLERY.toString())){
				return PHOTO_GALLERY;
			}
			else if(type.equals(MAP.toString())){
				return MAP;
			}
			return null;
		}
		
	};
	
	public static enum InstallationState {
		NOT_YET_STARTED,
		CALCULATING_SIZE,
		DOWNLOADING,
		EXTRACTING,
		PARSING,
		COMPLETED
	}
	
	private static float paddingHome;
	
    // The background thread is doing logging
    public static final int STATE_LOG = -1;
	
	public static final boolean DEBUG_MODE = false;

	//Flag to turn purchases on and off
	//if flagg==true (free) if flag==flase(in app)
	private static final boolean MAKE_PURCHASE_AVAILABLE = true;
	
	public static final String PHOTO_GALLERY  		= "photo-gallery";
	public static final String MAP		   			= "map";
	public static final String PAID_GUIDE 			= "audio-guides";
	public static final String PAID_COVER_GALLERY 	= "cover";
	public static final String FREE_GUIDE 			= "FG";
	public static final String FREE_GUIDE_GALLERY 	= "FGGallery";
	public static final String FREE_COVER_GALLERY 	= "FGGallery";
	public static final String PAID_GUIDE_GALLERY   = "guides-gallery";
	
	
	public static final String APP_KEY 			 	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr60ebCUJ7WJzR+eqwczxQy1rum4BPUIHxOFF6EQtndkXlBNiMWfAytV25+zADqjm5tr4enSxQ1R8SY9K6mXi5cUrc+DQSLLOE8ByvjMOqm+pm5x/Q7xAcI/iVxzzSwJ5vl2+jnGyvpeIaTf9xUXVf7dy6ujfgnNeLDVrSnJpuchlc9IlxIWtO86LIwyiMM5h8XUVhVzL62FxOtruVNbHg8gd+96WtdsLoXYvyhhAZtlC7Yg3hmru5Oz/KdbDrFLoA2zH+2fxonQHJxIogGm9rYSF+W7TikIVJP34NPpIbQ0LPZEpJ6wwoCM55JInnPArZ+AVIyrE8Jb6XUOeIswoWQIDAQAB";
	
	// ===========================================================
	// Fields
	// ===========================================================
		
	private static SharedPreferences prefs;
	
	public static DisplayMetrics displayMetrics;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	
	
	public static int getDisplayHeight(){
		return displayMetrics.heightPixels;
	}
	
	public static int getDisplayWidth(){
		return displayMetrics.widthPixels;
	}
	
	
	public static float getPaddingHome() {
		return paddingHome;
	}
	
	// ===============================================================================
	// Installations Preferences
	// ===============================================================================
	
	public static void setDownloaded(Museum museum, 
			DownloadType type, String language,boolean downloaded){
		setPrefs("dwnld_"
			+museum.getShortName()+type.toString()+language, downloaded);
	}
	
	public static boolean isDownloaded(Museum museum, 
			DownloadType type, String language){
		return getBooleanPrefs("dwnld_"
			+museum.getShortName()+type.toString()+language);
	}
	
	public static void setDownloaded(Museum museum, 
			DownloadType type, boolean downloaded){
		setPrefs("dwnld_"
			+museum.getShortName()+type.toString(), downloaded);
	}
	
	public static boolean isDownloaded(Museum museum, 
			DownloadType type){
		return getBooleanPrefs("dwnld_"
			+museum.getShortName()+type.toString());
	}
	
	public static final void setInstalled(Museum musuem,DownloadType type,boolean installed){
		setPrefs("instl_"+musuem.getShortName()+type.toString(), installed);
	}
	
	public static boolean isInstalled(Museum museum,DownloadType type){
		return getBooleanPrefs("instl_"+museum.getShortName()+type.toString());
	}
	
	public static final void setInstalled(Museum musuem,DownloadType type,String language,boolean installed){
		setPrefs("instl"+musuem.getShortName()+language+type.toString(), installed);
	}
	
	public static boolean isInstalled(Museum museum,DownloadType type,String language){
		return getBooleanPrefs("instl"+museum.getShortName()+language+type.toString());
	}
	
	// ===============================================================================
	// Audio Zip Preferences
	// ===============================================================================
	public static void setAudioZipSize(Museum museum,String language,float size) {
		setPrefs("ad_zip_sz_"+museum.getShortName()+language, size);
	}
	
	public static float getAudioZipSize(Museum museum,String language) {
		return getFloatPrefs("ad_zip_sz_"+museum.getShortName()+language);
	}
	
	public static void setAudioGalZipSize(Museum museum,float size) {
		setPrefs("ad_gal_zip_sz_"+museum.getShortName(), size);
	}
	
	public static float getAudioGalZipSize(Museum museum) {
		return getFloatPrefs("ad_gal_zip_sz_"+museum.getShortName());
	}
	
	
	public static void setMapZipSize(Museum museum,float size) {
		setPrefs("map_zip_sz_"+museum.getShortName(), size);
		
	}
	
	public static float getMapZipSize(Museum museum) {
		return getFloatPrefs("map_zip_sz_"+museum.getShortName());
		
	}
	
	
	public static void setTotalAudioSize(Museum museum,String language,float size) {
		setPrefs("total_audio_"+museum.getShortName()+language, size);
	}
	
	
	
	public static float getTotalAudioSize(Museum museum,String language) {
		return getFloatPrefs("total_audio_"+museum.getShortName()+language);
	}
	
	// ===============================================================================
	// Audio Zip Preferences
	// ===============================================================================
	public static final boolean isFirstTime(){
		return prefs.getBoolean("firstTime", true);
	}
	
	public static final void setFirstTime(boolean isFirstTime){
		Editor editor = prefs.edit();
		editor.putBoolean("firstTime", isFirstTime);
		editor.commit();
	}
	
	// ===============================================================================
	// Download Service State Preferences
	// ===============================================================================
	
	public static void setGalleryInstallationState(Museum museum,String language,InstallationState state){
		setPrefs("gal_instal"+museum.getShortName()+language, state.toString());
	}
	
	public static InstallationState getGalleryInstallationState(Museum museum,String language){
		return InstallationState.valueOf(getStringPrefs("gal_instal"+museum.getShortName()+language
				,InstallationState.NOT_YET_STARTED.toString()));
	}
	
	public static void setAudioGuideInstallationState(Museum museum,String language,AudioGuideInstallationState state){
		DownloadType downloadType = state.downloadType;
		InstallationState isState = state.state;
		setPrefs("ag_installation"+museum.getShortName()+language, downloadType.toString()+"<<_>>"+isState.toString());
	}
	
	public static AudioGuideInstallationState getAudioGuideInstallationState(Museum museum,String language){
		String[] states  = getStringPrefs("ag_installation"+museum.getShortName()+language).split("<<_>>");
		if(states!=null&&states.length==2){
			DownloadType downloadType	= DownloadType.valueOf(states[0]);
			InstallationState inState	= InstallationState.valueOf(states[1]);
			return new AudioGuideInstallationState(downloadType, inState);
		}
		return new AudioGuideInstallationState(DownloadType.AUDIO_GUIDE_IMAGE, InstallationState.NOT_YET_STARTED);
	}
	
	public static void setMuseumforURL(String url,Museum museum){
		setPrefs("mu_"+url, museum.getShortName());
	}
	
	public static Museum getMusuemFromURL(String url){
		return Museum.getMuseum(getStringPrefs("mu_"+url));
	}
	
	public static void setLangaugeforURL(String url,String language){
		setPrefs("lg_"+url, language);
	}
	
	public static String getLanguageFromURL(String url){
		return getStringPrefs("lg_"+url);
	}
	
	public static DownloadType getDownloadTypeForUrl(String url) {
		String downloadType = getStringPrefs("d_type"+url, null);
		return downloadType==null ? null : DownloadType.getType(downloadType);
	}

	public static void setDownloadTypeforURL(String url,
			DownloadType downloadType) {
		setPrefs("d_type"+url, downloadType.toString());
	}
	
	// ===============================================================================
	// App Preferences
	// ===============================================================================
	
	private static final void setPrefs(String key,long value){
		Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	private static final void setPrefs(String key,float value){
		Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	private static final void setPrefs(String key,boolean value){
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private static final void setPrefs(String key,String value){
		Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	private static final boolean getBooleanPrefs(String key){
		return getBooleanPrefs(key, false);
	}
	
	private static final boolean getBooleanPrefs(String key,boolean value){
		return prefs.getBoolean(key, value);
	}
	
	private static final float getFloatPrefs(String key){
		return getFloatPrefs(key, 0);
	}
	
	private static final float getFloatPrefs(String key,int value){
		return prefs.getFloat(key, value);
	}
	
	private static final String getStringPrefs(String key){
		return prefs.getString(key, "");
	}
	
	private static final String getStringPrefs(String key,String defValue){
		return prefs.getString(key, defValue);
	}
	
	/*******************************************************************************
	 * @param locale 	applanguage to be used from the given locale
	 * 					,if locale language is not found
	 * 					,default language {@link LanguageConstants #ENGLISH} is used   <br/>
	 * 					
	 * ******************************************************************************/
	
	public static final void setAppLocale(Locale locale){
		setAppLocale(LanguageConstants.convertToShort(locale));
	}
	
	/*******************************************************************************
	 *
	 * @param shortLang applanguage to be used, use one of the below language	from the list <br/>
	 * 					{@link LanguageConstants #ENGLISH} <b>(DEFAULT)</b><br/>
	 * 					{@link LanguageConstants #GERMAN}<br/>
	 * 					{@link LanguageConstants #ITALIAN}<br/>
	 * 					{@link LanguageConstants #FRENCH}
	 * ******************************************************************************/
	
	public static final void setAppLocale(String shortLang){
		Editor editor = prefs.edit();
		editor.putString("appLocale", shortLang);
		editor.commit();
	}
	
	/*******************************************************************************

	 * 		   
	 * @return  one of the saved app languges from the below list <br/>
	 * 			{@link LanguageConstants #ENGLISH} <b>(DEFAULT)</b><br/>
	 * 			{@link LanguageConstants #GERMAN}<br/>
	 * 			{@link LanguageConstants #ITALIAN}<br/>
	 * 			{@link LanguageConstants #FRENCH}
	 * ******************************************************************************/
	public static final String getAppLocaleAsStr(){
		return prefs.getString("appLocale", LanguageConstants.ENGLISH);
	}
	
	public static final Locale getAppLocale(){
		return LanguageConstants.getLocale(getAppLocaleAsStr());
	}
	
	public static final long getCurrentDownloadId(DownloadType type,Museum museum,String language){
		return prefs.getLong("cd_id"+type.toString()+museum.toString()+language, -1);
	}
	
	public static final void setCurrentDownloadID(DownloadType type,Museum museum,String language,long id){
		Editor editor = prefs.edit();
		editor.putLong("cd_id"+type.toString()+museum.toString()+language, id);
		editor.commit();
	}
	
	public static void setLanguageForDownloadId(long id, String language){
		Editor editor = prefs.edit();
		editor.putString("langdownload"+id, language);
		editor.commit();
	}
	
	public static String getLanguageForDownloadId(long id){
		return prefs.getString("langdownload"+id,"");
	}
	
	public static void setMuseumForDownloadId(long id, Museum musem){
		Editor editor = prefs.edit();
		editor.putString("musdownload"+id, musem.getShortName());
		editor.commit();
	}
	
	public static Museum getMuseumForDownloadId(long id){
		
		return Museum.getMuseum(prefs.getString("musdownload"+id,""));
	}
	
	public static String getPathForMuseum(Context context,Museum musuem){
		return context.getExternalFilesDir(musuem.getFolderName()).getAbsolutePath();
	}
	
	public static String getPathForMuseum(Context context,AudioGuide guide){
		return getPathForMuseum(context, guide.getMuseum());
	}
	
	public static String getPathForMuseum(Context context,Museum musuem,String language){
		return context.getExternalFilesDir(musuem.getFolderName()).getAbsolutePath()+File.separator+language;
	}
	
//	public static String getMuseumPath(Context context,Museum musuem){
//		return context.getExternalFilesDir(musuem.getFolderName()).getAbsolutePath();
//	}
	
	public static String getGalleryPath(Context context,Museum musuem){
		return context.getExternalFilesDir(musuem.getFolderName()).getAbsolutePath()
				+File.separator+PHOTO_GALLERY;
	}
	
	public static String getAudioFolder(Context context, Museum museum,
			String languageShort, boolean free) {
		if(!free)
			return context.getExternalFilesDir(museum.getFolderName())+File.separator+languageShort+File.separator+PAID_GUIDE;
		else
			return context.getExternalFilesDir(museum.getFolderName())+File.separator+languageShort+File.separator+FREE_GUIDE;
	}
	
	public static String getPaidAudioGalleryFolder(Context context, Museum museum) {
		// TODO Auto-generated method stub
		return context.getExternalFilesDir(museum.getFolderName())+File.separator+PAID_GUIDE_GALLERY;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		prefs = getSharedPreferences("AppIsole", MODE_PRIVATE);
		
		displayMetrics = new DisplayMetrics();
		WindowManager windowManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		windowManger.getDefaultDisplay().getMetrics(displayMetrics);
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(android.R.color.transparent)
			.showImageForEmptyUri(android.R.color.transparent)
			.showImageOnFail(android.R.color.transparent)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		ImageLoaderConfiguration.Builder configBuilder	= new ImageLoaderConfiguration.Builder(getApplicationContext())
									.denyCacheImageMultipleSizesInMemory()
									.memoryCache(new LRULimitedMemoryCache(2 * 1024 * 1024))
									.diskCache(new UnlimitedDiscCache(getCacheDir())) // default
									.diskCacheSize(50 * 1024 * 1024)
									.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
									.defaultDisplayImageOptions(options);
		if(DEBUG_MODE)
			configBuilder.writeDebugLogs();
		
		if(MAKE_PURCHASE_AVAILABLE){
			setPurchased(Museum.MUSEUM_ISOLA_BELLA, true);
			setPurchased(Museum.MUSEUM_ISOLA_MADRE, true);
			setPurchased(Museum.MUSEUM_ROCA_D_ANGERA, true);
			setSingleItemPurchsed(true);
		}
		
		ImageLoaderConfiguration config = configBuilder.build();
		
		ImageLoader.getInstance().init(config);
		
		paddingHome =	getResources().getDimension(R.dimen.padding_home_up);



	}
	// ===========================================================
	// Methods
	// ===========================================================
	
	@Override
	public SQLiteOpenHelper getDBHelper() {
		// TODO Auto-generated method stub
		return DBHelper.getInstance(this);
	}
	
	public static File getPhotoGalleryZip(Context context,Museum museum){
		return new File(AppIsole.getPathForMuseum(context, museum)+File.separator+AppIsole.PHOTO_GALLERY+".zip");
	}
	
	public static String getFreeAudioGalleryFolder(Context context,
			Museum museum) {
		// TODO Auto-generated method stub
		return getPathForMuseum(context, museum)+File.separator+AppIsole.FREE_GUIDE_GALLERY;
	}
	
	public static File getAudioGuideZip(Context context,Museum museum,String language){
		return new File(AppIsole.getPathForMuseum(context, museum)
				+File.separator+language+File.separator+PAID_GUIDE+".zip");
	}
	
	
	
	public static File getAudioGalleryZip(Context context,Museum museum){
		return new File(AppIsole.getPathForMuseum(context, museum)
				+File.separator+PAID_GUIDE_GALLERY+".zip");
	}
	
	public static File getAudioGalleryFolder(Context context,AudioGuide audioGuide){
		String folder = "";
		if(audioGuide.isFree())
			folder = FREE_GUIDE_GALLERY;
		else
			folder = PAID_GUIDE_GALLERY;
		
		return new File(AppIsole.getPathForMuseum(context, audioGuide.getMuseum())
				+File.separator+folder);
	}
	
	public static File getAudioCoverFolder(Context context,AudioGuide audioGuide){
		String folder = "";
		if(audioGuide.isFree())
			folder = FREE_COVER_GALLERY;
		else
			folder = PAID_COVER_GALLERY;
		
		return new File(AppIsole.getPathForMuseum(context, audioGuide.getMuseum())
				+File.separator+folder);
	}
	
	public static File getAudioGalleryFolder(Context context,Museum museum,boolean isFree){
		String folder = "";
		if(isFree)
			folder = FREE_GUIDE_GALLERY;
		else
			folder = PAID_GUIDE_GALLERY;
		
		return new File(AppIsole.getPathForMuseum(context, museum)
				+File.separator+folder);
	}
	
	public static File getMapZip(Context context,
			Museum museum) {
		// TODO Auto-generated method stub
		return new File(AppIsole.getPathForMuseum(context, museum)
				+File.separator+MAP+".zip");
	}
	
	public static File getMapImage(Context context,
			Museum museum,int section) {
		// TODO Auto-generated method stub
		return new File(AppIsole.getPathForMuseum(context, museum)
				+File.separator+MAP+File.separator+"map"+section+".png");
	} 
	
	public static void clearAppData(Context context){
		File cache = context.getCacheDir();
		File appDir = new File(cache.getParent());
		if (appDir.exists()) {
		 String[] children = appDir.list();
		 for (String s : children) {
		   if (!s.equals("lib")) {
			   deleteDir(new File(appDir, s));
		   }
		 }
		}
		for(Museum musuem:Museum.getAllMuseums()){
			File extDir = context.getExternalFilesDir(musuem.getFolderName());
			if (extDir.exists()) {
			 String[] children = extDir.list();
			 for (String s : children) {
			   if (!s.equals("lib")) {
				   deleteDir(new File(extDir, s));
			   }
			 }
			}
		}
		prefs.edit().clear().commit();
	}
	public static boolean deleteDir(File dir) {
	   if (dir != null && dir.isDirectory()) {
	       String[] children = dir.list();
	       for (int i = 0; i < children.length; i++) {
	           boolean success = deleteDir(new File(dir, children[i]));
	           if (!success) {
	               return false;
	           }
	       }
	   }
	   return dir.delete();
	}

	public static ArrayList<DownloadFileInfo> getAllIds(){
		ArrayList<DownloadFileInfo> listIds = new ArrayList<DownloadFileInfo>();
		Museum[] museums = Museum.getAllMuseums();
		String[] laguages = LanguageConstants.getAllLanguages();
		for(Museum museum:museums){
			for(String language:laguages){
				long id = getCurrentDownloadId(DownloadType.AUDIO_GUIDE, museum, language);
				long pgId = getCurrentDownloadId(DownloadType.PHOTO_GALLERY, museum, language);
				long agId = getCurrentDownloadId(DownloadType.AUDIO_GUIDE_IMAGE, museum, language);
				long mapId = getCurrentDownloadId(DownloadType.MAP, museum, language);
				if(id!=-1){
					listIds.add(new DownloadFileInfo(id,DownloadType.AUDIO_GUIDE));
					
				}
				if(pgId!=-1){
					listIds.add(new DownloadFileInfo(pgId, DownloadType.PHOTO_GALLERY));
				}
				if(agId!=-1){
					listIds.add(new DownloadFileInfo(agId,DownloadType.AUDIO_GUIDE_IMAGE));
				}
				if(mapId!=-1){
					listIds.add(new DownloadFileInfo(mapId, DownloadType.MAP));
				}
			}
		}
		return listIds;
			
		
	}

	public static boolean isLanguageSelected() {
		// TODO Auto-generated method stub
		return getBooleanPrefs("isole_lang_selected");
	}

	public static void setLanguageSelected(boolean selected) {
		setPrefs("isole_lang_selected", selected);
	}

	public static void setPrice(Museum museum, String price) {
		setPrefs(museum.getShortName()+"_skuprice", price);
	}
	
	public static String getPrice(Context context,Museum museum){
		String buy = context.getResources().getString(R.string.buy);
		return getStringPrefs(museum.getShortName()+"_skuprice", buy);
	}

	public static void setPurchased(Museum museum, boolean purchased) {
		setPrefs(museum.getShortName()+"_skupurchased", purchased);
	}
	
	public static boolean isPurchased(Museum museum) {
		return getBooleanPrefs(museum.getShortName()+"_skupurchased", false);
	}

	public static void setSingleItemPurchsed(boolean purchased) {
		setPrefs("single_item_purchased", purchased);
		
	}
	
	public static boolean isSingleItemPurchsed() {
		return getBooleanPrefs("single_item_purchased");
		
	}
	
	public static ArrayList<Museum> getAllPurchases(){
		ArrayList<Museum> purchasedMusuems = new ArrayList<Museum>();
		if(isPurchased(Museum.MUSEUM_ALL_IN_ONE)){
			purchasedMusuems.add(Museum.MUSEUM_ISOLA_BELLA);
			purchasedMusuems.add(Museum.MUSEUM_ISOLA_MADRE);
			purchasedMusuems.add(Museum.MUSEUM_ROCA_D_ANGERA);
		}
		else{
			if(isPurchased(Museum.MUSEUM_ISOLA_BELLA)){
				purchasedMusuems.add(Museum.MUSEUM_ISOLA_BELLA);
			}
			if(isPurchased(Museum.MUSEUM_ISOLA_MADRE)){
				purchasedMusuems.add(Museum.MUSEUM_ISOLA_MADRE);
			}
			if(isPurchased(Museum.MUSEUM_ROCA_D_ANGERA)){
				purchasedMusuems.add(Museum.MUSEUM_ROCA_D_ANGERA);
			}
			
			
		}
		
		return purchasedMusuems;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	public static class AudioGuideInstallationState{
		public DownloadType downloadType;
		public InstallationState state;
	
		public AudioGuideInstallationState(DownloadType downloadType, 
				InstallationState state) {
			this.downloadType 	= downloadType;
			this.state 			= state;
		}
	}

	


}
