package algorithms;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import algorithms.datatypes.SnapshotItem;

/**
 * ABSTRACT<br>
 * Template for all Path Finding Algorithms used.<br>
 */
public abstract class PathFindingAlgorithm {
    private LinkedList<List<SnapshotItem>> snapshotList;
    protected GridGraph graph;

    protected int parent[];
    protected final int sizeX;
    protected final int sizeXplusOne;
    protected final int sizeY;

    protected final int sx;
    protected final int sy;
    protected final int ex;
    protected final int ey;
    
    private boolean recordingMode;

    public PathFindingAlgorithm(GridGraph graph, int sizeX, int sizeY,
            int sx, int sy, int ex, int ey) {
        this.graph = graph;
        this.sizeX = sizeX;
        this.sizeXplusOne = sizeX+1;
        this.sizeY = sizeY;
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        snapshotList = new LinkedList<>();
    }
    
    /**
     * Call to start tracing the algorithm's operation.
     */
    public void startRecording() {
        recordingMode = true;
    }
    
    /**
     * Call to stop tracing the algorithm's operation.
     */
    public void stopRecording() {
        recordingMode = false;
    }
    
    /**
     * @return retrieve the trace of the algorithm that has been recorded.
     */
    public LinkedList<List<SnapshotItem>> retrieveSnapshotList() {
        return snapshotList;
    }
    
    /**
     * Call this to compute the path.
     */
    public abstract void computePath();

    /**
     * @return retrieve the path computed by the algorithm
     */
    public abstract int[][] getPath();
    
    /**
     * An optimal overridable method which prints some statistics when called for.
     */
    public void printStatistics() {
    }
    
    protected int toOneDimIndex(int x, int y) {
        return y*sizeXplusOne + x;
    }
    
    protected int toTwoDimX(int index) {
        return index%sizeXplusOne;
    }
    
    protected int toTwoDimY(int index) {
        return index/sizeXplusOne;
    }
    
    protected void maybeSaveSearchSnapshot() {
        if (recordingMode) {
            saveSearchSnapshot();
        }
    }
    
    protected boolean isRecording() {
        return recordingMode;
    }

    private void saveSearchSnapshot() {
        snapshotList.addLast(computeSearchSnapshot());
    }

    protected final void addSnapshot(List<SnapshotItem> snapshotItemList) {
        snapshotList.addLast(snapshotItemList);
    }
    
    protected List<SnapshotItem> computeSearchSnapshot() {
        List<SnapshotItem> list = new ArrayList<>();
        
        for (int i=0; i<parent.length; i++) {
            if (parent[i] != -1) {
                list.add(new SnapshotItem(snapshotEdge(i)));
            }
            Integer[] vertexSnapshot = snapshotVertex(i);
            if (vertexSnapshot != null) {
                list.add(new SnapshotItem(vertexSnapshot));
            }
        }

        return list;
    }
    
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent[endIndex];
        edge[2] = toTwoDimX(endIndex);
        edge[3] = toTwoDimY(endIndex);
        if (startIndex < 0) {
            edge[0] = edge[2];
            edge[1] = edge[3];
        } else {
            edge[0] = toTwoDimX(startIndex);
            edge[1] = toTwoDimY(startIndex);
        }
        
        return edge;
    }
    
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Integer[] edge = new Integer[2];
            edge[0] = toTwoDimX(index);
            edge[1] = toTwoDimY(index);
            return edge;
        }
        return null;
    }
    
    protected boolean selected(int index) {
        return false;
    }
}
