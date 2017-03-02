
package autonomousplanner;

import autonomousplanner.geometry.Point;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.SegmentGroup;
import java.awt.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * A path. This is the second step in the sequence.
 * This creates segments with x, y, position, and 
 * first derivative, and calculates length.
 *
 * @author Team 236
 */
public class Path {

    public double length;
    private final int numberOfSegments = 1000;
    ArrayList<Double> x = new ArrayList<>();
    ArrayList<Double> y = new ArrayList<>();
    ArrayList<Double> l = new ArrayList<>(); //knot messy graph
    ArrayList<Double> segFlag = new ArrayList<>(); //index values of each "segment"
    SegmentGroup inGroup = new SegmentGroup();
    /**
     * The output main segment group.
     */
    public SegmentGroup group = new SegmentGroup();

    /**
     * Create a new Path with segments and perform all needed math.
     * 
     */
    public Path(SegmentGroup s) {
        inGroup = s;
        try{
            makePath();
        } catch (NullPointerException e){
             StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println(sw.toString());
            JOptionPane.showMessageDialog(null, new TextArea(sw.toString()), "Spline Error", 3);
        }
        

    }
    
    /**
     * Reset everything
     */
    public void reset(){
        x = new ArrayList<>();
        y = new ArrayList<>();
        l = new ArrayList<>();
        segFlag = new ArrayList<>();
        group = new SegmentGroup();
    }

    /**
     * Do all the math involved to make a path with segments.
     * Runs through all the methods in the class in the right order.
     */
    public final void makePath() {
        long start = System.currentTimeMillis();
        print("GENERATING PATH");
        makeScaledLists();
        calculateLength();
        walkSpline();
        createSegments();
        print("SUCCESS! TIME: "
                + (System.currentTimeMillis() - start) + " MS");
    }
    
    /**
     * Generate lists needed for path math.
     */
    public void makeScaledLists(){
        for(int i = 0; i < inGroup.s.size(); i++){
            x.add((inGroup.s.get(i).x-250)/20);
            y.add((inGroup.s.get(i).y-250)/20);
        }
    }
    
     
    
    
    /**
     * Calculate the length of the spline using lengths of many small lines.
     *
     * @return the length
     */
    public double calculateLength() {
        System.out.println("     Calculating Length...");
        //make a bunch of lines between points, then connect the dots.
        long start = System.currentTimeMillis();
        for (int i = 1; i < x.size(); i++) {
            //a^2 + b^2 = c^2
            double dx = x.get(i) - x.get(i - 1);
            double dy = y.get(i) - y.get(i - 1);
            double c2 = (dx * dx) + (dy * dy);
            length += Math.sqrt(c2);
            
            double prevLength = 0;
            if (i != 1) {
                prevLength = l.get(l.size() - 1);
            }
            //add an accumulating length total to the array to 
            //store distance travelled at each point.
            l.add(Math.sqrt(c2) + prevLength);
        }
        print("     Length: " + length + " Time: " + (System.currentTimeMillis() - start));
        return length;
    }

    /**
     * Create segments from spline data.
     */
    public void createSegments() {
        print("     Calculating Segments...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < x.size() - 1; i++) {
//            double s = segFlag.get(i);
//            double s2 = segFlag.get(i + 1);
            double s = i;
            double s2 = i+1;
            int segIndex = (int) s; //there's got to be a better way!
            Segment seg = new Segment();
            double xV = x.get(segIndex);
            //double endX = x.get(segIndex + 1);
            double yV = y.get(segIndex);
            seg.x = xV;
            seg.y = yV;
            seg.posit = l.get((int) s);
            seg.dydx = derivative((int) s, (int) s2);
            if(i!=0){
                            seg.dx = seg.posit - group.s.get(group.s.size()-1).posit;

            }
            
            group.s.add(seg);
        }
        print("     Created " + group.s.size() + " segments. "
                + "Time: " + (System.currentTimeMillis() - start));
        
        

    }
    
    /**
     * "Walk" across the spline, splitting into equal segments.
     * Not used currently
     */
    public void walkSpline() {
        System.out.println("     Dividing Segments...");
        long start = System.currentTimeMillis();
        double segLength = length / numberOfSegments;
        print("     Segment Length: " + segLength);
        double currentLength = 0;
        for (int i = 1; i < x.size(); i++) {
            //step through adding lengths
            double dx = x.get(i) - x.get(i - 1);
            double dy = y.get(i) - y.get(i - 1);
            double c2 = (dx * dx) + (dy * dy);
            currentLength += Math.sqrt(c2);
            if (currentLength > segLength) {
                //we're too big, cut.
                segFlag.add((double) i);
                currentLength = 0;
            }
        }
        print("     Split into " + segFlag.size()
                + " segments. Time: " + (System.currentTimeMillis() - start));
    }

    /**
     * Get the derivative of a segment spanning t1-t2
     *
     * @param t1
     * @param t2
     * @return
     */
    public double derivative(int t1, int t2) {
        double slope;

        slope = Util.slope(new Point(x.get(t1), y.get(t1)),
                new Point(x.get(t2), y.get(t2)));

        return slope;
    }

    /**
     * Get the first derivative at a point (looking back)
     *
     * @param t index of point
     * @return the derivative.
     */
    public double pointDerivative(int t) {
        double slope;
        if (t == 0) {
            slope = Util.slope(new Point(x.get(0), y.get(0)),
                    new Point(x.get(1), y.get(1)));
        } else {
            slope = Util.slope(new Point(x.get(t), y.get(t)),
                    new Point(x.get(t - 1), y.get(t - 1)));
        }
        return slope;
    }
    
    public void print(Object o){
        System.out.println(o);
    }


}


