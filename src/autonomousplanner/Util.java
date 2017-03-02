/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomousplanner;

import autonomousplanner.geometry.Point;
import autonomousplanner.geometry.RobotSegmentGroup;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.SegmentGroup;
import java.awt.TextArea;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 * A few useful methods.
 *
 * @author Jared
 */
public class Util {

    /**
     * Create a window asking for a number.
     *
     * @param message Prompt for the number
     * @param title Title of window
     * @return The number
     */
    public static double messageBoxDouble(String message, String title) {
        double x;
        try {
            x = Double.valueOf(JOptionPane.showInputDialog(
                    null, message, title, JOptionPane.PLAIN_MESSAGE));
        } catch (NumberFormatException e) {
            x = 0;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, new TextArea(sw.toString()),
                    "Number Error", 3);
        }
        return x;
    }

    /**
     * Create a window asking for a string.
     *
     * @param message prompt
     * @param title title of window
     * @return the string
     */
    public static String messageBoxString(String message, String title) {
        return JOptionPane.showInputDialog(
                null, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Display an error message.
     *
     * @param message The message
     * @param title Title of the window.
     */
    public static void displayMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, 2);
    }

    /**
     * Return slope between the two points.
     *
     * @param a
     * @param b
     * @return
     */
    public static double slope(Point a, Point b) {
        double dy = a.y - b.y;
        double dx = a.x - b.x;
        return dy / dx;
    }

    /**
     * Slope to radian. It's atan2.
     *
     * @param x
     * @param y
     * @return
     */
    public static double slopeToRadians(double x, double y) {
        return Math.atan2(y, x);
    }

    /**
     * Angle to slope. It's tangent(theta).
     *
     * @param theta
     * @return
     */
    public static double angleToSlope(double theta) {
        return Math.tan(theta);
    }

    /**
     * Create a segment group from a string of data. 
     * Based off of 254's version.
     * @param in
     * @return
     */
    public static RobotSegmentGroup stringToPath236(String in) {
        //a.time,  a.vel, a.acc, a.posit, a.x, a.y, a.dydx, a.d2ydx2
        RobotSegmentGroup rsg = new RobotSegmentGroup();
        rsg.left = new SegmentGroup();
        rsg.right = new SegmentGroup();
        rsg.robot = new SegmentGroup();
        //string breaker.  Uses \n
        StringTokenizer tokenizer = new StringTokenizer(in, "\n");
        System.out.println("found " + tokenizer.countTokens() + " things");
        //the first line is the name.  It's compatible with team 254's paths too.
        String title = tokenizer.nextToken();

        int size = Integer.parseInt(tokenizer.nextToken());

        for (int i = 0; i < size; i++) {
            StringTokenizer lineToken = new StringTokenizer(
                    tokenizer.nextToken(), " ");
            Segment s = new Segment();
            s.time = getNextDouble(lineToken);
            s.vel = getNextDouble(lineToken);
            s.acc = getNextDouble(lineToken);
            s.posit = getNextDouble(lineToken);
            s.x = getNextDouble(lineToken);
            s.y = getNextDouble(lineToken);
            s.dydx = getNextDouble(lineToken);
            s.d2ydx2 = getNextDouble(lineToken);
            rsg.robot.add(s);
        }

        for (int i = 0; i < size; i++) {
            StringTokenizer lineToken = new StringTokenizer(
                    tokenizer.nextToken(), " ");
            Segment s = new Segment();
            s.time = getNextDouble(lineToken);
            s.vel = getNextDouble(lineToken);
            s.acc = getNextDouble(lineToken);
            s.posit = getNextDouble(lineToken);
            s.x = getNextDouble(lineToken);
            s.y = getNextDouble(lineToken);
            s.dydx = getNextDouble(lineToken);
            s.d2ydx2 = getNextDouble(lineToken);
            rsg.left.add(s);
        }

        for (int i = 0; i < size; i++) {
            StringTokenizer lineToken = new StringTokenizer(
                    tokenizer.nextToken(), " ");
            Segment s = new Segment();
            s.time = getNextDouble(lineToken);
            s.vel = getNextDouble(lineToken);
            s.acc = getNextDouble(lineToken);
            s.posit = getNextDouble(lineToken);
            s.x = getNextDouble(lineToken);
            s.y = getNextDouble(lineToken);
            s.dydx = getNextDouble(lineToken);
            s.d2ydx2 = getNextDouble(lineToken);
            rsg.right.add(s);
        }
        return rsg;
    }

    /**
     * Get the next double from the given tokenizer.
     * @param tok
     * @return
     */
    public static double getNextDouble(StringTokenizer tok) {
        return Double.parseDouble(tok.nextToken());
    }

    public RobotSegmentGroup stringToPath254(String in) {
        return null;
    }
    /**
     * Read a file into a string.  Not used.
     * @param path
     * @param encoding
     * @return
     * @throws IOException 
     */
    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    /**
     * A better file reader. 
     * @param file
     * @return
     * @throws IOException
     */
    public static String readStringFromFile(File file) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());
        return new String(data, Charset.defaultCharset());
    }

        /**
     * Method from 2014 data viewer.
     *
     * @param x
     * @param y
     * @param name
     * @param yaxis
     */
    public static void makeGraph(SegmentGroup group, String name, String yaxis) {
//        XYSeriesCollection collection = new XYSeriesCollection();
//        XYSeries series = new XYSeries(name);
//
//        //make sure the lists are the same size/from same path
//        //add to series
//        for (int i = 0; i < group.s.size() - 1; i++) {
//            series.add(group.s.get(i).time, group.s.get(i).acc);
//        }
//        //add series to collection
//        collection.addSeries(series);
//
//        //graph and make window
//        JFreeChart chart = ChartFactory.createScatterPlot(name, "X", yaxis,
//                collection, PlotOrientation.VERTICAL, true, true, false);
//        ChartFrame frame = new ChartFrame(name, chart);
//        frame.pack();
//        frame.setVisible(true);

    }
}
