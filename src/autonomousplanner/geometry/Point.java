/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autonomousplanner.geometry;
/**
 * A class which represents a simple point. It is better because it can have a
 * double, which works well when dealing with feet.
 *
 * @author Team 236
 */
public class Point {
    /**
     * x point.
     */
    public double x,

    /**
     * y point.
     */
    y, h, quinticOverride = -999;
    boolean canRotate;
    public boolean isOverridden = false;

    /**
     * Make a new point!
     * @param x
     * @param y
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Point(double x, double y, double h){
        this.x = x;
        this.y = y;
        this.h = h;
    }
    

    /**
     * Get the x value of the point!
     * @return
     */
    public double getX() {
        return x;
    }
    
    /**
     * Set an optional heading for the point.
     * @param h heading
     */
    public void setHeading(double h){
        this.h = h;
    }
    
    /**
     * Get heading.
     * @return heading.
     */
    public double getHeading(){
        return h;
    }

    /**
     * Move the point to this new place.
     * @param x
     * @param y
     */
    public void move(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the Y value of the point.
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Scales many points.
     *
     * @param p
     * @param s
     * @return
     */
    public static Point[] scalePoints(Point[] p, double s) {
        for (int i = 0; i < p.length; i++) {
            scalePoint(p[i], s);
        }
        return p;
    }

    /**
     * Scales a single point.
     *
     * @param p
     * @param s
     * @return
     */
    public static Point scalePoint(Point p, double s) {
        p.x *= s;
        p.y *= s;
        return p;
    }

    /**
     * Move a single point dx units.
     * @param p
     * @param dx
     * @return
     */
    public static Point transformPoint(Point p, double dx) {
        p.x += dx;
        return p;
    }
    
    /**
     * Move many points dx units.
     * @param p
     * @param dx
     * @return
     */
    public static Point[] transformPoints(Point[] p, double dx){
        for (int i = 0; i < p.length; i++) {
            transformPoint(p[i], dx);
        }
        return p;
    }
    
    /**
     * The x and y coordinates of point.
     * @return
     */
    @Override
    public String toString(){
        return "X: " + x + " Y: " + y;
    }
    
    /**
     * Can this point be freely rotated?
     * 
     * @param rotate
     */
    public void setRotate(boolean rotate){
        canRotate = rotate;
    }
    
    /**
     * Can this point be freely rotated?
     * @return
     */
    public boolean getRotate(){
        return canRotate;
    }

}

