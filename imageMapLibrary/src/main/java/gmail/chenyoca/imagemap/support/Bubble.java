package gmail.chenyoca.imagemap.support;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * author : chenyoca@gmail.com
 * date   : 13-05-14
 * The bubble wrapper.
 */
@SuppressLint("ViewConstructor")
public class Bubble extends FrameLayout{

    static final boolean IS_API_11_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    public final View view;
    public final PointF position = new PointF();

    private RenderDelegate renderDelegate;

    private Rect parentBounds;
    
    public Bubble(View view){
		super(view.getContext());
        this.view = view;
        final int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        LayoutParams params = new LayoutParams(wrapContent,wrapContent);
        this.view.setLayoutParams(params);
        this.view.setClickable(true);
        addView(view);
        
    }

	
	public interface RenderDelegate {
        void onDisplay(Shape shape, View bubbleView);
    }

   
    public void setRenderDelegate (RenderDelegate renderDelegate) {
        this.renderDelegate = renderDelegate;
    }
    
    public void setParentBounds(Rect parentBounds) {
		this.parentBounds = parentBounds;
	}
    
    public Rect getParentBounds() {
		return parentBounds;
	}

    /**
     * Show the bubble view controller on the shape.
     * @param shape the shape to show on
     */
    public void showAtShape(Shape shape){
        if(view == null) return;
        shape.createBubbleRelation(this);
        setBubbleViewAtPosition(shape.getCenterPoint());
        if (renderDelegate != null){
            renderDelegate.onDisplay(shape, view);
        }
        view.setVisibility(View.VISIBLE);
    }

    private void setBubbleViewAtPosition(PointF center){
    	float posX = center.x - view.getWidth()/2;
        float posY = center.y - view.getHeight();
        
        if(posX<parentBounds.left){
        	posX = parentBounds.left;
        }
        else if(posX+view.getWidth()>parentBounds.right){
        	posX = parentBounds.right-view.getWidth();
        }
        
        if(posY<parentBounds.top){
        	posY = center.y;
        }
        
		setBubbleViewAtPosition(posX, posY);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setBubbleViewAtPosition(float x, float y){

		// BUG : HTC SDK 2.3.3 
		if(position.equals(x,y)) return;

        position.set(x,y);

		if(IS_API_11_LATER){
            view.setX(x);
            view.setY(y);
        }else{
            LayoutParams params = (LayoutParams) view.getLayoutParams();
			int left = (int)x;
			int top = (int)y;
			// HTC SDK 2.3.3 Required
			params.gravity = Gravity.CENTER_VERTICAL | Gravity.TOP;
			params.leftMargin = left;
			params.topMargin = top;
			view.setLayoutParams(params);
        }
    }

}
