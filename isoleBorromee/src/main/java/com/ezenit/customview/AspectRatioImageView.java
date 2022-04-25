package com.ezenit.customview;


import com.ezenit.isoleborromee.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class AspectRatioImageView extends androidx.appcompat.widget.AppCompatImageView {

	
	private float aspectRatio;
	
    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttribute(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttribute(attrs);
    }
    
    public float getAspectRatio() {
		return aspectRatio;
	}
    

	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	
	private void parseAttribute(AttributeSet attrs){
		TypedArray a = getContext().getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.AspectRatioImageView,
		        0, 0);

		   try {
		       aspectRatio = a.getFloat(R.styleable.AspectRatioImageView_aspect
		    		   , -1.0f);
		   
		   } finally {
		       a.recycle();
		   }
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        
        if(aspectRatio==-1)
        	aspectRatio = getDrawable().getIntrinsicHeight() /(float) getDrawable().getIntrinsicWidth();
        int height = (int) (width * aspectRatio);
        setMeasuredDimension(width, height);
    }

	
}