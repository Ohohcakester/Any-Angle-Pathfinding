package algorithms.sparsevgs;

import grid.GridGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import algorithms.AStarStaticMemory;
import algorithms.datatypes.SnapshotItem;
import algorithms.priorityqueue.ReusableIndirectHeap;


public class VertexNLevelSparseVisibilityGraphAlgorithm extends AStarStaticMemory {
    private final int maxLevel = 99999;
    protected VertexNLevelSparseVisibilityGraph visibilityGraph;
    protected boolean reuseGraph = false;
    
    private VertexNLevelSparseVisibilityGraphAlgorithm(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static VertexNLevelSparseVisibilityGraphAlgorithm graphReuse(GridGraph graph, int sx, int sy, int ex, int ey) {
        VertexNLevelSparseVisibilityGraphAlgorithm algo = new VertexNLevelSparseVisibilityGraphAlgorithm(graph, sx, sy, ex, ey);
        algo.reuseGraph = true;
        return algo;
    }

    public VertexNLevelSparseVisibilityGraph getVisibilityGraph() {
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
            
            if (current == finish) {
                break;
            }

            VertexNLevelSVGNode node = visibilityGraph.getOutgoingEdges(current);
            int[] outgoingEdges = node.outgoingEdges;
            float[] outgoingWeights = node.edgeWeights;
            int nEdges = node.nEdges;
            
            for (int i=0;i<nEdges;++i) {
                int dest = outgoingEdges[i];
                float weight = outgoingWeights[i];
                
                if (visibilityGraph.isRelevantVertex[dest] && !visited(dest) && relax(current, dest, weight)) {
                    // If relaxation is done.
                    int destX = visibilityGraph.xCoordinateOf(dest);
                    int destY = visibilityGraph.yCoordinateOf(dest);
                    
                    pq.decreaseKey(dest, distance(dest) + graph.distance(destX, destY, ex, ey));
                }
            }
            if (node.hasEdgeToGoal) {
                float weight = graph.distance(node.x, node.y, ex, ey);
                if (relax(current, finish, weight)) {
                    pq.decreaseKey(finish, distance(finish));
                }
            }
            
            maybeSaveSearchSnapshot();
        }
    }

    protected void setupVisibilityGraph() {
        if (reuseGraph) {
            visibilityGraph = VertexNLevelSparseVisibilityGraph.getStoredGraph(graph, maxLevel);
        } else {
            visibilityGraph = new VertexNLevelSparseVisibilityGraph(graph, maxLevel);
        }
        
        if (isRecording()) {
            visibilityGraph.setSaveSnapshotFunction(()->saveVisibilityGraphSnapshot(false));
            visibilityGraph.initialise(sx, sy, ex, ey);
            saveVisibilityGraphSnapshot(false);
            saveVisibilityGraphSnapshot(true);
        } else {
            visibilityGraph.initialise(sx, sy, ex, ey);
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

    protected void saveVisibilityGraphSnapshot(boolean showMarked) {
        int size = visibilityGraph.size();
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

        addSnapshot(snapshotItemList);
    }
    
    @Override
    public void printStatistics() {
        System.out.println("Nodes: " + visibilityGraph.size());
        System.out.println("Edges (Directed): " + visibilityGraph.computeSumDegrees());
    }

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
        Color.BLUE,
    };
}
