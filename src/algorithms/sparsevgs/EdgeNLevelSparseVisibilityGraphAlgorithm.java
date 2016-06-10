package algorithms.sparsevgs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import algorithms.AStarStaticMemory;
import algorithms.datatypes.Memory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;


public class EdgeNLevelSparseVisibilityGraphAlgorithm extends AStarStaticMemory {
    protected EdgeNLevelSparseVisibilityGraph visibilityGraph;
    protected boolean reuseGraph = false;
    
    private EdgeNLevelSparseVisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static EdgeNLevelSparseVisibilityGraphAlgorithm graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        EdgeNLevelSparseVisibilityGraphAlgorithm algo = new EdgeNLevelSparseVisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }

    public EdgeNLevelSparseVisibilityGraph getVisibilityGraph() {
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

        if (graph.lineOfSight(sx, sy, ex, ey)) {
            // There is a direct path from (sx, sy) to (ex, ey).
            if (sx != ex || sy != ey) {
                setParent(finish, visibilityGraph.startNode());
            }
            return;
        }

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            setVisited(current, true);
            resolveSkipEdgeNextNode(current);
            
            if (current == finish) {
                break;
            }

            int nOutgoingEdges = visibilityGraph.nOutgoingEdgess[current];
            int[] outgoingEdges = visibilityGraph.outgoingEdgess[current];
            int[] outgoingEdgeIndexes = visibilityGraph.outgoingEdgeIndexess[current];
            
            for (int i=0;i<nOutgoingEdges;++i) {
                int dest = outgoingEdges[i];
                int edgeIndex = outgoingEdgeIndexes[i];
                float weight = visibilityGraph.edgeWeights[edgeIndex];
                if (visibilityGraph.isMarked[visibilityGraph.isMarkedIndex[edgeIndex]] &&
                        !Memory.visited(dest) &&
                        relax(current, dest, weight)) {

                    int destX = visibilityGraph.xPositions[dest];
                    int destY = visibilityGraph.yPositions[dest];
                    
                    pq.decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey));
                }
            }
            
            int nSkipEdges = visibilityGraph.nSkipEdgess[current];
            int[] outgoingSkipEdges = visibilityGraph.outgoingSkipEdgess[current];
            float[] outgoingSkipEdgeWeights = visibilityGraph.outgoingSkipEdgeWeightss[current];
            int[] outgoingSkipEdgeNextNodes = visibilityGraph.outgoingSkipEdgeNextNodess[current];
            
            for (int i=0;i<nSkipEdges;++i) {
                int dest = outgoingSkipEdges[i];
                int nextNode = outgoingSkipEdgeNextNodes[i];
                float edgeWeight = outgoingSkipEdgeWeights[i];
                
                if (!Memory.visited(dest) && relaxViaSkipEdge(current, dest, nextNode, edgeWeight)) {
                    int destX = visibilityGraph.xPositions[dest];
                    int destY = visibilityGraph.yPositions[dest];
                    pq.decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey));
                }
            }
            
            if (visibilityGraph.hasEdgeToGoal[current]) {
                int currX = visibilityGraph.xPositions[current];
                int currY = visibilityGraph.yPositions[current];
                
                float weight = graph.distance(currX, currY, ex, ey);
                if (relax(current, finish, weight)) {
                    pq.decreaseKey(finish, distance(finish));
                }
            }
            
            maybeSaveSearchSnapshot();
        }
    }

    protected void setupVisibilityGraph() {
        if (reuseGraph) {
            visibilityGraph = EdgeNLevelSparseVisibilityGraph.initialiseNew(graph);
        } else {
            EdgeNLevelSparseVisibilityGraph.clearMemory();
            visibilityGraph = EdgeNLevelSparseVisibilityGraph.initialiseNew(graph);
        }
        
        /*if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot(false));
            visibilityGraph.initialise(sx, sy, ex, ey);
            saveVisibilityGraphSnapshot(false);
            saveVisibilityGraphSnapshot(true);
        } else {
            visibilityGraph.initialise(sx, sy, ex, ey);
        }*/
    }

    protected final boolean relax(int u, int v, float weightUV) {
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
                int x3 = visibilityGraph.xPositions[v];
                int y3 = visibilityGraph.yPositions[v];
                
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
            setParent(v, u + Integer.MAX_VALUE);
            return true;
        }
        return false;
    }
    
    private final void resolveSkipEdgeNextNode(int v) {
        int parent = Memory.parent(v);
        if (parent >= -1) return;
        parent -= Integer.MAX_VALUE;
        int nSkipEdges = visibilityGraph.nSkipEdgess[v];
        int[] outgoingSkipEdges = visibilityGraph.outgoingSkipEdgess[v];
        for (int i=0;i<nSkipEdges;++i) {
            if (outgoingSkipEdges[i] == parent) {
                Memory.setParent(v, visibilityGraph.outgoingSkipEdgeNextNodess[v][i] + Integer.MAX_VALUE);
                return;
            }
        }
        throw new UnsupportedOperationException("SHOULD NOT REACH HERE!");
    }
    
    private final int getNextNodeIndex(int p) {
        return (p >= -1) ? p : p - Integer.MAX_VALUE;
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
    protected int goalParentIndex() {
        return visibilityGraph.endNode();
    }

    @Override
    protected Integer[] snapshotEdge(int endIndex) {
        Integer[] edge = new Integer[4];
        int startIndex = parent(endIndex);
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
        /*int size = visibilityGraph.size();
        List<SnapshotItem> snapshotItemList = new ArrayList<>(size);

        for (int i=0;i<size;i++) {
            VertexNLevelSVGNode curr = visibilityGraph.getOutgoingEdges(i);
            int x1 = curr.x;
            int y1 = curr.y;
            
            int[] neighbours = curr.outgoingEdges;
            int nNeighbours = curr.nEdges;
            
            for (int j=0;j<nNeighbours;++j) {
                int neighbour = neighbours[j];
                VertexNLevelSVGNode next = visibilityGraph.getOutgoingEdges(neighbour);
                int x2 = next.x;
                int y2 = next.y;
                
                Integer[] path = new Integer[4];
                path[0] = x1;
                path[1] = y1;
                path[2] = x2;
                path[3] = y2;
                
                Color color = vertexColours[Math.min(Math.min(curr.level, next.level), vertexColours.length-1)];
                SnapshotItem snapshotItem = SnapshotItem.generate(path, color);
                snapshotItemList.add(snapshotItem);
            }
            
            Integer[] vert = new Integer[2];
            vert[0] = x1;
            vert[1] = y1;

            SnapshotItem snapshotItem = null;
            if (showMarked) {
                int index = Math.min(curr.level, vertexColours.length-1);
                if (visibilityGraph.isRelevantVertex[i]) index = 0;
                snapshotItem = SnapshotItem.generate(vert, vertexColours[index]);
            }
            else {
                int index = Math.min(curr.level, vertexColours.length-1);
                snapshotItem = SnapshotItem.generate(vert, vertexColours[index]);
            }
            snapshotItemList.add(snapshotItem);
        }

        addSnapshot(snapshotItemList);*/
    }
    
    @Override
    public void printStatistics() {
        /*System.out.println("Nodes: " + visibilityGraph.size());
        System.out.println("Edges (Directed): " + visibilityGraph.computeSumDegrees());*/
    }

    private static final Color[] vertexColours = new Color[] {
        Color.BLACK,
        Color.RED,
        new Color(191,0,0),
        new Color(255,127,0),
        Color.YELLOW,
        new Color(191,191,0),
        new Color(127,255,0),
        Color.GREEN,
        new Color(0,191,0),
        new Color(0,255,127),
        Color.CYAN,
        new Color(0,191,191),
        new Color(0,127,255),
        Color.BLUE,
        new Color(0,0,191),
        new Color(127,0,255),
        Color.MAGENTA,
        new Color(191,0,191),
        new Color(255,0,127),
        new Color(127,127,127),
        new Color(160,160,160),
        new Color(191,191,191),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        new Color(224,224,224),
        Color.WHITE,
    };
}
