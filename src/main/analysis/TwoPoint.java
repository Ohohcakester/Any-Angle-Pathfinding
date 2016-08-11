package main.analysis;

import algorithms.datatypes.Point;

public class TwoPoint {
    public final Point p1, p2;
    public TwoPoint (Point p1, Point p2) {
        this.p1 = p1; this.p2 = p2;
    }
    public TwoPoint (int x1, int y1, int x2, int y2) {
        this.p1 = new Point(x1,y1);
        this.p2 = new Point(x2,y2);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TwoPoint)) return false;
        TwoPoint other = (TwoPoint)obj;
        if (p1.equals(other.p1) && p2.equals(other.p2)) {
            return true;
        }
        if (p1.equals(other.p2) && p2.equals(other.p1)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Symmetric. Switching p1 and p2 should give the same hash code.
        final int prime = 31;
        int result = 1;
        int v1 = (p1 == null) ? 0 : p1.hashCode();
        int v2 = (p2 == null) ? 0 : p2.hashCode();
        int sum = v1 + v2;
        int diff = Math.abs(v1 - v2);
        
        result = prime * result + sum;
        result = prime * result + diff;
        return result;
    }
    
    @Override
    public String toString() {
        return p1.x + " " + p1.y + " " + p2.x + " " + p2.y;
    }
}