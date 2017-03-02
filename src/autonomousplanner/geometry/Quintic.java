package autonomousplanner.geometry;

import java.util.ArrayList;

/**
 * A quintic spline. quintic hermite spline interpolation- // generates
 * coefficients for a 5th degree polynomial that has specified // endpoints,
 * endpoint first derivatives, and endpoint second derivatives // it is
 * originally solved for on the interval [0, 1] // function value - p // first
 * derivative - q // second derivative - r // quintic hermite representation //
 * c(t) = H0(t)*p0 + H1(t)*q0 + H2(t)*r0 + H3(t)*r1 + H4(t)*q1 + H5(t)*p1 //
 * here are the basis functions //H0(t) = 1 - 10*t*t*t + 15*t*t*t*t -
 * 6*t*t*t*t*t //H1(t) = t - 6*t*t*t + 8*t*t*t*t - 3*t*t*t*t*t //H2(t) = .5*t*t
 * - 1.5*t*t*t + 1.5*t*t*t*t - .5*t*t*t*t*t //H3(t) = .5*t*t*t - t*t*t*t
 * +.5*t*t*t*t*t //H4(t) = -4*t*t*t + 7*t*t*t*t -3*t*t*t*t*t //H5(t) = 10*t*t*t
 * - 15*t*t*t*t + 6*t*t*t*t*t
 *
 * @author Team 236
 *
 * Note: Can decrease memory usage. Do all calculations with one set of data.
 */
public class Quintic implements Spline {
    boolean leftAbsEnd, rightAbsEnd;
    double p0, q0, r0, p1, q1, r1;
    int index = 0;
    double xChange, d_x;
    int distancep = 100;
    int ID = 0;
    SegmentGroup sg = new SegmentGroup();
    ArrayList<Double> xList = new ArrayList<>();
    ArrayList<Double> yList = new ArrayList<>();

    /**
     * Creates a relaxed Spline with these parameters. This is not a parametric
     * spline! Parametric splines need separate dy/dt, dx/dt, and dy/dx for each
     * endpoint. It's difficult to find values that result in a smooth spline.
     * See Parametric Quintic.
     *
     * @param x0
     * @param y0
     * @param dydx0
     * @param x1
     * @param y1
     * @param dydx1
     */
    public Quintic(double x0, double y0, double dydx0,
            double x1, double y1, double dydx1) {
//        double d2ydx2 = 0; //second derivative is zero.
//        xChange = x0; //how much do we have to move to get x0 at x = 0?
//        x0 = 0;
//        x1 -= xChange; //move x1 to match
//        d_x = x1; //what distance do we have to span?
//        x0 /= d_x; //smush x into this distance
//        x1 /= d_x;
//        y0 /= d_x; //smush y down too so we're proportional
//        y1 /= d_x;
//        //make the spline!
//        makeSpline(y0, dydx0, d2ydx2, y1, dydx1, d2ydx2);
        editSpline(x0, y0, dydx0, x1, y1, dydx1);
    }

    /**
     * Edit spline parameters.
     *
     * @param x0
     * @param y0
     * @param dydx0
     * @param x1
     * @param y1
     * @param dydx1
     */
    public void editSpline(double x0, double y0, double dydx0,
            double x1, double y1, double dydx1) {

        double d2ydx2 = 0; //second derivative is zero.
        xChange = x0;
        x0 = 0;
        x1 -= xChange;
        d_x = x1; //what distance do we have to span?
        x0 /= d_x; //smush x into this distance
        x1 /= d_x;
        y0 /= d_x; //smush y down too so we're proportional
        y1 /= d_x;

        //make the spline!
        makeSpline(y0, dydx0, d2ydx2, y1, dydx1, d2ydx2);
    }

    /**
     * Makes a new Quintic Hermite Spline
     *
     * @param p0 endpoint0 y
     * @param q0 endpoint0 first derivative
     * @param r0 endpoint0 second derivative
     * @param p1 endpoint1 y
     * @param q1 endpoint1 first derivative
     * @param r1 endpoint1 second derivative
     */
    private void makeSpline(double p0, double q0, double r0, double p1, double q1, double r1) {
        this.p0 = p0;
        this.q0 = q0;
        this.r0 = r0;
        this.p1 = p1;
        this.q1 = q1;
        this.r1 = r1;
    }

    /**
     * Evaluates spline from 0 to 1. This is before scaling.
     *
     * @param t
     * @return
     */
    public double evaluateSpline(double t) {
        double h0, h1, h2, h3, h4, h5; //base polynomials
        //exponents would have been easier....
        h0 = 1 - 10 * t * t * t + 15 * t * t * t * t - 6 * t * t * t * t * t;
        h1 = t - 6 * t * t * t + 8 * t * t * t * t - 3 * t * t * t * t * t;
        h2 = .5 * t * t - 1.5 * t * t * t + 1.5 * t * t * t * t - .5 * t * t * t * t * t;
        h3 = .5 * t * t * t - t * t * t * t + .5 * t * t * t * t * t;
        h4 = -4 * t * t * t + 7 * t * t * t * t - 3 * t * t * t * t * t;
        h5 = 10 * t * t * t - 15 * t * t * t * t + 6 * t * t * t * t * t;
        double result = p0 * h0 + q0 * h1 + r0 * h2 + r1 * h3 + q1 * h4 + p1 * h5;
        return result;
    }

    /**
     * Get scaled points. This method does heavy math.
     */
    public void getScaledPoints() {
        //go from 1 to 1000 little tiny steps.
        for (int i = 0; i < distancep; i++) {
            //add each point to sequence.
            double j = (double) i / distancep;
            xList.add((j * d_x) + xChange);
            yList.add(evaluateSpline((double) i / distancep) * d_x);
        }
    }
    
    /**
     * A high resolution spline calculation.
     * Currently not used?
     */
    public void getBigScaledPoints() {
        distancep = 10000;
        //go from 1 to 1000 little tiny steps.
        for (int i = 0; i < distancep; i++) {
            //add each point to sequence.
            double j = (double) i / distancep;
            xList.add((j * d_x) + xChange);
            yList.add(evaluateSpline((double) i / distancep) * d_x);
        }
    }
    
    /**
     * Get all the segments!
     * @return 
     */
    @Override
    public SegmentGroup getSegments() {
        return sg;
    }

    /**
     * Calculate segments.
     * @param resolution
     */
    @Override
    public synchronized void calculateSegments(int resolution) {
        distancep = resolution;
        getScaledPoints();
        sg.s.clear();
        

        for (int i = 0; i < xList.size(); i++) {
            Segment s = new Segment();
            s.x = xList.get(i);
            s.y = yList.get(i);
            sg.add(s);
        }
        xList.clear();
        yList.clear();

    }

    /**
     * Length of spline in segments.
     * @return Number of segments.
     */
    @Override
    public int length() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Set new extreme values for spline.
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    @Override
    public void setExtremePoints(double x0, double y0, double x1, double y1) {
        sg.s.clear();
        xList.clear();
        yList.clear();
        editSpline(x0, y0, 0, x1, y1, 0);
    }
    
    /**
     * For the editor, set the index of the waypoint where this spline begins.
     * Used for the spline search/calculation order.
     * @param i
     */
    @Override
    public void setStartingWaypointIndex(int i) {
        index = i;
    }

    /**
     * The index of the waypoint where the spline starts.
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
        return q0;
    }

    /**
     * Get the slope at the last point.
     * @return
     */
    @Override
    public double endDYDX() {
        return q1;
    }

    /**
     * Set the slope at the first point.
     * @param dydx
     */
    @Override
    public void setStartDYDX(double dydx) {
        q0 = dydx;
    }

    /**
     * Set the slope at the last point.
     * @param dydx
     */
    @Override
    public void setEndDYDX(double dydx) {
        q1 = dydx;
    }
   
    /**
     * The type of spline.
     * @return
     */
    @Override
    public String getType(){
        return "Quintic";
    }
    
    /**
     * The spline ID.  Not used right now.
     * @return
     */
    @Override
    public int splineID() {
        return ID;
    }

    /**
     * Set spline ID.  Used to help order splines.
     * @param id
     */
    @Override
    public void setSplineID(int id) {
        this.ID = id;
    }

    /**
     * This is not a parametric spline.
     * Don't use this method.
     * @param isY
     * @return
     */
    @Override
    public SegmentGroup getParametricData(boolean isY) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Normal quintic splines can't be flipped.
     * Always returns false and uses default calculation order every time.
     * @return
     */
    @Override
    public boolean isFlipped() {
        return false;
    }

    /**
     * Does nothing.  Cannot be flipped.
     * @param isFlipped
     */
    @Override
    public void setFlipped(boolean isFlipped) {
    }

}
