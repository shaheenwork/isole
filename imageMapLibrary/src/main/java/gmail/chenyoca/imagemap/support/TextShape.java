package gmail.chenyoca.imagemap.support;

import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.animation.BounceInterpolator;

public class TextShape extends Shape {

	

	
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	private String text;
	private PointF center;
	private float  textSize;
	private Rect   textBounds;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public TextShape(String text,Object tag, int coverColor) {
		super(tag, coverColor);
		// TODO Auto-generated constructor stub
		this.text = text;
		this.textSize = 14;

		textBounds = new Rect();
	}
	
	public void setTextAlign(Align align){
		drawPaint.setTextAlign(align);
	}
	
	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
    /**
     * Set Center,radius
     * @param coords centerX,CenterY,radius
     */
    @Override
	public void setValues(float...coords){

        final float centerX = coords[0];
        final float centerY = coords[1];

        this.center = new PointF(centerX, centerY);

       updateBounds();

	}

    public void setText(String text) {
        this.text = text;
    }

  
    
    @Override
	public PointF getCenterPoint(){
		return center;
	}


	@Override
	public void draw(Canvas canvas) {
		drawPaint.setAlpha(alaph);
		updateBounds();
		canvas.drawText(this.text, center.x
				, center.y+textBounds.height()/2, drawPaint);
	}

	
	 
	@Override
	public void scaleBy (float scale, float centerX, float centerY) {
        PointF newCenter = ScaleUtility.scaleByPoint(center.x, center.y, centerX, centerY, scale);
        textSize *= scale;
        center.set(newCenter.x,newCenter.y);
        updateBounds();
	}

	@Override
	public void onScale(float scale){
//		scaleBy = (float)Math.sqrt(scaleBy);
		textSize *= scale;
		center.set( center.x *= scale , center.y *= scale );
		updateBounds();
	}

    @Override
    public void translate(float deltaX, float deltaY) {
        center.x += deltaX;
        center.y += deltaY;
    }

    @Override
    public boolean inArea(float x, float y) {
        
        return false;
    }
	// ===========================================================
	// Methods
	// ===========================================================
	
	
	
	public void updateBounds(){
		drawPaint.setTextSize(textSize);
		drawPaint.getTextBounds(text, 0, text.length(), textBounds);
		
	}

	

	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
