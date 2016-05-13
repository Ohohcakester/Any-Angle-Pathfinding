package algorithms.incrementalvgs;

import grid.GridGraph;
import algorithms.BasicThetaStar;
import algorithms.JumpPointSearch;
import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.priorityqueue.ReusableIndirectHeap;
import algorithms.sparsevgs.LineOfSightScanner;

public class IVGAlgorithm extends PathFindingAlgorithm {
    
    private static Memory.Context thetaMemoryContext = new Memory.Context();
    private static Memory.Context jpsMemoryContext = new Memory.Context();
    private static Memory.Context ivgMemoryContext = new Memory.Context();
    private static ReusableIndirectHeap.Context thetaHeapContext = new ReusableIndirectHeap.Context();
    private static ReusableIndirectHeap.Context jpsHeapContext = new ReusableIndirectHeap.Context();
    private static ReusableIndirectHeap.Context ivgHeapContext = new ReusableIndirectHeap.Context();
    
    int startIndex;
    int endIndex;
    
    float upperBoundLength;
    IVG visibilityGraph;
    boolean hasDirectPathToGoal = false;
    
    ReusableIndirectHeap pq;
    LineOfSightScanner losScanner;

    public IVGAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {
        // Special case: start has LOS to goal.
        if (graph.lineOfSight(sx, sy, ex, ey)) {
            hasDirectPathToGoal = true;
            return;
        }
        
        int totalSize = (sizeX+1)*(sizeY+1);

        // Step 1: Initial upper bound (Theta*)
        {
            Memory.loadContext(thetaMemoryContext);
            ReusableIndirectHeap.loadContext(thetaHeapContext);
            //PathFindingAlgorithm algo = new BasicThetaStar(graph, sx, sy, ex, ey);
            PathFindingAlgorithm algo = JumpPointSearch.repeatedPostSmooth(graph, sx, sy, ex, ey);
            //PathFindingAlgorithm algo = new StrictThetaStar(graph, sx, sy, ex, ey);
            
            if (isRecording()) {
                algo.startRecording();
                algo.computePath();
            } else {
                algo.computePath();
            }
            
            upperBoundLength = algo.getPathLength();
            
            Memory.saveContext(thetaMemoryContext);
            ReusableIndirectHeap.saveContext(thetaHeapContext);
            if (upperBoundLength < 0.001f) {
                return;
            }
        }
        // Step 1 End: Initial upper bound (Theta*)

        // Step 2: JPS Lower Bound Search
        {
            visibilityGraph = new IVG(graph, sx, sy, ex, ey, upperBoundLength);
            losScanner = new LineOfSightScanner(graph);
            visibilityGraph.initialise();
            visibilityGraph.findPointsReachableFromGoal(losScanner);
            
            Memory.loadContext(jpsMemoryContext);
            ReusableIndirectHeap.loadContext(jpsHeapContext);
            //IVGJPS lowerBoundSearch = new IVGJPS(graph, ex ,ey, sx, sy, upperBoundLength, visibilityGraph);
            IVGBFS lowerBoundSearch = new IVGBFS(graph, ex ,ey, sx, sy, upperBoundLength, visibilityGraph);
    
            if (isRecording()) {
                //lowerBoundSearch.startRecording();
                lowerBoundSearch.computePath();
                //inheritSnapshotListFrom(lowerBoundSearch);
            } else {
                lowerBoundSearch.computePath();
            }
            
            visibilityGraph.strengthenHeuristics();
            
            startIndex = visibilityGraph.startNode();
            endIndex = visibilityGraph.endNode();
    
            Memory.saveContext(jpsMemoryContext);
            ReusableIndirectHeap.saveContext(jpsHeapContext);
        }
        // Step 2 End : JPS Lower Bound Search

        // Step 3: Incremental Visibility Graph Search
        {
            Memory.loadContext(ivgMemoryContext);
            ReusableIndirectHeap.loadContext(ivgHeapContext);
            
            initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);
            pq = new ReusableIndirectHeap(visibilityGraph.size(), totalSize);
            initialise(startIndex);
            
            int start = pq.popMinIndex(); // pop start.
            setVisited(start, true);
            processStart(start);
            
            while (!pq.isEmpty()) {
                int current = pq.popMinIndex();
                setVisited(current, true);
                
                if (current == endIndex) break;
    
                process(current);
                
                maybeSaveSearchSnapshot();
            }
    
            Memory.saveContext(ivgMemoryContext);
            ReusableIndirectHeap.saveContext(ivgHeapContext);
        }
        // Step 3 End: Incremental Visibility Graph Search
    }
    
    /**
     * Assumption: Start vertex is not visible from the goal vertex.
     */
    private final void processStart(int start) {
        int currX = visibilityGraph.xCoordinateOf(start);
        int currY = visibilityGraph.yCoordinateOf(start);
        
        losScanner.computeAllVisibleTautSuccessors(currX, currY);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int x = losScanner.successorsX[i];
            int y = losScanner.successorsY[i];
            int dest = visibilityGraph.tryGetIndexOf(x, y);
            if (dest == -1) continue;
            
            float h = visibilityGraph.lowerBoundRemainingDistance(x, y);
            if (h != Float.POSITIVE_INFINITY && relax(start, dest, graph.distance(sx, sy, x, y))) {
                pq.decreaseKey(dest, distance(dest) + h);
            }
        }
    }
    
    private final void process(int index) {
        int parentIndex = parent(index);
        
        int currX = visibilityGraph.xCoordinateOf(index);
        int currY = visibilityGraph.yCoordinateOf(index);
        int parX = visibilityGraph.xCoordinateOf(parentIndex);
        int parY = visibilityGraph.yCoordinateOf(parentIndex);
        
        losScanner.computeAllVisibleIncrementalTautSuccessors(currX, currY, currX-parX, currY-parY);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int x = losScanner.successorsX[i];
            int y = losScanner.successorsY[i];
            int dest = visibilityGraph.tryGetIndexOf(x, y);
            if (dest == -1) continue;
            
            float h = visibilityGraph.lowerBoundRemainingDistance(x, y);
            if (h != Float.POSITIVE_INFINITY && relax(index, dest, graph.distance(currX, currY, x, y))) {
                pq.decreaseKey(dest, distance(dest) + h);
            }
        }
        
        // Has Line of Sight to goal. (handled as a separate edge case as LOSScanner doesn't detect non-corner vertex goals)
        // Note / TODO: Might make multiple parallel edges to goal.
        if (visibilityGraph.isVisibleFromGoal(currX, currY)) {
            if (relax(index, endIndex, graph.distance(currX, currY, ex, ey))) {
                pq.decreaseKey(endIndex, distance(endIndex) + visibilityGraph.lowerBoundRemainingDistance(ex, ey));
            }
        }
    }

    protected final boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        float newWeight = distance(u) + weightUV;
        if (newWeight < distance(v)) {
            setDistance(v, newWeight);
            setParent(v, u);
            return true;
        }
        return false;
    }
    

    private final void initialise(int s) {
        setDistance(s, 0f);
        pq.decreaseKey(s, 0);
    }

    private int pathLength() {
        int length = 0;
        int current = endIndex;
        while (current != -1) {
            current = parent(current);
            length++;
        }
        return length;
    }

    @Override
    public int[][] getPath() {
        if (visibilityGraph == null) {
            if (hasDirectPathToGoal) {
                return new int[][]{{sx,sy},{ex,ey}};
            } else {
                // No path to goal.
                return new int[0][];
            }
        }
        
        int length = pathLength();
        int[][] path = new int[length][];
        int current = endIndex;
        
        int index = length-1;
        while (current != -1) {
            path[index] = new int[2];
            path[index][0] = visibilityGraph.xCoordinateOf(current);
            path[index][1] = visibilityGraph.yCoordinateOf(current);
            
            index--;
            current = parent(current);
        }
        
        return path;
    }

    @Override
    public float getPathLength() {
        return 0;
    }


    /**
     * Used by snapshot computation
     */
    @Override
    protected int goalParentIndex() {
        return endIndex;
    }

    @Override
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent(endIndex);
        edge[0] = visibilityGraph.xCoordinateOf(startIndex);
        edge[1] = visibilityGraph.yCoordinateOf(startIndex);
        edge[2] = visibilityGraph.xCoordinateOf(endIndex);
        edge[3] = visibilityGraph.yCoordinateOf(endIndex);
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Integer[] edge = new Integer[2];
            edge[0] = visibilityGraph.xCoordinateOf(index);
            edge[1] = visibilityGraph.yCoordinateOf(index);
            return edge;
        }
        return null;
    }
    
    
    protected final int parent(int index) {
        return Memory.parent(index);
    }
    
    protected final void setParent(int index, int value) {
        Memory.setParent(index, value);
    }
    
    protected final float distance(int index) {
        return Memory.distance(index);
    }
    
    protected final void setDistance(int index, float value) {
        Memory.setDistance(index, value);
    }
    
    protected final boolean visited(int index) {
        return Memory.visited(index);
    }
    
    protected final void setVisited(int index, boolean value) {
        Memory.setVisited(index, value);
    }
}
