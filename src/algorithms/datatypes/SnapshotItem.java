package algorithms.datatypes;

import java.awt.Color;

/**
 * Contains a [x1,y1,x2,y2] or [x,y] and a colour.
 * Refer to GridObjects.java for how the path array works.
 */
public class SnapshotItem {
    public final Integer[] path;
    public final Color color;
    
    public SnapshotItem(Integer[] path, Color color) {
        this.path = path;
        this.color = color;
    }
    
    public SnapshotItem(Integer[] path) {
        this.path = path;
        this.color = null;
    }
}
