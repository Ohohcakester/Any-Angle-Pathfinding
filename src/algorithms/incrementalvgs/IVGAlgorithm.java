package algorithms.incrementalvgs;

import grid.GridGraph;
import algorithms.BasicThetaStar;
import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.Point;
import algorithms.priorityqueue.ReusableIndirectHeap;
import algorithms.sparsevgs.LineOfSightScanner;

public class IVGAlgorithm extends PathFindingAlgorithm {
    
    private static Memory.Context thetaMemoryContext = new Memory.Context();
    private static Memory.Context jpsMemoryContext = new Memory.Context();
    private static Memory.Context ivgMemoryContext = new Memory.Context();
    private static ReusableIndirectHeap.Context thetaHeapContext = new ReusableIndirectHeap.Context();
    private static ReusableIndirectHeap.Context jpsHeapContext = new ReusableIndirectHeap.Context();
    private static ReusableIndirectHeap.Context ivgHeapContext = new ReusableIndirectHeap.Context();
    
    float upperBoundLength;
    IVG visibilityGraph;
    
    ReusableIndirectHeap pq;
    LineOfSightScanner losScanner;

    public IVGAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {
        // Step 1: Initial upper bound (Theta*)
        {
            Memory.loadContext(thetaMemoryContext);
            ReusableIndirectHeap.loadContext(thetaHeapContext);
            PathFindingAlgorithm algo = new BasicThetaStar(graph, sx, sy, ex, ey);
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

        // Step 2: JPS
        Memory.loadContext(jpsMemoryContext);
        ReusableIndirectHeap.loadContext(jpsHeapContext);
        IVGJPS lowerBoundSearch = new IVGJPS(graph, ex ,ey, sx, sy, upperBoundLength);

        Memory.saveContext(jpsMemoryContext);
        ReusableIndirectHeap.saveContext(jpsHeapContext);
        
        // Step 3: Search
        Memory.loadContext(ivgMemoryContext);
        ReusableIndirectHeap.loadContext(ivgHeapContext);
        
        if (isRecording()) {
            lowerBoundSearch.startRecording();
            lowerBoundSearch.computePath();
            inheritSnapshotListFrom(lowerBoundSearch);
        } else {
            lowerBoundSearch.computePath();
        }
        
        visibilityGraph = new IVG(graph, sx, sy, ex, ey, upperBoundLength, lowerBoundSearch);
        visibilityGraph.initialise();
        losScanner = new LineOfSightScanner(graph);
        
        initialiseMemory(visibilityGraph.size(), Float.POSITIVE_INFINITY, -1, false);
        pq = new ReusableIndirectHeap(visibilityGraph.size());
        initialise(visibilityGraph.startNode());
        
        int start = pq.popMinIndex(); // pop start.
        setVisited(start, true);
        processStart(start);
        
        int finish = visibilityGraph.endNode();
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            setVisited(current, true);
            
            if (current == finish) {
                break;
            }

            process(current);
            
            maybeSaveSearchSnapshot();
        }

        Memory.saveContext(ivgMemoryContext);
        ReusableIndirectHeap.saveContext(ivgHeapContext);
    }
    
    private final void processStart(int start) {
        int currX = visibilityGraph.xCoordinateOf(start);
        int currY = visibilityGraph.yCoordinateOf(start);
        
        losScanner.computeAllVisibleTautSuccessors(currX, currY);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int x = losScanner.successorsX[i];
            int y = losScanner.successorsY[i];
            int dest = visibilityGraph.tryGetIndexOf(x, y);
            if (dest != -1 && relax(start, dest, graph.distance(sx, sy, x, y))) {
                pq.decreaseKey(dest, distance(dest));
            }
        }
    }
    
    private final void process(int index) {
        int currX = visibilityGraph.xCoordinateOf(index);
        int currY = visibilityGraph.yCoordinateOf(index);
        
        
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
        int current = visibilityGraph.endNode();
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
        int current = visibilityGraph.endNode();
        
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
        // TODO Auto-generated method stub
        return 0;
    }


    /**
     * Used by snapshot computation
     */
    @Override
    protected int goalParentIndex() {
        return visibilityGraph.endNode();
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
