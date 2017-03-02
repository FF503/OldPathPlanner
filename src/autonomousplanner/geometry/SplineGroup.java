
package autonomousplanner.geometry;

/**
 * A group of splines!  Currently used only for the piecewise cubic spline.
 * @author Jared
 */
public interface SplineGroup {
    public void setPoint(int x, int y, int i);
    public void calculateSpline(double stepDistance);
    public SegmentGroup getSegments();
    public int getStartingIndex();
    public void setStartingIndex(int i);
    public double getStartDYDX();
    public double getEndDYDX();
  
    public int splineID();
    public void setSplineID(int id);
}
