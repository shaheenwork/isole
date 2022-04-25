package gmail.chenyoca.imagemap.support;

import android.graphics.Canvas;
import android.graphics.PointF;

public class CircleShape extends Shape {

	private PointF center;
	private float radius = 5f;

	public CircleShape(Object tag, int coverColor) {
		super(tag, coverColor);
	}

    /**
     * Set Center,radius
     * @param coords centerX,CenterY,radius
     */
    @Override
	public void setValues(float...coords){

        final float centerX = coords[0];
        final float centerY = coords[1];

        this.center = new PointF(centerX, centerY);

        if(coords.length > 2){
            this.radius = coords[2];
        }

	}

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
	public PointF getCenterPoint(){
		return center;
	}

	@Override
	public void draw(Canvas canvas) {
		drawPaint.setAlpha(alaph);
		canvas.drawCircle(center.x, center.y, radius, drawPaint);
	}

	@Override
	public void scaleBy (float scale, float centerX, float centerY) {
        PointF newCenter = ScaleUtility.scaleByPoint(center.x, center.y, centerX, centerY, scale);
        radius *= scale;
        center.set(newCenter.x,newCenter.y);
	}

	@Override
	public void onScale(float scale){
//		scaleBy = (float)Math.sqrt(scaleBy);
		radius *= scale;
		center.set( center.x *= scale , center.y *= scale );
	}

    @Override
    public void translate(float deltaX, float deltaY) {
        center.x += deltaX;
        center.y += deltaY;
    }

    @Override
    public boolean inArea(float x, float y) {
        boolean ret = false;
        float dx = center.x - Math.abs(x);
        float dy = center.y - Math.abs(y);
        float d = (float)Math.sqrt((dx*dx)+(dy*dy));
        if (d<radius) {
            ret = true;
        }
        return ret;
    }

}
