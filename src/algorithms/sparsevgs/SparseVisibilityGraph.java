package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;

//import main.utility.TimeCounter;

public class SparseVisibilityGraph {

    private static SparseVisibilityGraph storedVisibilityGraph;
    private static GridGraph storedGridGraph;
    
    protected final GridGraph graph;
    protected int[][] nodeIndex;
    protected int startIndex;
    protected boolean startIsNewNode;
    protected int endIndex;
    protected boolean endIsNewNode;
    protected final int sx, sy, ex, ey;
    
    private Runnable saveSnapshot;
    
    private int maxSize;
    private SVGNode[] nodes;
    private int nNodes;
    //private ArrayList<Point> nodeList;
    //private ArrayList<ArrayList<Edge>> outgoingEdgeList;

    public SparseVisibilityGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
        this.graph = graph;
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
    }

    public void setSaveSnapshotFunction(Runnable saveSnapshot) {
        this.saveSnapshot = saveSnapshot;
    }

    public void initialise() {
        if (nodes != null) {
            //("already initialised.");
            return;
        }
        
        nodes = new SVGNode[11];
        nNodes = 0;
        
        addNodes();
        
        maxSize = nNodes + 2;
        nodes = Arrays.copyOf(nodes, maxSize);
        
        addAllEdges();
        addStartAndEnd(sx, sy, ex, ey);
    }

    protected void addNodes() {
        nodeIndex = new int[graph.sizeY+1][];
        for (int y=0;y<nodeIndex.length;y++) {
            nodeIndex[y] = new int[graph.sizeX+1];
            for (int x=0; x<nodeIndex[y].length; x++) {
                if (isCorner(x, y)) {
                    nodeIndex[y][x] = assignNode(x, y);
                } else {
                    nodeIndex[y][x] = -1;
                }
            }
        }
    }

    protected final int assignNode(int x, int y) {
        int index = nNodes;
        if (index >= nodes.length) nodes = Arrays.copyOf(nodes, nodes.length*2);
        nodes[index] = new SVGNode(x,y);
        nNodes++;
        return index;
    }

    protected int assignNodeAndConnect(int x, int y) {
        int index = nNodes;
        if (index >= nodes.length) nodes = Arrays.copyOf(nodes, nodes.length*2);
        nodes[index] = new SVGNode(x,y);


        for (int i=0; i<nNodes; i++) {
            int toX = xCoordinateOf(i);
            int toY = yCoordinateOf(i);
            if (shouldAddEdge(x, y, toX, toY)) {
                float weight = computeWeight(x, y, toX, toY);
                addEdge(i, index, weight);
                addEdge(index, i, weight);
            }
        }

        nNodes++;
        return index;
    }

    /**
     * This comes from a special case. 
     * Edges that cannot be part of a taut path are not connected in the visibility graph.
     * This is checked by a certain TLBR-TRBL condition in shouldAddEdge.
     * However, this condition is relaxed when one of the endpoints is the start or end vertex.
     * Thus, we may need to add some additional edges to the existing vertex if the start/end vertex
     * happens to be one of the existing vertices in the visibility graph.
     */
    protected void connectAdditionalEdges(int index, int x, int y) {
        int lastMatchingIndex = 0;
        int[] outgoingEdges = nodes[index].outgoingEdges;
        int existingNEdges = nodes[index].nEdges;
        
        for (int i=0; i<nNodes; i++) {
            // We skip over nodes that are already connected to the current node, in order to avoid duplicates.
            // We can do each check in constant time by assuming that the neighbours are ordered by index.
            if (lastMatchingIndex < existingNEdges && outgoingEdges[lastMatchingIndex] == i) {
                // Already connected.
                lastMatchingIndex++;
                continue;
            }
            if (i == index) continue; // Avoid connecting to self.
            
            int toX = xCoordinateOf(i);
            int toY = yCoordinateOf(i);
            if (shouldAddEdge(x, y, toX, toY)) {
                float weight = computeWeight(x, y, toX, toY);
                addEdge(i, index, weight);
                addEdge(index, i, weight);
            }
        }
    }
    
    private void removeExtraEdgesFromAllTargets(int[] outgoingEdges, int nEdges) {
        for (int i=0;i<nEdges;++i) {
            nodes[outgoingEdges[i]].removeExtraEdges();
        }
    }
    
    /**
     * Assumption: start and end are the last two nodes, if they exist.
     */
    protected void removeStartAndEnd() {
        // Clean up the extra edges. (Note: There is no problem with repeated calls to removeExtraEdges for the same vertex)
        {
            int nEndOutgoingEdges = nodes[endIndex].nEdges; // This value is saved beforehand as cleaning the start node may clear this value.
            
            removeExtraEdgesFromAllTargets(nodes[startIndex].outgoingEdges, nodes[startIndex].nEdges);
            nodes[startIndex].removeExtraEdges();
            removeExtraEdgesFromAllTargets(nodes[endIndex].outgoingEdges, nEndOutgoingEdges);
            nodes[endIndex].removeExtraEdges();
        }
        
        if (endIsNewNode) {
            int index = nNodes-1;
            nodeIndex[ey][ex] = -1;
            nodes[index] = null;
            nNodes--;

            endIsNewNode = false;
        }
        
        if (startIsNewNode) {
            int index = nNodes-1;
            nodeIndex[sy][sx] = -1;
            nodes[index] = null;
            nNodes--;

            startIsNewNode = false;
        }
    }
    
    protected void addStartAndEnd(int sx, int sy, int ex, int ey) {
        if (isNode(sx, sy)) {
            startIndex = indexOf(sx, sy);
            startIsNewNode = false;
            connectAdditionalEdges(startIndex, sx, sy);
        } else {
            startIndex = nodeIndex[sy][sx] = assignNodeAndConnect(sx, sy);
            startIsNewNode = true;
        }

        if (isNode(ex, ey)) {
            endIndex = indexOf(ex, ey);
            endIsNewNode = false;
            connectAdditionalEdges(endIndex, ex, ey);
        } else {
            endIndex = nodeIndex[ey][ex] = assignNodeAndConnect(ex, ey);
            endIsNewNode = true;
        }
    }
    
    protected void addAllEdges() {
        int saveFactor = nNodes/10;
        if (saveFactor == 0) saveFactor = 1;
        
        int fromX, fromY, toX, toY;
        for (int i=0; i<nNodes; i++) {
            fromX = xCoordinateOf(i);
            fromY = yCoordinateOf(i);

            for (int j=i+1; j<nNodes; j++) {
                toX = xCoordinateOf(j);
                toY = yCoordinateOf(j);
                
                if (shouldAddEdge(fromX, fromY, toX, toY)) {
                    float weight = computeWeight(fromX, fromY, toX, toY);
                    addEdge(i, j, weight);
                    addEdge(j, i, weight);
                }
            }
            
            if (i%saveFactor == 0)
                maybeSaveSnapshot();
        }
        
        for (int i=0; i<nNodes; ++i) {
            nodes[i].lockOriginalNEdges();
        }
    }
    
    private boolean shouldAddEdge(int x1, int y1, int x2, int y2) {
        // Assumption: (x1,y1), (x2,y2) are either outer corners or start / goal vertices
        int dx = x2-x1;
        int dy = y2-y1;
        
        boolean p1Ok = (sx == x1 && sy == y1) || (ex == x1 && ey == y1);
        boolean p2Ok = (sx == x2 && sy == y2) || (ex == x2 && ey == y2);
        if (dx*dy > 0) {
            p1Ok = p1Ok || graph.topLeftOfBlockedTile(x1, y1) || graph.bottomRightOfBlockedTile(x1, y1);
            p2Ok = p2Ok || graph.topLeftOfBlockedTile(x2, y2) || graph.bottomRightOfBlockedTile(x2, y2);
        } else if (dx*dy < 0) {
            p1Ok = p1Ok || graph.bottomLeftOfBlockedTile(x1, y1) || graph.topRightOfBlockedTile(x1, y1);
            p2Ok = p2Ok || graph.bottomLeftOfBlockedTile(x2, y2) || graph.topRightOfBlockedTile(x2, y2);
        } else {
            // dx == 0 xor dy == 0
            if (dx > 0) {
                p1Ok = p1Ok || graph.bottomLeftOfBlockedTile(x1, y1) || graph.topLeftOfBlockedTile(x1, y1);
                p2Ok = p2Ok || graph.bottomRightOfBlockedTile(x2, y2) || graph.topRightOfBlockedTile(x2, y2);
            } else if (dy > 0) {
                p1Ok = p1Ok || graph.bottomLeftOfBlockedTile(x1, y1) || graph.bottomRightOfBlockedTile(x1, y1);
                p2Ok = p2Ok || graph.topLeftOfBlockedTile(x2, y2) || graph.topRightOfBlockedTile(x2, y2);
            } else if (dx < 0) {
                p1Ok = p1Ok || graph.bottomRightOfBlockedTile(x1, y1) || graph.topRightOfBlockedTile(x1, y1);
                p2Ok = p2Ok || graph.bottomLeftOfBlockedTile(x2, y2) || graph.topLeftOfBlockedTile(x2, y2);
            } else { // dy < 0
                p1Ok = p1Ok || graph.topLeftOfBlockedTile(x1, y1) || graph.topRightOfBlockedTile(x1, y1);
                p2Ok = p2Ok || graph.bottomLeftOfBlockedTile(x2, y2) || graph.bottomRightOfBlockedTile(x2, y2);
            }
        }
        return p1Ok && p2Ok && graph.lineOfSight(x1,y1,x2,y2);
    }
    
    protected final float computeWeight(int x1, int y1, int x2, int y2) {
        int dx = x2-x1;
        int dy = y2-y1;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    
    protected final void addEdge(int fromI, int toI, float weight) {
        nodes[fromI].addEdge(toI, weight);
    }
    
    
    protected int indexOf(int x, int y) {
        return nodeIndex[y][x];
    }
    
    protected boolean isNode(int x, int y) {
        return nodeIndex[y][x] != -1;
    }
    

    protected final boolean isCorner(int x, int y) {
        boolean a = graph.isBlocked(x-1, y-1);
        boolean b = graph.isBlocked(x, y-1);
        boolean c = graph.isBlocked(x, y);
        boolean d = graph.isBlocked(x-1, y);
        
        return ((!a && !c) || (!d && !b)) && (a || b || c || d);
        
        /* NOTE
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *  
         *  The definition below excludes the above case.
         */
        
        /*int results = 0;
        if(a)results++;
        if(b)results++;
        if(c)results++;
        if(d)results++;
        return (results == 1);*/
    }

    public int xCoordinateOf(int index) {
        return nodes[index].x;
    }

    public int yCoordinateOf(int index) {
        return nodes[index].y;
    }

    public int size() {
        return nNodes;
    }
    
    public int maxSize() {
        return maxSize;
    }
    
    public int computeSumDegrees() {
        int sum = 0;
        for (int i=0;i<nNodes;++i) {
            sum += nodes[i].nEdges;
        }
        return sum;
    }

    SVGNode getOutgoingEdges(int source) {
        return nodes[source];
    }

    /*public Edge getEdge(int source, int dest) {
        
        ArrayList<Edge> edges = outgoingEdgeList.get(source);
        for (Edge edge : edges) {
            if (edge.dest == dest) {
                return edge;
            }
        }
        return new Edge(source, dest, Float.POSITIVE_INFINITY);
    }*/

    public int startNode() {
        return startIndex;
    }

    public int endNode() {
        return endIndex;
    }
    
    private void maybeSaveSnapshot() {
        if (saveSnapshot != null)
            saveSnapshot.run();
    }
    

    
    public static SparseVisibilityGraph repurpose(SparseVisibilityGraph oldGraph, int sx, int sy, int ex, int ey) {
//long start = System.nanoTime();
        oldGraph.removeStartAndEnd();
//long end = System.nanoTime();
//TimeCounter.timeA    += (end-start);
//start = System.nanoTime();

        SparseVisibilityGraph newGraph = new SparseVisibilityGraph(oldGraph.graph, sx, sy, ex, ey);
        newGraph.nodeIndex = oldGraph.nodeIndex;
        newGraph.startIndex = oldGraph.startIndex;
        newGraph.startIsNewNode = oldGraph.startIsNewNode;
        newGraph.endIndex = oldGraph.endIndex;
        newGraph.endIsNewNode = oldGraph.endIsNewNode;
        newGraph.nodes = oldGraph.nodes;
        newGraph.nNodes = oldGraph.nNodes;
        newGraph.maxSize = oldGraph.maxSize;
//end = System.nanoTime();
//TimeCounter.timeD += (end-start);
//start = System.nanoTime();
        newGraph.addStartAndEnd(sx, sy, ex, ey);

//end = System.nanoTime();
//TimeCounter.timeB += (end-start);
        return newGraph;
    }
    
    public static SparseVisibilityGraph getStoredGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
        SparseVisibilityGraph visibilityGraph = null;
        if (storedGridGraph != graph || storedVisibilityGraph == null) {
            //("Get new graph");
            visibilityGraph = new SparseVisibilityGraph(graph, sx, sy ,ex, ey);
            storedVisibilityGraph = visibilityGraph;
            storedGridGraph = graph;
        } else {
            //("Reuse graph");
            visibilityGraph = repurpose(storedVisibilityGraph, sx, sy, ex, ey);
            storedVisibilityGraph = visibilityGraph;
        }
        return visibilityGraph;
    }

    private void printAllEdges() {
        for (int i=0;i<nNodes;++i) {
            SVGNode node = nodes[i];
            System.out.println("Node " + i + " : " + node.nEdges + " edges");
            for (int j=0;j<node.nEdges;++j) {
                System.out.println(i + " -> " + node.outgoingEdges[j]);
            }
        }
    }
    
}

class SVGNode {
    int x;
    int y;

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
    int nEdges;
    int originalNEdges; // Number of edges before adding start/end node.
    
    public SVGNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.outgoingEdges = new int[11];
        this.edgeWeights = new float[11];
        this.nEdges = 0;
    }

    public void addEdge(int toIndex, float weight) {
        if (nEdges >= outgoingEdges.length) {
            outgoingEdges = Arrays.copyOf(outgoingEdges, outgoingEdges.length*2);
            edgeWeights = Arrays.copyOf(edgeWeights, edgeWeights.length*2);
        }
        outgoingEdges[nEdges] = toIndex;
        edgeWeights[nEdges] = weight;
        nEdges++;
    }
    
    public void lockOriginalNEdges() {
        originalNEdges = nEdges;
    }
    
    public void removeExtraEdges() {
        nEdges = originalNEdges;
    }

    /*public void removeEdgeTo(int target) {
        int i = 0;
        while (outgoingEdges[i] != target) {
            ++i;
            if (i >= nEdges) return; // edge does not exist.
        }
        
        nEdges--;
        while (i < nEdges) {
            outgoingEdges[i] = outgoingEdges[i+1];
            edgeWeights[i] = edgeWeights[i+1];
        }
    }*/
}
