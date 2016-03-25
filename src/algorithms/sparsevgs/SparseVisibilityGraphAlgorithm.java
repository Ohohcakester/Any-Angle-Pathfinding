package algorithms.sparsevgs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import algorithms.AStarStaticMemory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;


public class SparseVisibilityGraphAlgorithm extends AStarStaticMemory {
    protected SparseVisibilityGraph visibilityGraph;
    protected boolean reuseGraph = false;
    
    private SparseVisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static SparseVisibilityGraphAlgorithm graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        SparseVisibilityGraphAlgorithm algo = new SparseVisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }

    public SparseVisibilityGraph getVisibilityGraph() {
        return visibilityGraph;
    }
    
    @Override
    public void computePath() {
        setupVisibilityGraph();

        int size = visibilityGraph.size();
        int memorySize = visibilityGraph.maxSize();
        pq = new ReusableIndirectHeap(size, memorySize);
        this.initialiseMemory(memorySize, Float.POSITIVE_INFINITY, -1, false);
        
        initialise(visibilityGraph.startNode());
        int finish = visibilityGraph.endNode();

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            setVisited(current, true);
            
            if (current == finish) {
                break;
            }
            
            SVGNode node = visibilityGraph.getOutgoingEdges(current);
            int[] outgoingEdges = node.outgoingEdges;
            float[] outgoingWeights = node.edgeWeights;
            int nEdges = node.nEdges;
            
            for (int i=0;i<nEdges;++i) {
                int dest = outgoingEdges[i];
                float weight = outgoingWeights[i];
                
                if (!visited(dest) && relax(current, dest, weight)) {
                    // If relaxation is done.
                    int destX = visibilityGraph.xCoordinateOf(dest);
                    int destY = visibilityGraph.yCoordinateOf(dest);
                    
                    pq.decreaseKey(dest, distance(dest) + heuristic(destX, destY));
                }
            }
            
            maybeSaveSearchSnapshot();
        }
    }

    protected void setupVisibilityGraph() {
        if (reuseGraph) {
            visibilityGraph = SparseVisibilityGraph.getStoredGraph(graph, sx, sy, ex, ey);
        } else {
            visibilityGraph = new SparseVisibilityGraph(graph, sx, sy, ex, ey);
        }
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot());
            visibilityGraph.initialise();
            saveVisibilityGraphSnapshot();
        } else {
            visibilityGraph.initialise();
        }
    }

    protected final boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        float newWeight = distance(u) + weightUV;
        if (newWeight < distance(v)) {
            int p = parent(u);
            if (p != -1) {
                int x1 = visibilityGraph.xCoordinateOf(p);
                int y1 = visibilityGraph.yCoordinateOf(p);
                int x2 = visibilityGraph.xCoordinateOf(u);
                int y2 = visibilityGraph.yCoordinateOf(u);
                int x3 = visibilityGraph.xCoordinateOf(v);
                int y3 = visibilityGraph.yCoordinateOf(v);
                
                if (!graph.isTaut(x1,y1,x2,y2,x3,y3)) return false;
            }
            setDistance(v, newWeight);
            setParent(v, u);
            return true;
        }
        return false;
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
            int x = visibilityGraph.xCoordinateOf(current);
            int y = visibilityGraph.yCoordinateOf(current);
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = parent(current);
        }
        
        return path;
    }

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
    
    protected void saveVisibilityGraphSnapshot() {
        /*if (!isRecording()) {
            return;
        }*/
        int size = visibilityGraph.size();
        
        List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

        for (int i=0;i<size;i++) {
            SVGNode node = visibilityGraph.getOutgoingEdges(i);
            int[] outgoingEdges = node.outgoingEdges;
            int nEdges = node.nEdges;
            
            for (int j=0;j<nEdges;++j) {
                int source = i;
                int dest = outgoingEdges[j];
                
                if (source < dest) {
                    Integer[] path = new Integer[4];
                    path[0] = visibilityGraph.xCoordinateOf(source);
                    path[1] = visibilityGraph.yCoordinateOf(source);
                    path[2] = visibilityGraph.xCoordinateOf(dest);
                    path[3] = visibilityGraph.yCoordinateOf(dest);
                    
                    SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.GREEN);
                    snapshotItemList.add(snapshotItem);
                }
            }
        }
        addSnapshot(snapshotItemList);
    }
    
    @Override
    public void printStatistics() {
        System.out.println("Nodes: " + visibilityGraph.size());
        System.out.println("Edges (Directed): " + visibilityGraph.computeSumDegrees());
    }
}
