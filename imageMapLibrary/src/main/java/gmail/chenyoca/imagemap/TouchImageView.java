package gmail.chenyoca.imagemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 *  * TouchImageView - A full View with Scale/Drag support.
 */
public class TouchImageView extends androidx.appcompat.widget.AppCompatImageView {

	private static final String TAG = TouchImageView.class.getName();
	
	private final Matrix imageUsingMatrix = new Matrix();
	private final Matrix imageSavedMatrix = new Matrix();
	private final Matrix overLayerMatrix = new Matrix();

	/* æƒ¯æ€§ç³»æ•° */
    private static final float FRICTION = 0.9f;

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	private int mode = NONE;

	private float redundantXSpace;
	private float redundantYSpace;

	private float right, bottom, origWidth, origHeight, bmWidth, bmHeight;

	/* View width and height */
    private float viewWidth;
	private float viewHeight;

	private PointF last = new PointF();
	private PointF mid = new PointF();
	private  PointF start = new PointF();

	private float[] matrixValues;

	/* Absolute position X
	 * Absolute position Y
	 * */
	private float absoluteOffsetX;
	private float absoluteOffsetY;

	private float saveScale = 1f;
	private float minScale = 1f;
	private float maxScale = 5f;
	private float oldDist = 1f;

	private PointF lastDelta = new PointF(0, 0);
	private float velocity = 0;
	private long lastDragTime = 0;
    
    private Context mContext;
    private ScaleGestureDetector mScaleDetector;

    public boolean onLeftSide = false, onTopSide = false, onRightSide = false, onBottomSide = false;
    
    public boolean reachedRightSide = false;
    public boolean reachedLeftSide  = false;
    
    private PointF currentTouch;
    
    private OnEdgeReachListener listener;
    
    

    public TouchImageView(Context context) {
        this(context,null);
    }
    
    public TouchImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        super.setClickable(true);
        this.mContext = context;
        initialized();
    }
    
    public void setOnEdgeListener(OnEdgeReachListener listener) {
		this.listener = listener;
	}
    
    
    
    private OnTouchListener touchListener = new OnTouchListener() {

    	final static float MAX_VELOCITY = 1.2f;
    	
    	private long dragTime ;
    	private float dragVelocity;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
            mScaleDetector.onTouchEvent(event);

            fillAbsoluteOffset();
            PointF curr = new PointF(event.getX(), event.getY());
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    float xOnView =  event.getX(0);
                    float yOnView = event.getY(0);
                    currentTouch.set(xOnView, yOnView);
                    imageSavedMatrix.set(imageUsingMatrix);
                    last.set(event.getX(), event.getY());
                    start.set(last);

                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        imageSavedMatrix.set(imageUsingMatrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                	if(mode == DRAG){
                		velocity = dragVelocity;
                	}
                	if(currentTouch.x!=-1&&currentTouch.y!=-1){
                		 onViewClick(currentTouch.x, currentTouch.y);
                		 resetTouch();
                		
                	}
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    velocity = 0;
                    imageSavedMatrix.set(imageUsingMatrix);
                    oldDist = spacing(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                    	dragTime = System.currentTimeMillis();
                    	dragVelocity = (float)distanceBetween(curr, last) / (dragTime - lastDragTime) * FRICTION;
                    	dragVelocity = Math.min(MAX_VELOCITY,dragVelocity);
                        lastDragTime = dragTime;
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        if(deltaX>10||deltaY>10){
                        	checkAndSetTranslate(deltaX, deltaY);
                        	lastDelta.set(deltaX, deltaY);
                        	last.set(curr.x, curr.y);
                        	resetTouch();

							Log.d(TAG, "Absolute X,Y "+absoluteOffsetX+","+absoluteOffsetY);
							Log.e(TAG, "Image width, height "+right+", "+bottom);
							if(absoluteOffsetX-right==0){
								if(listener!=null)
									listener.onRightReached();
							}
							else if(absoluteOffsetX==0){
								if(listener!=null)
									listener.onLeftReached();
							}
							else{
								if(listener!=null){
									listener.onOutOfEdged();
								}
							}
                        }
                    }
                    break;
                }

            setImageMatrix(imageUsingMatrix);
            invalidate();
            return false;
		}
    };
    
	protected void initialized () {
        matrixValues = new float[9];
        
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(imageUsingMatrix);
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        setOnTouchListener(touchListener);

		float scale = saveScale = 1.0f;
		imageUsingMatrix.setScale(scale, scale);
		overLayerMatrix.setScale(scale, scale);
		currentTouch = new PointF(-1, -1);
    }

	private void resetTouch(){
		 currentTouch.x = -1;
		 currentTouch.y = -1;
	}
	/**
	 * Viewè¢«ç‚¹å‡»
	 * @param xOnView Viewä¸Šçš„Xå��æ ‡
	 * @param yOnView Viewä¸Šçš„Yå��æ ‡
	 */
    protected void onViewClick (float xOnView, float yOnView){}
	
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        scrolling();
    }

	public PointF getAbsoluteCenter (){
		fillAbsoluteOffset();
		return new PointF( Math.abs(absoluteOffsetX) + viewWidth/2,Math.abs(absoluteOffsetY) + viewHeight/2 );
	}

	public void moveBy (float deltaX, float deltaY){
		checkAndSetTranslate(deltaX, deltaY);
		setImageMatrix(imageUsingMatrix);
		
	}

	/**
	 * æƒ¯æ€§æ»šåŠ¨
	 */
	private void scrolling (){
		final float deltaX = lastDelta.x * velocity;
		final float deltaY = lastDelta.y * velocity;
		if (deltaX > viewWidth || deltaY > viewHeight) return;
		velocity *= FRICTION;
		if (Math.abs(deltaX) < 0.1 && Math.abs(deltaY) < 0.1) {
			return;
		}
		moveBy(deltaX, deltaY);
	}

	/**
	 * æ��äº¤å¹³ç§»å�˜æ�¢
	 * @param deltaX å¹³ç§»è·�ç¦»X
	 * @param deltaY å¹³ç§»è·�ç¦»Y
	 */
    protected void postTranslate(float deltaX, float deltaY){
    	imageUsingMatrix.postTranslate(deltaX, deltaY);
    	overLayerMatrix.postTranslate(deltaX, deltaY);
		fillAbsoluteOffset();
    }

	/**
	 * @param scaleFactor ç¼©æ”¾æ¯”ä¾‹
	 * @param scaleCenterX ç¼©æ”¾ä¸­å¿ƒX
	 * @param scaleCenterY ç¼©æ”¾ä¸­å¿ƒY
	 */
    protected void postScale(float scaleFactor, float scaleCenterX, float scaleCenterY){
    	imageUsingMatrix.postScale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
        overLayerMatrix.postScale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
		fillAbsoluteOffset();
    }

	/**
	 * æ£€æµ‹å¹³ç§»è¾¹ç•Œå¹¶è®¾ç½®å¹³ç§»
	 * @param deltaX å¹³ç§»è·�ç¦»X
	 * @param deltaY å¹³ç§»è·�ç¦»Y
	 */
    private void checkAndSetTranslate(float deltaX, float deltaY)
    {
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        fillAbsoluteOffset();
		final float x = absoluteOffsetX;
		final float y = absoluteOffsetY;
        if (scaleWidth < viewWidth) {
            deltaX = 0;
            if (y + deltaY > 0)
                deltaY = -y;
            else if (y + deltaY < -bottom)
                deltaY = -(y + bottom);
        } else if (scaleHeight < viewHeight) {
            deltaY = 0;
            if (x + deltaX > 0)
                deltaX = -x;
            else if (x + deltaX < -right)
                deltaX = -(x + right);
        }
        else {
            if (x + deltaX > 0)
                deltaX = -x;
            else if (x + deltaX < -right)
                deltaX = -(x + right);

            if (y + deltaY > 0)
                deltaY = -absoluteOffsetY;
            else if (y + deltaY < -bottom)
                deltaY = -(y + bottom);
        }
        postTranslate(deltaX, deltaY);
        checkSiding();
    }

	/**
	 * å�–å¾—å¹³ç§»é‡�
	 * @return å¹³ç§»é‡�
	 */
	public PointF getAbsoluteOffset (){
		fillAbsoluteOffset();
		return new PointF(absoluteOffsetX, absoluteOffsetY);
	}

	public float getScale(){
		return saveScale;
	}

    private void checkSiding() {
        fillAbsoluteOffset();
		final float x = absoluteOffsetX;
		final float y = absoluteOffsetY;
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        onLeftSide = onRightSide = onTopSide = onBottomSide = false;
        if (-x < 10.0f ) onLeftSide = true;
        if ((scaleWidth >= viewWidth && (x + scaleWidth - viewWidth) < 10) ||
            (scaleWidth <= viewWidth && -x + scaleWidth <= viewWidth)) onRightSide = true;
        if (-y < 10.0f) onTopSide = true;
        if (Math.abs(-y + viewHeight - scaleHeight) < 10.0f) onBottomSide = true;
    }
    
    private void calcPadding(){
        right = viewWidth * saveScale - viewWidth - (2 * redundantXSpace * saveScale);
        bottom = viewHeight * saveScale - viewHeight - (2 * redundantYSpace * saveScale);
    }
    
    private void fillAbsoluteOffset (){
        imageUsingMatrix.getValues(matrixValues);
        absoluteOffsetX = matrixValues[Matrix.MTRANS_X];
        absoluteOffsetY = matrixValues[Matrix.MTRANS_Y];
        
	}
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        bmWidth = bm.getWidth();
        bmHeight = bm.getHeight();
	    RectF drawableRect = new RectF(0, 0, bmWidth, bmHeight);
	    float width = bmWidth;
	    if(bmWidth<viewWidth)
	    	width = viewWidth;
	    RectF viewRect = new RectF(0, 0, width, bmHeight);
	    imageUsingMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
        super.setImageBitmap(bm);
        
       
    }


    
    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		float newWidth = MeasureSpec.getSize(widthMeasureSpec);
		float newHeight= MeasureSpec.getSize(heightMeasureSpec);

		viewWidth = newWidth;
        viewHeight = newHeight;

		initSize();
		calcPadding();
    }

	private void initSize(){
		// åˆ�å§‹æ—¶ï¼Œå›¾ç‰‡ä¸ŽViewè¾¹ç¼˜ä¹‹é—´çš„è·�ç¦»
		redundantYSpace = viewHeight - (saveScale * bmHeight) ;
		redundantXSpace = viewWidth - (saveScale * bmWidth);

		redundantYSpace /= (float)2;
		redundantXSpace /= (float)2;

		origWidth = viewWidth - 2 * redundantXSpace;
		origHeight = viewHeight - 2 * redundantYSpace;
	}

	private double distanceBetween(PointF left, PointF right){
        return Math.sqrt(Math.pow(left.x - right.x, 2) + Math.pow(left.y - right.y, 2));
    }

	/**
	 * è®¡ç®—ä¸¤ä¸ªæ‰‹æŒ‡ä¹‹é—´çš„è·�ç¦»
	 * @param event è§¦æ‘¸æ•´çš„
	 * @return è·�ç¦»
	 */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

	public void postScaleToImage(float scaleFactor,float scaleFocusX,float scaleFocusY){
		float origScale = saveScale;
		saveScale *= scaleFactor;
		if (saveScale > maxScale) {
			saveScale = maxScale;
			scaleFactor = maxScale / origScale;
		} else if (saveScale < minScale) {
			saveScale = minScale;
			scaleFactor = minScale / origScale;
		}
		right = viewWidth * saveScale - viewWidth - (2 * redundantXSpace * saveScale);
		bottom = viewHeight * saveScale - viewHeight - (2 * redundantYSpace * saveScale);

		if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
			final float scaleCenterX = viewWidth / 2;
			final float scaleCenterY = viewHeight / 2;
			postScale(scaleFactor, scaleCenterX, scaleCenterY);

			if (scaleFactor < 1) {
				fillAbsoluteOffset();
				final float x = absoluteOffsetX;
				final float y = absoluteOffsetY;
				if (scaleFactor < 1) {
					if (Math.round(origWidth * saveScale) < viewWidth) {
						float deltaX = 0,deltaY = 0;
						if (y < -bottom){
							deltaY = -(y + bottom);
							postTranslate(deltaX, deltaY);
						}
						else if (y > 0){
							deltaY = -y;
							postTranslate(deltaX, deltaY);
						}
					} else {
						float deltaX = 0,deltaY = 0;
						if (x < -right){
							deltaX = -(x + right);
							postTranslate(deltaX, deltaY);
						}
						else if (x > 0){
							deltaX = -x;
							postTranslate(deltaX, deltaY);
						}
					}
				}
			}
		} else {
			postScale(scaleFactor, scaleFocusX, scaleFocusY);
			fillAbsoluteOffset();
			final float x = absoluteOffsetX;
			final float y = absoluteOffsetY;

			if (scaleFactor < 1) {
				float deltaX = 0,deltaY = 0;
				if (x < -right){
					deltaX = -(x + right);
					deltaY = 0;
					postTranslate(deltaX, deltaY);
				}
				else if (x > 0){
					deltaX = -x;
					deltaY = 0;
					postTranslate(deltaX, deltaY);
				}
				if (y < -bottom){
					deltaX = 0;
					deltaY = -(y + bottom);
					postTranslate(deltaX, deltaY);
				}
				else if (y > 0){
					deltaX = 0;
					deltaY = -y;
					postTranslate(deltaX, deltaY);
				}
			}
		}
		postInvalidate();
	}
    
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            postScaleToImage(scaleFactor,detector.getFocusX(),detector.getFocusY());
            return true;
        }
    }
	
	public interface OnEdgeReachListener{
		public void onRightReached();
		public void onLeftReached();
		public void onOutOfEdged();
	}
}