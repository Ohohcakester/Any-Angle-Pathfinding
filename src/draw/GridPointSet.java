package draw;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

public class GridPointSet {
    private ArrayList<ColourPoint> pointList;
    private int minCircleSize;

    public class ColourPoint {
        public final Color color;
        public final int x;
        public final int y;
        
        public ColourPoint(int x, int y, Color color) {
            this.color = color;
            this.x = x;
            this.y = y;
        }
    }
    
    public GridPointSet() {
        pointList = new ArrayList<>();
    }
    
    public void setMinCircleSize() {
        minCircleSize = 8;
    }
    
    public void clear() {
        pointList.clear();
    }
    
    public void addPoint(int x, int y, Color color) {
        pointList.add(new ColourPoint(x,y,color));
    }
    
    public Collection<ColourPoint> getPointList() {
        return pointList;
    }

    public int minCircleSize() {
        if (minCircleSize > 0) {
            return minCircleSize;
        }
        return Integer.MIN_VALUE;
    }
}
