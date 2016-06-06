package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;

public class EdgeNLevelSparseVisibilityGraph {

    public static int LEVEL_W = Integer.MAX_INT;

    private static EdgeNLevelSparseVisibilityGraph storedVisibilityGraph;
    private static GridGraph storedGridGraph;
    
    private final GridGraph graph;
    private LineOfSightScanner losScanner;
    private int[][] nodeIndex;
    private int startIndex = -1;
    private int endIndex = -1;
    
    private Runnable saveSnapshot;

    private int originalSize;
    private int maxSize;
    //private SVGNode[] nodes;
    private int nNodes;

    private int[] startStoredNeighbours;
    private float[] startStoredWeights;
    private int startStoredNumNeighbours;
    private int[] endStoredNeighbours;
    private float[] endStoredWeights;
    private int endStoredNumNeighbours;

    
    // Nodes: Indexed by node Index.
    public int[] xPositions;
    public int[] yPositions;
    public int[] nOutgoingEdgess;          // value: number of outgoingEdges.
    public int[][] outgoingEdgess;         // value: nodeIndex of destination
    public int[][] outgoingEdgeIndexess;   // value: edgeIndex of edge to destination.

    public int[] nSkipEdgess;                            // value: number of outgoing skip-edges.
    public int[][] outgoingSkipEdgess;                   // value: nodeIndex of destination
    public int[][] outgoingSkipEdgeNextNodess;           // value: nodeIndex of the next node in the actual path if the skip edge were to be followed.
    public int[][] outgoingSkipEdgeNextNodeEdgeIndexess; // value: edgeIndex of edge to next node in the actual path.
    public float[][] outgoingSkipEdgeWeightss;           // value: weight of edge to destination.
    
    
    // Edges: Indexed by edge Index (only for non-skip-edges)
    public int nEdges;
    public int[] edgeLevels;
    public float[] edgeWeights;
    public int[] isMarkedIndex;   // value: index to check in the isMarked array to see whether the edge is marked.
                                  //        note: for all non-level-W edges, isMarkedIndex[i] == i.

    // Edge Marking Groups: Indexed by isMarkedIndex.
    // we do thise so that all Level-W edges in a set can be marked together as one.
    public boolean[] isMarked;
    
    public EdgeNLevelSparseVisibilityGraph(GridGraph graph) {
        this.graph = graph;
    }

    public final void setSaveSnapshotFunction(Runnable saveSnapshot) {
        this.saveSnapshot = saveSnapshot;
    }

    public final void initialise(int sx, int sy, int ex, int ey) {
        // Check if graph already initialised
        if (nodes == null) {
            constructGraph();
        }

        restoreOriginalGraph();
        addStartAndEnd(sx,sy,ex,ey);
    }
    

    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///   GRAPH CONSTRUCTION PHASE - START  ///
    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///

    private final void constructGraph() {
        losScanner = new LineOfSightScanner(graph);
        

        // STEP 1: Construct SVG (Strict Visibility Graph)
        
        // Initialise SVG Vertices
        xPositions = new xPositions[11];
        yPositions = new yPositions[11];
        nNodes = 0;
        addNodes();
        
        // Now xPositions and yPositions should be correctly initialised.
        //  We then initialise the rest of the node data.
        originalSize = nNodes;
        maxSize = nNodes + 2;
        xPositions = Arrays.copyOf(xPositions, maxSize);
        yPositions = Arrays.copyOf(yPositions, maxSize);
        nOutgoingEdgess = new int[maxSize][];
        outgoingEdgess = new int[maxSize][];
        outgoingEdgeIndexess = new int[maxSize][];
        for (int i=0;i<originalSize;++i) {
            nEdges[i] = 0;
            outgoingEdgess[i] = new int[11];
            outgoingEdgeIndexess = new int[11];
        }
        

        // Initialise SVG Edges + edgeWeights
        edgeWeights = new float[11];
        nEdges = 0;
        addAllEdges();

        // Now all the edges, indexes and weights should be correctly initialise.
        //  Now we initialise the rest of the edge data.
        edgeWeights = Arrays.copyOf(edgeWeights, nEdges);
        edgeLevels = new int[nEdges];
        Arrays.fill(edgeLevels, LEVEL_W);
        isMarkedIndex = new int[nEdges];
        for (int i=0;i<isMarkedIndex.length;++i) isMarkedIndex[i] = i;
        isMarked = new boolean[nEdges];
        //Arrays.fill(isMarked, false); // default initialises to false anyway.

        // STEP 2: Label edge levels in SVG.
        computeAllEdgeLevels();


        // STEP 3: Initialise the skip-edges & Group together Level-W edges using isMarkedIndex.
        setupSkipEdges();

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
        if (index >= xPositions.length) {
            xPositions = Arrays.copyOf(xPositions, xPositions.length*2);
            yPositions = Arrays.copyOf(yPositions, yPositions.length*2);
        }
        xPositions[index] = x;
        yPositions[index] = y;

        nNodes++;
        return index;
    }


    private final void addAllEdges() {
        int fromX, fromY, toX, toY;
        for (int i=0;i<nNodes;++i) {
            fromX = xPositions[i];
            fromY = yPositions[i];
            
            losScanner.computeAllVisibleTwoWayTautSuccessors(fromX, fromY);
            int nSuccessors = losScanner.nSuccessors;
            for (int succ=0;succ<nSuccessors;++succ) {
                toX = losScanner.successorsX[succ];
                toY = losScanner.successorsY[succ];
                int j = nodeIndex[toY][toX];

                // We add both ways at the same time. So we use this to avoid duplicates
                if (i >= j) continue;

                float weight = graph.distance(fromX, fromY, toX, toY);
                addEdge(i, j, weight);
            }
        }
    }
    
    // Adds an edge from node v1 to node v2, and from node v2 to node v1
    private final void addEdge(int v1, int v2, float weight) {
        if (nEdges >= edgeWeights.length) {
            edgeWeights = Arrays.copyOf(edgeWeights, edgeWeights.length*2);
        }
        int edgeIndex = nEdges;

        // add edge to v1
        {
            int index = nOutgoingEdgess[v1];
            if (index >= outgoingEdgess[v1].length) {
                outgoingEdgess[v1] = Arrays.copyOf(outgoingEdgess[v1], outgoingEdgess[v1].length*2);
                outgoingEdgeIndexess[v1] = Arrays.copyOf(outgoingEdgeIndexess[v1], outgoingEdgeIndexess[v1].length*2);
            }
            outgoingEdges[v1][index] = v2;
            outgoingEdgeIndexess[v1][index] = edgeIndex;
        }

        // add edge to v2
        {
            int index = nOutgoingEdgess[v2];
            if (index >= outgoingEdgess[v2].length) {
                outgoingEdgess[v2] = Arrays.copyOf(outgoingEdgess[v2], outgoingEdgess[v2].length*2);
                outgoingEdgeIndexess[v2] = Arrays.copyOf(outgoingEdgeIndexess[v2], outgoingEdgeIndexess[v2].length*2);
            }
            outgoingEdges[v2][index] = v1;
            outgoingEdgeIndexess[v2][index] = edgeIndex;
        }

        edgeWeights[nEdges] = weight;
        ++nEdges;
    }


    private final void computeAllEdgeLevels() {
        boolean hasChanged = false;
        int currentLevel = 0;

        // Runs in time linesr on the number of edges per iteration.
        // The number of iterations will be the max level of any edge.
        while(hasChanged) {
            currentLevel++;
            hasChanged = false;

            // Note: we ignore all edges of level < currentLevel.
            for (int v1=0;v1<nNodes;++v1) {
                int nOutgoingEdges = nOutgoingEdgess[v1];
                int[] outgoingEdges = outgoingEdgess[v1];
                int[] outgoingEdgeIndexes = outgoingEdgeIndexess[v1];

                for (int j=0;j<nOutgoingEdges;++j) {
                    int edgeIndex = outgoingEdgeIndexes[j];
                    if (edgeLevels[edgeIndex] < currentLevel) continue;

                    int v2 = outgoingEdges[j];
                    
                    // Avoid repeated work.
                    if (v1 >= v2) continue;

                    // This essentially is a loop through all unmarked edges (v1,v2)

                    // Note: not be pruned, the edge must have a taut exit on BOTH ends.
                    if (!hasTautExit(v1, v2, currentLevel) || !!hasTautExit(v2, v1, currentLevel)) {
                        edgeLevels[edgeIndex] = currentLevel;
                        hasChanged = true;
                    }
                }
            }
        }

        // maxLevel = currentLevel;
    }

    // Checks whether there is a taut exist in the graph, considering only unmarked edges.
    // Note: unmarked edges are edges whose level >= currentLevel.
    private final void hasTautExit(int vFrom, int vTo, int currentLevel) {
        int x1 = xPositions[vFrom];
        int y1 = yPositions[vFrom];
        int x2 = xPositions[vTo];
        int y2 = yPositions[vTo];

        int nOutgoingEdges = nOutgoingEdgess[vTo];
        int[] outgoingEdges = outgoingEdgess[vTo];
        int[] outgoingEdgeIndexes = outgoingEdgeIndexess[vTo];
        for (int j=0;j<nOutgoingEdges;++j) {
            if (edgeLevels[outgoingEdgeIndexes[j]] < currentLevel) continue;
            int v3 = outgoingEdges[j];
            int x3 = xPositions[v3];
            int y3 = yPositions[v3];
            if (graph.isTaut(x1,y1,x2,y2,x3,y3)) return true;
        }
        return false;
    }

    // We start from the graph of all Level-W edges.
    // We setup skip edges to reduce the graph to one where every vertex has degree >2.
    // Note: we can prove that the set of Level-W edges has no edge of degree 1.
    private final void setupSkipEdges() {
        markSkipVertices();
        connectSkipEdgesAndGroupLevelWEdges();
    }

    /**
     * For each skip vertex, we initialise outgoingSkipEdgeNextNodes and nSkipEdges for all skip vertices.
     * Thus, we have:
     * nSkipEdgess[v] == 0 iff v is not a skip vertex.
     * if v is a skip vertex, then nSkipEdgess[v] >= 3
     */
    private final void markSkipVertices() {
        for (int i=0;i<nNodes;++i) {
            int nOutgoingEdges = nOutgoingEdgess[i];
            int[] outgoingEdgeIndexes = outgoingEdgeIndexess[i];

            int nLevelWNeighbours = 0;
            for (int j=0;j<nOutgoingEdges;++j) {
                int edgeIndex = outgoingEdgeIndexes[j];
                if (edgeLevels[edgeIndex] == LEVEL_W) ++nLevelWNeighbours;
            }

            // Skip vertices must have at least 3 level-W neighbours.
            if (nLevelWNeighbours <= 2) continue;

            int[] outgoingEdges = outgoingEdgess[i];

            // Else, i is a skip-vertex.
            nSkipEdgess[i] = nLevelWNeighbours;
            int[] nextNodes = new int[nLevelWNeighbours];
            int[] edgeIndexes = new int[nLevelWNeighbours];
            float[] weights = new float[nLevelWNeighbours];
            int destinations = new int[nLevelWNeighbours];
            int current = 0;
            for (int j=0;j<nOutgoingEdges;++j) {
                int edgeIndex = outgoingEdgeIndexes[j];
                if (edgeLevels[edgeIndex] == LEVEL_W) {
                    nextNodes[current] = outgoingEdges[j];
                    edgeIndexes[current] = edgeIndex; 
                    current++;
                }
            }

            outgoingSkipEdgess = destinations;
            outgoingSkipEdgeNextNodess[i] = nextNodes;
            outgoingSkipEdgeNextNodeEdgeIndexess[i] = edgeIndexes;
            outgoingSkipEdgeWeightss[i] = weights;
        }
    }

    /**
     * This does two things:
     * 1. Connects the previously marked skip vertices to form a graph of skip-edges.
     * 2. set up isMarkedIndex groups for each of the sets of Level-W edges.
     */
    void connectSkipEdgesAndGroupLevelWEdges() {
        for (int v1=0;v1<nNodes;++v1) {
            int nSkipEdges = nSkipEdgess[v1];
            // Loop through only skip vertices.
            if (nSkipEdges == 0) continue;

            int[] nextNodes = outgoingSkipEdgeNextNodess[v1];
            int[] nextNodeEdgeIndexes = outgoingSkipEdgeNextNodeEdgeIndexess[v1];
            int[] outgoingSkipEdges = outgoingSkipEdgess[v1];
            float[] skipWeights = outgoingSkipEdgeWeightss[v1];
            for (int j=0;j<nSkipEdges;++j) {
                // Start from vertex v1, move in direction j.
                int previous = v1;
                int current = nextNodes[j];
                int firstEdgeIndex = nextNodeEdgeIndexes[j];
                skipWeights[j] = edgeWeights[firstEdgeIndex];
                int indexGroup = isMarkedIndex[firstEdgeIndex];

                // invariants:
                // 1. outgoingSkipEdges[j] == current.
                // 2. skipWeights[j] == path length from v1 up to current.
                // initially, skipWeights[j] = weight(v1, nextNodes[j])

                while (nSkipEdgess[current] == 0) {
                    // While current is still not yet a skip vertex,
                    // Else continue expanding.
                    int nOutgoingEdges = nOutgoingEdgess[current];
                    int[] outgoingEdges = outgoingEdgess[current];
                    int[] outgoingEdgeIndexes = outgoingEdgeIndexess[current];

                    boolean DEBUG_ASSERT_WILL_RUN = false; // TODO: DEBUG CODE
                    for (int k=0;k<nOutgoingEdges;++k) {
                        int edgeIndex = outgoingEdgeIndexes[k];
                        if (edgeLevels[edgeIndex] != LEVEL_W) continue;
                        int next = outgoingEdges[k];
                        if (next == previous) continue;

                        // now next == the next node in the list.
                        // ASSERT: THIS MUST RUN.
                        DEBUG_ASSERT_WILL_RUN = true; // TODO: DEBUG CODE
                        previous = current;
                        current = next;
                        nextNodes[j] = current;
                        skipWeights[j] += edgeWeights[edgeIndex];
                        isMarkedIndex[edgeIndex] = indexGroup;
                        break;
                    }
                    assert DEBUG_ASSERT_WILL_RUN; // TODO: DEBUG CODE
                }
                // now all the edges along that subpath will be of the same index group.
            }
        }
    }

    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///    GRAPH CONSTRUCTION PHASE - END   ///
    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///


    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///  REPURPOSING PHASE - START  ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    
    private final void restoreOriginalGraph() {
        // Safeguard against multiple restores.
        if (startIndex == -1) return;
        
        // Reset hasEdgeToGoal array.
        markHasEdgeToGoal(false);
        // PostCondition: hasEdgeToGoal has all values == false.
        if (endIndex >= originalSize) {
            SVGNode e = nodes[endIndex];
            nodeIndex[e.y][e.x] = -1;
        } else {
            SVGNode endNode = nodes[endIndex];
            endNode.outgoingEdges = endStoredNeighbours;
            endNode.edgeWeights = endStoredWeights;
            endNode.nEdges = endStoredNumNeighbours;
            endStoredNeighbours = null;
            endStoredWeights = null;
        }
        
        if (startIndex >= originalSize) {
            SVGNode s = nodes[startIndex];
            nodeIndex[s.y][s.x] = -1;
        } else {
            SVGNode startNode = nodes[startIndex];
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
            nodes[startIndex] = new SVGNode(sx, sy);
        } else {
            startIndex = nodeIndex[sy][sx];
            SVGNode startNode = nodes[startIndex];
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
            nodes[endIndex] = new SVGNode(ex, ey);
            
        } else {
            endIndex = nodeIndex[ey][ex];
            SVGNode endNode = nodes[endIndex];
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
        losScanner.computeAllVisibleTautSuccessors(x, y);
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

    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///   REPURPOSING PHASE - END   ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    
    public final int size() {
        return nNodes;
    }
    
    public final int maxSize() {
        return maxSize;
    }
    
    public final SVGNode getOutgoingEdges(int source) {
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
    
    
    private static final EdgeNLevelSparseVisibilityGraph repurpose(EdgeNLevelSparseVisibilityGraph oldGraph) {
        EdgeNLevelSparseVisibilityGraph newGraph = new EdgeNLevelSparseVisibilityGraph(oldGraph.graph);
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
    
    public static final EdgeNLevelSparseVisibilityGraph getStoredGraph(GridGraph graph) {
        EdgeNLevelSparseVisibilityGraph visibilityGraph = null;
        if (storedGridGraph != graph || storedVisibilityGraph == null) {
            //("Get new graph");
            visibilityGraph = new EdgeNLevelSparseVisibilityGraph(graph);
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