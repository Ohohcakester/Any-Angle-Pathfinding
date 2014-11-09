package grid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class PathFindingAlgorithm {
    protected LinkedList<List<Integer[]>> snapshotList;
    protected GridGraph graph;

    protected int parent[];
    private final int sizeX;
    private final int sizeXplusOne;
    private final int sizeY;

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
    
    public void startRecording() {
        recordingMode = true;
    }
    
    public void stopRecording() {
        recordingMode = false;
    }
    
    public LinkedList<List<Integer[]>> retrieveSnapshotList() {
        return snapshotList;
    }
    
    public abstract void computePath();

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

    protected void saveSearchSnapshot() {
        snapshotList.addLast(computeSearchSnapshot());
    }
    
    private List<Integer[]> computeSearchSnapshot() {
        List<Integer[]> list = new ArrayList<>();
        
        for (int i=0; i<parent.length; i++) {
            if (parent[i] != -1) {
                list.add(snapshotEdge(i));
            }
            Integer[] vertexSnapshot = snapshotVertex(i);
            if (vertexSnapshot != null) {
                list.add(vertexSnapshot);
            }
        }

        return list;
    }
    
    private Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent[endIndex];
        edge[0] = toTwoDimX(startIndex);
        edge[1] = toTwoDimY(startIndex);
        edge[2] = toTwoDimX(endIndex);
        edge[3] = toTwoDimY(endIndex);
        return edge;
    }
    
    private Integer[] snapshotVertex(int index) {
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
