/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autonomousplanner.geometry;

/**
 * A segment, which is a point in a path. It can store lots of data too.
 * @author Team 236
 */
public class Segment {
    public double x, h, y, posit, vel, acc, dydx, d2ydx2, jerk, dt, time, dx;
    public boolean stopHere = false;
    
    /**
     * A segment.
     */
    public Segment(){
        x = 0;
        y = 0;
        posit = 0;
        time = 0;
        vel = 0;
        acc = 0;
        jerk = 0;
        dydx = 0;
        d2ydx2 = 0;
        dt = 0;
        dx = 0;
        h = 0;
    }
    
    /**
     * Outputs some info about the segment for debugging.
     * @return
     */
    @Override
    public String toString(){
//        return ("dt " + dt + " x " + x + " y " + y + " p " + posit + " vel " +  + vel + " acc " + acc
//                + " dydx " + dydx + " d2ydx2 " + d2ydx2 + " jerk " + jerk);
        return ("dt " + dt + " vel " + vel + " acc " + acc + " time " + time + " potato " + posit);
    }
}
