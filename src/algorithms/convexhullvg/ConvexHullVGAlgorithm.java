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
        convexHullGraph.initialise(sx, sy, ex, ey);

        int size = convexHullGraph.size();
        int memorySize = size+2;
        start = size+1;
        finish = size+2;

        pq = new ReusableIndirectHeap(size, memorySize);
        this.initialiseMemory(size, Float.POSITIVE_INFINITY, -1, false);
        
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

        int nNeighbours = scanner.nSuccessors;
        for (int i=0; i<nNeighbours; ++i)
        {
            int succX = scanner.successorsX[i];
            int succY = scanner.successorsY[i];
            int succ = convexHullGraph.indexOf(succX, succY); // index of successor

            float newWeight = graph.distance(currX, currY, succX, succY);
            if (newWeight < distance(succ)) {
                setDistance(succ, newWeight);
                setParent(succ, currIndex);
                pq.decreaseKey(succ, newWeight + heuristic(succX, succY));
                // maybeSaveSearchSnapshot();
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

}
