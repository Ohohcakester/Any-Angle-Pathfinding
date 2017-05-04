package algorithms.sparsevgs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;

import algorithms.jgrapht.FibonacciHeap;
import algorithms.jgrapht.FibonacciHeapNode;


public class SparseVisibilityGraphAlgorithmFibHeap extends PathFindingAlgorithm {
    protected SparseVisibilityGraph visibilityGraph;
    protected boolean reuseGraph = false;

    private static FibonacciHeapNode<Integer>[] nodeRefs;
    private FibonacciHeap<Integer> pq = new FibonacciHeap<>();
    
    private SparseVisibilityGraphAlgorithmFibHeap(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }
    
    public static SparseVisibilityGraphAlgorithmFibHeap graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        SparseVisibilityGraphAlgorithmFibHeap algo = new SparseVisibilityGraphAlgorithmFibHeap(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }

    public SparseVisibilityGraph getVisibilityGraph() {
        return visibilityGraph;
    }

    private final void initialise(int s) {
        decreaseKey(s, 0f, true);
        setDistance(s, 0f);
    }
    
    @Override
    public void computePath() {
        setupVisibilityGraph();

        int size = visibilityGraph.size();
        int memorySize = visibilityGraph.maxSize();
        pq.clear();
        this.initialiseMemory(memorySize, Float.POSITIVE_INFINITY, -1, false);
        
        initialise(visibilityGraph.startNode());
        int finish = visibilityGraph.endNode();

        if (graph.lineOfSight(sx, sy, ex, ey)) {
            // There is a direct path from (sx, sy) to (ex, ey).
            if (sx != ex || sy != ey) {
                setParent(finish, visibilityGraph.startNode());
            }
            return;
        }

        while (!pq.isEmpty()) {
            //int current = pq.popMinIndex();
            FibonacciHeapNode<Integer> currNode = pq.removeMin();
            int current = currNode.getData();

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
                boolean isNewItem = distance(dest) == Float.POSITIVE_INFINITY;
                
                if (!visited(dest) && relax(current, dest, weight)) {
                    // If relaxation is done.
                    int destX = visibilityGraph.xCoordinateOf(dest);
                    int destY = visibilityGraph.yCoordinateOf(dest);
                    
                    decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey), isNewItem);
                }
            }
            if (node.hasEdgeToGoal) {
                boolean isNewItem = distance(finish) == Float.POSITIVE_INFINITY;

                float weight = graph.distance(node.x, node.y, ex, ey);
                if (relax(current, finish, weight)) {
                    decreaseKey(finish, distance(finish), isNewItem);
                }
            }
            
            maybeSaveSearchSnapshot();
        }
    }

    private final void decreaseKey(int index, float newCost, boolean isNewItem) {
        if (isNewItem) {
            nodeRefs[index] = new FibonacciHeapNode<>(index);
            pq.insert(nodeRefs[index], newCost);
        } else {
            pq.decreaseKey(nodeRefs[index], newCost);
        }
    }

    protected void setupVisibilityGraph() {
        if (reuseGraph) {
            visibilityGraph = SparseVisibilityGraph.getStoredGraph(graph);
        } else {
            visibilityGraph = new SparseVisibilityGraph(graph);
        }
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot());
            visibilityGraph.initialise(sx, sy, ex, ey);
            saveVisibilityGraphSnapshot();
        } else {
            visibilityGraph.initialise(sx, sy, ex, ey);
        }

        if (nodeRefs == null || visibilityGraph.maxSize() != nodeRefs.length) {
            nodeRefs = (FibonacciHeapNode<Integer>[])new FibonacciHeapNode[visibilityGraph.maxSize()];
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
    public float getPathLength() {
        return 0;
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
        System.out.println("Edges: " + (visibilityGraph.computeSumDegrees()/2));
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

    public static void clearMemory() {
        nodeRefs = null;
        System.gc();
    }
}
