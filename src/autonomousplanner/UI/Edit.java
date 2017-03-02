
package autonomousplanner.UI;

import autonomousplanner.Util;
import autonomousplanner.geometry.Cubic;
import autonomousplanner.geometry.Drawing;
import autonomousplanner.geometry.Line;
import autonomousplanner.geometry.ParametricQuintic;
import autonomousplanner.geometry.Point;
import autonomousplanner.geometry.Quintic;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.SegmentGroup;
import autonomousplanner.geometry.Spline;
import autonomousplanner.geometry.SplineGroup;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

/**
 * A superior Spline Editor.
 * @author Jared
 */
public class Edit extends TimerTask{
    Point startPoint;
    Timer timer = new Timer();
    Editor display;
    public JFrame jf;
    boolean isDragging;
    ArrayList<Spline> splines = new ArrayList<>();
    ArrayList<SplineGroup> sGroups = new ArrayList<>();
    double startX, startY, startH;
    int currentID = 0;

    int LOW_RES = 100;
    int HIGH_RES = 200_000;

    /**
     * Make new auto mode.
     *
     * @param x start x value
     * @param y start y value
     * @param h start heading (radians pls)
     * @param name name of auto mode.
     */
    public Edit(double x, double y, double h, String name) {
        startX = x;
        startY = y;
        startH = h;
        startPoint = new Point(x, y);
        startPoint.setHeading(h);
        jf = new JFrame();
        jf.setTitle("Editor");
        jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jf.setBounds(120, 30, 800, 800);
        jf.setLocation(400, 50);//consider making bigger
        display = new Editor();
        jf.getContentPane().add(display);
        jf.setVisible(true);
        timer.scheduleAtFixedRate(this, 0, 20);

    }

    /**
     * Causes the paintComponent() method to run often and update the display.
     */
    @Override
    public void run() {
        display.repaint();
    }

    /**
     * The editor to go with an auto mode.
     */
    public class Editor extends JComponent implements MouseInputListener {

        //THIS VALUE FOR RESOLUTION!!!
        int pointMover;
        boolean isHeadingMode;
        boolean canDraw = true;
        boolean isDragging; //java mouse listeners 
        @SuppressWarnings("LeakingThisInConstructor")
        ArrayList<Point> waypoints = new ArrayList<>();
        Point lastClicked = new Point(0, 0);
        Point waypointInFocus = new Point(-9999, -9999);

        @SuppressWarnings("LeakingThisInConstructor")
        public Editor() {
            addMouseListener(this); 
            addMouseMotionListener(this);
            Point s = new Point(0, 0);
            s.x = startX * 20 + 250;
            s.y = startY * 20 + 250;
            s.h = startH;
            waypoints.add(s);
        }
        
        public void toggleHeadingMode(){
            isHeadingMode = !isHeadingMode;
        }

        /**
         * Draw the graphics to the screen
         *
         * @param g
         */
        @Override
        public void paintComponent(Graphics g) {
            if(canDraw){
            calculateSplines(100);
            Image img = null;
            try {
                img = ImageIO.read(new File("Untitled.png"));
            } catch (IOException ex) {
                System.out.println(ex);
            }
            //g.drawImage(img, 0, 0, null);
            //origin
            g.drawOval(250 - 5, 250 - 5, 10, 10);
            //last click
            Drawing.drawFilledRectOn(lastClicked.x, lastClicked.y, 5, g);
            drawWaypoints(g, waypoints);
            //focused point
            Drawing.drawFocusedPointAt(
                    waypointInFocus.x, waypointInFocus.y, 10, g);
            drawSplines(g, splines, sGroups);}
        }

        /**
         * Draw some waypoints.
         *
         * @param g graphics
         * @param waypoints points to draw
         */
        public void drawWaypoints(Graphics g, ArrayList<Point> waypoints) {
            for (int i = 0; i < waypoints.size(); i++) {
                Drawing.drawFilledCircleAt(
                        waypoints.get(i).x, waypoints.get(i).y, 7, g);
            }
        }
        
        /**
         * Enable drawing to screen.
         */
        public void startDrawing(){
            canDraw = true;
        }
        
        /**
         * Disable drawing to screen.
         * Useful when modifying data normally used
         * in the drawing process.
         */
        public void stopDrawing(){
            canDraw = false;
        }

        /**
         * Draw some splines. Uses a bunch of tiny lines
         *
         * @param g graphics
         * @param splines splines to draw
         * @param sGroups groups
         */
        public void drawSplines(Graphics g, ArrayList<Spline> splines,
                ArrayList<SplineGroup> sGroups) {
            for (int i = 0; i < splines.size(); i++) {
                Spline sp = splines.get(i);

                for (int j = 1; j < sp.getSegments().s.size() - 1; j++) {
                    //look ahead line draw
                    Segment s1 = sp.getSegments().s.get(j - 1);
                    Segment s2 = sp.getSegments().s.get(j);
                    g.drawLine((int) s1.x, (int) s1.y, (int) s2.x, (int) s2.y);
                }
            }
            //now sgroups
            for (int i = 0; i < sGroups.size(); i++) {
                SplineGroup sp = sGroups.get(i);

                for (int j = 1; j < sp.getSegments().s.size(); j++) {
                    //look ahead line draw
                    Segment s1 = sp.getSegments().s.get(j - 1);
                    Segment s2 = sp.getSegments().s.get(j);
                    g.drawLine((int) s1.x, (int) s1.y, (int) s2.x, (int) s2.y);
                }
                Segment max = sp.getSegments().s.get(sp.getSegments().s.size() - 1);
                //g.drawRect((int)max.x, (int)max.y, 2, 2);
            }
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            //not used!
            
        }

        /**
         * Take care of left/right mouse clicks.
         * 
         * @param me
         */
        @Override
        public void mousePressed(MouseEvent me) {

            //check if we hit near a point.  If so,
            //set the move flag
            //if rt. click, do waypoint box.
            waypointInFocus.x = -9999;
            int x = me.getX();
            int y = me.getY();
            for (int i = 0; i < waypoints.size(); i++) {
                double dx = Math.abs(waypoints.get(i).getX() - x);
                double dy = Math.abs(waypoints.get(i).getY() - y);
                if ((dx < 4) && (dy < 4)) {
                    if (me.getButton() == 3) {
                        //right clicked near point i.
                        //go through input box steps
                        //Util.displayMessage(String.valueOf(waypoints.get(i).h), "WAYPOINT HEADING");
                        double xNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "X Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                        double yNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "Y Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                        if (true) {
                            double hNew = Double.valueOf(JOptionPane.showInputDialog(null, "Heading", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                            waypoints.set(i, coordinateTransform(new Point(250 + xNew, 250 + yNew)));
                            waypoints.get(i).h = hNew;
                            waypoints.get(i).quinticOverride = hNew;
                            waypoints.get(i).setHeading(hNew);
                            //print(waypoints.get(i).h + " starteh at " + i);
                        }

                        recalculateAllSplines(splines, sGroups, LOW_RES);
                        //find out if we need to have a heading prompt.

                    } else {
                        //left clicked near point i.
                        waypointInFocus.x = waypoints.get(i).x;
                        waypointInFocus.y = waypoints.get(i).y;
                        waypointInFocus.h = i;
                        pointMover = i;
                        isDragging = true;
                    }

                }
            }
            //set editor focus to point
            lastClicked.x = x;
            lastClicked.y = y;
        }

        /**
         * A somewhat useless appearing method. 
         * It takes a point, and moves it to the position it is already at.
         * It likely does nothing, but netbeans didn't recognize the project
         * properly when I deleted it, so I'm a little scared to remove it.
         * @param in
         * @return
         */
        public Point coordinateTransform(Point in) {
            in.move((in.x), (in.y));
            return in;
        }

        /**
         * Turn off movement when mouse is released.
         * @param me
         */
        @Override
        public void mouseReleased(MouseEvent me) {
            //kill move flag when mouse is released.
            //it doesn't always seem to catch?
            pointMover = 6;
            isDragging = false;
            recalculateAllSplines(splines, sGroups, LOW_RES);
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }

        /**
         * Take care of actually moving dragged waypoints.
         * @param me
         */
        @Override
        public void mouseDragged(MouseEvent me) {
            //hackish way with dealing with mousedragged called before
            //mouse pressed
            if (isDragging) {
                if (pointMover < waypoints.size()) {
                    waypoints.get(pointMover).move(me.getX(), me.getY());
                    waypointInFocus.x = me.getX();
                    waypointInFocus.y = me.getY();
                    repaint();
                }
            }
            recalculateAllSplines(splines, sGroups, LOW_RES);
            //move the point!
            //this gets called a bunch to update fast.

        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }

        /**
         * Adds a segment ending at the given point.
         *
         * @param type
         * @param x
         * @param y
         */
        public void addSegment(String type, double x, double y) {

            if (null != type) {
                switch (type) {
                    case "Line": {
                        waypoints.add(new Point(x, y));
                        int i = waypoints.size() - 1;
                        Line line = new Line(waypoints.get(i - 1).x, x,
                                waypoints.get(i - 1).y, y, waypoints.get(i - 1).h, 0);
                        line.calculateSegments(LOW_RES);
                        line.setStartingWaypointIndex(waypoints.size() - 1);
                        line.setSplineID(currentID);
                        currentID++;
                        splines.add(line);
                        break;
                    }
                    case "Piecewise Cubic":
                        //placeholder test
                        //add six new waypoints.
                        Cubic c = new Cubic();
                        c.setStartingIndex(waypoints.size() - 1);
                        Point p = waypoints.get(waypoints.size() - 1);
                        addCubicWaypoints(p.x, p.y, lastClicked.x, lastClicked.y, waypoints);
                        waypoints.add(new Point(x, y));
                        c.setSplineID(currentID);
                        currentID++;
                        sGroups.add(c);
                        recalculateAllSplines(splines, sGroups, LOW_RES);
                        break;
                    case "Quintic": {
                        waypoints.add(new Point(x, y));
                        int i = waypoints.size() - 1;
                        Quintic q = new Quintic(waypoints.get(i - 1).x, x,
                                waypoints.get(i - 1).y, y, waypoints.get(i - 1).h, 0);
                        q.setStartingWaypointIndex(waypoints.size() - 1);
                        q.setSplineID(currentID);
                        currentID++;
                        splines.add(q);
                        recalculateAllSplines(splines, sGroups, LOW_RES);
                        break;
                    }
                    case "Parametric Quintic": {
                        waypoints.add(new Point(x, y));
                        int i = waypoints.size() - 1;
                        ParametricQuintic q = new ParametricQuintic(waypoints.get(i - 1).x, x,
                                waypoints.get(i - 1).y, y, waypoints.get(i - 1).h, 0);
                        q.setStartingWaypointIndex(waypoints.size() - 1);
                        q.setSplineID(currentID);
                        currentID++;
                        splines.add(q);
                        recalculateAllSplines(splines, sGroups, LOW_RES);
                        break;
                    }
                }
            }
        }

//        public void testMethod(){
//            Util.makeGraph(splines.get(0).getParametricData(true), "a", "b");
//        }
        /**
         * Adds some cool equidistant points.
         * Used for cubic thing.
         * @param x0
         * @param y0
         * @param x1
         * @param y1
         * @param s segment group to add to.
         */
        public void addCubicWaypoints(double x0, double y0, double x1,
                double y1, ArrayList<Point> s) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            //waypoints.add(new Point(x0, y0));
            for (int i = 1; i < 5; i++) {
                waypoints.add(new Point(i * (dx / 5) + x0, i * (dy / 5) + y0));
            }

            //waypoints.add(new Point(x1, y1));
        }

        /**
         * Toolbox move point.
         */
        void movePoint() {
            if (waypointInFocus.x == -9999) {
                //you don't have it in focus.
                Util.displayMessage("Click on a waypoint first.", "Error");
            } else {
                double xNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "X Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                double yNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "Y Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                waypoints.set((int) waypointInFocus.h, coordinateTransform(new Point(250 + xNew, 250 + yNew)));
            }
            recalculateAllSplines(splines, sGroups, LOW_RES);
            repaint();
        }
        /**
         * Override an existing preset heading.
         * May cause first or second derivative discontinuities.
         */
        void overrideHeading() {

            if (waypointInFocus.x == -9999) {
                Util.displayMessage("Click on a waypoint first.", "Error");
            } else {
                double h = Double.valueOf(JOptionPane.showInputDialog(null, "Heading Override", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                double slope = Util.angleToSlope(h);
                waypoints.get((int) waypointInFocus.h).isOverridden = true;
                waypoints.get((int) waypointInFocus.h).quinticOverride = slope;
            }
            recalculateAllSplines(splines, sGroups, LOW_RES);
        }

        /**
         * If splines exist, recalculate. This is a small disaster.
         * The trick is calculating things in the right order.
         * In order to make the the quintic things continuous, I must calculate
         * the cubics and lines first.  Then, I somehow have to pass these slopes
         * to the quintics.
         * The trick is that there can be any number (or none) of each type of spline
         * and the splines can be arranged in any way, but only certain combinations
         * result in certain slopes.
         * @param splines
         * @param sGroups
         * @param resolution
         */
        public void recalculateAllSplines(ArrayList<Spline> splines, ArrayList<SplineGroup> sGroups, int resolution) {

            calculateSplines(resolution);
//            // print(waypoints.get(splines.get(0).getWaypointIndex()).h + " h at "  + splines.get(0).getWaypointIndex());
//            //do groups first
//            if (sGroups.size() > 0) {
//                for (int i = 0; i < sGroups.size(); i++) {
//                    int startPoint = sGroups.get(i).getStartingIndex();
//                    for (int j = 0; j < 6; j++) {
//                        sGroups.get(i).setPoint(
//                                (int) waypoints.get(startPoint + j).x,
//                                (int) waypoints.get(startPoint + j).y, j);
//
//                        //System.out.println(waypoints.get(waypoints.size()-1).x);
//                    }
//                    //set slopes.
//                    sGroups.get(i).calculateSpline(1/(double)resolution); //stupid rounding.
//
//                    waypoints.get(startPoint).h = sGroups.get(i).getStartDYDX();
//                    waypoints.get(startPoint + 5).h = sGroups.get(i).getEndDYDX();
//
//                }
//            }
//            if (splines.size() > 0) {
//                for (int i = 0; i < splines.size(); i++) {
//
//                    double x0, x1, y0, y1;
//                    x0 = waypoints.get(splines.get(i).getWaypointIndex()).x;
//                    x1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).x;
//                    y0 = waypoints.get(splines.get(i).getWaypointIndex()).y;
//                    y1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).y;
//                    splines.get(i).setExtremePoints(x0, y0, x1, y1);
//                    splines.get(i).calculateSegments(resolution);
//                    //do some slopes.
//                    double dydx0 = waypoints.get(splines.get(i).getWaypointIndex() - 1).h;
//                    double dydx1 = waypoints.get(splines.get(i).getWaypointIndex()).h;
//                    //print(waypoints.get(splines.get(i).getWaypointIndex()).h + " h at "  + splines.get(i).getWaypointIndex());
//                    if(waypoints.get(splines.get(i).getWaypointIndex()).isOverridden){
//                        dydx1 = waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride;
//                    }
//                    if(waypoints.get(splines.get(i).getWaypointIndex()-1).isOverridden){
//                        dydx0 = waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride;
//                    }
//                    splines.get(i).setStartDYDX(dydx1);
//                    splines.get(i).setEndDYDX(dydx0);
//                    
//                    splines.get(i).calculateSegments(resolution);
//                    waypoints.get(splines.get(i).getWaypointIndex() - 1).h = splines.get(i).endDYDX();
//                    waypoints.get(splines.get(i).getWaypointIndex()).h = splines.get(i).startDYDX();
//                    //if we've got quintic-quintic, quintic-end, begininning-quintic
//                    //allow point rotation
//                    //there's possibilities for array oob, so try/catch.
//                    try {
//                        if ("Quintic".equals(splines.get(i).getType())||"Parametric Quintic".equals(splines.get(i).getType())) {
//                            System.out.println("AAAA");
//                            boolean rotateEnd = false;
//                            boolean rotateStart = false;
//                            if (splines.get(i).getWaypointIndex() == waypoints.size() - 1) {
//                                print("a");
//                                //at end of list.
//                                rotateEnd = true;
//                            } else if ("Quintic".equals(splines.get(i - 1).getType())||"Parametric Quintic".equals(splines.get(i - 1).getType())) {
//                                rotateStart = true;
//                                print("b");
//                            }
//                            if (splines.get(i).getWaypointIndex() == 1) {
//                                print("c");
//                                //at the beginning
//                                rotateStart = true;
//                            } else if ("Quintic".equals(splines.get(i + 1).getType())||"Parametric Quintic".equals(splines.get(i + 1).getType())) {
//                                rotateEnd = true;
//                                print("d");
//                            }
//                            waypoints.get(splines.get(i).getWaypointIndex()).setRotate(rotateEnd);//does nothing?
//                            waypoints.get(splines.get(i).getWaypointIndex() - 1).setRotate(rotateStart);
//
//                        }
//                    } catch (IndexOutOfBoundsException ex) {
//
//                    }
//
//                }
//            }

        }

        /**
         * A second attempt at calculating splines in an effective order.
         * It works, but if you put in a bad combination, it doesn't
         * know it's not C1.
         * @param resolution
         */
        public void calculateSplines(int resolution) {
            //first we do things that don't depend on derivatives of others.
//                //this is all the splinegroup type splines.
            if (sGroups.size() > 0) {
                for (int i = 0; i < sGroups.size(); i++) {
                    int startPoint = sGroups.get(i).getStartingIndex();
                    for (int j = 0; j < 6; j++) {
                        sGroups.get(i).setPoint(
                                (int) waypoints.get(startPoint + j).x,
                                (int) waypoints.get(startPoint + j).y, j);

                        //System.out.println(waypoints.get(waypoints.size()-1).x);
                    }
                    //set slopes.
                    sGroups.get(i).calculateSpline(1 / (double) resolution); //stupid rounding.

                    waypoints.get(startPoint).h = sGroups.get(i).getStartDYDX();
                    waypoints.get(startPoint + 5).h = sGroups.get(i).getEndDYDX();
                    //System.out.println("slope at " + startPoint + " is " + sGroups.get(i).getStartDYDX());
                    //System.out.println("slope at " + startPoint+5 + " is " + sGroups.get(i).getEndDYDX());
                }
            }
            //more not dependent stuffs.
            if (splines.size() > 0) {
                for (int i = 0; i < splines.size(); i++) {
                    if ("Line".equals(splines.get(i).getType())) {
                        double x0, x1, y0, y1;
                        x0 = waypoints.get(splines.get(i).getWaypointIndex()).x;
                        x1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).x;
                        y0 = waypoints.get(splines.get(i).getWaypointIndex()).y;
                        y1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).y;

                        splines.get(i).setExtremePoints(x0, y0, x1, y1);
                        splines.get(i).calculateSegments(resolution);
                        waypoints.get(splines.get(i).getWaypointIndex()).h = splines.get(i).endDYDX();
                        waypoints.get(splines.get(i).getWaypointIndex() - 1).h = splines.get(i).endDYDX();
                    }

                }
            }
            //now dependent stuffs.
            for (int i = 0; i < splines.size(); i++) {
                if (!"Line".equals(splines.get(i).getType())) {
                    double x0, x1, y0, y1;
                    x0 = waypoints.get(splines.get(i).getWaypointIndex()).x;
                    x1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).x;
                    y0 = waypoints.get(splines.get(i).getWaypointIndex()).y;
                    y1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).y;
                    splines.get(i).setExtremePoints(x0, y0, x1, y1);
                    //splines.get(i).calculateSegments(resolution);
                    //do some slopes.
                    double dydx0 = waypoints.get(splines.get(i).getWaypointIndex() - 1).h;
                    double dydx1 = waypoints.get(splines.get(i).getWaypointIndex()).h;
                    if (waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride != -999) {
                        dydx1 = waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride;
                    }
                    if (waypoints.get(splines.get(i).getWaypointIndex() - 1).quinticOverride != -999) {
                        dydx0 = waypoints.get(splines.get(i).getWaypointIndex() - 1).quinticOverride;
                    }
                    //print(waypoints.get(splines.get(i).getWaypointIndex()).h + " h at "  + splines.get(i).getWaypointIndex());
                    if (waypoints.get(splines.get(i).getWaypointIndex()).isOverridden) {
                        dydx1 = waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride;
                    }
                    if (waypoints.get(splines.get(i).getWaypointIndex() - 1).isOverridden) {
                        dydx0 = waypoints.get(splines.get(i).getWaypointIndex()).quinticOverride;
                    }
                    splines.get(i).setStartDYDX(dydx1);
                    splines.get(i).setEndDYDX(dydx0);

                    splines.get(i).calculateSegments(resolution);
                    //waypoints.get(splines.get(i).getWaypointIndex() - 1).h = splines.get(i).endDYDX();
                    //waypoints.get(splines.get(i).getWaypointIndex()).h = splines.get(i).startDYDX();
                    //if we've got quintic-quintic, quintic-end, begininning-quintic
                    //allow point rotation
                    //there's possibilities for array oob, so try/catch.
//                    try {
//                        if ("Quintic".equals(splines.get(i).getType())||"Parametric Quintic".equals(splines.get(i).getType())) {
//                            boolean rotateEnd = false;
//                            boolean rotateStart = false;
//                            if (splines.get(i).getWaypointIndex() == waypoints.size() - 1) {
//                                print("a");
//                                //at end of list.
//                                rotateEnd = true;
//                            } else if ("Quintic".equals(splines.get(i - 1).getType())||"Parametric Quintic".equals(splines.get(i - 1).getType())) {
//                                rotateStart = true;
//                                print("b");
//                            }
//                            if (splines.get(i).getWaypointIndex() == 1) {
//                                print("c");
//                                //at the beginning
//                                rotateStart = true;
//                            } else if ("Quintic".equals(splines.get(i + 1).getType())||"Parametric Quintic".equals(splines.get(i + 1).getType())) {
//                                rotateEnd = true;
//                                print("d");
//                            }
//                            waypoints.get(splines.get(i).getWaypointIndex()).setRotate(rotateEnd);//does nothing?
//                            waypoints.get(splines.get(i).getWaypointIndex() - 1).setRotate(rotateStart);
//
//                        }
//                    } catch (IndexOutOfBoundsException ex) {

//                    }
                }
            }

        }

        public void print(Object s) {
            //System.out.println(s);
        }

        public void printl(Object s) {
            //System.out.println(s);
        }

        /**
         * Joins all splines in editor sequentially by spline ID. Has many for
         * loops.
         * Tricky because some splines are backward, and "start segment" isn't
         * always what I think it is.
         *
         * @return
         */
        public SegmentGroup joinSplines() {
            long startTime = System.currentTimeMillis();
            System.out.println("Sorting and joining Splines...");
            SegmentGroup s = new SegmentGroup();
            recalculateAllSplines(splines, sGroups, HIGH_RES);
            printl(currentID + " curr Id");
            for (int i = 0; i < currentID; i++) {
                //loops once per each spline.
                boolean isGroup = false;
                Spline spline = null;
                SplineGroup sGroup = null;
                for (int j = 0; j < currentID; j++) {
                    try {
                        if (splines.get(j).splineID() == i) {
                            isGroup = false;
                            spline = splines.get(j);
                            printl(j + " j");
                        }
                    } catch (IndexOutOfBoundsException e) {
                    }
                    //if the desired spline is a spline.
                    try {
                        if (sGroups.get(j).splineID() == i) {
                            isGroup = true;
                            sGroup = sGroups.get(j);
                        }
                    } catch (IndexOutOfBoundsException e) {

                    }

                }
                if (isGroup) {
                    ArrayList<Segment> p = sGroup.getSegments().s;
                    for (int k = 0; k < sGroup.getSegments().s.size(); k++) {
                        Segment seg = new Segment();
                        seg.x = p.get(k).x;
                        seg.y = p.get(k).y;
                        s.add(seg);
                    }
                } else {
                    ArrayList<Segment> p = spline.getSegments().s;
                    System.out.println(spline.getSegments().s.size());
//                        for (int l = 0; l < spline.getSegments().s.size(); l++) {
////                            Segment seg = new Segment();
////                            seg.x = p.get(l).x;
////                            seg.y = p.get(l).y;
////                            s.add(seg);
////                            //System.out.println("t");
////                             //System.out.println(spline.getSegments().s.size() + " size");
//                        }    
                    for(int turd = p.size()-1; turd > -1; turd--){
                        Segment seg = new Segment();
                           seg.x = p.get(turd).x;
                           seg.y = p.get(turd).y;
                           s.add(seg);
                    }
                }
            }
            System.out.println("Done! In " + (System.currentTimeMillis()-startTime) + " ms.");
            return s;
        }

    }
    /**
     * Flip the last spline to the opposite of what is is now.
     */
    void flipLastSpline(){
        splines.get(splines.size()-1).setFlipped(!splines.get(splines.size()-1).isFlipped());
        display.calculateSplines(100);
        display.repaint();
    }
    
}
