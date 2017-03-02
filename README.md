AutonomousPlanner
=================

The second (and improved) version of path planner


This software has a graphical interface to drag and drop points on a robot's path.  You can right click on a point to constrain it to a certain spot.  Also, for each section (the path between two waypoints), you can select either a line or a curved path that's constructed from two 5th degree polynomials.  If you connect a line and a line, your path may have a corner where the robot must stop and turn.  To avoid this, use a line and a parametric quintic, or a parametric quintic and a parametric quintic.  If a line is joined to a curved path, and the heading of the line is changed, the curved path will automatically adjust itself to remove corners from the path.


How it Works
============
The program starts by generating all line paths.  Each path is an array of points, called PathSegments.  Each pathSegment contains x and y location and first and second derivatives of y with respect to x (used to calculate heading, and rate of change of heading with respect to distance traveled).  This code is located in AutonomousMode.java, which is in the src/UI package.  The inverse tangent of the first derivative gives the heading, and the second derivative will always be zero for the lines because the heading never changes.

Next, the curved paths are generated, starting from the first path, and moving to the end.  Each section is scaled down so that the robot's total displacement is the x direction is 1 unit, and scaled up to real size after being calculated.  Then, two 5th degree polynomial functions are interpolated to represent the path.  One polynomial represents x position, and the other represents y position, and both are functions of a parameter s.  In my implementation, x goes from 0 to 1 as the robot moves across the section of the path. A list of all points along the   For more information on parametric functions, see this wikipedia page
http://en.wikipedia.org/wiki/Parametric_equation

It turns out that given a starting and ending x position, y position, first derivative, and second derivative, there is only one 5th degree polynomial that satisfies these conditions.  However, before we can generate the x position and y position polynomials, we must supply the start/end conditions.  The x and y position are given by the location of the waypoints, and the heading is given either by the user or is driven by another segment, such as a line.  The second derivative is set to zero, making it a "relaxed" spline.  You can read more about this in the second paper.  Unfortunetly, we have no way to determine the individual first derivative of the x position and the y position functions.  Instead, we only know the heading, which can be used to compute the ratio of the x position's first derivative (with respect to our parameter variable, s) to the y position's first derivative. To solve this problem, the user can slide the curviness slider, which sets the x position's derivative, and from that, the y-position's derivative is set.  Additionally, negating both derivatives has the effect of "twisting" the path.  It would change a path shaped like a C to a path shaped like an S.  Both start and end in the same spot (but with one heading reversed!), but the S has an additional twist.  It turns out that this helps create a smoother curve, as not every pair of x position and y position derivatives will result in an efficient path.  

The process to generate these curves is run in a lower resolution mode continuously so that the user can drag around points and curviness sliders to see how the path will update as he adjusts things.

https://www.rose-hulman.edu/~finn/CCLI/Notes/day09.pdf
This paper outlines the approach I took, which used the quintic hermite basis functions to construct the polynomial.  This method limits you to paths where y position is a function of x position, which removes the functionality for paths that are circles, or loop back on themselves.  I took it a step further, and used two polynomials to represent the path.  This parametric configuration allows for any shape of path.

http://www.math.ucla.edu/~baker/149.1.02w/handouts/dd_splines.pdf
This paper is also very helpful.  It discusses how we must also pay attention to the second derivative when creating our paths.  A path may be smooth and first derivative continous, but still very difficult to drive, due to discontinuities in the second derivative.  Imagine driving a car, and having to immediately transition from a sharp left turn to a sharp right turn.  This happens if the second derivative of your path (meaning second derivative of y position with respect to x position) has jumps or discontinuities.   This paper also discusses the parametric/non-parametric difference nicely.

##Finalizing Path
This final computation begins by computing all segments in a very high resolution (roughly 15k points/foot of distance travelled).  This is located in ContinuousPath.java  This super-high resolution method is probably not the best way to solve this problem, but as you will see later, is required for one of my methods.  Even though there are many ponits, it easily runs this segment in less than 50ms, but uses a fair amount of RAM. 

These points contain first derivatives at each point (can be used to find heading), but we must find the second derivative from the data.  

Next, we must tackle the problem of finding the robot's velocity at each point.  Before we worry about acceleration limits, we must create a function that represents the maximum possible velocity at each point, given no restratints on acceleration.  There are three limits to velocity (excluding acceleration)

* Maximum Velocity of the outside wheel in a turn
* Robot's maximum velocity
* Maximum  acceleration the drive motors can provide to change the direction of the robot's direction

The third one is very important- a robot weighs 150 lbs, and can't take a right angle turn while traveling at 15 feet/second.  There is some math involved to compute the radius of curvature of the path and the speed of the outermost point on the robot (must be given the width of the robot first!), as well as some approximations for the robot's moment of inertia (how much torque is needed to get angular acceleration of the robot).

After we have data that represents maximum velocities at each point, we must generate a robot velocity function that always remains below this maximum velocity fuction.  Also, the steepness of this curve, which is related to acceleration, must have limits, as we can only accelerate so quickly.  This gets more complicated, because our function is a veloctity position curve instead of a velocity time curve, so acceleration is not simply the slope of the graph.  This math is explained in the code.  To generate the robot velocity curve, we first work from the start to the end, only paying attention to our maximum velocity and maximum acceleration, and we disregard maximum deceleration.  This result will have possible accelerations, but may have a deceleration that's too steep.  To correct this, we work from back to front with the same calculation, paying attention to acceleration in the back to front direction.  Finally, we compare the curves, and pick the lowest velocity between the two for each.  This likely doesn't make much sense without a diagram, but it does work quite well for producing a velocity/displacement curve with limited acceleration that's always lower than a second maximum velocity curve.

Finally, we add the velocity data to the position curve from earlier, and calculate the overall time and the change in time between/at each point.

```Java
ArrayList<Segment> p = pathSegments.s;
        for (int i = 1; i < p.size(); i++) {
            if (p.get(i).dx == 0) {
                p.remove(i);
            }
        }
        p.get(0).vel = 0;
        double time = 0;
        for (int i = 1; i < p.size(); i++) {
            //what is the maximum our v_f can be?
            //sqrt(v_0^2 + 2*a_max*dx)
            double v_0 = p.get(i - 1).vel;
            double dx = p.get(i - 1).dx;
            if (dx != 0) {
                double v_max = Math.sqrt(Math.abs(v_0 * v_0 + 2 * max_acc * dx));
                double v = Math.min(v_max, p.get(i).vel);
                if (Double.isNaN(v)) {
                    v = p.get(i - 1).vel;
                }
                p.get(i).vel = v;
            } else {
                p.get(i).vel = p.get(i - 1).vel;
            }
        }
        p.get(p.size() - 1).vel = 0;
        for (int i = p.size() - 2; i > 1; i--) {
            double v_0 = p.get(i + 1).vel;
            double dx = p.get(i + 1).dx;
            double v_max = Math.sqrt(Math.abs(v_0 * v_0 + 2 * max_dcc * dx));
            double v = Math.min((Double.isNaN(v_max) ? max_vel : v_max), p.get(i).vel);
            p.get(i).vel = v;
        }

        for (int i = 1; i < p.size(); i++) {
            double v = p.get(i).vel;
            double dx = p.get(i - 1).dx;
            double v_0 = p.get(i - 1).vel;
            time = time + (2 * dx) / (v + v_0);
            time = (Double.isNaN(time)) ? 0 : time;
            p.get(i).time = time;

        }
        //get rid of no dt segs
        for (int i = 1; i < p.size(); i++) {
            double dt = p.get(i).time - p.get(i - 1).time;
            if (dt == 0 || Double.isInfinite(dt)) {
                p.remove(i);
            }
        }
```
Next, we throw away the majority of our data points, only picking the ones closest to each 0.01 second time interval.  This is why we had so much resolution in our path up to now.  The high resolution ensures that there will a point close to each of our time points, each spaced out every .01 seconds.  This method will detect if any individual point is off by more than 3%, and will attempt to interpolate a new data point closer to the correct time, but it has yet to pick up on any inaccuracy greater than 1%.

We split by time so that the robot's path following thread, which runs every .01 seconds can just pull a new data point after each iteration without having to worry about additional timing issues.

Next, the path is split into two separate paths, one for each side of the robot.  The user must enter the robot's width to get these paths in the right place.  Now that each path has new x and y locations, and first derivatives are recalculated.  Second derivatives are skipped because they were only needed for the radius of curvature calculation.  In addition, we also store the velocity/acceleration data for each point in each of the sides.  Note that these velocities/acceleratiosn will be different for each side.  In addition, distance travelled at each point is stored in the path array.  This helps the robot know if it's on path at any point in time by comparing actual and desired distance travelled, and can be used to compute the neccessary correction.

Next, this data is loaded into a text file which can be FTP'd to the robot controller.

On startup, the robot controller reads in the text file, and converts it back to a path object.  Java's StringTokenizer class is the coolest thing in the world.

To follow the path, we use two feedforward terms, and three PID loops.  PID controllers are better described on Wikipedia 
http://en.wikipedia.org/wiki/PID_controller

The feedforward terms attempt to approximate the motor output power needed to stay on the path at all points in time.  It turns out there is a fairly linear relation between velocity and motor voltage and acceleration and motor voltage, so we can approximate our output with this function
```
output = (acc * K_a) + (vel * K_v);
```
Where ka and kv are two constants determined through testing that related desired velocity/accleration to motor voltage.  Two PID loops monitor the two side's distance travelled, measured with sensors installed in the robot's gearboxes, and correct the output power slightly to keep the actual distance traveled accurate.  A third PID loop compares actual robot heading to desired robot heading, and adjusts outputs to ensure the robot is always pointed in the right direction.




