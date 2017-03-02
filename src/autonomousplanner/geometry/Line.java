package autonomousplanner.geometry;

/**
 * The most boring of splines.
 *
 * @author Jared
 */
public class Line implements Spline {
    boolean leftAbsEnd, rightAbsEnd;
    double x1, x2, y1, y2, h1, h2;
    int index = 0;
    int length = 0;
    SegmentGroup sg = new SegmentGroup();
    double dydx;
    boolean isContinousAtEnd = true;
    boolean isContinousAtStart = true;
    int ID=0;

    /**
     * Makes a new line with these points. In the future, the heading can be
     * used to implement a stop and turn.
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param h1
     * @param h2
     */
    public Line(double x1, double x2, double y1, double y2, double h1, double h2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.h1 = h1;
        this.h2 = h2;
    }

    /**
     * Set the start point of the line.
     *
     * @param x
     * @param y
     */
    public void setStartPoint(double x, double y) {
        x1 = x;
        y1 = y;
    }

    /**
     * Set the end point of the line.
     *
     * @param x
     * @param y
     */
    public void setEndPoint(double x, double y) {
        x2 = x;
        y2 = y;
    }

    /**
     * Get the segments.
     *
     * @return Most recently calculated values.
     */
    @Override
    public SegmentGroup getSegments() {
        return sg;
    }

    /**
     * Calculate the group.
     *
     * @param resolution Number of segments.
     */
    @Override
    public void calculateSegments(int resolution) {
        sg = new SegmentGroup();
        //first find dy and dx
        double dy = y2 - y1;
        double dx = x2 - x1;
        //now dy/dx
        dydx = dy / dx;
        //y1 = dydx (x1) + b
        //y1-(dydx*x1) = b
        double b = y2 - (dydx * x2);
        length = resolution;
        //how much do we travel in each segment
        double dSeg = dx / (resolution-1);
        for (int i = 0; i < resolution; i++) {
            
            //make a segment
            Segment s = new Segment();
            double x = x1 + i * dSeg; //x to evaulate at
            s.x = x;
            s.y = dydx * x + b; //evaulate
            sg.add(s);
        }
    }
    
    /**
     * Evaluate the line at a point.  Can be between the original endpoints,
     * or outside them.
     * @param x The x value to evaluate at.
     * @return The y value.
     */
    public double evaluateAt(double x){
        double dy = y2 - y1;
        double dx = x2 - x1;
        dydx = dy/dx;
        double b = y2 - (dydx * x2);
        return dydx * x + b;
    }

    /**
     * Length of spline in segments
     * @return number of segments
     */
    @Override
    public int length() {
        return length;
    }

    /**
     * Set new extreme values for the spline.
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    @Override
    public void setExtremePoints(double x0, double y0, double x1, double y1) {
        setStartPoint(x0, y0);
        setEndPoint(x1, y1);
    }

    /**
     * Set the index of the starting waypoint segment for drawing.
     * @param i
     */
    @Override
    public void setStartingWaypointIndex(int i) {
        index = i;
    }

    /**
     * Get the index of the starting waypoint segment index.
     * @return
     */
    @Override
    public int getWaypointIndex() {
        return index;
    }

    /**
     * Get the slope at the first point.
     * @return
     */
    @Override
    public double startDYDX() {
        return dydx;
    }

    /**
     * Get the slope at the last point.
     * @return
     */
    @Override
    public double endDYDX() {
        return dydx;
    }

    /**
     * Set the slope at the first point.
     * @param dydx
     */
    @Override
    public void setStartDYDX(double dydx) {
        isContinousAtStart = false;
    }

    /**
     * Set the slope at the last point.
     * @param dydx
     */
    @Override
    public void setEndDYDX(double dydx) {
        isContinousAtEnd = false;
        
    }
    
    
    /**
     * The type of spline.
     * @return
     */
    @Override
    public String getType(){
        return "Line";
    }

    @Override
    public int splineID() {
        return ID;
    }

    @Override
    public void setSplineID(int id) {
        this.ID = id;
    }

    @Override
    public SegmentGroup getParametricData(boolean isY) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFlipped() {
        return false; //it's a line!
    }

    @Override
    public void setFlipped(boolean isFlipped) {
    }

}
