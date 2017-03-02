
package autonomousplanner;

/**
 * Something that can follow a path.  A follower will automatically update 
 * these methods every 10 ms.
 * @author Jared
 */
public interface FollowerOutput {
    public void setLeftPower(double p);
    public void setRightPower(double p);
    public double getLeftDistance();
    public double getRightDistance();
    public double getHeading();
    public void resetHeading();
    public void resetDistances();
}
