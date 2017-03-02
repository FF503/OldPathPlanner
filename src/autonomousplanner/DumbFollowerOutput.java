
package autonomousplanner;

/**
 * A time only dead reckoning follower output.
 * @author Jared
 */
public interface DumbFollowerOutput {
    public void setLeftPower(double p);
    public void setRightPower(double p);
}
