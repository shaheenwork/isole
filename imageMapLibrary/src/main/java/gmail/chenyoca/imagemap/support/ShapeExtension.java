package gmail.chenyoca.imagemap.support;

/**
 * ShapeExtension
 */
public interface ShapeExtension{

    public interface OnShapeActionListener {

        /**
         * @param shape
         * @param xOnImage
         * @param yOnImage
         */
        void onShapeClick(Shape shape, float xOnImage, float yOnImage);

        void onClickOutSide();

    }

    /**
     * @param shape 
     */
    void addShape(Shape shape);
    
    
    
    void addOverShape(Shape shape);
    
    /**
     * @param tag 
     */
    void removeOverShape(Object tag);

    /**
     * @param tag 
     */
    void removeShape(Object tag);

    /**
     * 
     */
    void clearShapes();
}