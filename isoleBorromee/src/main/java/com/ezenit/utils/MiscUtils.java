package com.ezenit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.AudioManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ezenit.isoleborromee.AppIsole;

public class MiscUtils {
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

	public static void showAsPopup(Activity activity,int layout,int width,int height) {
	    //To show activity as dialog and dim the background, you need to declare android:theme="@style/PopupTheme" on for the chosen activity on the manifest
//	    activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    
	    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = activity.getWindow().getAttributes(); 
	    params.height = height; //fixed height
	    params.width = width; //fixed width
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
		
	    activity.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
	    
	    activity.setContentView(layout);
	}
	
	public static void hideKeyBoard(final Activity activity,View view){

	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {
	        view.setOnTouchListener(new OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                hideSoftKeyboard(activity);
	                return false;
	            }

	        });
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {

	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

	            View innerView = ((ViewGroup) view).getChildAt(i);
	            hideKeyBoard(activity,innerView);
	        }
	    }
		
	}
	
	public static boolean isEarPieceConnected(Context context) {
        AudioManager audio = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (audio.isWiredHeadsetOn()) {
            return false;
        } else {

            return true;
        }
    }
	
	public static void hideSoftKeyboard(Activity activity) {
		if(activity.getCurrentFocus()!=null){
		    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	public static void changeLanguage(Activity activity){
		changeLanguage(activity, AppIsole.getAppLocale());
	}
	
	public static void changeLanguage(Activity activity,String langShort){
		changeLanguage(activity, LanguageConstants.getLocale(langShort));
	}
	
	public static void changeLanguage(Activity activity,Locale locale){
		Log.d("Log", "Changed locale to "+locale.getLanguage());
		Configuration newConfig = new Configuration();
		newConfig.locale = locale;
		Configuration oldConfig = activity.getResources().getConfiguration();
		oldConfig.updateFrom(newConfig);
		activity.getResources().updateConfiguration(oldConfig, activity.getResources().getDisplayMetrics());
		 Log.d("log", "onConfigurationChanged() Called");
		activity.onConfigurationChanged(oldConfig);
	}
	
	public static void changeTextColor(Menu menu, int itemId, int color){
		MenuItem item = menu.findItem(itemId);
		Object itemTitle = item.getTitle();
		SpannableString s = null;
			
		if(itemTitle instanceof SpannableString){
		   s =  (SpannableString) itemTitle;
		}else{
			s = new SpannableString((String) itemTitle);
		}
		if(s!=null){
			s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
			item.setTitle(s);
		}
	}
	
	public static StringBuilder reuseForBetterPerformance(final StringBuilder sb) {
	    sb.delete(0, sb.length());
	    return sb;
	}
	
	public static void showToast(Context context, int msgId){
		Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
	}
	
	public static void showToast(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

//	public static void enableHomeButton(Activity activity) {
//		ActionBar actionBar = activity.getActionBar();
//		actionBar.setHomeButtonEnabled(true);
//		actionBar.setDisplayHomeAsUpEnabled(true);
//		ImageView view = (ImageView)activity.findViewById(android.R.id.home);
//		int padding = (int) AppIsole.getPaddingHome();
//		view.setPadding(padding, 0, padding, 0);
//	}

    public static void setListViewHeightBasedOnChildren(ListView listView,float maxHeight) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int maxChild = 0;
        int itemCount = listAdapter.getCount();
        int childHeight =0;
        if(itemCount>0){
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            		, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//            listItem.measure(0, 0);
            childHeight = listItem.getMeasuredHeight();
            if(childHeight>0)
            	maxChild = (int) (maxHeight/childHeight); 
        }
      
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        if(itemCount>maxChild){
        	params.height = (int) maxHeight;
        }
        else{
        	params.height = (childHeight*itemCount) + (listView.getDividerHeight() * (itemCount - 1));
        }
        
        listView.setLayoutParams(params);
        listView.requestLayout();
    }     
    
    public static void disableABCShowHideAnimation(ActionBar actionBar) {
//        try
//        {
//            actionBar.getClass().getDeclaredMethod("setShowHideAnimationEnabled", boolean.class).invoke(actionBar, false);
//        }
//        catch (Exception exception)
//        {
//            try {
//                Field mActionBarField = actionBar.getClass().getSuperclass().getDeclaredField("mActionBar");
//                mActionBarField.setAccessible(true);
//                Object icsActionBar = mActionBarField.get(actionBar);
//                Field mShowHideAnimationEnabledField = icsActionBar.getClass().getDeclaredField("mShowHideAnimationEnabled");
//                mShowHideAnimationEnabledField.setAccessible(true);
//                mShowHideAnimationEnabledField.set(icsActionBar,false);
//                Field mCurrentShowAnimField = icsActionBar.getClass().getDeclaredField("mCurrentShowAnim");
//                mCurrentShowAnimField.setAccessible(true);
//                mCurrentShowAnimField.set(icsActionBar,null);
////               icsActionBar.getClass().getDeclaredMethod("setShowHideAnimationEnabled", boolean.class).invoke(icsActionBar, false);
//            }catch (Exception e){
//                //....
//            	e.printStackTrace();
//            }
//        }
    }
	
    public static PointF getBitmapSize(File file){
		PointF point = new PointF();
		try{
			FileInputStream inputStream = new FileInputStream(file);
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
			
			point.set(bitmapOptions.outWidth
					,bitmapOptions.outHeight) ;
			inputStream.close();
		}
		catch(IOException e){
			point.set(0, 0);
		}
		return point;
	}
    
    public static float getBitmapAspect(File file){
		float aspect = 0;
		try{
			FileInputStream inputStream = new FileInputStream(file);
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
			
			aspect = bitmapOptions.outWidth/(float)bitmapOptions.outHeight ;
			inputStream.close();
		}
		catch(IOException e){
			
		}
		return aspect;
	}
    
    
    
    public static final void setTitle(AppCompatActivity activity, String title, int subtitleId){
    	ActionBar actionBar = activity.getSupportActionBar();
    	actionBar.setTitle(title);
    	actionBar.setSubtitle(subtitleId);
    }
    
    public static final void setTitle(AppCompatActivity activity,int titleId,int subtitleId){
    	Resources resources = activity.getResources();
    	setTitle(activity, resources.getString(titleId), resources.getString(subtitleId));
    }
	
    public static final void setTitle(AppCompatActivity activity,String title,String subtitle){
    	ActionBar actionBar = activity.getSupportActionBar();
    	actionBar.setTitle(title);
    	actionBar.setSubtitle(subtitle);
  	 
    }
    
    public static final void setTitle(AppCompatActivity activity,int titleId){
    	ActionBar actionBar = activity.getSupportActionBar();
    	actionBar.setTitle(titleId);
  	 
    }
    

	public static CharSequence getVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
    
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
