package gmail.chenyoca.imagemap.support;

import java.util.Comparator;

import android.graphics.*;

public abstract class Shape implements Comparable<Shape>{

	public final int color;
	public final Object tag;

    protected Bubble displayBubble;
	protected int alaph = 255;
	protected final Paint drawPaint;

    protected final static Paint cleanPaint;

    public boolean visible;
    public int	   zIndex;



    private int compare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    static {
        cleanPaint = new Paint();
        cleanPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

	public Shape(Object tag, int coverColor){
		this.tag = tag;
		this.color = coverColor;

		drawPaint = new Paint();
		drawPaint.setColor(coverColor);
		drawPaint.setStyle(Paint.Style.FILL);
		drawPaint.setAntiAlias(true);
        drawPaint.setFilterBitmap(true);

    }

	public void setAlaph(int alaph){
		this.alaph = alaph;
	}

    public void createBubbleRelation(Bubble displayBubble) {
        this.displayBubble = displayBubble;
    }

    public void cleanBubbleRelation(){
        this.displayBubble = null;
        visible = false;
    }

    public abstract void setValues(float...coords);

    /**
     * Set coords. Split by char ',' .
     * @param coords coords
     */
    public void setValues(String coords){
        String[] parametrs = coords.split(",");
        final int size = parametrs.length;
        final float[] args = new float[size];
        for(int i=0;i<size;i++){
            args[i] = Float.valueOf(parametrs[i].trim());
        }
        setValues(args);
    }

    /**
     * Set coords. Split by char ',' .
     * @param coords coords
     */
    public void setValues(String coords,float scale){
        String[] parametrs = coords.split(",");
        final int size = parametrs.length;
        final float[] args = new float[size];
        for(int i=0;i<size;i++){
            args[i] = Float.valueOf(parametrs[i].trim())*scale;
        }
        setValues(args);
    }

    /**
     * ç”±HightlightImageViewè°ƒåº¦
     * @param scale ç¼©æ”¾é‡�
     * @param centerX ç¼©æ”¾ä¸­å¿ƒ x
     * @param centerY ç¼©æ”¾ä¸­å¿ƒ y
     */
    public final void onScale(float scale,float centerX,float centerY){
        scaleBy(scale, centerX, centerY);
        if(displayBubble != null){
            displayBubble.showAtShape(this);
        }
    }

	public abstract void onScale(float scale);

    /**
     * ç”±HightlightImageViewè°ƒåº¦
     * @param deltaX ç§»åŠ¨é‡� x
     * @param deltaY ç§»åŠ¨é‡� y
     */
    public final void onTranslate(float deltaX,float deltaY){
		translate(deltaX,deltaY);
        if(displayBubble != null){
            displayBubble.showAtShape(this);
        }
    }

    /**
     * ç”±HightlightImageViewè°ƒåº¦ã€‚
     * @param canvas ç»˜åˆ¶ç”»å¸ƒ
     */
    public final void onDraw(Canvas canvas){
        draw(canvas);
        // å¦‚æžœå½“å‰�Shapeä¸ŽBubbleæœ‰å…³è�”ï¼Œåˆ™å°†Bubbleä¹Ÿæ˜¾ç¤ºå‡ºæ�¥
        if(displayBubble != null){
			displayBubble.showAtShape(this);
        }
    }

    public abstract void draw(Canvas canvas);

	public abstract void scaleBy (float scale, float centerX, float centerY);

    public abstract void translate(float deltaX,float deltaY);

    public abstract boolean inArea(float x,float y);

    public abstract PointF getCenterPoint();

    public static String getCenterCoord(final String _coords){
        if (_coords == null || _coords.trim().length() == 0)
            return null;

        try {
            String coords = _coords.trim();
            String[] pts = coords.split(" ");
            int nPts = pts.length;
            float x = 0;
            float y = 0;
            float f;
            int j = nPts-1;
            String p1;
            String p2;

            for (int i = 0; i < nPts; j = i++) {
                p1 = pts[i].trim();
                if (p1.length() == 0)
                    continue;

                float p1_x = Float.parseFloat(p1.split(",")[0]);
                float p1_y = Float.parseFloat(p1.split(",")[1]);

                p2 = pts[j].trim();
                if (p2.length() == 0)
                    continue;

                float p2_x = Float.parseFloat(p2.split(",")[0]);
                float p2_y = Float.parseFloat(p2.split(",")[1]);

                f = p1_x * p2_y - p2_x * p1_y;
                x +=(p1_x + p2_x) * f;
                y +=(p1_y + p2_y) * f;
            }

            f = area(pts) * 6;

            return x/f + ","+ y/f;
        } catch (Throwable e) {
            return null;
        }
    }

    private static float area(String[] pts) {
        float area = 0;
        int nPts = pts.length;
        int j = nPts-1;
        String p1;
        String p2;

        for (int i = 0; i < nPts; j = i++) {
            p1 = pts[i].trim();
            if (p1.length() == 0)
                continue;

            float p1_x = Float.parseFloat(p1.split(",")[0]);
            float p1_y = Float.parseFloat(p1.split(",")[1]);

            p2 = pts[j].trim();
            if (p2.length() == 0)
                continue;

            float p2_x = Float.parseFloat(p2.split(",")[0]);
            float p2_y = Float.parseFloat(p2.split(",")[1]);

            area += p1_x * p2_y;
            area -= p1_y * p2_x;
        }

        area/=2;
        return area;
    }

    public void setVisible(boolean visible) {
		this.visible = visible;
	}

    public boolean isVisible() {
		return visible;
	}

    @Override
   	public int compareTo(Shape another) {
   		// TODO Auto-generated method stub
   		return  compare(zIndex, another.zIndex);
   	}

}
