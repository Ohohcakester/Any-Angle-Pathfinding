package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;

public class VisibilityGraphOptimised {

    private static VisibilityGraphOptimised storedVisibilityGraph;
    private static GridGraph storedGridGraph;
    
    private final GridGraph graph;
    private LineOfSightScanner losScanner;
    private int[][] nodeIndex;
    private int startIndex = -1;
    private int endIndex = -1;
    
    private Runnable saveSnapshot;

    private int originalSize;
    private int maxSize;
    private VGNode[] nodes;
    private int nNodes;

    private int[] startStoredNeighbours;
    private float[] startStoredWeights;
    private int startStoredNumNeighbours;
    private int[] endStoredNeighbours;
    private float[] endStoredWeights;
    private int endStoredNumNeighbours;

    public VisibilityGraphOptimised(GridGraph graph) {
        this.graph = graph;
    }

    public final void setSaveSnapshotFunction(Runnable saveSnapshot) {
        this.saveSnapshot = saveSnapshot;
    }

    public final void initialise(int sx, int sy, int ex, int ey) {
        // Check if graph already initialised
        if (nodes == null) {
            long _st = System.nanoTime();
            losScanner = new LineOfSightScanner(graph);
            
            nodes = new VGNode[11];
            nNodes = 0;
            
            addNodes();
            
            originalSize = nNodes;
            maxSize = nNodes + 2;
            nodes = Arrays.copyOf(nodes, maxSize);
            
            addAllEdges();
            
            long _ed = System.nanoTime();
            System.out.println("Construction Time: " + (_ed-_st)/1000000.);
        }

        restoreOriginalGraph();
        addStartAndEnd(sx,sy,ex,ey);
    }

    private final void addNodes() {
        nodeIndex = new int[graph.sizeY+1][];
        for (int y=0;y<nodeIndex.length;y++) {
            nodeIndex[y] = new int[graph.sizeX+1];
            for (int x=0; x<nodeIndex[y].length; x++) {
                if (graph.isOuterCorner(x, y)) {
                    nodeIndex[y][x] = assignNode(x, y);
                } else {
                    nodeIndex[y][x] = -1;
                }
            }
        }
    }

    private final int assignNode(int x, int y) {
        int index = nNodes;
        if (index >= nodes.length) nodes = Arrays.copyOf(nodes, nodes.length*2);
        nodes[index] = new VGNode(x,y);
        nNodes++;
        return index;
    }
    
    private final void restoreOriginalGraph() {
        // Safeguard against multiple restores.
        if (startIndex == -1) return;
        
        // Reset hasEdgeToGoal array.
        markHasEdgeToGoal(false);
        // PostCondition: hasEdgeToGoal has all values == false.
        if (endIndex >= originalSize) {
            VGNode e = nodes[endIndex];
            nodeIndex[e.y][e.x] = -1;
        } else {
            VGNode endNode = nodes[endIndex];
            endNode.outgoingEdges = endStoredNeighbours;
            endNode.edgeWeights = endStoredWeights;
            endNode.nEdges = endStoredNumNeighbours;
            endStoredNeighbours = null;
            endStoredWeights = null;
        }
        
        if (startIndex >= originalSize) {
            VGNode s = nodes[startIndex];
            nodeIndex[s.y][s.x] = -1;
        } else {
            VGNode startNode = nodes[startIndex];
            startNode.outgoingEdges = startStoredNeighbours;
            startNode.edgeWeights = startStoredWeights;
            startNode.nEdges = startStoredNumNeighbours;
            startStoredNeighbours = null;
            startStoredWeights = null;
        }

        // Reset size.
        nNodes = originalSize;
        
        startIndex = -1;
        endIndex = -1;
    }

    public final void addStartAndEnd(int sx, int sy, int ex, int ey) {
        
        // START:
        if (nodeIndex[sy][sx] == -1) {
            startIndex = nNodes;
            ++nNodes;
            
            nodeIndex[sy][sx] = startIndex;
            nodes[startIndex] = new VGNode(sx, sy);
        } else {
            startIndex = nodeIndex[sy][sx];
            VGNode startNode = nodes[startIndex];
            startStoredNeighbours = startNode.outgoingEdges;
            startStoredWeights = startNode.edgeWeights;
            startStoredNumNeighbours = startNode.nEdges;
            startNode.outgoingEdges = new int[11];
            startNode.edgeWeights = new float[11];
            startNode.nEdges = 0;
        }
        addEdgesToVisibleNeighbours(startIndex, sx, sy);
        
        // END:
        if (nodeIndex[ey][ex] == -1) {
            endIndex = nNodes;
            ++nNodes;

            nodeIndex[ey][ex] = endIndex;
            nodes[endIndex] = new VGNode(ex, ey);
            
        } else {
            endIndex = nodeIndex[ey][ex];
            VGNode endNode = nodes[endIndex];
            endStoredNeighbours = endNode.outgoingEdges;
            endStoredWeights = endNode.edgeWeights;
            endStoredNumNeighbours = endNode.nEdges;
            endNode.outgoingEdges = new int[11];
            endNode.edgeWeights = new float[11];
            endNode.nEdges = 0;
        }
        addEdgesToVisibleNeighbours(endIndex, ex, ey);
        
        // Mark all hasEdgeToGoal vertices.
        markHasEdgeToGoal(true);
    }

    private final int addEdgesToVisibleNeighbours(int index, int x, int y) {
        losScanner.computeAllVisibleSuccessors(x, y);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int toX = losScanner.successorsX[i];
            int toY = losScanner.successorsY[i];
            int targetIndex = nodeIndex[toY][toX];

            float weight = graph.distance(x, y, toX, toY);
            addEdge(index, targetIndex, weight);
        }
        
        return index;
    }

    private final void markHasEdgeToGoal(boolean value) {
        int[] endNeighbours = nodes[endIndex].outgoingEdges;
        int n = nodes[endIndex].nEdges;
        for (int i=0;i<n;++i) {
            nodes[endNeighbours[i]].hasEdgeToGoal = value;
        }
    }
    
    private final void addAllEdges() {
        int fromX, fromY, toX, toY;
        for (int i=0;i<nNodes;++i) {
            fromX = xCoordinateOf(i);
            fromY = yCoordinateOf(i);
            
            losScanner.computeAllVisibleSuccessors(fromX, fromY);
            int nSuccessors = losScanner.nSuccessors;
            for (int j=0;j<nSuccessors;++j) {
                toX = losScanner.successorsX[j];
                toY = losScanner.successorsY[j];
                int toIndex = nodeIndex[toY][toX];

                float weight = graph.distance(fromX, fromY, toX, toY);
                addEdge(i, toIndex, weight);
            }
        }
    }
    
    private final void addEdge(int fromI, int toI, float weight) {
        nodes[fromI].addEdge(toI, weight);
    }
    
    public final int xCoordinateOf(int index) {
        return nodes[index].x;
    }

    public final int yCoordinateOf(int index) {
        return nodes[index].y;
    }

    public final int size() {
        return nNodes;
    }
    
    public final int maxSize() {
        return maxSize;
    }
    
    public final int computeSumDegrees() {
        int sum = 0;
        for (int i=0;i<nNodes;++i) {
            sum += nodes[i].nEdges;
        }
        return sum;
    }

    public final VGNode getOutgoingEdges(int source) {
        return nodes[source];
    }

    public final int startNode() {
        return startIndex;
    }

    public final int endNode() {
        return endIndex;
    }
    
    private final void maybeSaveSnapshot() {
        if (saveSnapshot != null) saveSnapshot.run();
    }
    
    
    private static final VisibilityGraphOptimised repurpose(VisibilityGraphOptimised oldGraph) {
        VisibilityGraphOptimised newGraph = new VisibilityGraphOptimised(oldGraph.graph);
        newGraph.nodeIndex = oldGraph.nodeIndex;
        newGraph.startIndex = oldGraph.startIndex;
        newGraph.endIndex = oldGraph.endIndex;
        newGraph.nodes = oldGraph.nodes;
        newGraph.nNodes = oldGraph.nNodes;
        newGraph.originalSize = oldGraph.originalSize;
        newGraph.maxSize = oldGraph.maxSize;
        newGraph.losScanner = oldGraph.losScanner;

        newGraph.startStoredNeighbours = oldGraph.startStoredNeighbours;
        newGraph.startStoredWeights = oldGraph.startStoredWeights;
        newGraph.startStoredNumNeighbours = oldGraph.startStoredNumNeighbours;
        newGraph.endStoredNeighbours = oldGraph.endStoredNeighbours;
        newGraph.endStoredWeights = oldGraph.endStoredWeights;
        newGraph.endStoredNumNeighbours = oldGraph.endStoredNumNeighbours;
        
        return newGraph;
    }
    
    public static final VisibilityGraphOptimised getStoredGraph(GridGraph graph) {
        VisibilityGraphOptimised visibilityGraph = null;
        if (storedGridGraph != graph || storedVisibilityGraph == null) {
            //("Get new graph");
            visibilityGraph = new VisibilityGraphOptimised(graph);
            storedVisibilityGraph = visibilityGraph;
            storedGridGraph = graph;
        } else {
            //("Reuse graph");
            visibilityGraph = repurpose(storedVisibilityGraph);
            storedVisibilityGraph = visibilityGraph;
        }
        return visibilityGraph;
    }

    private final void printAllEdges() {
        for (int i=0;i<nNodes;++i) {
            VGNode node = nodes[i];
            System.out.println("Node " + i + " : " + node.nEdges + " edges");
            for (int j=0;j<node.nEdges;++j) {
                System.out.println(i + " -> " + node.outgoingEdges[j]);
            }
        }
    }

    public static void clearMemory() {
        storedVisibilityGraph = null;
        storedGridGraph = null;
        System.gc();
    }
    
}

final class VGNode {
    final int x;
    final int y;

    /** Invariant: If any new neighbour is added to outgoingEdges, (i.e. not in the static version)
     *             it will appear after all other outgoing edges
     *             
     * i.e. the possibilities for outgoingEdges[] are:
     *    [x x x x x x d d d]
     *                ^     ^
     *                |     '--- nEdges marker
     *                |
     *                '--- originalNEdges marker.
     *                
     *  Invariant: Within the original neighbours, the outgoing edges are sorted by increasing index.
     *             Within the new neighbours, the outgoing edges are sorted by increasing index.
     */
    int[] outgoingEdges;
    float[] edgeWeights;
    int nEdges = 0;
    
    boolean hasEdgeToGoal = false;
    
    public VGNode(int x, int y) {
        this.x = x;
        this.y = y;
        outgoingEdges = new int[11];
        edgeWeights = new float[11];
    }

    public final void addEdge(int toIndex, float weight) {
        if (nEdges >= outgoingEdges.length) {
            outgoingEdges = Arrays.copyOf(outgoingEdges, outgoingEdges.length*2);
            edgeWeights = Arrays.copyOf(edgeWeights, edgeWeights.length*2);
        }
        outgoingEdges[nEdges] = toIndex;
        edgeWeights[nEdges] = weight;
        nEdges++;
    }
}