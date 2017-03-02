package autonomousplanner;

import autonomousplanner.geometry.RobotSegmentGroup;
import autonomousplanner.geometry.Segment;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A time and feedback based trajectory follower. Uses only proportional
 * feedback loops. No PID here.
 *
 * @author Jared
 */
public class P_LoopFollower extends TimerTask {

    boolean isRunning = false;
    RobotSegmentGroup rsg;
    FollowerOutput out;
    int i;
    public double kV = .06, kAcc = 0, kP_distance = 0, kP_heading = 0;

    public P_LoopFollower(RobotSegmentGroup rsg, FollowerOutput out) {
        this.rsg = rsg;
        this.out = out;
        new Timer().scheduleAtFixedRate(this, 0L, 10);
    }

    @Override
    public void run() {
        if (isRunning) {

            Segment l = rsg.left.s.get(i);
            Segment r = rsg.right.s.get(i);

            double lBase = (l.vel * kV + l.acc * kAcc);
            double rBase = (r.vel * kV + r.acc * kAcc);

            double lAdjust = (out.getLeftDistance() - l.posit) * kP_distance +
                    (out.getHeading() - rsg.robot.s.get(i).h) * kP_heading;
            double rAdjust = (out.getRightDistance() - r.posit) * kP_distance - 
                    (out.getHeading() - rsg.robot.s.get(i).h) * kP_heading;
            out.setLeftPower(lBase + lAdjust);
            out.setRightPower(rBase + rAdjust);
            i++;
        }
        if (i == rsg.left.s.size()) {
            i = 0;
            isRunning = false; //reset and stop at end.
        }
    }
}
