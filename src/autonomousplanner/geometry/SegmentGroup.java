
package autonomousplanner.geometry;

import java.util.ArrayList;

/**
 * A group of segments.
 * @author Team 236
 */
public class SegmentGroup {

    public ArrayList<Segment> s = new ArrayList<>();
    
    /**
     * Puts together all of the toString() methods for every segment.
     * Used for file output.r
     * @return
     */
    @Override
    public String toString(){
        String str = "";
        for(int i = 0; i < s.size(); i+=1){
            //str = str + s.get(i).toString() + '\n';
            Segment a = s.get(i);
            //str += String.format("%4f %4f %4f %4f %4f %4f %4f %4f  \n" , 
             //       a.time,  a.vel, a.acc, a.posit, a.x, a.y, a.dydx, a.d2ydx2);
             str += String.format("%4f %4f %4f", a.time, a.vel, a.posit);
             str+= ",";
             
        }
        //System.out.println(str);
        return str;
    }

    /**
     * Generate a number of segments to full up the segment group with.
     * @param size
     */
    public void setSize(int size){
        for(int i = 1; i < size; i++){
            s.add(new Segment());
        }
    }
    
    /**
     * Add a segment to the end of the segment group.
     * @param ss
     */
    public void add(Segment ss){
        s.add(ss);
    }
}

//String r = header;
//        for(int i = 0; i < s.s.size(); i++){
//            Segment a = s.s.get(i);
//            r += String.format("%4f %4f %4f %4f %4f %4f %4f %4f \n" , 
//                    a.x, a.y, a.time, a.posit, a.vel, a.acc, a.dydx, a.d2ydx2);
//        }