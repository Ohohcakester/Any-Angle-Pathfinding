package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.Arrays;
import algorithms.datatypes.Memory;

public class DirectedEdgeNLevelSparseVisibilityGraph {

    public static int LEVEL_W = Integer.MAX_VALUE;

    private static DirectedEdgeNLevelSparseVisibilityGraph storedVisibilityGraph;
    private static GridGraph storedGridGraph;
    
    private final GridGraph graph;
    private LineOfSightScanner losScanner;
    public int[][] nodeIndex;
    private int startIndex = -1;
    private int endIndex = -1;
    
    private Runnable saveSnapshot;

    private int originalSize;
    private int maxSize;
    private int nNodes;

    private int[] queue;
    private int queueSize;

    // || Nodes: Indexed by node Index. ||
    public int[] xPositions;
    public int[] yPositions;
    public int[] nOutgoingEdgess;          // value: number of outgoingEdges.
    public int[][] outgoingEdgess;         // value: nodeIndex of destination
    public int[][] outgoingEdgeIndexess;   // value: edgeIndex of edge to destination.
    public int[][] outgoingEdgeOppositeIndexess;   // value: index within outgoingEdgess of the opposite edge.
    public boolean[] hasEdgeToGoal;
    
    // Used to iterate through the Level-W edges quickly
    public int[] nLevelWNeighbourss;
    public int[][] levelWEdgeOutgoingIndexess;    // value: index of edge in outgoingEdges array.
    public boolean[][] levelWEdgeIsMarkedss;
    
    // Used to iterate through the marked edges quickly
    public int[] nMarkedEdgess;
    public int[][] outgoingMarkedEdgeIndexess;   // value: index of edge in outgoingEdges array.
    
    // Used to iterate through the directed edges quickly
    public int[] nDirectedNeighbourss;
    public int[][] directedNeighbourOutgoingIndexess;

    // Skip Edges
    public int[] nSkipEdgess;                            // value: number of outgoing skip-edges.
    public int[][] outgoingSkipEdgess;                   // value: nodeIndex of destination
    public int[][] outgoingSkipEdgeNextNodess;           // value: nodeIndex of the next node in the actual path if the skip edge were to be followed.
    public int[][] outgoingSkipEdgeNextNodeEdgeIndexess; // value: edgeIndex of edge to next node in the actual path.
    public float[][] outgoingSkipEdgeWeightss;           // value: weight of edge to destination.
    
    // Vertices connected directly to the end. For easy unmarking during cleanup.
    public int nVerticesConnectedToEnd;
    public int[] verticesConnectedToEnd;
    
    // || Edges: Indexed by edge Index (only for non-skip-edges) ||
    public int nEdges;
    public float[] edgeWeights;
    public boolean[] isMarked;
    
    
    private DirectedEdgeNLevelSparseVisibilityGraph(GridGraph graph) {
        this.graph = graph;
    }

    public final void setSaveSnapshotFunction(Runnable saveSnapshot) {
        this.saveSnapshot = saveSnapshot;
    }

    public static final DirectedEdgeNLevelSparseVisibilityGraph initialiseNew(GridGraph graph) {
        if (DirectedEdgeNLevelSparseVisibilityGraph.storedGridGraph == graph) {
            storedVisibilityGraph.restoreOriginalGraph();
            return storedVisibilityGraph;
        }
        long _st = System.nanoTime();
        
        DirectedEdgeNLevelSparseVisibilityGraph.storedGridGraph = graph;
        DirectedEdgeNLevelSparseVisibilityGraph vGraph = DirectedEdgeNLevelSparseVisibilityGraph.storedVisibilityGraph = new DirectedEdgeNLevelSparseVisibilityGraph(graph);
        vGraph.constructGraph();
        
        long _ed = System.nanoTime();
        System.out.println("Construction Time: " + (_ed-_st)/1000000.);
        return vGraph;
    }
    

    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///   GRAPH CONSTRUCTION PHASE - START  ///
    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///

    private final void constructGraph() {
        losScanner = new LineOfSightScanner(graph);
        queue = new int[11];

        queueSize = 0;

        // STEP 1: Construct SVG (Strict Visibility Graph)
        
        // Initialise SVG Vertices
        xPositions = new int[11];
        yPositions = new int[11];
        nNodes = 0;
        addNodes();
        
        // Now xPositions and yPositions should be correctly initialised.
        //  We then initialise the rest of the node data.
        originalSize = nNodes;
        maxSize = nNodes + 2;
        xPositions = Arrays.copyOf(xPositions, maxSize);
        yPositions = Arrays.copyOf(yPositions, maxSize);
        hasEdgeToGoal = new boolean[maxSize];
        nOutgoingEdgess = new int[maxSize];
        outgoingEdgess = new int[maxSize][];
        outgoingEdgeIndexess = new int[maxSize][];
        outgoingEdgeOppositeIndexess = new int[maxSize][];
        for (int i=0;i<nNodes;++i) {
            nOutgoingEdgess[i] = 0;
            outgoingEdgess[i] = new int[11];
            outgoingEdgeIndexess[i] = new int[11];
            outgoingEdgeOppositeIndexess[i] = new int[11];
        }

        // Initialise SVG Edges + edgeWeights
        edgeWeights = new float[11];
        nEdges = 0;
        addAllEdges();

        
        // Now all the edges, indexes and weights should be correctly initialise.
        //  Now we initialise the rest of the edge data.
        int maxPossibleNEdges = nEdges + nNodes*2;
        edgeWeights = Arrays.copyOf(edgeWeights, maxPossibleNEdges);
        isMarked = new boolean[maxPossibleNEdges];
        //Arrays.fill(isMarked, false); // default initialises to false anyway.
        
        
        // Reserve space in level w edge and marked edge arrays.
        nLevelWNeighbourss = new int[maxSize];
        levelWEdgeOutgoingIndexess = new int[maxSize][];
        levelWEdgeIsMarkedss = new boolean[maxSize][];
        nMarkedEdgess = new int[maxSize];
        outgoingMarkedEdgeIndexess = new int[maxSize][];
        nDirectedNeighbourss = new int[maxSize];
        directedNeighbourOutgoingIndexess = new int[maxSize][];
        for (int i=0;i<nNodes;++i) {
            levelWEdgeOutgoingIndexess[i] = new int[nOutgoingEdgess[i]];
            directedNeighbourOutgoingIndexess[i] = new int[nOutgoingEdgess[i]];
            outgoingMarkedEdgeIndexess[i] = new int[nOutgoingEdgess[i]+1];
        }
        for (int i=nNodes;i<maxSize;++i) {
            levelWEdgeOutgoingIndexess[i] = new int[11];
            directedNeighbourOutgoingIndexess[i] = new int[11];
            //outgoingMarkedEdgeIndexess[i] = new int[11]; // keep it null
        }

        
        // STEP 2: Label edge cardinal directions and mark Level-W edges.
        computeEdgeCardinalDirections();

        for (int i=0;i<nNodes;++i) {
            levelWEdgeOutgoingIndexess[i] = Arrays.copyOf(levelWEdgeOutgoingIndexess[i], nLevelWNeighbourss[i]);
            levelWEdgeIsMarkedss[i] = new boolean[nLevelWNeighbourss[i]];
            directedNeighbourOutgoingIndexess[i] = Arrays.copyOf(directedNeighbourOutgoingIndexess[i], nDirectedNeighbourss[i]);
        }
        

        nSkipEdgess = new int[maxSize];
        outgoingSkipEdgess = new int[maxSize][];
        outgoingSkipEdgeNextNodess = new int[maxSize][];
        outgoingSkipEdgeNextNodeEdgeIndexess = new int[maxSize][];
        outgoingSkipEdgeWeightss = new float[maxSize][]; 

        // STEP 3: Initialise the skip-edges & Group together Level-W edges using isMarkedIndex.
        setupSkipEdges();
        
        pruneParallelSkipEdges();
        

        nVerticesConnectedToEnd = 0;
        verticesConnectedToEnd = new int[11];
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
        int v1Index;
        {
            v1Index = nOutgoingEdgess[v1];
            if (v1Index >= outgoingEdgess[v1].length) {
                int newLength = outgoingEdgess[v1].length*2;
                outgoingEdgess[v1] = Arrays.copyOf(outgoingEdgess[v1], newLength);
                outgoingEdgeIndexess[v1] = Arrays.copyOf(outgoingEdgeIndexess[v1], newLength);
                outgoingEdgeOppositeIndexess[v1] = Arrays.copyOf(outgoingEdgeOppositeIndexess[v1], newLength);
            }
            outgoingEdgess[v1][v1Index] = v2;
            outgoingEdgeIndexess[v1][v1Index] = edgeIndex;
            nOutgoingEdgess[v1]++;
        }

        // add edge to v2
        int v2Index;
        {
            v2Index = nOutgoingEdgess[v2];
            if (v2Index >= outgoingEdgess[v2].length) {
                int newLength = outgoingEdgess[v2].length*2;
                outgoingEdgess[v2] = Arrays.copyOf(outgoingEdgess[v2], newLength);
                outgoingEdgeIndexess[v2] = Arrays.copyOf(outgoingEdgeIndexess[v2], newLength);
                outgoingEdgeOppositeIndexess[v2] = Arrays.copyOf(outgoingEdgeOppositeIndexess[v2], newLength);
            }
            outgoingEdgess[v2][v2Index] = v1;
            outgoingEdgeIndexess[v2][v2Index] = edgeIndex;
            nOutgoingEdgess[v2]++;
        }

        outgoingEdgeOppositeIndexess[v1][v1Index] = v2Index;
        outgoingEdgeOppositeIndexess[v2][v2Index] = v1Index;
        
        edgeWeights[nEdges] = weight;
        ++nEdges;
    }
    
    
    /**
     * Does two things:
     * 1. Computes all edge cardinal directions
     * 2. Labels all level-W edges as such.
     */
    private final void computeEdgeCardinalDirections() {
        // TODO: This can be done much faster by building a dependency graph.
        //       i.e. O(m*d), m = no. edges, d = max degree
        
        boolean hasChanged = true;
        
        // Array containing all the edges to be considered.
        int[] sourceVertices = new int[nEdges];
        int[] outgoingIndexes = new int[nEdges];
        int keepSize = nEdges;
        int currSize = 0;
        
        // First fill up the array with all the edges.
        for (int v1=0;v1<nNodes;++v1) {
            int nOutgoingEdges = nOutgoingEdgess[v1];
            int[] outgoingEdges = outgoingEdgess[v1];

            for (int j=0;j<nOutgoingEdges;++j) {
                int v2 = outgoingEdges[j];
                
                // Avoid repeated work.
                if (v1 >= v2) continue;

                // This essentially is a loop through all unmarked edges (v1,v2)
                sourceVertices[currSize] = v1;
                outgoingIndexes[currSize] = j;
                ++currSize;
            }
        }
        

        // We mark edges (by edgeIndex) to show that they have been pruned.
        Arrays.fill(isMarked, false);

        // Runs in time linear on the number of edges per iteration.
        // The number of iterations will be the max level of any edge.
        while (hasChanged) {
            hasChanged = false;
            
            for (int i=0;i<keepSize;++i) {
                int v1 = sourceVertices[i];
                int v1OutgoingIndex = outgoingIndexes[i];
                int v2 = outgoingEdgess[v1][v1OutgoingIndex];
                int v2OutgoingIndex = outgoingEdgeOppositeIndexess[v1][v1OutgoingIndex];

                boolean v1HasTautExit = hasTautExit(v2, v1);
                boolean v2HasTautExit = hasTautExit(v1, v2);

                if (!v1HasTautExit) {
                    addToDirected(v1, v1OutgoingIndex);
                }
                
                if (!v2HasTautExit) {
                    addToDirected(v2, v2OutgoingIndex);
                }
                
                if (!v1HasTautExit || !v2HasTautExit) {
                    hasChanged = true;
                    
                    --keepSize;
                    int last = keepSize;
                    
                    int temp = sourceVertices[i];
                    sourceVertices[i] = sourceVertices[last];
                    sourceVertices[last] = temp;
                    
                    temp = outgoingIndexes[i];
                    outgoingIndexes[i] = outgoingIndexes[last];
                    outgoingIndexes[last] = temp;
                    
                    --i;
                }
            }

            for (int i=keepSize;i<currSize;++i) {
                int edgeIndex = outgoingEdgeIndexess[sourceVertices[i]][outgoingIndexes[i]];
                isMarked[edgeIndex] = true;
            }
            
            currSize = keepSize;
        }
        
        Arrays.fill(isMarked, false);
        
        
        // The remaining edges are level-W edges.
        for (int i=0;i<keepSize;++i) {
            int v1 = sourceVertices[i];
            int v1OutgoingIndex = outgoingIndexes[i];
            int v2 = outgoingEdgess[v1][v1OutgoingIndex];
            int v2OutgoingIndex = outgoingEdgeOppositeIndexess[v1][v1OutgoingIndex];
            
            levelWEdgeOutgoingIndexess[v1][nLevelWNeighbourss[v1]] = v1OutgoingIndex;
            ++nLevelWNeighbourss[v1];
            
            levelWEdgeOutgoingIndexess[v2][nLevelWNeighbourss[v2]] = v2OutgoingIndex;
            ++nLevelWNeighbourss[v2];
        }
    }

    private final void addToDirected(int v1, int outgoingIndex) {
        directedNeighbourOutgoingIndexess[v1][nDirectedNeighbourss[v1]] = outgoingIndex;
        nDirectedNeighbourss[v1]++;
    }

    // Checks whether there is a taut exist in the graph, considering only unmarked edges.
    // Note: unmarked edges are edges whose level >= currentLevel.
    private final boolean hasTautExit(int vFrom, int vTo) {
        int x1 = xPositions[vFrom];
        int y1 = yPositions[vFrom];
        int x2 = xPositions[vTo];
        int y2 = yPositions[vTo];

        int nOutgoingEdges = nOutgoingEdgess[vTo];
        int[] outgoingEdges = outgoingEdgess[vTo];
        int[] outgoingEdgeIndexes = outgoingEdgeIndexess[vTo];
        for (int j=0;j<nOutgoingEdges;++j) {
            if (isMarked[outgoingEdgeIndexes[j]]) continue;
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
        int nSkipVertices = markSkipVertices();
        if (nSkipVertices > 0) {
            connectSkipEdgesAndGroupLevelWEdges();
        }
    }

    /**
     * For each skip vertex, we initialise outgoingSkipEdgeNextNodes and nSkipEdges for all skip vertices.
     * Thus, we have:
     * nSkipEdgess[v] == 0 iff v is not a skip vertex.
     * if v is a skip vertex, then nSkipEdgess[v] >= 3
     */
    private final int markSkipVertices() {
        int nSkipVertices = 0;
        
        for (int i=0;i<nNodes;++i) {
            int nLevelWNeighbours = nLevelWNeighbourss[i];
            
            // Skip vertices must have at most 1 or at least 3 level-W neighbours.
            if (nLevelWNeighbours == 0 || nLevelWNeighbours == 2) continue;

            // Else, i is a skip-vertex.
            nSkipEdgess[i] = nLevelWNeighbours;
            int[] nextNodes = new int[nLevelWNeighbours];
            int[] edgeIndexes = new int[nLevelWNeighbours];
            float[] weights = new float[nLevelWNeighbours];
            int[] destinations = new int[nLevelWNeighbours];

            int[] outgoingEdges = outgoingEdgess[i];
            int[] outgoingEdgeIndexes = outgoingEdgeIndexess[i];
            int[] levelWEdgeOutgoingIndexes = levelWEdgeOutgoingIndexess[i];
            
            for (int j=0;j<nLevelWNeighbours;++j) {
                int index = levelWEdgeOutgoingIndexes[j];
                nextNodes[j] = outgoingEdges[index];
                edgeIndexes[j] = outgoingEdgeIndexes[index];
            }

            outgoingSkipEdgess[i] = destinations;
            outgoingSkipEdgeNextNodess[i] = nextNodes;
            outgoingSkipEdgeNextNodeEdgeIndexess[i] = edgeIndexes;
            outgoingSkipEdgeWeightss[i] = weights;
            
            nSkipVertices++;
        }
        return nSkipVertices;
    }

    /**
     * Connects the previously marked skip vertices to form a graph of skip-edges.
     */
    private final void connectSkipEdgesAndGroupLevelWEdges() {
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
                outgoingSkipEdges[j] = current;
                skipWeights[j] = edgeWeights[firstEdgeIndex];

                // invariants:
                // 1. outgoingSkipEdges[j] == current.
                // 2. skipWeights[j] == path length from v1 up to current.
                // initially, skipWeights[j] = weight(v1, nextNodes[j])

                while (nSkipEdgess[current] == 0) {
                    int nLevelWNeighbours = nLevelWNeighbourss[current];
                    int[] levelWEdgeOutgoingIndexes = levelWEdgeOutgoingIndexess[current];
                    
                    // While current is still not yet a skip vertex,
                    // Else continue expanding.
                    int[] outgoingEdges = outgoingEdgess[current];

                    for (int k=0;k<nLevelWNeighbours; ++k) {
                        int index = levelWEdgeOutgoingIndexes[k];
                        int next = outgoingEdges[index];
                        if (next == previous) continue;
                        
                        int edgeIndex = outgoingEdgeIndexess[current][index];

                        // now next == the next node in the list.
                        previous = current;
                        current = next;

                        outgoingSkipEdges[j] = current;
                        skipWeights[j] += edgeWeights[edgeIndex];
                        break;
                    }
                }
                // now all the edges along that subpath will be of the same index group.
            }
        }
    }
    
    /**
     * OPTIMISATION: Parallel Skip-Edge Reduction
     *  Idea: Fix two vertices u and v. Suppose there exists two parallel skip edges e1, e2, between u and v.
     *  Let length of e1 <= length of e2 WLOG.
     *  Then we can prune e2 from the skip graph.
     *   '-> Note: this also prevents some bookkeeping bugs in the original version.
     *   
     * Uses Memory.
     */
    private final void pruneParallelSkipEdges() {
        // TODO: IF THERE ARE MULTIPLE EDGES WITH THE SAME EDGE WEIGHT, WE ARBITRARILY PICK THE FIRST EDGE TO KEEP
        //       THE ORDERING MAY BE DIFFERENT FROM BOTH SIDES OF THE EDGE, WHICH CAN LEAD TO A NONSYMMETRIC GRAPH
        //       However, no issues have cropped up yet. Perhaps the ordering happens to be the same for both sides,
        //         due to how the graph is constructed. This is not good to rely upon, however.
        Memory.initialise(maxSize, Float.POSITIVE_INFINITY, -1, false);
        
        int maxDegree = 0;
        for (int i=0;i<nNodes;++i) {
            maxDegree = Math.max(maxDegree, nSkipEdgess[i]);
        }
        
        int[] neighbourIndexes = new int[maxDegree];
        int[] lowestCostEdgeIndex = new int[maxDegree];
        float[] lowestCost = new float[maxDegree];
        int nUsed = 0;
        
        for (int i=0;i<nNodes;++i) {
            nUsed = 0;
            int degree = nSkipEdgess[i];
            int[] sEdges = outgoingSkipEdgess[i];
            float[] sWeights = outgoingSkipEdgeWeightss[i];
            
            for (int j=0;j<degree;++j) {
                int dest = sEdges[j];
                float weight = sWeights[j];
                
                int p = Memory.parent(dest);
                int index = -1;
                
                if (p == -1) {
                    index = nUsed;
                    ++nUsed;
                    
                    Memory.setParent(dest, index);
                    neighbourIndexes[index] = dest;
                    
                    lowestCostEdgeIndex[index] = j;
                    lowestCost[index] = weight;
                } else {
                    index = p;
                    if (weight < lowestCost[index]) {
                        // Remove existing
                        
                        /*   ________________________________
                         *  |______E__________C____________L_|
                         *   ________________________________
                         *  |______C__________L____________E_|
                         *                                 ^
                         *                                 |
                         *                            nSkipEdges--
                         */
                        swapSkipEdges(i, lowestCostEdgeIndex[index], j);
                        swapSkipEdges(i, j, degree-1);
                        --j; --degree;
                        
                        // lowestCostEdgeIndex[index] happens to be the same as before.
                        lowestCost[index] = weight;
                    } else {
                        // Remove this.
                        
                        /*   ________________________________
                         *  |______E__________C____________L_|
                         *   ________________________________
                         *  |______E__________L____________C_|
                         *                                 ^
                         *                                 |
                         *                            nSkipEdges--
                         */
                        swapSkipEdges(i, j, degree-1);
                        --j; --degree;
                    }
                }
                
            }
            nSkipEdgess[i] = degree;
            
            // Cleanup
            for (int j=0;j<nUsed;++j) {
                Memory.setParent(neighbourIndexes[j], -1); 
            }
        }
    }

    private final void swapSkipEdges(int v, int i1, int i2) {
        int temp;
        {
            temp = outgoingSkipEdgess[v][i1];
            outgoingSkipEdgess[v][i1] = outgoingSkipEdgess[v][i2];
            outgoingSkipEdgess[v][i2] = temp;
        }
        {
            temp = outgoingSkipEdgeNextNodess[v][i1];
            outgoingSkipEdgeNextNodess[v][i1] = outgoingSkipEdgeNextNodess[v][i2];
            outgoingSkipEdgeNextNodess[v][i2] = temp;
        }
        {
            temp = outgoingSkipEdgeNextNodeEdgeIndexess[v][i1];
            outgoingSkipEdgeNextNodeEdgeIndexess[v][i1] = outgoingSkipEdgeNextNodeEdgeIndexess[v][i2];
            outgoingSkipEdgeNextNodeEdgeIndexess[v][i2] = temp;
        }
        {
            float tempf = outgoingSkipEdgeWeightss[v][i1];
            outgoingSkipEdgeWeightss[v][i1] = outgoingSkipEdgeWeightss[v][i2];
            outgoingSkipEdgeWeightss[v][i2] = tempf;
        }
    }

    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///    GRAPH CONSTRUCTION PHASE - END   ///
    /// \\\ /// \\\ /// \\\ /// \\\ /// \\\ ///


    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///  REPURPOSING PHASE - START  ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///

    private final void restoreOriginalGraph() {
        // Safeguard against multiple restores
        if (startIndex == -1) return;
        
        unmarkEdgesFromGoal(xPositions[endIndex], yPositions[endIndex]);

        nNodes = originalSize;
        startIndex = -1;
        endIndex = -1;
    }

    // Assumption: No edge between start and end.
    // Uses Memory
    public final void addStartAndEnd(int sx, int sy, int ex, int ey) {
        // START:
        if (nodeIndex[sy][sx] == -1) {
            startIndex = nNodes;
            xPositions[startIndex] = sx;
            yPositions[startIndex] = sy;
            ++nNodes;
        } else {
            startIndex = nodeIndex[sy][sx];
        }

        // END:
        if (nodeIndex[ey][ex] == -1) {
            endIndex = nNodes;
            xPositions[endIndex] = ex;
            yPositions[endIndex] = ey;
            ++nNodes;
        } else {
            endIndex = nodeIndex[ey][ex];
        }
        
        markEdgesFromGoal(ex, ey);
    }

    // Uses Memory
    private final void markEdgesFromGoal(int ex, int ey) {
        
        losScanner.computeAllVisibleTautSuccessors(ex, ey);
        int nSuccessors = losScanner.nSuccessors;

        nVerticesConnectedToEnd = nSuccessors;
        {
            int length = verticesConnectedToEnd.length;
            while (length < nSuccessors) length *= 2;
            if (length > verticesConnectedToEnd.length) {
                verticesConnectedToEnd = new int[length];
            }
        }
        
        for (int i=0;i<nSuccessors;++i) {
            int toX = losScanner.successorsX[i];
            int toY = losScanner.successorsY[i];
            
            int targetIndex = nodeIndex[toY][toX];

            verticesConnectedToEnd[i] = targetIndex;
            hasEdgeToGoal[targetIndex] = true;
        }
        
        markReachableEdgesFromGoal(ex, ey, true);
    }
    
    private final void unmarkEdgesFromGoal(int ex, int ey) {        
        for (int i=0;i<nVerticesConnectedToEnd;++i) {
            int targetIndex = verticesConnectedToEnd[i];

            hasEdgeToGoal[targetIndex] = false;
        }
        
        markReachableEdgesFromGoal(ex, ey, false);
        nVerticesConnectedToEnd = 0;
    }

    /**
     * Mark all edges reachable with a path of edges of increasing level from the source.
     */
    private final void markReachableEdgesFromGoal(int ex, int ey, boolean value) {
        
        queueSize = 0;   
        for (int i=0;i<nVerticesConnectedToEnd;++i) {
            int curr = verticesConnectedToEnd[i];
            searchAndMark(value, curr, ex, ey);
        }
        
        int currIndex = 0;
        while (currIndex < queueSize) {
            int parent = queue[currIndex];
            ++currIndex;
            int parentOutgoingIndex = queue[currIndex];
            ++currIndex;

            int curr = outgoingEdgess[parent][parentOutgoingIndex];
            
            int parX = xPositions[parent];
            int parY = yPositions[parent];
            
            searchAndMark(value, curr, parX, parY);
        }
    }

    private void searchAndMark(boolean value, int curr, int parX, int parY) {
        int currX = xPositions[curr];
        int currY = yPositions[curr];
        
        int[] outgoingEdges = outgoingEdgess[curr];
        int[] outgoingEdgeIndexes = outgoingEdgeIndexess[curr];
        int[] outgoingEdgeOppositeIndexes = outgoingEdgeOppositeIndexess[curr];

        int nDirectedNeighbours = nDirectedNeighbourss[curr];
        int[] directedNeighbourOutgoingIndexes = directedNeighbourOutgoingIndexess[curr];
        for (int j=0;j<nDirectedNeighbours;++j) {
            int index = directedNeighbourOutgoingIndexes[j];
            int next = outgoingEdges[index];
            if (!graph.isTaut(parX, parY, currX, currY, xPositions[next], yPositions[next])) continue;
            
            int edgeIndex = outgoingEdgeIndexes[index];
            if (isMarked[edgeIndex] != value) {
                isMarked[edgeIndex] = value;
                if (value) {
                    addToMarkedEdges(outgoingEdges[index], outgoingEdgeOppositeIndexes[index]);
                }
                else {
                    clearMarkedEdges(outgoingEdges[index]);
                }
                
                addPairToQueue(curr, index);
            }
        }
        
        int nLevelWNeighbours = nLevelWNeighbourss[curr];
        int[] levelWEdgeOutgoingIndexes = levelWEdgeOutgoingIndexess[curr];
        boolean[] levelWEdgeIsMarked = levelWEdgeIsMarkedss[curr];
        for (int j=0;j<nLevelWNeighbours;++j) {
            int index = levelWEdgeOutgoingIndexes[j];
            int next = outgoingEdges[index];
            if (!graph.isTaut(parX, parY, currX, currY, xPositions[next], yPositions[next])) continue;
            
            if (levelWEdgeIsMarked[j] != value) {
                levelWEdgeIsMarked[j] = value;
                if (value) {
                    addToMarkedEdges(outgoingEdges[index], outgoingEdgeOppositeIndexes[index]);
                }
                else {
                    clearMarkedEdges(outgoingEdges[index]);
                }
                
                if (nSkipEdgess[next] == 0) {
                    addPairToQueue(curr, index);
                }
            }
        }
    }

    private final void addToMarkedEdges(int current, int outgoingIndex) {
        outgoingMarkedEdgeIndexess[current][nMarkedEdgess[current]] = outgoingIndex;
        ++nMarkedEdgess[current];
    }
    
    private final void clearMarkedEdges(int current) {
        nMarkedEdgess[current] = 0;
    }
    
    private final void addPairToQueue(int parent, int parentOutgoingIndex) {
        if (queueSize+1 >= queue.length) {
            queue = Arrays.copyOf(queue, queue.length*2);
        }
        queue[queueSize] = parent;
        ++queueSize;
        queue[queueSize] = parentOutgoingIndex;
        ++queueSize;
    }
    
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///   REPURPOSING PHASE - END   ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    

    
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///     SEARCH PHASE - START    ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    
    public final LineOfSightScanner findReachableVerticesFromStart(int sx, int sy) {
        losScanner.computeAllVisibleTautSuccessors(sx, sy);
        return losScanner;
    }
    
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    ///      SEARCH PHASE - END     ///
    /// \\\ /// \\\ /// \\\ /// \\\ ///
    
    public final int size() {
        return nNodes;
    }
    
    public final int maxSize() {
        return maxSize;
    }
    
    public final int startNode() {
        return startIndex;
    }

    public final int endNode() {
        return endIndex;
    }
    
    public final int computeNumSkipEdges() {
        int sumdegrees = 0;
        for (int i=0;i<nNodes;++i) {
            sumdegrees += nSkipEdgess[i];
        }
        return sumdegrees/2;
    }
    
    private final void maybeSaveSnapshot() {
        if (saveSnapshot != null) saveSnapshot.run();
    }
    
    public static void clearMemory() {
        storedVisibilityGraph = null;
        storedGridGraph = null;
        System.gc();
    }
    
    private String vertexToStr(int index) {
        return xPositions[index] + ", " + yPositions[index];
    }
}