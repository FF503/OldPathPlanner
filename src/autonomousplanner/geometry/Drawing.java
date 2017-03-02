package autonomousplanner.geometry;

import java.awt.Color;
import java.awt.Graphics;

/**
 * All things related to pretty pictures.
 *
 * @author Jared
 */
public class Drawing {

    /**
     * Draw a filled in rectangle centered at a point.
     *
     * @param x x
     * @param y y
     * @param size size
     * @param g graphics object
     */
    public static void drawFilledRectOn(
            double x, double y, int size, Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect((int) (x - (size / 2)), (int) (y - (size / 2)), size, size);
        g.setColor(Color.BLACK);
    }

    /**
     * Draw a filled in oval centered at a point.
     *
     * @param x
     * @param y
     * @param size
     * @param g
     */
    public static void drawFilledCircleAt(
            double x, double y, int size, Graphics g) {
        g.fillOval((int) (x - (size / 2)), (int) (y - (size / 2)), size, size);
    }

    /**
     * Draw a focused point here.
     *
     * @param x
     * @param y
     * @param size
     * @param g
     */
    public static void drawFocusedPointAt(
            double x, double y, int size, Graphics g) {
        g.setColor(Color.red);
        drawFilledCircleAt(x, y, size, g);
        g.setColor(Color.black);
    }

}
