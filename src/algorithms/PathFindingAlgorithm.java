package algorithms;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;

/**
 * ABSTRACT<br>
 * Template for all Path Finding Algorithms used.<br>
 */
public abstract class PathFindingAlgorithm {
    private static final int SNAPSHOT_INTERVAL = 0;
    private int snapshotCountdown = 0;
    
    private ArrayList<List<SnapshotItem>> snapshotList;
    protected GridGraph graph;

    protected int parent[];
    protected final int sizeX;
    protected final int sizeXplusOne;
    protected final int sizeY;

    protected final int sx;
    protected final int sy;
    protected final int ex;
    protected final int ey;
    
    private int ticketNumber = -1;
    
    private boolean recordingMode;
    private boolean usingStaticMemory = false;

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
        snapshotList = new ArrayList<>();
    }
    
    protected void initialiseMemory(int size, float defaultDistance, int defaultParent, boolean defaultVisited) {
        usingStaticMemory = true;
        ticketNumber = Memory.initialise(size, defaultDistance, defaultParent, defaultVisited);
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
    public ArrayList<List<SnapshotItem>> retrieveSnapshotList() {
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
     * @return directly get path length without computing path.
     * Has to run fast, unlike getPath.
     */
    public abstract float getPathLength();
    
    /**
     * An optimal overridable method which prints some statistics when called for.
     */
    public void printStatistics() {
    }
    
    protected final int toOneDimIndex(int x, int y) {
        return graph.toOneDimIndex(x, y);
    }
    
    protected final int toTwoDimX(int index) {
        return graph.toTwoDimX(index);
    }
    
    protected final int toTwoDimY(int index) {
        return graph.toTwoDimY(index);
    }
    
    protected final void maybeSaveSearchSnapshot() {
        if (recordingMode) {
            if (usingStaticMemory && ticketNumber != Memory.currentTicket())
                throw new UnsupportedOperationException("Ticket does not match!");
            
            saveSearchSnapshot();
        }
    }
    
    public List<SnapshotItem> getCurrentSearchSnapshot() {
        return computeSearchSnapshot();
    }
    
    protected final boolean isRecording() {
        return recordingMode;
    }

    
    private void saveSearchSnapshot() {
        if (snapshotCountdown > 0) {
            snapshotCountdown--;
            return;
        }
        snapshotCountdown = SNAPSHOT_INTERVAL;
        
        snapshotList.add(computeSearchSnapshot());
    }

    protected final void addSnapshot(List<SnapshotItem> snapshotItemList) {
        snapshotList.add(snapshotItemList);
    }
    
    protected int goalParentIndex() {
        return toOneDimIndex(ex,ey);
    }
    
    private int getParent(int index) {
        if (usingStaticMemory) return Memory.parent(index);
        else return parent[index];
    }
    
    private void setParent(int index, int value) {
        if (usingStaticMemory) Memory.setParent(index, value);
        else parent[index] = value;
    }
    
    protected int getSize() {
        if (usingStaticMemory) return Memory.size();
        else return parent.length;
    }
    
    protected List<SnapshotItem> computeSearchSnapshot() {
        List<SnapshotItem> list = new ArrayList<>();
        int current = goalParentIndex();
        Set<Integer> finalPathSet = null;
        if (getParent(current) >= 0) {
            finalPathSet = new HashSet<Integer>();
            while (current >= 0) {
                finalPathSet.add(current);
                current = getParent(current);
            }
        }

        int size = getSize();
        for (int i=0; i<size; i++) {
            if (getParent(i) != -1) {
                if (finalPathSet != null && finalPathSet.contains(i)) {
                    list.add(SnapshotItem.generate(snapshotEdge(i), Color.BLUE));
                } else {
                    list.add(SnapshotItem.generate(snapshotEdge(i)));
                }
            }
            Integer[] vertexSnapshot = snapshotVertex(i);
            if (vertexSnapshot != null) {
                list.add(SnapshotItem.generate(vertexSnapshot));
            }
        }

        return list;
    }
    
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = getParent(endIndex);
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
    
    protected void inheritSnapshotListFrom(PathFindingAlgorithm algo) {
        this.snapshotList = algo.snapshotList;
    }
}
