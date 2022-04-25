package com.ezenit.isoleborromee;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ezenit.customview.VerticalSeekBar;
import com.ezenit.isoleborromee.AppIsole.DownloadType;
import com.ezenit.isoleborromee.db.DBHelper;
import com.ezenit.isoleborromee.db.table.TableAudioGuide;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.AudioGuide;
import com.ezenit.isoleborromee.db.table.TableAudioGuide.BaseAudioGuide;
import com.ezenit.isoleborromee.db.table.TableDownloadQueue.DownloadQueue;
import com.ezenit.isoleborromee.db.table.TableMuseum.Museum;
import com.ezenit.isoleborromee.db.table.TablePhotoGallery.GalleryImage;
import com.ezenit.isoleborromee.fragments.FragmentAlert;
import com.ezenit.isoleborromee.fragments.FragmentAudioDetail;
import com.ezenit.isoleborromee.fragments.FragmentAudioList;
import com.ezenit.isoleborromee.fragments.FragmentBuyAll;
import com.ezenit.isoleborromee.fragments.FragmentDownloadView;
import com.ezenit.isoleborromee.fragments.FragmentDownloadViewer;
import com.ezenit.isoleborromee.fragments.FragmentMuseumItems;
import com.ezenit.isoleborromee.fragments.FragmentMuseumSelection;
import com.ezenit.isoleborromee.fragments.FragmentPhotoGrid;
import com.ezenit.isoleborromee.service.AppIsoleReceiver;
import com.ezenit.mediaplayer.AudioGuideTimeline;
import com.ezenit.mediaplayer.PlaybackActivity;
import com.ezenit.mediaplayer.PlaybackService;
import com.ezenit.mediaplayer.QueryTask;
import com.ezenit.utils.MiscUtils;
import com.ezenit.utils.iab.IabHelper;
import com.ezenit.utils.iab.IabResult;
import com.ezenit.utils.iab.Inventory;
import com.ezenit.utils.iab.Purchase;
import com.ezenit.utils.iab.SkuDetails;
import com.ezenit.utils.iab.IabHelper.QueryInventoryFinishedListener;
import com.mapsaurus.paneslayout.PanesSizer.PaneSizer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/********
 * Foolish MARIO had asked to disable click on isole madre and rocca d angera so search for this code
 * 
 * if(museum.equals(Museum.MUSEUM_ISOLA_MADRE)||museum.equals(Museum.MUSEUM_ROCA_D_ANGERA))
 *
 * and delete it when he asks to :)					
 * ***/
public class ActivityAudioPlayer extends PlaybackActivity 
			implements FragmentAudioDetail.OnAudioDetailClickListener,
				FragmentMuseumSelection.OnMuseumSelectListener,
				FragmentMuseumItems.OnMuseumItemClickListener,
				FragmentDownloadView.FragmentDownloadListener,
				FragmentPhotoGrid.FragmentPhotoGridListener, 
				FragmentAudioList.OnAudioClickListener,
				FragmentAlert.OnDialogClickListener,
				FragmentBuyAll.FragmentBuyAllListener,
				SeekBar.OnSeekBarChangeListener{

	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final String TAG 				 = ActivityAudioPlayer.class.getName();
	
	private static final int REQ_CODE_LANG_SELECTION = 101;
	private static final int REQ_CODE_MAP			 = 102;
	
	private static final String TAG_MUSUEM_SELECTION = "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_MUSEUM_SELECTION";
	private static final String TAG_MUSUEM_ITEMS	 = "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_MUSEUM_ITEMS";
	private static final String TAG_PHOTO_GRID	 	 = "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_PHOTO_GRID";
	
	private static final String TAG_AUDIO_GUIDE	 	 	= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_AUDIO_GUIDE";
	private static final String TAG_AUDIO_GUIDE_DETAIL	= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_AUDIO_GUIDE_DETAIL";
	
	private static final String TAG_MAP				 	= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_MAP";
	
	private static final String TAG_DOWNLOAD		 	= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_DOWNLOAD";
	
	private static final String TAG_DOWNLOAD_PROGRESS	= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_DOWNLOAD_PROGRESS";
	private static final String TAG_DOWNLOAD_ALL 		= "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_DOWNLOAD_ALL";

    public static final String TAG_BUY_ALL = "com.ezenit.isoleborromee.ActivityAudioPlayer.TAG_BUY_ALL";
	
	private static final String EXTRA_SELECTION_MUSUEUM = "com.ezenit.isoleborromee.ActivityAudioPlayer.EXTRA_SELECTION_MUSUEUM";
	
	/**
	 * Update the seekbar progress with the current song progress. This must be
	 * called on the UI Handler.
	 */
	private static final int MSG_UPDATE_PROGRESS 	 = 10;
	
	
	// (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;
    
    public static final String ACTION_CHANGE_LANGUAGE = "com.ezenit.isoleborromee.ActivityAudioPlayer.ACTION_CHANGE_LANGUAGE";
    public static final String ACTION_PURCHASE_RESTORED = "com.ezenit.isoleborromee.ActivityAudioPlayer.ACTION_PURCHASE_RESTORED";
    

	// ===========================================================
	// Fields
	// ===========================================================
//	private String 		language;
	private View   		layoutAudio;
	
	private ImageView 	imgMuseum;
	private ImageLoader imgLoader;
	private ProgressBar imgProgress;
	private TextView 	fieldMuseum;
	private TextView	fieldTitle;
	private TextView    fieldPosition;
	
	private long		selectedmusuem;
	
	private boolean 	isTablet;
	private TextView    elapsedView;
	private TextView	durationView;
	
	private SeekBar		seekBar;
	private long 		mDuration;
	private boolean 	mSeekBarTracking;
	private boolean 	mPaused;
	
//	private String 		strStanza;
	
	
	private IabHelper   mHelper;
	
	private OnChangeReceiver onChangeReceiver;
	
	/**
	 * Cached StringBuilder for formatting track position.
	 */
	private final StringBuilder mTimeBuilder = new StringBuilder();
	
	/**
	 * The PaneSizer allows you to programatically size each pane.
	 * 
	 * Type is an arbitrary integer that represents the type of view or fragment 
	 * contained within a pane.
	 * 
	 * In this example, there are only two types, 'default' and 'unknown.'
	 * Panes associated with ExampleFragments are given type 'default.'
	 * 
	 * Certain panes can also be focused. This means that swipe gestures will
	 * not be detected on those panes.
	 */
	private class ExamplePaneSizer implements PaneSizer {
		private static final int DEFAULT_PANE_TYPE = 0;
		private static final int PHOTOGRID_PANE_TYPE = 1;

		
		
		@Override
		public int getWidth(int index, int type, int parentWidth, int parentHeight) {
			int panewidth = (int) (0.5 * parentWidth);
			if (parentWidth > parentHeight) {
				if (type == DEFAULT_PANE_TYPE && index == 0)
					return panewidth;
				else if (type == DEFAULT_PANE_TYPE)
					return panewidth;
				else if(type == PHOTOGRID_PANE_TYPE){
					int columnNumbers = FragmentPhotoGrid.NUM_COLUMNS;
					if(panewidth%columnNumbers!=0){
						panewidth = ((panewidth/columnNumbers)+1)*columnNumbers;
					}
					return panewidth;
				}
					
				else throw new IllegalStateException("Pane has unknown type");
			} else {
				if (type == DEFAULT_PANE_TYPE && index == 0)
					return panewidth;
				else if (type == DEFAULT_PANE_TYPE)
					return panewidth;
				else if(type == PHOTOGRID_PANE_TYPE){
					int columnNumbers = FragmentPhotoGrid.NUM_COLUMNS;
					if(panewidth%columnNumbers!=0){
						panewidth = ((panewidth/columnNumbers)+1)*columnNumbers;
					}
					return  panewidth;
				}
				else throw new IllegalStateException("Pane has unknown type");
			}
		}

		@Override
		public int getType(Object o) {
			if (o instanceof FragmentPhotoGrid)
				return PHOTOGRID_PANE_TYPE;
			
			return DEFAULT_PANE_TYPE;
//			else return UNKNOWN_PANE_TYPE;
		}

		@Override
		public boolean getFocused(Object o) {
//			if(o instanceof FragmentAudioDetail)
//				return true;
				
			return false;
		}
	}
	
	@Override
	public float getSmallestWidthForTabletLayout() {
		// TODO Auto-generated method stub
		return 800;
	}
	
	// ===========================================================
	// External Accessors
	// ===========================================================
	
	public static void updateToLanguageChange(Context context){
		Intent intent = new Intent(ACTION_CHANGE_LANGUAGE);
		context.sendBroadcast(intent);
	}
	
	public static void updateRestorePurchase(Context context){
		Intent intent = new Intent(ACTION_PURCHASE_RESTORED);
		context.sendBroadcast(intent);
	}
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public static void startActivity(Context context) {
		Intent intent = new Intent(context,ActivityAudioPlayer.class);
		context.startActivity(intent);
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);				
		MiscUtils.changeLanguage(this);
		setContentView(R.layout.ac_main_audio);
		layoutAudio = findViewById(R.id.layoutAudioPlayer);
		imgLoader = ImageLoader.getInstance();
		View btnAutoPlay = findViewById(R.id.btnAutoPlay);
		btnAutoPlay.setTag(1);
		btnAutoPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				toggleAutoPlay();
			}
		});
		init();
				
		//Tablet is selected
		if(findViewById(R.id.layoutSidePanel)!=null){
//			MiscUtils.showToast(this, "Loading large screen");
			initTabletControls(savedInstanceState);
			getSupportActionBar().hide();
			isTablet = true;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else{
//			MiscUtils.showToast(this, "Loading phone Screen");
			initPhoneControls();
			isTablet = false;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
			
		if(AppIsole.DEBUG_MODE){
			enableDebugFeatures();
		}
		MiscUtils.hideKeyBoard(this, findViewById(R.id.root));
		
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		onChangeReceiver = new OnChangeReceiver();
		IntentFilter filter = new IntentFilter(ACTION_CHANGE_LANGUAGE);
		filter.addAction(ACTION_PURCHASE_RESTORED);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(onChangeReceiver, filter);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(onChangeReceiver!=null)
			unregisterReceiver(onChangeReceiver);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(isTablet)
			updateElapsedTime();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putLong(EXTRA_SELECTION_MUSUEUM, selectedmusuem);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onServiceReady() {
		// TODO Auto-generated method stub
		super.onServiceReady();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (isTablet&&fromUser) {
			elapsedView.setText(DateUtils.formatElapsedTime(mTimeBuilder, progress * mDuration / 1000000));
			PlaybackService.get(this).seekToProgress(progress);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				updateElapsedTime();
				break;
	
			default:
				super.handleMessage(msg);
		}
		return true;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mSeekBarTracking = true;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mSeekBarTracking = false;
	}
	


	private void enableDebugFeatures() {
		View btnExport = findViewById(R.id.btnExport);
		btnExport.setVisibility(View.VISIBLE);
		btnExport.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DBHelper.exportDB(ActivityAudioPlayer.this);
			}
		});
		View btnClear = findViewById(R.id.btnClear);
		btnClear.setVisibility(View.VISIBLE);
		btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AppIsole.clearAppData(ActivityAudioPlayer.this);
			}
		});
	}





	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		MiscUtils.disableABCShowHideAnimation(getSupportActionBar());
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Pass on the activity result to the helper for handling
        if (mHelper==null||!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            onActivityResultInActivity(requestCode, resultCode, data);
        }
        else {
//            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
		
	}
	
	
	/// map relatd 
	
	private void onActivityResultInActivity(int requestCode, int resultCode, Intent data){
		if(resultCode==RESULT_OK){
			if(requestCode==REQ_CODE_LANG_SELECTION){
				MiscUtils.changeLanguage(this);
			}
			else if(requestCode==REQ_CODE_MAP){
				BaseAudioGuide guide = ActivityMapContainer.getAudioGuide(data);
				
//				fm.popBackStack();
				addAudioDetailFragment(guide);
				FragmentManager fm = getSupportFragmentManager();
				FragmentAudioList fragmentAudioList = (FragmentAudioList) fm.findFragmentByTag(TAG_AUDIO_GUIDE);
				if(fragmentAudioList!=null&&isTablet){					
					fragmentAudioList.setSelection(guide);
					fragmentAudioList.scrollToSelection(guide);
				}
			}
		}
	}
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
	}
	
	@Override
	public void upSwipe() {
		
	}





	@Override
	public void downSwipe() {
		
	}
	
	//AudioDetail Click Listener
	@Override
	public void onAudioClicked(AudioGuide audio, boolean play) {
		PlaybackService service = PlaybackService.get(this);
		if(play){
			
			if(layoutAudio.getVisibility()==View.GONE)
				layoutAudio.setVisibility(View.VISIBLE);
//			AudioGuide currentGuide = service.getAudioGuide(0);
//			if(currentGuide==null||currentGuide.getId()!=audio.getId()){
					
//			service.clearQueue();
			
			QueryTask queryTask = new QueryTask();
			queryTask.museum = audio.getMuseum();
			queryTask.language = audio.getLanguageShort();
			
			queryTask.mode = AudioGuideTimeline.MODE_PLAY_ID_FIRST;
			queryTask.data = audio.getId();
			service.addSongs(queryTask);
//			new Handler().postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					setState(PlaybackService.FLAG_PLAYING);
//				}
//			}, 1000);
			
			checkStatusAndPlay();
			onServiceReady();
//			}
//			setState(service.play());
//			service.updateChildActivities();
			
		}
		else{
			setState(service.pause());
			service.userActionTriggered();
//			onStateChange(~PlaybackService.FLAG_PLAYING, 1);
//			service.updateChildActivities();
		}
	}
	
	

	@Override
	public void onMapClicked(AudioGuide museum) {
//		addMapFragment(guide);
	}

	@Override
	public void onAudioGalleryClicked(int index, AudioGuide guide) {
		// TODO Auto-generated method stub
		addImageGallery(index,guide);
	}

	
		
	//Museum Selection Listener
	@Override
	public void OnMuseumSelected(Museum museum) {
		if(!isTablet){
			PlaybackService service = PlaybackService
					.get(ActivityAudioPlayer.this);
			AudioGuide guide = service
								.getAudioGuide(0);
			if(guide!=null){
				if(!guide.getMuseum().equals(museum)||
						service.getTimelinePosition()==-1){
					stopAndHidePlayer(service);
				}
				else{
					layoutAudio.setVisibility(View.VISIBLE);
					updateAudioPlayer(guide);
				}
			}
			else{
				layoutAudio.setVisibility(View.GONE);
			}
		}
		addMuseumFragment(museum,false);
	}
	
	@Override
	public void onMuseumFragmentStart() {
		layoutAudio.setVisibility(View.GONE);
	}

	@Override
	public void onMuseumFragmentStop() {
//		if(PlaybackService.get(ActivityAudioPlayer.this).getAudioGuide(0)!=null
//				){
//			
//		}
	}

	
	@Override
	public void OnSettingClicked() {
		ActivitySettings.startActivity(this,true);		
	}
	
	@Override
	public boolean onMuseumItemClicked(DownloadType type, Museum museum,
			String language, boolean isFree) {
		switch (type) {
			case AUDIO_GUIDE:
				if(museum.equals(Museum.MUSEUM_ROCA_D_ANGERA))
					return false;
				if(isTablet){
					if(AppIsole.isInstalled(museum, DownloadType.AUDIO_GUIDE,language)){
						addAudioGuideFragment(museum, language, isFree);
						
					}
					else{
						addDownloadViewFragment(museum,language,DownloadType.AUDIO_GUIDE);
					}
					return true;
					
				}
				else{
					addAudioGuideFragment(museum,language,isFree);
				}
				break;
			case PHOTO_GALLERY:
				if(isTablet){
					if(AppIsole.isInstalled(museum, DownloadType.PHOTO_GALLERY,language)){
						addPhotoGridFragment(museum,language);
					}
					else{
						addDownloadViewFragment(museum,language,DownloadType.PHOTO_GALLERY);
					}
					return true;
				}						
				else
					addPhotoGalleryFragment(museum,language);
				break;
			default:
				break;
		}
		return false;
	}
	
	@Override
	public void onBuyMuseumFromMuseumItem(Museum museum) {
//		if(AppIsole.isSingleItemPurchsed()){
			buyMuseum(museum);
//		}
//		else{
//			showFragmentBuyAll(museum);
//		}
	}
	
	@Override
	public void onBuyMuseumFromFragmentBuyAll(Museum museum) {
		dismissFragmentBuyAll();
		buyMuseum(museum);
	}

	@Override
	public void onBuyAllFromFragmentBuyAll() {
		dismissFragmentBuyAll();
		buyAll();
	}
	
	
	
	private void showFragmentBuyAll(Museum museum) {
		FragmentBuyAll fragmentBuyAll = (FragmentBuyAll) getSupportFragmentManager()
				.findFragmentByTag(TAG_BUY_ALL);
		if(fragmentBuyAll==null)
			fragmentBuyAll = FragmentBuyAll.getInstance(museum);
		else{
			fragmentBuyAll.dismiss();
			fragmentBuyAll.update(museum);
		}
		fragmentBuyAll.show(getSupportFragmentManager(), TAG_BUY_ALL);
	}
	
	private void dismissFragmentBuyAll(){
		FragmentBuyAll fragment = (FragmentBuyAll) getSupportFragmentManager()
				.findFragmentByTag(TAG_BUY_ALL);
		if(fragment!=null)
			fragment.dismiss();
	}

	//Fragment Download Listener
	@Override
	public void onDownloadCompleted(Museum museum, String language,
			DownloadType type) {
		switch (type) {
			case PHOTO_GALLERY:
				addPhotoGridFragment(museum, language);
				break;
			case AUDIO_GUIDE:
				addAudioGuideFragment(museum, language, false);
				break;
	
			default:
				break;
		}
	}
	
	@Override
	public void onBuyMuseumClicked(Museum museum) {
		buyMuseum(museum);
	}



	@Override
	public void onBuyAllClicked() {
		buyAll();
        
	}
	
	private void buyMuseum(Museum museum){
		String payload = "";

        mHelper.launchPurchaseFlow(this, museum.getSku(), RC_REQUEST,
                mPurchaseFinishedListener, payload);
	}
	
	private void buyAll(){
		mHelper.launchPurchaseFlow(this, Museum.MUSEUM_ALL_IN_ONE.getSku(), RC_REQUEST,
                mPurchaseFinishedListener, "");
	}
	
	
	
	//Fragment Photo Grid Listener
	@Override
	public void onPhotoClicked(int index, GalleryImage image) {
		ActivityPhotoGallery.startActivity(ActivityAudioPlayer.this, image,index);
	}
	
	//Fragment Audio Click Listener
	@Override
	public void onAudioClicked(FragmentAudioList fragment,BaseAudioGuide audioGuide) {
		// TODO Auto-generated method stub
		if(audioGuide==null)
			return;
		addAudioDetailFragment(audioGuide);
//		if(isTablet)
			fragment.setSelection(audioGuide);
	}

	@Override
	public void onMapClicked(Museum museum, String language,
			boolean isFree) {
//		addMapFragment(museum,language,isFree);
		startMapActivity(museum,language,isFree);
	}
	

	@Override
	public void onPositiveClicked(String tag) {
			
		//if the button is from buy all alert box
		//then we will start our downloader
		//to download all the museums that were purchased
		if(tag.equals(TAG_DOWNLOAD_ALL)){
			ArrayList<Museum> museums = AppIsole.getAllPurchases();
			TableDownloadQueue tableDownloadQueue = new TableDownloadQueue();
			for(Museum museum:museums){
				DownloadQueue queue = new DownloadQueue(museum, DownloadType.AUDIO_GUIDE
						, AppIsole.getAppLocaleAsStr());
				tableDownloadQueue.insertOrUpdate(queue);
			}
			showDownloadProgress();
			dismissDownloadAll();
		}
	}

	@Override
	public void onNegativeClicked(String tag) {
		if(tag.equals(TAG_DOWNLOAD_ALL)){
			dismissDownloadAll();
		}
	}
	
	
	
	
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	private void showDownloadProgress(){
		FragmentDownloadViewer downloadProgresser = (FragmentDownloadViewer) getSupportFragmentManager()
				.findFragmentByTag(TAG_DOWNLOAD_PROGRESS);
		if(downloadProgresser==null)
			downloadProgresser = FragmentDownloadViewer.getInstance();
		
		downloadProgresser.show(getSupportFragmentManager(), TAG_DOWNLOAD_PROGRESS);
	}
	
	private void showDownloadAll(){
		
		FragmentAlert fragmentDownloadAll = (FragmentAlert) getSupportFragmentManager().findFragmentByTag(TAG_DOWNLOAD_ALL);
		String negativeButton = getResources().getString(R.string.cancel);
		String positiveButton = getResources().getString(android.R.string.ok);
		if(fragmentDownloadAll==null)
				fragmentDownloadAll = FragmentAlert.newInstance(R.string.download_all_bought, negativeButton, positiveButton);
		
		
		fragmentDownloadAll.show(getSupportFragmentManager(), TAG_DOWNLOAD_ALL);
	}
	
	private void dismissDownloadAll(){
		FragmentAlert alert = (FragmentAlert) getSupportFragmentManager()
				.findFragmentByTag(TAG_DOWNLOAD_ALL);
		if(alert!=null)
			alert.dismiss();
	}
	
	private void init() {
		
		mPlayPauseButton = (ImageButton) findViewById(R.id.play_pause);
		mPlayPauseButton.setOnClickListener(this);
		imgMuseum = (ImageView) findViewById(R.id.imgAudio);
		imgProgress	= (ProgressBar) findViewById(R.id.imgProgress);
		fieldTitle = (TextView) findViewById(R.id.fieldTitle);
		fieldMuseum = (TextView) findViewById(R.id.fieldMuseum);
		fieldPosition = (TextView) findViewById(R.id.fieldPosition);
		
		mHelper	= new IabHelper(this, AppIsole.APP_KEY);
		Log.d("init ", "bibin");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
		
			@Override
			public void onIabSetupFinished(IabResult result) {
				 if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
					
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
//                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
                Log.d("onIabSetupFinished ", "bibin");
            	/*if(true){
           		 throw new NullPointerException("crashed by bibin"); 
           	}*/
           	
			}
		});
	}
	
	private QueryInventoryFinishedListener mGotInventoryListener = new QueryInventoryFinishedListener() {
		
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
			if(result.isSuccess()){
				Museum[] museums = Museum.getAllSkus();
				
				for(Museum museum:museums){
					 SkuDetails museumSkuDetails = inv.getSkuDetails(museum.getSku());			
					 if(museumSkuDetails!=null){
						 AppIsole.setPrice(museum,museumSkuDetails.getPrice());
						
					 }
					 Purchase purchase = inv.getPurchase(museum.getSku());
					 if(purchase!=null){
						 if(purchase.getPurchaseState()==0){
							 if(museum.equals(Museum.MUSEUM_ALL_IN_ONE)){
								 AppIsole.setPurchased(Museum.MUSEUM_ISOLA_BELLA, true);
								 AppIsole.setPurchased(Museum.MUSEUM_ISOLA_MADRE, true);
								 //anu changed
								 AppIsole.setPurchased(Museum.MUSEUM_ROCA_D_ANGERA, true);
							 }
							 AppIsole.setPurchased(museum, true);
							
						 }
					 }
					 updatePurchase();
					
				}
			}
		}
	};
	
	
	
	// Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                int errorResponse = result.getResponse();
                switch (errorResponse) {
					case IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
						if(purchase!=null){
	                		purchaseMuseum(purchase.getSku());
		                	updatePurchase();
	                	}
	                	else{
	                		String msg = getResources().getString(R.string.item_already_purchased);
	                		complain(msg);
	                	}
	                	return;
					case IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED:
					case IabHelper.IABHELPER_USER_CANCELLED:
						complain(getResources().getString(R.string.msg_inapp_user_cancelled));
						return;
					default:
						break;
				}
                
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain(getString(R.string.msg_inapp_verification_failed));
                return;
            }
           
            if (result.isSuccess()) {
                String sku = purchase.getSku();
                purchaseMuseum(sku);
                updatePurchase();
            }

            
        }
    };
    
    private void purchaseMuseum(String sku){
    	Museum[] museums = Museum.getAllSkus();
        for(Museum museum:museums){
        	if(museum.getSku().equals(sku)){
        		if(Museum.MUSEUM_ALL_IN_ONE.equals(museum)){
        			AppIsole.setPurchased(Museum.MUSEUM_ISOLA_BELLA, true);
        			AppIsole.setPurchased(Museum.MUSEUM_ISOLA_MADRE, true);
        			AppIsole.setPurchased(Museum.MUSEUM_ROCA_D_ANGERA, true);
        			
        			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
        					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ISOLA_BELLA,
        					AppIsole.getAppLocaleAsStr());
        			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
        					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ISOLA_MADRE,
        					AppIsole.getAppLocaleAsStr());
        			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
        					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ROCA_D_ANGERA,
        					AppIsole.getAppLocaleAsStr());
        			AppIsole.setSingleItemPurchsed(true);
        			showDownloadAll();
        			
        		}
        		else{
        			AppIsole.setPurchased(museum, true);
        			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this, DownloadType.AUDIO_GUIDE, museum, AppIsole.getAppLocaleAsStr());
        			AppIsole.setSingleItemPurchsed(true);
        		}
        	}
        }
    }
    
    // Called when consumption is complete
//    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
//        public void onConsumeFinished(Purchase purchase, IabResult result) {
////            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
//
//            // if we were disposed of in the meantime, quit.
//            if (mHelper == null) return;
//
//            if (result.isSuccess()) {
////                Log.d(TAG, "Consumption successful. Provisioning.");
//                String sku = purchase.getSku();
//                Museum[] museums = Museum.getAllSkus();
//                for(Museum museum:museums){
//                	if(museum.getSku().equals(sku)){
//                		if(Museum.MUSEUM_ALL_IN_ONE.equals(museum)){
//                			AppIsole.setPurchased(Museum.MUSEUM_ISOLA_BELLA, true);
//                			AppIsole.setPurchased(Museum.MUSEUM_ISOLA_MADRE, true);
//                			AppIsole.setPurchased(Museum.MUSEUM_ROCA_D_ANGERA, true);
//                			
//                			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
//                					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ISOLA_BELLA,
//                					AppIsole.getAppLocaleAsStr());
//                			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
//                					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ISOLA_MADRE,
//                					AppIsole.getAppLocaleAsStr());
//                			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this,
//                					DownloadType.AUDIO_GUIDE, Museum.MUSEUM_ROCA_D_ANGERA,
//                					AppIsole.getAppLocaleAsStr());
//                			AppIsole.setSingleItemPurchsed(true);
//                			
//                			
//                		}
//                		else{
//                			AppIsole.setPurchased(museum, true);
//                			AppIsoleReceiver.addToQueue(ActivityAudioPlayer.this, DownloadType.AUDIO_GUIDE, museum, AppIsole.getAppLocaleAsStr());
//                			AppIsole.setSingleItemPurchsed(true);
//                		}
//                	}
//                }
//            }
//            else {
//                complain("Error while consuming: " + result);
//            }
////            Log.d(TAG, "End consumption flow.");
//        }
//    };
    
    void complain(String message) {
        alert(message);
    }

    void alert(String message) {
    	
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
//        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
//		if(isTablet){
		if(layoutAudio!=null){	
//			((TextView)findViewById(R.id.btnIsolaBella)).setText(R.string.isola_bella);
//			((TextView)findViewById(R.id.btnIsolaMadre)).setText(R.string.isola_madre);
//			((TextView)findViewById(R.id.btnRoccaDAngera)).setText(R.string.rocca_d_angera);
			if(isTablet) ((TextView)findViewById(R.id.btnSettings)).setText(R.string.settings);
			
			((TextView)findViewById(R.id.lblAutomatic)).setText(R.string.automatic);
			
			TextView autoPlay = (TextView) findViewById(R.id.lblPlay);
			Integer status  = (Integer) autoPlay.getTag(); 
				
			
			if(status==null||status==1){
				
				autoPlay.setText(R.string.play_on);
			}
			else{
				autoPlay.setText(R.string.play_off);
			}
			

			
		}
		
		if(PlaybackService.hasInstance()) {
			//
			try {
				//
				PlaybackService service = PlaybackService.get(this);
				AudioGuide guide = service.getAudioGuide(0);
				//
				if(guide!=null) {
					//
					Museum museum = guide.getMuseum();
					String language = AppIsole.getAppLocaleAsStr();
					//
					if(!guide.getLanguageShort().equals(language)) {
						//
						stopAndHidePlayer(service);
					     AudioGuide tableAudioGuide = TableAudioGuide.getAudioGuide(guide.getCodeNo(), language, museum, guide.isFree());
						//
					     if (tableAudioGuide != null) {
					    	 //
					    	 _updateTitle(tableAudioGuide.getTitle());
							_updateNameId(museum.getNameId());
					     }  
					}
				}
			} catch (Exception e) {
				Log.e("", "ActivityAudioPlayer.onConfigurationChanged");
				e.printStackTrace();
			} 
		} 
	}
	
	private final void _updateTitle(String title){
		if(title==null) title ="This is null tile";
		((TextView)findViewById(R.id.fieldTitle)).setText(title); 
	}
	
	private final void _updateNameId(int nameId){
		((TextView)findViewById(R.id.fieldMuseum)).setText(nameId); 
	}
	
	
	private void stopAndHidePlayer(PlaybackService service){
		if(layoutAudio!=null)
			layoutAudio.setVisibility(View.GONE);
		if(service!=null){
			service.pause();
			service.clear();
			
		}
	}
	
	private void updateAudioPlayer(BaseAudioGuide guide){
		
		_updateTitle(guide.getTitle());
		_updateNameId(guide.getMuseum().getNameId());
	
		imgLoader.displayImage("file://"+guide.getCoverArtPath(this), imgMuseum, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				imgProgress.setVisibility(View.VISIBLE);					
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				imgProgress.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				// TODO Auto-generated method stub
				imgProgress.setVisibility(View.GONE);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				imgProgress.setVisibility(View.GONE);
			}
		});
	}
	
	private void initTabletControls(Bundle savedInstanceState) {
		initPane(savedInstanceState);
		setPaneSizer(new ExamplePaneSizer());
		if(AppIsole.DEBUG_MODE||!AppIsole.isLanguageSelected()){
			ActivityLanguageSelection.startActivity(this
					, REQ_CODE_LANG_SELECTION, false);
		}
		
		final View btnIsolaBella = findViewById(R.id.btnIsolaBella);
		final View btnIsolaMadre = findViewById(R.id.btnIsolaMadre);
		final View btnRoccaDAngera  = findViewById(R.id.btnRoccaDAngera);
		final View btnSettings	 = findViewById(R.id.btnSettings);
		final ImageView bgView	 = (ImageView) findViewById(R.id.bgWallpaper);
		final int  baseColor	 = getResources().getColor(R.color.base_color);
		
		elapsedView				 = (TextView) findViewById(R.id.elapsed);
		durationView = (TextView)findViewById(R.id.duration);
		
		seekBar = (SeekBar)findViewById(R.id.seek_bar);
		seekBar.setMax(1000);
		seekBar.setOnSeekBarChangeListener(this);
		
		final PopupWindow window = new PopupWindow(this);
		View popup = getLayoutInflater().inflate(R.layout.pu_vertical_bar, (ViewGroup) findViewById(R.id.root),false);
		popup.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		int height = popup.getMeasuredHeight();
		int width = popup.getMeasuredWidth();
		window.setBackgroundDrawable(new BitmapDrawable());
		window.setHeight(height);
		window.setWidth(width);
		window.setContentView(popup);
		window.setOutsideTouchable(true);
		window.setFocusable(true);
		
		final VerticalSeekBar volumeSeekBar = (VerticalSeekBar) popup.findViewById(R.id.seekVolume);
		volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(PlaybackService.hasInstance()){
					PlaybackService service = PlaybackService.get(ActivityAudioPlayer.this);
					service.updateVolume(progress);
				}
				else{
					PlaybackService.updateVolume(ActivityAudioPlayer.this,progress);
				}
					
			}
		});
		
		
		
		View btnVolumeControl = findViewById(R.id.btnVolumeControl);
		btnVolumeControl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int volume = PlaybackService.getVolume(ActivityAudioPlayer.this);
				volumeSeekBar.setProgress(volume);
				window.showAsDropDown(v);
			}
		});
		
		btnIsolaBella.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Museum museum = Museum.MUSEUM_ISOLA_BELLA;
				if(selectedmusuem!=museum.getId()){
					bgView.setImageBitmap(null);
					bgView.setBackgroundColor(baseColor);
					
					addMuseumFragment(museum,true);
					btnIsolaBella.setSelected(true);
					btnIsolaMadre.setSelected(false);
					btnRoccaDAngera.setSelected(false);
					selectedmusuem = museum.getId();
					
					
				}
			}
		});
		
		btnIsolaMadre.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Museum museum = Museum.MUSEUM_ISOLA_MADRE;
				if(selectedmusuem!=museum.getId()){
					bgView.setImageBitmap(null);
					bgView.setBackgroundColor(baseColor);
					
					addMuseumFragment(museum,true);
					btnIsolaBella.setSelected(false);
					btnIsolaMadre.setSelected(true);
					btnRoccaDAngera.setSelected(false);
					selectedmusuem = museum.getId();
				}
			}
		});
		
		btnRoccaDAngera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Museum museum = Museum.MUSEUM_ROCA_D_ANGERA;
				if(selectedmusuem!=museum.getId()){
					bgView.setImageBitmap(null);
					bgView.setBackgroundColor(baseColor);
					
					addMuseumFragment(museum,true);
					btnIsolaBella.setSelected(false);
					btnIsolaMadre.setSelected(false);
					btnRoccaDAngera.setSelected(true);
					selectedmusuem = museum.getId();
				}
			}
		});
		
		btnSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ActivitySettings.startActivity(ActivityAudioPlayer.this,false);
			}
		});
		
		long selectedMuseumId = -1;
		if(savedInstanceState!=null)
			selectedMuseumId = savedInstanceState.getLong(EXTRA_SELECTION_MUSUEUM,-1);
		if(selectedMuseumId!=-1){
			if(selectedMuseumId==Museum.MUSEUM_ISOLA_BELLA.getId()){
				btnIsolaBella.setSelected(true);
				btnIsolaMadre.setSelected(false);
				btnRoccaDAngera.setSelected(false);
			}
			else if(selectedMuseumId==Museum.MUSEUM_ISOLA_MADRE.getId()){
				btnIsolaBella.setSelected(false);
				btnIsolaMadre.setSelected(true);
				btnRoccaDAngera.setSelected(false);
			}
			else if(selectedMuseumId==Museum.MUSEUM_ROCA_D_ANGERA.getId()){
				btnIsolaBella.setSelected(false);
				btnIsolaMadre.setSelected(false);
				btnRoccaDAngera.setSelected(true);
			}
			selectedmusuem = selectedMuseumId;
		}
			
		setDuration(0);
		
	}
	
	private void initPhoneControls() {
		if(AppIsole.DEBUG_MODE||!AppIsole.isLanguageSelected()){
			ActivityLanguageSelection.startActivity(this
					, REQ_CODE_LANG_SELECTION, true);
		}
		addMuseumSelectionFragment();
	}

	




    /************************************************
     * Museum Selection Fragment
     * Shown in phone for selecting the museum
     *  ->Isola bella
     *  ->Isola madre
     *  ->Rocca d angera
     * **********************************************/
	private void addMuseumSelectionFragment() {
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentMuseumSelection fragment = (FragmentMuseumSelection) fm.findFragmentByTag(TAG_MUSUEM_SELECTION);
		
		if(fragment==null){
			fragment = FragmentMuseumSelection.getInstance();
			fm.beginTransaction()
			  .replace(R.id.fragContainer, fragment, TAG_MUSUEM_SELECTION)
			  .commit();
		}
	}
	
	
//	protected void startSettingsActivity() {
////		FragmentManager fm = getSupportFragmentManager();
////		FragmentSettings fragment = (FragmentSettings) fm.findFragmentByTag("Frag_settings");
////		if(fragment==null){
////			fragment = FragmentSettings.getInstance();
////			fm.beginTransaction().replace(R.id.fragContainer, fragment, "Frag_settings").addToBackStack(null).commit();
////		}
//		
//	}
//	


	private void addMuseumFragment(Museum museum,final boolean tablet){
		FragmentManager fm = getSupportFragmentManager();
		FragmentMuseumItems fragment = (FragmentMuseumItems) fm.findFragmentByTag(TAG_MUSUEM_ITEMS);
		if(fragment==null){
			fragment = FragmentMuseumItems.getInstance(museum, AppIsole.getAppLocaleAsStr(),tablet);
			if(!tablet){
				fm.beginTransaction().replace(R.id.fragContainer, fragment, TAG_MUSUEM_ITEMS).addToBackStack(null).commit();
			}
			else
				setMenuFragment(fragment);
		}
		else{
			fragment.update(museum,AppIsole.getAppLocaleAsStr(),tablet);
		}
	}
	
	private void updatePurchase(){
		if(isTablet){
			FragmentDownloadView fragment = (FragmentDownloadView) getSupportFragmentManager()
											.findFragmentByTag(TAG_DOWNLOAD);
			if(fragment!=null){
				fragment.updateDownloadButtons();
			}
		}
		else{
			FragmentMuseumItems fragment = (FragmentMuseumItems) getSupportFragmentManager()
					.findFragmentByTag(TAG_MUSUEM_ITEMS);
			if(fragment!=null){
				fragment.setUpDownloadButtons();
			}
		}
	}

	protected void addDownloadViewFragment(Museum museum, String language,
			DownloadType downloadType) {
		Fragment prevFragment = getMenuFragment();
		if(prevFragment!=null&&prevFragment instanceof FragmentMuseumItems){
			FragmentManager fm = getSupportFragmentManager();
			FragmentDownloadView fragment = (FragmentDownloadView) fm.findFragmentByTag(TAG_DOWNLOAD);
			if(fragment==null){
				fragment = FragmentDownloadView.getInstance(museum, language,downloadType);
				addFragment(prevFragment, fragment,TAG_DOWNLOAD);
			}
			else{
				fragment.update(museum,language,downloadType);
			}
		}
	}

	protected void addPhotoGridFragment(Museum museum, String language) {
		Fragment prevFragment = getMenuFragment();
		if(prevFragment!=null&&prevFragment instanceof FragmentMuseumItems){
			FragmentManager fm = getSupportFragmentManager();
			FragmentPhotoGrid fragment = (FragmentPhotoGrid) fm.findFragmentByTag(TAG_PHOTO_GRID);
			if(fragment==null){
				fragment = FragmentPhotoGrid.getInstance(museum, language);
				addFragment(prevFragment, fragment,TAG_PHOTO_GRID);
			}
		}
	}

	protected void addPhotoGalleryFragment(Museum museum,String language) {
		ActivityPhotoGallery.startActivity(ActivityAudioPlayer.this, museum,language);
	}





	protected void addAudioGuideFragment(Museum musuem,String language,boolean isFree) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentAudioList fragment = (FragmentAudioList) fm.findFragmentByTag(TAG_AUDIO_GUIDE);
		if(fragment==null){
			fragment = FragmentAudioList.getInstance(musuem, language, isFree);
			if(!isTablet)
				fm.beginTransaction().replace(R.id.fragContainer, fragment,TAG_AUDIO_GUIDE).addToBackStack(null).commit();
			else{
				Fragment prevFragment = getMenuFragment();
				if(prevFragment!=null&&prevFragment instanceof FragmentMuseumItems){
					addFragment(prevFragment, fragment, TAG_AUDIO_GUIDE);
				}
			}
				
		}
	}

	protected void addAudioDetailFragment(final BaseAudioGuide audioGuide) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentAudioDetail fragment = (FragmentAudioDetail) fm.findFragmentByTag(TAG_AUDIO_GUIDE_DETAIL);
		if(fragment==null){
			fragment = FragmentAudioDetail.getInstance(audioGuide,isAudioGuidePlaying(audioGuide));
			if(!isTablet)
				fm.beginTransaction().replace(R.id.fragContainer, fragment,TAG_AUDIO_GUIDE_DETAIL).addToBackStack(null).commit();
			else {
				Fragment prevFragment = fm.findFragmentByTag(TAG_AUDIO_GUIDE);
				if(prevFragment!=null)
					addFragment(prevFragment, fragment,TAG_AUDIO_GUIDE_DETAIL);
			}
		}
		else if(isTablet){
			fragment.updateInstance(audioGuide,isAudioGuidePlaying(audioGuide));
			Fragment prevFragment = fm.findFragmentByTag(TAG_AUDIO_GUIDE);
			if(prevFragment!=null)
				addFragment(prevFragment, fragment,TAG_AUDIO_GUIDE_DETAIL);
		}
		else{
			fragment.updateInstance(audioGuide,isAudioGuidePlaying(audioGuide));
		}
		
	}
	
	protected void addImageGallery(int index, AudioGuide guide) {
		ActivityFSAudioGallery.startActivity(this, index, guide);
	}

//map related 

	private void startMapActivity(Museum musuem,String language,boolean isFree){
		ActivityMapContainer.startActivity(this, REQ_CODE_MAP, musuem, language,isFree);
	}
	
	private boolean isAudioGuidePlaying(BaseAudioGuide audioGuide){
		if(PlaybackService.hasInstance()){
			PlaybackService service = PlaybackService.get(this);
			AudioGuide currentGuide = service.getAudioGuide(0);
			return (currentGuide!=null&&currentGuide.getId()==audioGuide.getId()
					&&mState==PlaybackService.FLAG_PLAYING);
			
		}
		return false;
	}

//	private void addMapFragment(Museum musuem,String language,boolean isFree){
//		final FragmentManager fm = getSupportFragmentManager();
//		FragmentMap fragment = (FragmentMap) fm.findFragmentByTag(TAG_MAP);
//		if(fragment==null){
//			fragment = FragmentMap.getInstance(1,musuem,language,isFree);
//			if(!isTablet)
//				fm.beginTransaction().replace(R.id.fragContainer, fragment,TAG_MAP).addToBackStack(null).commit();
//			else{
//				
//			}
//				
//		}
//		Fragment prevFragment = getMenuFragment();
//		if(prevFragment!=null)
//			addFragment(prevFragment, fragment,TAG_MAP);
//		
//		
//		
////		fragment.setMapClickListener(new OnMapClickListener() {
////			
////			@Override
////			public void onAudioClicked(BaseAudioGuide guide){
////				fm.popBackStack();
////				
////			}
////		});
//	}
	
	@Override
	protected void onStateChange(int state, int toggled) {
		super.onStateChange(state, toggled);
		FragmentManager fm = getSupportFragmentManager();
		FragmentAudioDetail  fragment = (FragmentAudioDetail) fm.findFragmentByTag(TAG_AUDIO_GUIDE_DETAIL);
		if(fragment!=null){
			
			BaseAudioGuide guide = PlaybackService.get(this).getAudioGuide(0);
			fragment.onStateChange(guide, isAudioGuidePlaying(guide));
			
			
			
		}
		if(isTablet)
			updateElapsedTime();
	}
	
	@Override
	protected void setSong(AudioGuide guide) {
		// TODO Auto-generated method stub
		super.setSong(guide);
	}
	
	@Override
	protected void onSongChange(final AudioGuide guide) {
		// TODO Auto-generated method stub
		super.onSongChange(guide);
		FragmentManager fm = getSupportFragmentManager();
		FragmentAudioDetail  fragment = (FragmentAudioDetail) fm.findFragmentByTag(TAG_AUDIO_GUIDE_DETAIL);
		if(fragment!=null){
			fragment.onSongChange(guide,isAudioGuidePlaying(guide));			
		}
		FragmentMuseumItems fragmentMuseum 	= (FragmentMuseumItems) fm.findFragmentByTag(TAG_MUSUEM_ITEMS);
		FragmentAudioList 	fragmentList 	= (FragmentAudioList) 	fm.findFragmentByTag(TAG_AUDIO_GUIDE);
		if(guide!=null&&guide.getLanguageShort().equals(AppIsole.getAppLocaleAsStr())&&PlaybackService.hasInstance()&&(fragmentMuseum!=null
				||fragmentList!=null||PlaybackService.get(this).getState()==PlaybackService.FLAG_PLAYING)){
			layoutAudio.setVisibility(View.VISIBLE);
			imgLoader.displayImage("file://"+guide.getCoverArtPath(this), imgMuseum, new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					imgProgress.setVisibility(View.VISIBLE);					
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					// TODO Auto-generated method stub
					imgProgress.setVisibility(View.GONE);
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					imgProgress.setVisibility(View.GONE);
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub
					imgProgress.setVisibility(View.GONE);
				}
			});
			
			
			
			fieldTitle.setText(guide.getTitle());
			fieldMuseum.setText(guide.getMuseum().getNameId());
			
			imgMuseum.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					jumpToAudioDetailFragment(guide);
				}
			});
			
			fieldTitle.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					jumpToAudioDetailFragment(guide);
				}
			});
			
			String strStanza = getResources().getString(R.string.cod_with_dot);
			
			if(!TableAudioGuide.hasChildren(guide)){
				fieldPosition.setText(Html.fromHtml(strStanza+" <b>"+guide.getCodeNo()+"</b>"));
			}
			else{
				
				BaseAudioGuide parentAudio = TableAudioGuide.getBaseAudioGuide(guide);
				fieldPosition.setText(Html.fromHtml(strStanza+" <b>"+guide.getCodeNo()
						+" ("+parentAudio.getTitle()+")</b>"));
			}
			
			if(isTablet){
				updateElapsedTime();
				setDuration(guide.getAudioDuration(this));
			}
			
		}
		else{
			if(isTablet){
				setDuration(0);
				updateElapsedTime();
			}
			layoutAudio.setVisibility(View.GONE);
		}
	}
	
	/****
	 * Jump to audio detail fragment from anywhere
	 * 
	 * @param guide The audio guide whose detail is to be shown
	 * */
	private void jumpToAudioDetailFragment(BaseAudioGuide guide){
		int btnId = -1;
		if(isTablet){
			if(guide.getMuseum().equals(Museum.MUSEUM_ISOLA_BELLA)) {
				btnId = R.id.btnIsolaBella;
			}
			else if(guide.getMuseum().equals(Museum.MUSEUM_ISOLA_MADRE)){
				btnId = R.id.btnIsolaMadre;
			}
			else if(guide.getMuseum().equals(Museum.MUSEUM_ROCA_D_ANGERA)){
				btnId = R.id.btnRoccaDAngera;
			}

			View btnMuseum	  	  = findViewById(btnId);
			btnMuseum.performClick();
			getSupportFragmentManager().executePendingTransactions();
			FragmentMuseumItems fragment = (FragmentMuseumItems) getMenuFragment();
			fragment.clickAudio();
			getSupportFragmentManager().executePendingTransactions();
			FragmentAudioList fragmentAudioList = (FragmentAudioList) getSupportFragmentManager().findFragmentByTag(TAG_AUDIO_GUIDE);
			onAudioClicked(fragmentAudioList, guide);
			
		}
		else{
			addMuseumFragment(guide.getMuseum(), isTablet);
			addAudioGuideFragment(guide.getMuseum(), guide.getLanguageShort(), guide.isFree());
			getSupportFragmentManager().executePendingTransactions();
			FragmentAudioList fragmentAudioList = (FragmentAudioList) getSupportFragmentManager().findFragmentByTag(TAG_AUDIO_GUIDE);
			onAudioClicked(fragmentAudioList, guide);
		}
	}
	
	private void toggleAutoPlay(){
		PlaybackService service = PlaybackService.get(this);
		String playOn = getResources().getString(R.string.play_on);
		TextView lblPlayStatus = (TextView) findViewById(R.id.lblPlay);
		if(lblPlayStatus.getText().toString().equals(playOn)){
			
			service.setFinishAction(AudioGuideTimeline.FINISH_STOP_CURRENT);
			lblPlayStatus.setText(R.string.play_off);
			lblPlayStatus.setTag(0);
		}
		else{
			service.setFinishAction(AudioGuideTimeline.MODE_PLAY);
			lblPlayStatus.setText(R.string.play_on);
			lblPlayStatus.setTag(1);
		}
	}
	
	private void checkStatusAndPlay(){
		PlaybackService service = PlaybackService.get(this);
		String playOn = getResources().getString(R.string.play_on);
		TextView lblPlayStatus = (TextView) findViewById(R.id.lblPlay);
		if(lblPlayStatus.getText().toString().equals(playOn)){
			service.setFinishAction(AudioGuideTimeline.MODE_PLAY);
		}
		else{
			service.setFinishAction(AudioGuideTimeline.FINISH_STOP_CURRENT);
		}
	}

	
	/**
	 * Update seek bar progress and schedule another update in one second
	 */
	private void updateElapsedTime()
	{
		if(isTablet){
			long position = PlaybackService.hasInstance() ? PlaybackService.get(this).getPosition() : 0;
	
			if (!mSeekBarTracking) {
				long duration = mDuration;
				seekBar.setProgress(duration == 0 ? 0 : (int)(1000 * position / duration));
			}
	
//			Log.e(TAG, "Next position is "+position);
			
			elapsedView.setText(DateUtils.formatElapsedTime(mTimeBuilder, position / 1000));
//			Log.e(TAG, "Status are "+mState+","+mPaused);
//			Log.e(TAG, "Condition is "+(mState & PlaybackService.FLAG_PLAYING));
			if (!mPaused && (mState & PlaybackService.FLAG_PLAYING) != 0) {
				// Try to update right after the duration increases by one second
				long next = 1050 - position % 1000;
				mUiHandler.removeMessages(MSG_UPDATE_PROGRESS);
				mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, next);
//				Log.e(TAG, "Next schedule time is "+next);
			}
		}
	}
	
	/**
	 * Update the current song duration fields.
	 *
	 * @param duration The new duration, in milliseconds.
	 */
	private void setDuration(long duration)
	{
		if(isTablet){
			mDuration = duration;
			durationView.setText(DateUtils.formatElapsedTime(mTimeBuilder, duration / 1000));
		}
	}
	
	@Override
	protected void onResumeFragments() {
		// TODO Auto-generated method stub
		super.onResumeFragments();
		MiscUtils.changeLanguage(ActivityAudioPlayer.this);
		if(isTablet)
			manageLanguageChange();
	}
	
	private void manageLanguageChange(){
	
		String language = AppIsole.getAppLocaleAsStr();
		FragmentAudioList audioList = (FragmentAudioList) getSupportFragmentManager()
				.findFragmentByTag(TAG_AUDIO_GUIDE);
		if(audioList!=null){
			Museum museum = Museum.getMuseum(selectedmusuem);
			DownloadType downloadType = DownloadType.AUDIO_GUIDE;
			
			if(!AppIsole.isInstalled(museum, downloadType, language)){
				addDownloadViewFragment(museum, language, downloadType);
			}
		}

		else{
//			FragmentDownloadView downloadView = (FragmentDownloadView) getSupportFragmentManager()
//					.findFragmentByTag(TAG_DOWNLOAD);
//			if(downloadView!=null){
//				Museum museum = Museum.getMuseum(selectedmusuem);
//				DownloadType downloadType = DownloadType.AUDIO_GUIDE;
//				if(AppIsole.isInstalled(museum, downloadType, language)){
//					addAudioGuideFragment(museum, language, false);
//				}
//			}
			
	
		}
		
		
		
	}
	
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class OnChangeReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ACTION_CHANGE_LANGUAGE)){
				if(isTablet){
//					manageLanguageChange();					
//					MiscUtils.changeLanguage(ActivityAudioPlayer.this);
				}
			}
			else if(intent.getAction().equals(ACTION_PURCHASE_RESTORED)){
				updatePurchase();
			}
		}
		
	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		
	}















}
