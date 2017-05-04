package algorithms.sparsevgs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import algorithms.PathFindingAlgorithm;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;
import grid.GridGraph;
import main.AlgoFunction;

import algorithms.jgrapht.FibonacciHeap;
import algorithms.jgrapht.FibonacciHeapNode;


public class EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap extends PathFindingAlgorithm {
    private EdgeNLevelSparseVisibilityGraph visibilityGraph;
    private boolean reuseGraph = false;
    private int levelLimit = Integer.MAX_VALUE;

    private static FibonacciHeapNode<Integer>[] nodeRefs;
    private FibonacciHeap<Integer> pq = new FibonacciHeap<>();
    
    private EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, graph.sizeX, graph.sizeY, sx, sy, ex, ey);
    }
    
    public static EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap algo = new EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }
    
    public static AlgoFunction withLevelLimit(int levelLimit) {
        return (GridGraph graph, int sx, int sy, int ex, int ey) -> {
            EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap algo = new EdgeNLevelSparseVisibilityGraphAlgorithmFibHeap(graph, sx, sy, ex, ey);
            algo.reuseGraph = true;
            algo.levelLimit = levelLimit;
            return algo;
        };
    }

    public EdgeNLevelSparseVisibilityGraph getVisibilityGraph() {
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
            resolveSkipEdgeNextNode(current);
            
            if (current == finish) {
                break;
            }
            
            int[] outgoingEdges = visibilityGraph.outgoingEdgess[current];
            int[] outgoingEdgeIndexes = visibilityGraph.outgoingEdgeIndexess[current];

            // Scan through marked edges to neighbours
            int nMarkedEdges = visibilityGraph.nMarkedEdgess[current];
            int[] outgoingMarkedEdgeIndexes = visibilityGraph.outgoingMarkedEdgeIndexess[current];
            
            for (int i=0;i<nMarkedEdges;++i) {
                int index = outgoingMarkedEdgeIndexes[i];
                int dest = outgoingEdges[index];
                int edgeIndex = outgoingEdgeIndexes[index];
                float weight = visibilityGraph.edgeWeights[edgeIndex];
                boolean isNewItem = distance(dest) == Float.POSITIVE_INFINITY;

                if (!Memory.visited(dest) && relax(current, dest, weight)) {
                    int destX = visibilityGraph.xPositions[dest];
                    int destY = visibilityGraph.yPositions[dest];
                    
                    decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey), isNewItem);
                }
            }

            // Scan through skip edges to neighbours
            int nSkipEdges = visibilityGraph.nSkipEdgess[current];
            int[] outgoingSkipEdges = visibilityGraph.outgoingSkipEdgess[current];
            float[] outgoingSkipEdgeWeights = visibilityGraph.outgoingSkipEdgeWeightss[current];
            int[] outgoingSkipEdgeNextNodes = visibilityGraph.outgoingSkipEdgeNextNodess[current];
            
            for (int i=0;i<nSkipEdges;++i) {
                int dest = outgoingSkipEdges[i];
                int nextNode = outgoingSkipEdgeNextNodes[i];
                float edgeWeight = outgoingSkipEdgeWeights[i];
                boolean isNewItem = distance(dest) == Float.POSITIVE_INFINITY;
                
                if (!Memory.visited(dest) && relaxViaSkipEdge(current, dest, nextNode, edgeWeight)) {
                    int destX = visibilityGraph.xPositions[dest];
                    int destY = visibilityGraph.yPositions[dest];
                    decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey), isNewItem);
                }
            }

            if (visibilityGraph.hasEdgeToGoal[current]) {
                int currX = visibilityGraph.xPositions[current];
                int currY = visibilityGraph.yPositions[current];
                boolean isNewItem = distance(finish) == Float.POSITIVE_INFINITY;
                
                float weight = graph.distance(currX, currY, ex, ey);
                if (relax(current, finish, weight)) {
                    decreaseKey(finish, distance(finish), isNewItem);
                }
            }
            
            maybeSaveSearchSnapshot();
        }

        resolveFinalPath();
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
            visibilityGraph = EdgeNLevelSparseVisibilityGraph.initialiseNew(graph, levelLimit);
        } else {
            EdgeNLevelSparseVisibilityGraph.clearMemory();
            visibilityGraph = EdgeNLevelSparseVisibilityGraph.initialiseNew(graph, levelLimit);
        }
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot(false));
            saveVisibilityGraphSnapshot(false);
            visibilityGraph.addStartAndEnd(sx, sy, ex, ey);
            saveVisibilityGraphSnapshot(false);
            saveVisibilityGraphSnapshot(true);
        } else {
            visibilityGraph.addStartAndEnd(sx, sy, ex, ey);
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
                int[] xPositions = visibilityGraph.xPositions;
                int[] yPositions = visibilityGraph.yPositions;
                p = getNextNodeIndex(p);
                int x1 = xPositions[p];
                int y1 = yPositions[p];
                int x2 = xPositions[u];
                int y2 = yPositions[u];
                int x3 = xPositions[v];
                int y3 = yPositions[v];
                
                if (!graph.isTaut(x1,y1,x2,y2,x3,y3)) return false;
            }
            setDistance(v, newWeight);
            setParent(v, u);
            return true;
        }
        return false;
    }

    /**
     * Skip Edge Parents
     *                    
     *            .--'''''-. _ n
     *          .'          (_)
     *        _/               '-._
     *       (_)                 (_) v
     *      u
     * 
     * We use a hack to store the parent vertices of skip edges.
     * Non-skip-edge parents are stored with an index >= 0.
     * 
     * For skip edge parents,  [Suppose we are relaxing v from u, and the node before v ("next node") is n]
     * 
     *   When v is first relaxed, we store u + MAX_INT as v's parent.
     *   When v is generated (visited), we replace v's parent with n + MAX_INT
     * 
     * Thus when checking taut paths, vertex n will be used instead.
     * 
     * When setting up the final path, the path if Level-W edges from v -> n will be followed till the next skip node (u).
     * 
     */
    private final boolean relaxViaSkipEdge(int u, int v, int nextNode, float weightUV) {
        // return true iff relaxation is done.
        float newWeight = distance(u) + weightUV;
        if (newWeight < distance(v)) {
            int p = parent(u);
            if (p != -1) {
                p = getNextNodeIndex(p);
                int x1 = visibilityGraph.xPositions[p];
                int y1 = visibilityGraph.yPositions[p];
                int x2 = visibilityGraph.xPositions[u];
                int y2 = visibilityGraph.yPositions[u];
                int x3 = visibilityGraph.xPositions[nextNode];
                int y3 = visibilityGraph.yPositions[nextNode];
                
                if (!graph.isTaut(x1,y1,x2,y2,x3,y3)) return false;
            }
            setDistance(v, newWeight);
            setParent(v, u + Integer.MIN_VALUE);
            return true;
        }
        return false;
    }
    
    private final void resolveSkipEdgeNextNode(int v) {
        int parent = Memory.parent(v);
        if (parent >= -1) return;
        parent -= Integer.MIN_VALUE;
        int nSkipEdges = visibilityGraph.nSkipEdgess[v];
        int[] outgoingSkipEdges = visibilityGraph.outgoingSkipEdgess[v];

        for (int i=0;i<nSkipEdges;++i) {
            if (outgoingSkipEdges[i] == parent) {
                Memory.setParent(v, visibilityGraph.outgoingSkipEdgeNextNodess[v][i] + Integer.MIN_VALUE);
                return;
            }
        }
        throw new UnsupportedOperationException("SHOULD NOT REACH HERE!");
    }
    
    private final int getNextNodeIndex(int p) {
        return (p >= -1) ? p : (p - Integer.MIN_VALUE);
    }
    
    private void resolveFinalPath() {
        int current = visibilityGraph.endNode();
        int previous = -1;
        int[] edgeLevels = visibilityGraph.edgeLevels;
        
        while (current != -1) {
            if (current < -1) {
                current -= Integer.MIN_VALUE;
                Memory.setParent(previous, current);
                
                if (visibilityGraph.nSkipEdgess[current] != 0) {
                    previous = current;
                    current = Memory.parent(current);
                    continue;
                }
                
                int nOutgoingEdges = visibilityGraph.nOutgoingEdgess[current];
                int[] outgoingEdges = visibilityGraph.outgoingEdgess[current];
                int[] outgoingEdgeIndexes = visibilityGraph.outgoingEdgeIndexess[current];
                
                boolean done = false;
                for (int i=0;i<nOutgoingEdges;++i) {
                    if (edgeLevels[outgoingEdgeIndexes[i]] != EdgeNLevelSparseVisibilityGraph.LEVEL_W) continue;
                    if (outgoingEdges[i] == previous) continue;
                    
                    int next = outgoingEdges[i];
                    Memory.setParent(current, next + Integer.MIN_VALUE);
                    done = true;
                    break;
                }
                if (!done) throw new UnsupportedOperationException("SS");
            }
            previous = current;
            current = Memory.parent(current);
        }
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
            int x = visibilityGraph.xPositions[current];
            int y = visibilityGraph.yPositions[current];
            
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
        //startIndex = getNextNodeIndex(startIndex);
        if (startIndex < -1) {
            startIndex -= Integer.MIN_VALUE;
            int nSkipEdges = visibilityGraph.nSkipEdgess[endIndex];
            int[] outgoingSkipEdgeNextNodes = visibilityGraph.outgoingSkipEdgeNextNodess[endIndex];
            for (int i=0;i<nSkipEdges;++i) {
                if (outgoingSkipEdgeNextNodes[i] == startIndex) {
                    startIndex = visibilityGraph.outgoingSkipEdgess[endIndex][i];
                    break;
                }
            }
        }
        
        edge[0] = visibilityGraph.xPositions[startIndex];
        edge[1] = visibilityGraph.yPositions[startIndex];
        edge[2] = visibilityGraph.xPositions[endIndex];
        edge[3] = visibilityGraph.yPositions[endIndex];
        return edge;
    }

    @Override
    protected Integer[] snapshotVertex(int index) {
        if (selected(index)) {
            Integer[] edge = new Integer[2];
            edge[0] = visibilityGraph.xPositions[index];
            edge[1] = visibilityGraph.yPositions[index];
            return edge;
        }
        return null;
    }

    protected void saveVisibilityGraphSnapshot(boolean showMarked) {
        int size = visibilityGraph.size();
        List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

        int[] xPositions = visibilityGraph.xPositions;
        int[] yPositions = visibilityGraph.yPositions;
        
        TreeMap<Integer, ArrayList<SnapshotItem>> sortedSnapshots = new TreeMap<>(); 
        for (int i=0;i<size;i++) {
            int x1 = xPositions[i];
            int y1 = yPositions[i];

            int nOutgoingEdges = visibilityGraph.nOutgoingEdgess[i];
            int[] outgoingEdges = visibilityGraph.outgoingEdgess[i];
            int[] outgoingEdgeIndexes = visibilityGraph.outgoingEdgeIndexess[i];
            
            for (int j=0;j<nOutgoingEdges;++j) {
                int neighbour = outgoingEdges[j];
                int edgeIndex = outgoingEdgeIndexes[j];
                int x2 = xPositions[neighbour];
                int y2 = yPositions[neighbour];
                
                Integer[] path = new Integer[4];
                path[0] = x1;
                path[1] = y1;
                path[2] = x2;
                path[3] = y2;
                
                Color color;
                int colourIndex;
                
                if (showMarked && visibilityGraph.isMarked[edgeIndex]) {
                    colourIndex = 0;
                    color = vertexColours[colourIndex];
                } else if (visibilityGraph.edgeLevels[edgeIndex] == visibilityGraph.LEVEL_W) {
                    colourIndex = vertexColours.length;
                    color = levelWColour;
                } else {
                    colourIndex = Math.min(visibilityGraph.edgeLevels[edgeIndex], vertexColours.length-1);
                    color = vertexColours[colourIndex];
                    //color = Color.GREEN;
                }

                SnapshotItem snapshotItem = SnapshotItem.generate(path, color);
                if (!sortedSnapshots.containsKey(colourIndex)) {
                    sortedSnapshots.put(colourIndex, new ArrayList<>());
                }
                //if ( colourIndex == 0)
                sortedSnapshots.get(colourIndex).add(snapshotItem);
            }
            
            Integer[] vert = new Integer[2];
            vert[0] = x1;
            vert[1] = y1;

            SnapshotItem snapshotItem = null;
            if (visibilityGraph.nSkipEdgess[i] == 0) {
                // Regular vertex.
                snapshotItem = SnapshotItem.generate(vert, Color.BLUE);
            }
            else {
                snapshotItem = SnapshotItem.generate(vert, Color.WHITE);
            }
            snapshotItemList.add(snapshotItem);
        }
        
        {
            ArrayList<SnapshotItem> transfer = sortedSnapshots.remove(0);
            sortedSnapshots.put(vertexColours.length+1, transfer);
        }
        for (ArrayList<SnapshotItem> list : sortedSnapshots.values()) {
            if (list == null) continue;
            for (SnapshotItem item : list) {
                snapshotItemList.add(item);
            }
        }

        for (int i=0;i<size;i++) {
            int x1 = xPositions[i];
            int y1 = yPositions[i];

            int nSkipEdges = visibilityGraph.nSkipEdgess[i];
            int[] outgoingSkipEdges = visibilityGraph.outgoingSkipEdgess[i];
            
            for (int j=0;j<nSkipEdges;++j) {
                int neighbour = outgoingSkipEdges[j];
                int x2 = xPositions[neighbour];
                int y2 = yPositions[neighbour];
                
                Integer[] path = new Integer[4];
                path[0] = x1;
                path[1] = y1;
                path[2] = x2;
                path[3] = y2;
                
                SnapshotItem snapshotItem = SnapshotItem.generate(path, skipEdgeColour);
                snapshotItemList.add(snapshotItem);
            }
        }

        addSnapshot(snapshotItemList);
    }
    
    @Override
    public void printStatistics() {
        System.out.println("Nodes: " + visibilityGraph.size());
        System.out.println("Edges: " + visibilityGraph.nEdges);
        
        { // Print edge distribution.
            int[] edgeLevels = visibilityGraph.edgeLevels;
            
            int maxLevel = 0;
            for (int i=0;i<visibilityGraph.nEdges;++i) {
                int level = edgeLevels[i];
                if (level != visibilityGraph.LEVEL_W && level > maxLevel) maxLevel = level;
            }
            
            int countLevelW = 0;
            int[] counts = new int[maxLevel+1];
            for (int i=0;i<visibilityGraph.nEdges;++i) {
                int level = edgeLevels[i];

                if (level == visibilityGraph.LEVEL_W) {
                    countLevelW++;
                } else {
                    counts[level]++;
                }
            }
            
            for (int i=0;i<=maxLevel;++i) {
                System.out.println("Level " + i + " : " + counts[i]);
            }
            System.out.println("Level W : " + countLevelW);
        }
        System.out.println("Skip Edges: " + visibilityGraph.computeNumSkipEdges());
    }

    private static final Color skipEdgeColour = new Color(255,0,255);
    private static final Color levelWColour = new Color(0,0,255);
    private static final Color[] vertexColours = new Color[] {
        Color.BLACK,
        new Color(127,255,127),
        //Color.GREEN,
        //new Color(64,255,0),
        //new Color(127,255,0),
        //new Color(191,255,0),
        Color.YELLOW,
//        new Color(255,224,0),
//        new Color(255,191,0),
//        new Color(255,160,0),
        new Color(255,127,0),
//        new Color(255,96,0),
        new Color(255,64,0),
//        new Color(255,32,0),
        Color.RED,
//        new Color(224,0,0),
        new Color(191,0,0),
//        new Color(0,255,127),
//        Color.CYAN,
        //new Color(0,191,191),
//        new Color(0,127,255),
//        Color.BLUE,
        //new Color(0,0,191),
        //new Color(127,0,255),
        //Color.MAGENTA,

        //new Color(127, 127, 255),
        //new Color(191,0,191),
        /*new Color(255,0,127),
        new Color(127,127,127),
        new Color(160,160,160),
        new Color(191,191,191),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        Color.WHITE,*/
    };

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
