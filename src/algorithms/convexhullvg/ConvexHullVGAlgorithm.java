package algorithms.convexhullvg;

import java.util.Arrays;

import grid.GridGraph;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.rotationalplanesweep.ConvexHullRPSScanner;
import algorithms.priorityqueue.ReusableIndirectHeap;


public class ConvexHullVGAlgorithm extends PathFindingAlgorithm {

    private ConvexHullVG convexHullGraph;
    private ReusableIndirectHeap pq; 
    private int start;
    private int finish;

    public ConvexHullVGAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {

        // 1. Generate convex hulls
        convexHullGraph = new ConvexHullVG(graph);
        if (isRecording()) convexHullGraph.setSnapshotAction(() -> generateConvexHullSnapshot());
        convexHullGraph.initialise(sx, sy, ex, ey);

        int size = convexHullGraph.size();
        int memorySize = size+2;
        start = size;
        finish = size+1;

        pq = new ReusableIndirectHeap(size, memorySize);
        this.initialiseMemory(memorySize, Float.POSITIVE_INFINITY, -1, false);
        
        // 2. Search
        if (graph.lineOfSight(sx, sy, ex, ey)) {
            // There is a direct path from (sx, sy) to (ex, ey).
            if (sx != ex || sy != ey) {
                setParent(finish, start);
            }
            return;
        } 

        // Initialise Start
        setVisited(start, true);
        setDistance(start, 0);
        expand(start, sx ,sy);

        // pq
        while (!pq.isEmpty())
        {
            int current = pq.popMinIndex();
            setVisited(current, true);

            if (current == finish) {
                return;
            }

            int currX = convexHullGraph.getX(current);
            int currY = convexHullGraph.getY(current);
            
            expand(current, currX, currY);
        }
    }

    private final void expand(int currIndex, int currX, int currY) {
        // find neighbours
        ConvexHullRPSScanner scanner = convexHullGraph.computeAllVisibleSuccessors(currX, currY);
        if (isRecording()) addSnapshot(scanner.snapshotLines());

        int nNeighbours = scanner.nSuccessors;
        for (int i=0; i<nNeighbours; ++i)
        {
            int succX = scanner.successorsX[i];
            int succY = scanner.successorsY[i];
            if (!graph.lineOfSight(currX, currY, succX, succY)) continue;

            int succ = convexHullGraph.indexOf(succX, succY); // index of successor

            float newWeight = distance(currIndex) + graph.distance(currX, currY, succX, succY);
            if (newWeight < distance(succ)) {
                setDistance(succ, newWeight);
                setParent(succ, currIndex);
                pq.decreaseKey(succ, newWeight + heuristic(succX, succY));

                maybeSaveSearchSnapshot();
            }
        }

        // Check if the goal node is a successor
        if (graph.lineOfSight(currX, currY, ex, ey)) {
            float newWeight = distance(currIndex) + graph.distance(currX, currY, ex, ey);
            if (newWeight < distance(finish)) {
                setDistance(finish, newWeight);
                setParent(finish, currIndex);
                pq.decreaseKey(finish, newWeight + heuristic(ex, ey));

                maybeSaveSearchSnapshot();
            }
        }
    }

    private final float heuristic(int nx, int ny) {
        // SLD heuristic (naive)
        return graph.distance(nx, ny, ex, ey);
        // Convex hull heuristic
        // ????
    }

    private int pathLength() {
        int length = 0;
        int current = finish;
        while (current != -1) {
            current = parent(current);
            length++;
        }
        return length;
    }

    @Override
    public int[][] getPath() {
        int length = pathLength();
        int[][] path = new int[length][];
        int current = finish;
        
        int index = length-1;
        while (current != -1) {
            int x, y;
            if (current == start) {
                x = sx;
                y = sy;
            } else if (current == finish) {
                x = ex;
                y = ey;
            } else {
                x = convexHullGraph.getX(current);
                y = convexHullGraph.getY(current);
            }
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = parent(current);
        }
        
        return path;
    }

    @Override
    public float getPathLength() {
        return 0;
    }

    private final int parent(int index) {
        return Memory.parent(index);
    }
    
    private final void setParent(int index, int value) {
        Memory.setParent(index, value);
    }
    
    private final float distance(int index) {
        return Memory.distance(index);
    }
    
    private final void setDistance(int index, float value) {
        Memory.setDistance(index, value);
    }
    
    private final boolean visited(int index) {
        return Memory.visited(index);
    }
    
    private final void setVisited(int index, boolean value) {
        Memory.setVisited(index, value);
    }

    private final void generateConvexHullSnapshot() {
        addSnapshot(convexHullGraph.generateConvexHullSnapshot());
    }

    @Override
    protected int goalParentIndex() {
        return finish;
    }

    @Override
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent(endIndex);
        edge[0] = getX(startIndex);
        edge[1] = getY(startIndex);
        edge[2] = getX(endIndex);
        edge[3] = getY(endIndex);
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Integer[] edge = new Integer[2];
            edge[0] = getX(index);
            edge[1] = getY(index);
            return edge;
        }
        return null;
    }

    private final int getX(int index) {
        if (index == start) return sx;
        else if (index == finish) return ex;
        return convexHullGraph.getX(index);
    }

    private final int getY(int index) {
        if (index == start) return sy;
        else if (index == finish) return ey;
        return convexHullGraph.getY(index);

    }
}
