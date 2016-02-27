package draw;

import java.awt.Color;
import java.util.List;

import algorithms.anya.Fraction;
import algorithms.datatypes.SnapshotItem;

public class GridObjects {
    private static final Color POINT_COLOR = new Color(0,64,255);
    private static final Color LINE_COLOR = Color.RED;
    
    
    public final GridLineSet gridLineSet;
    public final GridPointSet gridPointSet;

    public GridObjects(GridLineSet gridLineSet, GridPointSet gridPointSet) {
        this.gridLineSet = gridLineSet;
        this.gridPointSet = gridPointSet;
    }
    
    private GridObjects() {
        this.gridLineSet = null;
        this.gridPointSet = null;
    }

    public static GridObjects nullObject() {
        return new GridObjects();
    }
    
    public boolean isNull() {
        return gridLineSet == null && gridPointSet == null;
    }
    
    /**
     * Convert a snapshot of an algorithm into a GridObjects instance.
     */
    public static GridObjects create(List<SnapshotItem> snapshot) {
        GridLineSet gridLineSet = new GridLineSet();
        GridPointSet gridPointSet = new GridPointSet();
        
        for (SnapshotItem item : snapshot) {
            Integer[] path = item.path;
            Color color = item.color;
            if (path.length == 4) {
                gridLineSet.addLine(path[0], path[1], path[2], path[3], or(LINE_COLOR,color));
            } else if (path.length == 2) {
                gridPointSet.addPoint(path[0], path[1], or(POINT_COLOR,color));
            } else if (path.length == 7) {
                // y, xLn, xLd, xRn, xRd, px, py
                Fraction y = new Fraction (path[0]);
                Fraction xL = new Fraction(path[1], path[2]);
                Fraction xR = new Fraction(path[3], path[4]);
                Fraction xMid = xR.minus(xL).multiplyDivide(1, 2).plus(xL);
                Fraction px = new Fraction (path[5]);
                Fraction py = new Fraction (path[6]);
                gridLineSet.addLine(px, py, xL, y, or(Color.CYAN,color));
                //gridLineSet.addLine(px, py, xMid, y, or(Color.CYAN,color));
                gridLineSet.addLine(px, py, xR, y, or(Color.CYAN,color));
                gridLineSet.addLine(xL, y, xR, y, or(LINE_COLOR,color));
                gridPointSet.addPoint(path[5], path[6], or(Color.BLUE,color));
            } else if (path.length == 5) {
                Fraction y = new Fraction (path[0]);
                Fraction xL = new Fraction(path[1], path[2]);
                Fraction xR = new Fraction(path[3], path[4]);
                gridLineSet.addLine(xL, y, xR, y, or(Color.GREEN,color));
            }
        }
        return new GridObjects(gridLineSet,gridPointSet);
    }

    private static Color or(Color color, Color original) {
        return (original==null?color:original);
    }
    
    
}
