package com.ezenit.customview;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SwipeableViewPager extends ViewPager {
	
	// ===========================================================
	// Constants
	// ===========================================================
	
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean enabled = true;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	public SwipeableViewPager(Context context) {
	    super(context);
    }

    public SwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
   	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
    	if(enabled)
            return super.onInterceptTouchEvent(event);
    		
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return super.onTouchEvent(event);
    
    }
    
   
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
    
    
    
}