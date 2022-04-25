package com.mapsaurus.paneslayout;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class Utils {
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19;
	private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;
	private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;
	
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
	public static Point getDeviceSize(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
	    Point point = new Point();

	    if (Build.VERSION.SDK_INT >= 17){
	        //new pleasant way to get real metrics
	        DisplayMetrics realMetrics = new DisplayMetrics();
	        display.getRealMetrics(realMetrics);
	        point.x = realMetrics.widthPixels;
	        point.y = realMetrics.heightPixels;

	    } else if (Build.VERSION.SDK_INT >= 14) {
	        //reflection for this weird in-between time
	        try {
	            Method mGetRawH = Display.class.getMethod("getRawHeight");
	            Method mGetRawW = Display.class.getMethod("getRawWidth");
	            point.x = (Integer) mGetRawW.invoke(display);
	            point.y = (Integer) mGetRawH.invoke(display);
	        } catch (Exception e) {
	            //this may not be 100% accurate, but it's all we've got
	        	point.x = display.getWidth();
	        	point.y = display.getHeight()+getStatusBarHeight(activity);
	            Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
	        }

	    } else {
	        //This should be close, as lower API devices should not have window navigation bars
	    	point.x = display.getWidth();
	    	point.y = display.getHeight();
	    }
	    return point;
	}
	
	private static int getStatusBarHeight(Activity activity) {
		  int result = 0;
		  int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = activity.getResources().getDimensionPixelSize(resourceId);
		  }
		  else{
			  DisplayMetrics outMetrics = new DisplayMetrics();
			  activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
			  result = getNormalStatusHeight(outMetrics);
		  }
		  return result;
	}
	
	private static int getNormalStatusHeight(DisplayMetrics displayMetrics){
		int statusBarHeight = 0;
		switch (displayMetrics.densityDpi) {
		    case DisplayMetrics.DENSITY_HIGH:
		        statusBarHeight = HIGH_DPI_STATUS_BAR_HEIGHT;
		        break;
		    case DisplayMetrics.DENSITY_MEDIUM:
		        statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
		        break;
		    case DisplayMetrics.DENSITY_LOW:
		        statusBarHeight = LOW_DPI_STATUS_BAR_HEIGHT;
		        break;
		    default:
		        statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
		}
		return statusBarHeight;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
