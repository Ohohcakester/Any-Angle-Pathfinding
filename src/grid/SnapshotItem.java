package grid;

import java.awt.Color;

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
