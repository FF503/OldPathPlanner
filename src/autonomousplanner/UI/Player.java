
package autonomousplanner.UI;

import autonomousplanner.ContinuousPath;
import autonomousplanner.geometry.RobotSegmentGroup;
import autonomousplanner.geometry.SegmentGroup;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Class to visually display a movement path.
 * @author Team 236
 */
public class Player extends TimerTask {
    
    Timer timer = new Timer();
    Window test;
    JFrame window;

    /**
     * View path of a robot.
     * @param robot
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Player(SegmentGroup sGroup, SegmentGroup r, SegmentGroup l) {
         window = new JFrame();
		//make window go away when closed
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.setBounds(30, 30, 800, 800);
        test = new Window(sGroup, r, l);
        window.getContentPane().add(test);
        window.setVisible(true);
		//start timertask to repaint the Draw window
        timer.scheduleAtFixedRate(this, 0, 20);
    }
    
    /**
     * View path from a segment group group.
     * @param s
     */
    public Player(RobotSegmentGroup s){
        window = new JFrame();
        window.setTitle("Path Player");
		//make window go away when closed
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.setBounds(30, 30, 800, 800);
        test = new Window(s.robot, s.right, s.left);
        window.getContentPane().add(test);
        window.setVisible(true);
		//start timertask to repaint the Draw window
        timer.scheduleAtFixedRate(this, 0, 20);
    }

    /**
     * Repaint the window
     */
    @Override
    public void run() {
        test.repaint();
    }

    void reset() {
       this.window.setVisible(false);
    }
}
/**
 * The display window for player.
 * @author Jared
 */
class Window extends JComponent {
    SpeedSlider slider = new SpeedSlider();

    double i;
    SegmentGroup s, r, l;
    int k_left = 300;
    

    public Window(SegmentGroup s, SegmentGroup r, SegmentGroup l) {
        this.s=s;
        this.r = r;
        slider.setVisible(true);
        this.l = l;
    }
    /**
     * Takes care of drawing the path to the screen.
     * @param g 
     */
    public void showPath(Graphics g) {
	//loop through segments, and plot x and y, scaled and centered.
        for (int j = 0; j < s.s.size(); j++) {
            g.drawRect((int) (s.s.get(j).x * 15 + k_left),
                    (int) (s.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
        for (int j = 0; j < r.s.size(); j++) {
            g.drawRect((int) (r.s.get(j).x * 15+ k_left),
                    (int) (r.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
        for (int j = 0; j < l.s.size(); j++) {
            g.drawRect((int) (l.s.get(j).x * 15+ k_left),
                    (int) (l.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
    }
    /**
     * The paint method for the window.  Gets provided the graphics
     * object to draw the path.
     * @param g 
     */
    @Override
    public void paintComponent(Graphics g) {
        boolean isAutomated = true;

			//draw the path
            showPath(g);
			//get x, y, and theta values for this point in time
            double x = s.s.get((int) i).x * 15;
            double y = s.s.get((int) i).y * 15 + (27 * 15);
            double theta = s.s.get((int) i).dydx;
            theta = Math.atan(theta);
            
            g.drawRect(0, 0, 27 * 30, 27 * 30);
            
            g.drawChars("Velocity".toCharArray(), 0, 8, 40, 20);
            g.drawChars((String.valueOf(roundToHundreths(s.s.get((int) i).vel)).toCharArray()), 0, 3, 40, 40);

            g.drawChars("Accel".toCharArray(), 0, 5, 100, 20);
            g.drawChars((String.valueOf(roundToHundreths(s.s.get((int) i).acc)).toCharArray()), 0, 3, 100, 40);

            g.drawChars("Time".toCharArray(), 0, 4, 160, 20);
            g.drawChars((String.valueOf(roundToHundreths(s.s.get((int) i).time)).toCharArray()), 0, 3, 160, 40);

            g.drawChars("Distance".toCharArray(), 0, 8, 220, 20);
            g.drawChars((String.valueOf(roundToHundreths(s.s.get((int) i).posit)).toCharArray()), 0, 3, 220, 40);

            //draw line that indicates heading
            g.drawLine((int) x+ k_left, (int) (y + (0)), (int) (100 * Math.cos(theta) + x)+ k_left, (int) (100 * Math.sin(theta) + y + (0)));
            g.drawLine((int) x+ k_left, (int) (y + (0)), (int) (-100 * Math.cos(theta) + x)+ k_left, (int) (-100 * Math.sin(theta) + y + (0)));
			//draw oval centered at x,y
            g.drawOval((int) x - 40+ k_left, (int) y - 40, 80, 80);
			//get and scale velocity
            double v = s.s.get((int) i).vel;
            v *= 8;
			//draw circle centered at x,y ,with diameter v
            g.drawOval((int) (x - v / 2)+ k_left, (int) (y - v / 2), (int) v, (int) v);
            g.drawOval((int) x - 40+ k_left, (int) y - 40, 80, 80);
			//automatically increment to next frame if auto playing
            if(isAutomated){
            i += slider.getPlayRate();
			//restart if needed.
            if (i > s.s.size()- slider.getPlayRate()) {
                i = 0;
            }
            }
    }
    
    public double roundToHundreths(double in){
       return Math.round(in * 100.0) / 100.0;
    }

}