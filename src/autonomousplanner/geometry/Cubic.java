package autonomousplanner.geometry;

import java.util.ArrayList;

/**
 * A cubic group.
 *
 * @author Jared
 */
public class Cubic implements SplineGroup {
    boolean leftAbsEnd, rightAbsEnd;

    SegmentGroup sg = new SegmentGroup();
    int index = 0;

    ArrayList<Double> bxList = new ArrayList<>();
    ArrayList<Double> byList = new ArrayList<>();
    //control points used to calculate derivate for hermite spline
    Point s1 = new Point(0, 0);
    Point s2 = new Point(0, 0);
    Point s3 = new Point(0, 0);
    Point s4 = new Point(0, 0);
    //path starts and ends.
    double xStart, yStart, hStart;
    double xEnd, yEnd, hEnd;
    int ID = 0;

    Point[] interp = new Point[6]; //always have six, that's as big as
    //the polynomials go.
    Point[] bezControl = new Point[6];	 //Bezier control points.
    int pointsToShow = 6; //all points are computed, only this number shown.
    double step;	 //step
    double prevDistance = .2;
    double disatncep = .02; //THIS VALUE FOR RESOLUTION!!!
    int pointMover; //0-5 to move that point, 6 to lock.
    boolean isDragging; //java mouse listeners );

    public Cubic() {
        bezControl = new Point[6];
        pointMover = 6; //yup.
        //set up default points
        for (int i = 0; i < 6; i++) {
            interp[i] = new Point(i*20, 50);
        }
    }

    /**
     * Generate the polynomial
     */
    public void generatePoly() {
        //compute hermite splines using bezier control points and user start/end points.
        int x1, y1, x2, y2;
        bezControl[0] = new Point(interp[0].x, interp[0].y); //the zeroth point is the start.
        bezControl[5] = new Point(interp[5].x, interp[5].y);
        //the last point is the end.

        //calculate points 1-4 with a formula from the internet.
        //why isn't internet in eclipse's dictionary?
        //I think it's based off some cool matrix math.
        //from a nice man on math stack exchange.
        x1 = (int) (1.6077 * interp[1].x - .26794 * interp[0].x
                - .43062 * interp[2].x + .11483 * interp[3].x
                - .028708 * interp[4].x + .004785 * interp[5].x);
        y1 = (int) (1.6077 * interp[1].y - .26794 * interp[0].y
                - .43062 * interp[2].y + .11483 * interp[3].y - .028708 * interp[4].y + .004785 * interp[5].y);

        bezControl[1] = new Point(x1, y1);
        x1 = (int) (-.43062 * interp[1].x + .07177 * interp[0].x
                + 1.7225 * interp[2].x - .45933 * interp[3].x
                + .11483 * interp[4].x - .019139 * interp[3].x);

        y1 = (int) (-.43062 * interp[1].y + .07177 * interp[0].y
                + 1.7225 * interp[2].y - .45933 * interp[3].y
                + .11483 * interp[4].y - .019139 * interp[3].y);

        bezControl[2] = new Point(x1, y1);
        x1 = (int) (.11483 * interp[1].x - .019139 * interp[0].x
                - .45933 * interp[2].x + 1.7225 * interp[3].x
                - .43062 * interp[4].x + .07177 * interp[5].x);

        y1 = (int) (.11483 * interp[1].y - .019139 * interp[0].y
                - .45933 * interp[2].y + 1.7225 * interp[3].y
                - .43062 * interp[4].y + .07177 * interp[5].y);

        bezControl[3] = new Point(x1, y1);
        x1 = (int) (-.028708 * interp[1].x + .004785 * interp[0].x
                + .114835 * interp[2].x - .43062 * interp[3].x
                + 1.6077 * interp[4].x - .26794 * interp[5].x);

        y1 = (int) (-.028708 * interp[1].y + .004785 * interp[0].y
                + .114835 * interp[2].y - .43062 * interp[3].y
                + 1.6077 * interp[4].y - .26794 * interp[5].y);

        bezControl[4] = new Point(x1, y1);

        //points for computing derivative.
        s1 = bezControl[0];
        s2 = bezControl[1];
        s3 = bezControl[4];
        s4 = bezControl[5];
        //s3.y -= 100;

        //now we have some control points, we use bernstein polynomial of degree
        //3 between each two points.
        //repeat for each segment.
        x1 = (int) interp[0].x;
        y1 = (int) interp[0].y;
        //loop for each point(6 times).
        for (int i = 1; i < pointsToShow; i++) {
            //loop through using step distance(very many times).
            for (step = i - 1; step <= i; step += prevDistance) {
                //copied from a printout from UCLA.
                //format is copied from math stack exchange guy
                //I don't really like it.
                //http://www.math.ucla.edu/~baker/149.1.02w/handouts/dd_splines.pdf
                x2 = (int) (interp[i - 1].x + (step - (i - 1)) * (-3 * interp[i - 1].x
                        + 3 * (.6667 * bezControl[i - 1].x + .3333 * bezControl[i].x)
                        + (step - (i - 1)) * (3 * interp[i - 1].x - 6 * (.6667 * bezControl[i - 1].x
                        + .3333 * bezControl[i].x) + 3 * (.3333 * bezControl[i - 1].x
                        + .6667 * bezControl[i].x) + (-interp[i - 1].x
                        + 3 * (.6667 * bezControl[i - 1].x + .3333 * bezControl[i].x)
                        - 3 * (.3333 * bezControl[i - 1].x + .6667 * bezControl[i].x)
                        + interp[i].x) * (step - (i - 1)))));
                y2 = (int) (interp[i - 1].y + (step - (i - 1)) * (-3 * interp[i - 1].y
                        + 3 * (.6667 * bezControl[i - 1].y + .3333 * bezControl[i].y)
                        + (step - (i - 1)) * (3 * interp[i - 1].y - 6 * (.6667 * bezControl[i - 1].y
                        + .3333 * bezControl[i].y) + 3 * (.3333 * bezControl[i - 1].y
                        + .6667 * bezControl[i].y) + (-interp[i - 1].y
                        + 3 * (.6667 * bezControl[i - 1].y + .3333 * bezControl[i].y) - 3 * (.3333 * bezControl[i - 1].y + .6667 * bezControl[i].y)
                        + interp[i].y) * (step - (i - 1)))));
                x1 = x2;
                y1 = y2;

            }
        }

    }

    /**
     * The second part of calculations
     */
    public void makeLists() {
        double x1, y1, x2, y2;
        //x1 = interp[0].x;
        //y1 = interp[0].y;
        //loop for each point(6 times).
        for (int i = 1; i < pointsToShow; i++) {
            //loop through using step distance(very many times).
            for (step = i - 1; step <= i; step += disatncep) {
                //copied from a printout from UCLA.
                //format is copied from math stack exchange guy
                //I don't really like it.
                //http://www.math.ucla.edu/~baker/149.1.02w/handouts/dd_splines.pdf
                x2 = (interp[i - 1].x + (step - (i - 1)) * (-3 * interp[i - 1].x
                        + 3 * (.6667 * bezControl[i - 1].x + .3333 * bezControl[i].x)
                        + (step - (i - 1)) * (3 * interp[i - 1].x - 6 * (.6667 * bezControl[i - 1].x
                        + .3333 * bezControl[i].x) + 3 * (.3333 * bezControl[i - 1].x
                        + .6667 * bezControl[i].x) + (-interp[i - 1].x
                        + 3 * (.6667 * bezControl[i - 1].x + .3333 * bezControl[i].x)
                        - 3 * (.3333 * bezControl[i - 1].x + .6667 * bezControl[i].x)
                        + interp[i].x) * (step - (i - 1)))));
                y2 = (interp[i - 1].y + (step - (i - 1)) * (-3 * interp[i - 1].y
                        + 3 * (.6667 * bezControl[i - 1].y + .3333 * bezControl[i].y)
                        + (step - (i - 1)) * (3 * interp[i - 1].y - 6 * (.6667 * bezControl[i - 1].y
                        + .3333 * bezControl[i].y) + 3 * (.3333 * bezControl[i - 1].y
                        + .6667 * bezControl[i].y) + (-interp[i - 1].y
                        + 3 * (.6667 * bezControl[i - 1].y + .3333 * bezControl[i].y) - 3 * (.3333 * bezControl[i - 1].y + .6667 * bezControl[i].y)
                        + interp[i].y) * (step - (i - 1)))));
                //g.drawLine(x1+100, y1+100, x2+100, y2+100);
                x1 = x2;
                y1 = y2;
                bxList.add(x1);
                byList.add(y1);

            }
        }
    }

    /**
     * Set a control point
     * @param x x coordinate
     * @param y y coordinate
     * @param i index (0-5)
     */
    @Override
    public void setPoint(int x, int y, int i) {
        try {
            interp[i].x = x;
            interp[i].y = y;
        } catch (Throwable ex) {
            System.out.println("control point array oob");
        }
    }

    /**
     * Do spline calculations and dump into segment group
     */
    @Override
    public void calculateSpline(double stepDistance) {
        sg = new SegmentGroup();
        disatncep = stepDistance;
        generatePoly();
        makeLists();
        for (int i = 0; i < bxList.size(); i++) {
            Segment s = new Segment();
            s.x = bxList.get(i);
            s.y = byList.get(i);
            sg.add(s);
        }
        bxList.clear();
        byList.clear();

    }

    /**
     * Get the segment group.
     * Needs to be calculated first.
     * @return
     */
    @Override
    public SegmentGroup getSegments() {
        return sg;
    }
    
    /**
     * Set waypoint index of starting segment.
     * @param i
     */
    @Override
    public void setStartingIndex(int i) {
        index = i;
    }

    /**
     * Get waypoint index of starting segment
     * @return
     */
    @Override
    public int getStartingIndex() {
        return index;
    }

    /**
     * Get starting slope.
     * @return
     */
    @Override
    public double getStartDYDX() {
        double dy = s2.y - s1.y;
        double dx = s2.x - s1.x;
        return dy/dx;
    }

    /**
     * Get ending slope.
     * @return
     */
    @Override
    public double getEndDYDX() {
        double dy = s4.y - s3.y;
        double dx = s4.x - s3.x;
        return dy/dx;
    }
    
  
    
    @Override
    public int splineID() {
        return ID;
    }

    @Override
    public void setSplineID(int id) {
        this.ID = id;
    }


}
