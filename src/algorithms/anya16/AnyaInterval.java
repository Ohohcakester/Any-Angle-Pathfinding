package pgraph.anya;

import java.awt.geom.Point2D;
import pgraph.grid.BitpackedGrid;

/**
 * Created with IntelliJ IDEA.
 * User: dindar
 * Date: 28.06.2014
 * Time: 12:26
 * To change this template use File | Settings | File Templates.
 */
public class AnyaInterval {
    private double left;
    private double right;
    private int row;
    
    boolean discrete_left;
    boolean discrete_right;
    boolean left_is_root;
    boolean right_is_root;

    public AnyaInterval(double left, double right, int row)
    {
    	init(left, right, row);
    }
    
    public void init(double left, double right, int row)
    {
        setLeft(left);
        setRight(right);
        setRow(row);        
    }

    public static final double DOUBLE_INEQUALITY_THRESHOLD = 0.0000001;

    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
        if( obj == null || ! (obj instanceof AnyaInterval))
            return false;
        AnyaInterval p = (AnyaInterval)obj;
        return Math.abs(p.left-left) < DOUBLE_INEQUALITY_THRESHOLD && Math.abs(p.right-right) <DOUBLE_INEQUALITY_THRESHOLD && p.row ==row;

        //return ((p.left <= left && p.right>=right)||(p.left >= left && p.right<=right) ) && p.row ==row;
    }

    public boolean covers(AnyaInterval i)
    {
        if ( Math.abs(i.left-left) < DOUBLE_INEQUALITY_THRESHOLD && Math.abs(i.right-right) <DOUBLE_INEQUALITY_THRESHOLD && i.row ==row )
            return true;

        return (left <= i.left && right>=i.right && row == i.row);

    }
    
    public boolean contains(Point2D.Double p)
    {
    	return ((int)p.y) == this.row && 
    			(p.x+BitpackedGrid.epsilon) >= left && 
				p.x <= (this.right+BitpackedGrid.epsilon);
    }


    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(left);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(right);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + row;
        return result;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
    	
    	this.left = left;
    	
    	discrete_left = Math.abs((int)(left+BitpackedGrid.epsilon) - left) < 
        		BitpackedGrid.epsilon;
        if(discrete_left)
        {
        	this.left = (int)(this.left+BitpackedGrid.epsilon);
        	return;
        }
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) 
    {	
        this.right = right;
    	
    	discrete_right = Math.abs((int)(right+BitpackedGrid.epsilon) - right) <
        		BitpackedGrid.epsilon;
        if(discrete_right)
        {
        	this.right = (int)(this.right + BitpackedGrid.epsilon);
        }
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
    
    double range_size() 
    { 
    	return right - left; 
    }
    
    public String toString()
    {
    	return "Interval ("+left+", "+right+", "+row+")";
    }
}
