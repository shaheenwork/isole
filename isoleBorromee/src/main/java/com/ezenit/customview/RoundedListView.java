package com.ezenit.customview;

import com.ezenit.isoleborromee.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class RoundedListView extends ListView{
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	 private Path clipArea;
	 private static final PaintFlagsDrawFilter DRAW_FILTER = new PaintFlagsDrawFilter(1, Paint.ANTI_ALIAS_FLAG);
	// ===========================================================
	// Constructors
	// ===========================================================
	public RoundedListView(Context context)
	{
	        super(context);
	        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

    public RoundedListView(Context context, AttributeSet attr)
    {
        super(context, attr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH)
    {
        super.onSizeChanged(w, h, oldW, oldH);
        clipArea = new Path();
        RectF rect = new RectF(0, 0, w, h);

        int cornerRadius = getResources().getDimensionPixelSize(R.dimen.rounded_corners); // we should convert px to dp here
        clipArea.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        canvas.save();
        canvas.setDrawFilter(DRAW_FILTER);
        canvas.clipPath(clipArea);
        
        super.dispatchDraw(canvas);
        
        canvas.restore();
    }
    
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================


}
