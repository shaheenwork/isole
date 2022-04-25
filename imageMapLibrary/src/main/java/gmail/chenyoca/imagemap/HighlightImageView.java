package gmail.chenyoca.imagemap;

import gmail.chenyoca.imagemap.support.Shape;
import gmail.chenyoca.imagemap.support.ShapeExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Based on TouchImageView class, Design for draw shapes on canvas of ImageView
 */
public class HighlightImageView extends TouchImageView implements ShapeExtension {


    public HighlightImageView(Context context) {
        this(context, null);
    }

    public HighlightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Map<Object, Shape> shapesCache = new HashMap<Object, Shape>();
    private Map<Object, Shape> overshapesCache = new HashMap<Object, Shape>();
    private OnShapeActionListener onShapeClickListener;

    public void setOnShapeClickListener(OnShapeActionListener onShapeClickListener) {
        this.onShapeClickListener = onShapeClickListener;
    }

    @Override
    public void addShape(Shape shape) {

        shapesCache.put(shape.tag, shape);

        postInvalidate();
    }

    @Override
    public void removeShape(Object tag) {
        if (shapesCache.containsKey(tag)) {
            shapesCache.remove(tag);
            postInvalidate();
        }
    }

    @Override
    public void clearShapes() {
        shapesCache.clear();
        overshapesCache.clear();
    }

    @Override
    public void addOverShape(Shape shape) {
        overshapesCache.put(shape.tag, shape);
        postInvalidate();
    }

    public void sortOverShapes() {
        overshapesCache = sortByValues(overshapesCache);
    }

    @Override
    public void removeOverShape(Object tag) {
        if (overshapesCache.containsKey(tag)) {
            overshapesCache.remove(tag);
            postInvalidate();
        }
    }

    public Shape getOverShape(Object tag) {
        if (overshapesCache.containsKey(tag)) {
            return overshapesCache.get(tag);
        }
        return null;
    }

    public List<Shape> getShapes() {
        return new ArrayList<Shape>(shapesCache.values());
    }

    public Map<Object, Shape> getOvershapesCache() {
        return overshapesCache;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (Shape shape : shapesCache.values()) {
            if (shape.visible)
                shape.onDraw(canvas);
        }
        canvas.save();


        onDrawWithCanvas(canvas);

        super.onDraw(canvas);
        for (Shape shape : overshapesCache.values()) {
            if (shape.visible)
                shape.onDraw(canvas);
        }
        canvas.save();

    }

    @SuppressWarnings("rawtypes")
    public HashMap<Object, Shape> sortByValues(Map<Object, Shape> map) {

        List<Entry<Object, Shape>> list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap<Object, Shape> sortedHashMap = new LinkedHashMap<Object, Shape>();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry<Object, Shape> entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    /**
     * - Override this method for draw something on canvas when YourClass extends HighlightImageView.
     *
     * @param canvas canvas on which drawing operation is to be performed
     */
    protected void onDrawWithCanvas(Canvas canvas) {
    }

    @Override
    protected void onViewClick(float xOnView, float yOnView) {
        if (onShapeClickListener == null) return;
        for (Shape shape : shapesCache.values()) {
            if (shape.inArea(xOnView, yOnView)) {

                // Callback by listener when a shape has been clicked
                onShapeClickListener.onShapeClick(shape, xOnView, yOnView);
                return;
            }
        }
        onShapeClickListener.onClickOutSide();
    }

    @Override
    protected void postScale(float scaleFactor, float scaleCenterX, float scaleCenterY) {
        super.postScale(scaleFactor, scaleCenterX, scaleCenterY);
        if (scaleFactor != 0) {
            for (Shape shape : shapesCache.values()) {
                if (scaleFactor != 0) {
                    shape.onScale(scaleFactor, scaleCenterX, scaleCenterY);
                }
            }
            for (Shape shape : overshapesCache.values()) {
                if (scaleFactor != 0) {
                    shape.onScale(scaleFactor, scaleCenterX, scaleCenterY);
                }
            }
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        if (!(deltaX == 0 && deltaY == 0)) {
            for (Shape shape : shapesCache.values()) {
                shape.onTranslate(deltaX, deltaY);
            }
            for (Shape shape : overshapesCache.values()) {
                shape.onTranslate(deltaX, deltaY);
            }
        }
    }

    public Shape getShape(Object tag) {
        // TODO Auto-generated method stub
        return shapesCache.get(tag);
    }


}
