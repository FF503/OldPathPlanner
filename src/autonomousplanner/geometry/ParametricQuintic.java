package autonomousplanner.geometry;

import autonomousplanner.UI.CurveAdjustment;

/**
 * An attempt at going parametric with the 1st/2nd derivative controllable
 * spline.
 * A drop in replacement for a normal
   spline.  When created, a window pops up allowing dy to be set.  Dy doesn't 
   affect dy/dx, but it affects dy/dt and dx/dt, making the spline sharper/smoother.
 * This spline also can be inverted (think mirror) by sliding the slider.
 * Also, the spline can be twisted/untwisted.  A twisted spline is like an s-
 * the input direction is similar to the output direction
 * An untwisted spline lets the input direction be the opposite direction of the output
 * allowing for semicircles.
 * @author Jared
 */
public class ParametricQuintic implements Spline {

    private Quintic xt, yt;
    double pLow = 0, pHigh = 1;
    boolean isFlipped = true;
    public double curveSize = 100;
            boolean canRecalculate = true;
            public boolean headingSlider = false, headingSlider2;
            public double sliderHeading = 0 , sliderHeading2 = 1;

    /**
     * Create a new parametric quintic spline.  
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @param f
     */
    public ParametricQuintic(double a, double b, double c, double d, double e, double f) {
        //t, the parameter is from 0 to 1, is x inputs of the normal quintic
        //the x output of the parametric spline is the y output of the normal quintic
        //the y output of the parametric spline is the y output of the other normal quintic
        xt = new Quintic(0, 0, 0, 1, 0, 0);
        yt = new Quintic(0, 0, 0, 1, 0, 0);
        new CurveAdjustment(this).setVisible(true);
    }

    /**
     * The points to interpolate
     * @param x0 x0
     * @param y0 y0
     * @param x1 x1
     * @param y1 y1
     */
    @Override
    public void setExtremePoints(double x0, double y0, double x1, double y1) {
        //parameter 0 to 1
        if (isFlipped) {
            xt.setExtremePoints(pLow, x1, pHigh, x0);
            yt.setExtremePoints(pLow, y1, pHigh, y0);
        } else {
            xt.setExtremePoints(pLow, x0, pHigh, x1);
            yt.setExtremePoints(pLow, y0, pHigh, y1);

        }
    }

    /**
     * Build parametric spline from xt and yt.
     *
     * @return
     */
    @Override
    public SegmentGroup getSegments() {
        SegmentGroup output = new SegmentGroup();
        SegmentGroup xSegs, ySegs;
        xSegs = xt.getSegments();
        ySegs = yt.getSegments();
        if (!isFlipped) {
            for (int i = 0; i < xSegs.s.size(); i++) {
                Segment out = new Segment();
                out.x = xSegs.s.get(i).y; //not really y.  Just output
                out.y = ySegs.s.get(i).y; //y this time.
                output.add(out);
            }
        } else {
            for (int i = xSegs.s.size() - 1; i > -1; i--) {
                Segment out = new Segment();
                out.x = xSegs.s.get(i).y; //not really y.  Just output
                out.y = ySegs.s.get(i).y; //y this time.
                output.add(out);
            }
        }
       
        return output;
    }
    
    

    /**
     * Calculate both xt and yt splines.
     * @param resolution
     */
    @Override
    public void calculateSegments(int resolution) {
        //System.out.println("res " + resolution);
        xt.calculateSegments(resolution);
        yt.calculateSegments(resolution);
        
    }
    
    /**
     * How many segments there currently are.
     * @return
     */
    @Override
    public int length() {
        return xt.length();
    }

    /**
     * Waypoint index (in editor) of where the spline begins.
     * "begins" can mean start or end, depending on spline state.
     * @param i
     */
    @Override
    public void setStartingWaypointIndex(int i) {
        xt.setStartingWaypointIndex(i);
        yt.setStartingWaypointIndex(i);
    }

    /**
     *Waypoint index (in editor) of where the spline begins.
     * "begins" can mean start or end, depending on spline state.
     * @return
     */
    @Override
    public int getWaypointIndex() {
        return xt.getWaypointIndex();
    }

    /**
     * The current slope at the "beginning".
     * The beginning can change during flips.
     * @return
     */
    @Override
    public double startDYDX() {

        return yt.startDYDX();
    }

    /**
     * The current slope at the "end".
     * The end can change during flips/twists.
     * @return
     */
    @Override
    public double endDYDX() {
        return yt.endDYDX();
    }

    /**
     * Set the slope at the beginning.
     * Because this is a parametric thing, you're setting
     * the ratio of dy to dx.  Sadly, if you want dy/dx to be 1, 
     * you can't just choose 1/1 arbitrarily.  1/1 is different from 10/10, which
     * is different from -10/-10.  It affects dx/dt and dy/dt, which aren't ratios,
     * but fixed values.
     * @param dydx
     */
    public void setSDYDX(double dydx) {
        //dy/dx of parametric thing is (dy/dt)/(dx/dt)
        //to make things easier, I'll make dt = 0
        //this means that dy/dt = dy/dx
        //and dx/dt = 1.
        if (!isFlipped) {
            yt.setStartDYDX(curveSize * dydx);
            xt.setStartDYDX(curveSize);
        } else {
            yt.setEndDYDX(curveSize * dydx);
            xt.setEndDYDX(curveSize);
        }

    }
/**
     * Set the slope at the end.
     * Because this is a parametric thing, you're setting
     * the ratio of dy to dx.  Sadly, if you want dy/dx to be 1, 
     * you can't just choose 1/1 arbitrarily.  1/1 is different from 10/10, which
     * is different from -10/-10.  It affects dx/dt and dy/dt, which aren't ratios,
     * but fixed values.
     * @param dydx
     */
    public void setEDYDX(double dydx) {
        if (isFlipped) {
            yt.setStartDYDX(curveSize * dydx);
            xt.setStartDYDX(curveSize);
        } else {
            yt.setEndDYDX(-curveSize * dydx);
            xt.setEndDYDX(-curveSize);
        }
        //same as start dydx

    }

    /**
     * The type of spline this is.
     * @return
     */
    @Override
    public String getType() {
        return "Parametric Quintic";
    }

    /**
     * An ID for searching.  Not implemented yet.
     * @return
     */
    @Override
    public int splineID() {
        return xt.splineID();
    }

    /**
     * Search ID.  Not implemented yet.
     * @param id
     */
    @Override
    public void setSplineID(int id) {
        xt.setSplineID(id);
        yt.setSplineID(id);
    }

    /**
     * Get individual splines for xt and yt.
     * Useful for debugging or to see how more than just
     * dy/dx matters, a value for dy should be specified.
     * @param isY
     * @return
     */
    @Override
    public SegmentGroup getParametricData(boolean isY) {
        if (isY) {
            return yt.getSegments();
        } else {
            return xt.getSegments();
        }
    }

    /**
     * Is the spline twisted? (think circle vs. s)
     * @return
     */
    @Override
    public boolean isFlipped() {
        return isFlipped;
    }

    /**
     * Twist the spline.  (think circle vs. s)
     * @param isFlipped
     */
    @Override
    public void setFlipped(boolean isFlipped) {
        this.isFlipped = isFlipped;
    }

    @Override
    public void setStartDYDX(double dydx) {
        if(headingSlider){
            setSDYDX(sliderHeading);
        }else{
            setSDYDX(dydx);
        }
    }

    @Override
    public void setEndDYDX(double dydx) {
        if(headingSlider2){
            setEDYDX(sliderHeading2);
        } else {
            setEDYDX(dydx);
        }
    }
    
    

}
