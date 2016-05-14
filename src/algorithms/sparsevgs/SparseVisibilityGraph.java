package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;
import java.util.HashSet;

public class SparseVisibilityGraph {

    private static SparseVisibilityGraph storedVisibilityGraph;
    private static GridGraph storedGridGraph;
    
    protected final GridGraph graph;
    protected LineOfSightScanner losScanner;
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

        losScanner = new LineOfSightScanner(graph);
        
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

        losScanner.computeAllVisibleTautSuccessors(x, y);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int toX = losScanner.successorsX[i];
            int toY = losScanner.successorsY[i];
            int targetIndex = nodeIndex[toY][toX];

            float weight = computeWeight(x, y, toX, toY);
            addEdge(targetIndex, index, weight);
            addEdge(index, targetIndex, weight);
        }
        
        /*// Disabled: Old method: LOS scan to all possible taut vertices.
          // Note: Remember to comment out "tryConnectStartAndEnd" if you want to use this scheme.
        for (int i=0; i<nNodes; i++) {
            int toX = xCoordinateOf(i);
            int toY = yCoordinateOf(i);
            if (shouldAddEdge(x, y, toX, toY)) {
                float weight = computeWeight(x, y, toX, toY);
                addEdge(i, index, weight);
                addEdge(index, i, weight);
            }
        }*/

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
        int[] outgoingEdges = nodes[index].outgoingEdges;
        int existingNEdges = nodes[index].nEdges;
        
        // I don't think this hashset will ever be a bottleneck, but if somehow it does,
        // this can be replaced with an index table check to check for duplicate edges.
        HashSet<Integer> existingEdges = new HashSet<>();
        for (int i=0;i<existingNEdges;++i) {
            existingEdges.add(outgoingEdges[i]);
        }

        losScanner.computeAllVisibleTautSuccessors(x, y);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int toX = losScanner.successorsX[i];
            int toY = losScanner.successorsY[i];
            int targetIndex = nodeIndex[toY][toX];
            
            if (existingEdges.contains(targetIndex)) continue;

            float weight = computeWeight(x, y, toX, toY);
            addEdge(targetIndex, index, weight);
            addEdge(index, targetIndex, weight);
        }
        
        
        /* // Disabled: Old method: LOS scan to all possible taut vertices.
           // Note: Remember to comment out "tryConnectStartAndEnd" if you want to use this scheme.
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
        }*/
    }
    
    /**
     * Special case that is missed by the LOS scanner method.
     * If this happens, might as well end the algorithm prematurely.
     * But whatever. It won't take much time anyway.
     * 
     * Note: As this does not check whether the edge already exists, this could
     *       possibly cause a repeated edge in the graph. This can happen when the
     *       when either the start or end is placed at an outer corner (an existing
     *       visibility graph vertex), causing the LOS scans to detect it.
     */
    protected void tryConnectStartAndEnd() {
        if (graph.lineOfSight(sx, sy, ex, ey)) {
            float weight = computeWeight(sx,sy,ex,ey);
            addEdge(startIndex, endIndex, weight);
            addEdge(endIndex, startIndex, weight);
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
        
        // The only case missed out from the LOS scans.
        tryConnectStartAndEnd();
    }
    
    protected void addAllEdges() {
        int saveFactor = nNodes/10;
        if (saveFactor == 0) saveFactor = 1;
        
        int fromX, fromY, toX, toY;
        for (int i=0;i<nNodes;++i) {
            fromX = xCoordinateOf(i);
            fromY = yCoordinateOf(i);
            
            losScanner.computeAllVisibleTwoWayTautSuccessors(fromX, fromY);
            int nSuccessors = losScanner.nSuccessors;
            for (int j=0;j<nSuccessors;++j) {
                toX = losScanner.successorsX[j];
                toY = losScanner.successorsY[j];
                int toIndex = nodeIndex[toY][toX];

                float weight = computeWeight(fromX, fromY, toX, toY);
                addEdge(i, toIndex, weight);
                //addEdge(toIndex, i, weight);
            }
        }
        
        
        /*// Disabled: VG construction by all-pairs LOS checks.
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
        }*/
        
        
        for (int i=0; i<nNodes; ++i) {
            nodes[i].lockOriginalNEdges();
        }
    }
    
    /**
     * Deprecated. Was used for the all-pairs LOS checks. Now the tautness
     * checks are implicitly covered in the LineOfSightScanner scans.
     */
    @Deprecated
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
    
    
    protected final int indexOf(int x, int y) {
        return nodeIndex[y][x];
    }
    
    protected final boolean isNode(int x, int y) {
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

    public final SVGNode getOutgoingEdges(int source) {
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

    public final int startNode() {
        return startIndex;
    }

    public final int endNode() {
        return endIndex;
    }
    
    private final void maybeSaveSnapshot() {
        if (saveSnapshot != null)
            saveSnapshot.run();
    }
    

    
    public static final SparseVisibilityGraph repurpose(SparseVisibilityGraph oldGraph, int sx, int sy, int ex, int ey) {
        oldGraph.removeStartAndEnd();
        
        SparseVisibilityGraph newGraph = new SparseVisibilityGraph(oldGraph.graph, sx, sy, ex, ey);
        newGraph.nodeIndex = oldGraph.nodeIndex;
        newGraph.startIndex = oldGraph.startIndex;
        newGraph.startIsNewNode = oldGraph.startIsNewNode;
        newGraph.endIndex = oldGraph.endIndex;
        newGraph.endIsNewNode = oldGraph.endIsNewNode;
        newGraph.nodes = oldGraph.nodes;
        newGraph.nNodes = oldGraph.nNodes;
        newGraph.maxSize = oldGraph.maxSize;
        newGraph.losScanner = oldGraph.losScanner;
        
        newGraph.addStartAndEnd(sx, sy, ex, ey);

        return newGraph;
    }
    
    public static final SparseVisibilityGraph getStoredGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
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

    public static void clearMemory() {
        storedVisibilityGraph = null;
        storedGridGraph = null;
        System.gc();
    }
    
}

final class SVGNode {
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
    int nEdges;
    int originalNEdges; // Number of edges before adding start/end node.
    
    public SVGNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.outgoingEdges = new int[11];
        this.edgeWeights = new float[11];
        this.nEdges = 0;
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
    
    public final void lockOriginalNEdges() {
        originalNEdges = nEdges;
    }
    
    public final void removeExtraEdges() {
        nEdges = originalNEdges;
    }
}
