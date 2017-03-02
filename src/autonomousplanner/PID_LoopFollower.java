package autonomousplanner;

import autonomousplanner.geometry.RobotSegmentGroup;
import autonomousplanner.geometry.Segment;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A follower that uses PID loops to keep the robot on target. If only P loop is
 * needed (likely), use P_LoopFollower for better performance.
 *
 * Gains cannot be updated mid run.
 *
 * @author Jared
 */
public class PID_LoopFollower extends TimerTask {

    boolean isRunning = false;
    RobotSegmentGroup rsg;
    FollowerOutput out;
    int i;
    public double kV = .06, kAcc = 0, kP_distance = 0, kP_heading = 0, kI_distance, kI_heading, kD_distance, kD_heading;
    PID lPID = new PID();
    PID rPID = new PID();
    PID hPID = new PID();

    public PID_LoopFollower(RobotSegmentGroup rsg, FollowerOutput out) {
        //set pid gains
        lPID.kP = kP_distance;
        rPID.kP = kP_distance;
        lPID.kI = kI_distance;
        rPID.kI = kI_distance;
        lPID.kD = kD_distance;
        rPID.kD = kD_distance;
        hPID.kP = kP_heading;
        hPID.kI = kI_heading;
        hPID.kD = kD_heading;
        
        this.rsg = rsg;
        this.out = out;
        new Timer().scheduleAtFixedRate(this, 0L, 10);
        
    }

    @Override
    public void run() {
        if (isRunning) {
            //our current segments to follow.
            Segment l = rsg.left.s.get(i);
            Segment r = rsg.right.s.get(i);
            
            //like a feed forward term, this calculates the values
            //if everything were perfect, and the PID calculated
            //values compensate for imperfection.
            double lBase = (l.vel * kV + l.acc * kAcc);
            double rBase = (r.vel * kV + r.acc * kAcc);
            //update goals
            lPID.goal = l.posit;
            rPID.goal = r.posit;
            hPID.goal = rsg.robot.s.get(i).h;
            //calculate adjustments by updating the pid loop.
            double hAdjust = hPID.update(out.getHeading());
            double lAdjust = lPID.update(out.getLeftDistance());
            double rAdjust = rPID.update(out.getRightDistance());
            //adjust the outputs, flipping one side's heading adjustment
            out.setLeftPower(lBase + lAdjust + hAdjust);
            out.setRightPower(rBase + rAdjust - hAdjust);
            i++;
        }
        if (i == rsg.left.s.size()) { //if we've gotten to the end...
            i = 0;
            isRunning = false; //reset and stop.
        }
    }
}
/**
 * A smaller implementation of PID.java.
 * @author Jared
 */
class PID {

    private double errorSum, lastError;
    //public double minIError; //error where I control will begin.  Prevents integral windup.
    public double kP, kI, kD, kF, goal;

    public double update(double lastSource) {

        double out = 0; //start with zero output.
        double error = goal - lastSource; //what is the difference between where
        //we are now, and where we want to be?
        double p = kP * error; //P term- is proportional to error

//        if (Math.abs(error) < minIError) { //are we close enough to worry about the I term?
//            errorSum += error; //if so, keep integrating.
//        }
        errorSum += error; //always integrate error.
        //we're always going to be close to our target because of the way the
        //path planner is written, so this sum should never get too big.
        
        double i = kI * errorSum; //scale i
        
        double dError = error - lastError; //calculate the derivative of the error
        //this method is called periodically, so dError is always equal to delta_Error/10ms.
        
        double d = kD * dError; //scale d
        
        lastError = error;  //store the last error to calculate the next derivative
        
        double ff = kF * goal; //calculate the feedforward term.  It's just proportional
        //to the goal.
        
        out = p + i + d + ff; //add p, i, d, and ff terms
        //lastOut = out;
        //lastDeltaError = dError;
        return out;
    }
}
