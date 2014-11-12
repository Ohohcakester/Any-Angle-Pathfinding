package grid.anya;


/**
 * An interval is represented within the set by it's right boundary. (xR)
 */
public class Interval implements Comparable<Interval> {

    public int y;
    public Fraction xL;
    public Fraction xR;
    public Point parent;
    public boolean visited;
    public float fValue;
    public boolean isStartPoint;

    public Interval(int yStart, int xStart) {
        this.y = yStart;
        this.xL = new Fraction(xStart);
        this.xR = xL;
        this.fValue = 0;
        isStartPoint = true;
        parent = new Point(xStart, yStart);
    }

    
    public Interval(int y, Fraction xL, Fraction xR) {
        this.y = y;
        this.xL = xL;
        this.xR = xR;
        this.fValue = Float.POSITIVE_INFINITY;
    }
    
    public Interval(int y, Fraction xL, Fraction xR, float fValue, Point parent) {
        this.y = y;
        this.xL = xL;
        this.xR = xR;
        this.fValue = fValue;
        this.parent = parent;
    }

    public boolean isLessThanOrEqual(Interval o) {
        return this.compareTo(o) <= 0;
    }

    @Override
    public int compareTo(Interval o) {
        int result = this.xR.compareTo(o.xR);
        if (result == 0) {
            if (this.isStartPoint) { // start point comes after.
                if (o.isStartPoint) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if (o.isStartPoint) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isStartPoint ? 1231 : 1237);
        result = prime * result + ((xR == null) ? 0 : xR.hashCode());
        result = prime * result + y;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Interval other = (Interval) obj;
        if (isStartPoint != other.isStartPoint)
            return false;
        if (xR == null) {
            if (other.xR != null)
                return false;
        } else if (!xR.equals(other.xR))
            return false;
        if (y != other.y)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "("+y+", "+xL+", "+xR+")";
    }
}

