package algorithms.sparsevgs;

import grid.GridGraph;

import java.util.ArrayList;
import java.util.Iterator;

import algorithms.datatypes.Point;
import algorithms.visibilitygraph.Edge;

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
    //private SVGNode[] nodes;
    private ArrayList<Point> nodeList;
    private ArrayList<ArrayList<Edge>> outgoingEdgeList;

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
        if (nodeList != null) {
            //("already initialised.");
            return;
        }
        
        nodeList = new ArrayList<>();
        outgoingEdgeList = new ArrayList<>();
        
        addNodes();
        maxSize = nodeList.size() + 2;
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
        int index = nodeList.size();
        nodeList.add(new Point(x,y));
        outgoingEdgeList.add(new ArrayList<Edge>());
        return index;
    }

    protected int assignNodeAndConnect(int x, int y) {
        int index = nodeList.size();
        nodeList.add(new Point(x,y));
        outgoingEdgeList.add(new ArrayList<Edge>());

        for (int i=0; i<nodeList.size()-1; i++) {
            Point toPoint = coordinateOf(i);
            if (shouldAddEdge(x, y, toPoint.x, toPoint.y)) {
                float weight = computeWeight(x, y, toPoint.x, toPoint.y);
                addEdge(i, index, weight);
                addEdge(index, i, weight);
            }
        }
        
        return index;
    }
    
    /**
     * Assumption: start and end are the last two nodes, if they exist.
     */
    protected void removeStartAndEnd() {
        if (startIsNewNode) {
            int index = nodeList.size()-1;
            nodeIndex[sy][sx] = -1;
            nodeList.remove(index);
            outgoingEdgeList.remove(index);
            removeInstancesOf(index);

            startIsNewNode = false;
        }
        if (endIsNewNode) {
            int index = nodeList.size()-1;
            nodeIndex[ey][ex] = -1;
            nodeList.remove(index);
            outgoingEdgeList.remove(index);
            removeInstancesOf(index);

            endIsNewNode = false;
        }
    }
    
    protected final void removeInstancesOf(int index) {
        for (ArrayList<Edge> edgeList : outgoingEdgeList) {
            Edge edge = new Edge(0, index, 0);
            edgeList.remove(edge);
        }
    }
    
    protected void addStartAndEnd(int sx, int sy, int ex, int ey) {
        if (isNode(sx, sy)) {
            startIndex = indexOf(sx, sy);
            startIsNewNode = false;
        } else {
            startIndex = nodeIndex[sy][sx] = assignNodeAndConnect(sx, sy);
            startIsNewNode = true;
        }

        if (isNode(ex, ey)) {
            endIndex = indexOf(ex, ey);
            endIsNewNode = false;
        } else {
            endIndex = nodeIndex[ey][ex] = assignNodeAndConnect(ex, ey);
            endIsNewNode = true;
        }
    }
    
    protected void addAllEdges() {
        int saveFactor = nodeList.size()/10;
        if (saveFactor == 0) saveFactor = 1;
        
        for (int i=0; i<nodeList.size(); i++) {
            Point fromPoint = coordinateOf(i);
            for (int j=i+1; j<nodeList.size(); j++) {
                Point toPoint = coordinateOf(j);
                if (shouldAddEdge(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y)) {
                    float weight = computeWeight(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                    addEdge(i, j, weight);
                    addEdge(j, i, weight);
                }
            }
            
            if (i%saveFactor == 0)
                maybeSaveSnapshot();
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

        /*// Check from (x1,y1)
        if ((sx != x1 || sy != y1) && (ex != x1 || ey != y1)) {
            // From (x1,y1). If not the start point.
            if (graph.topLeftOfBlockedTile(x1, y1) || graph.bottomRightOfBlockedTile(x1, y1)) {
                // TLBR corner. Accept only if (dx>=0, dy>=0) or (dx<=0,dy<=0)
                if (dx*dy < 0) return false;
            } else {
                // TRBL corner. Accept only if (dx>=0, dy<=0) or (dx<=0,dy>=0)
                if (dx*dy > 0) return false;
            }
        }

        // Check from (x2,y2)
        if ((sx != x2 || sy != y2) && (ex != x2 || ey != y2)) {
            // From (x1,y1). If not the start point.
            if (graph.topLeftOfBlockedTile(x2, y2) || graph.bottomRightOfBlockedTile(x2, y2)) {
                // TLBR corner. Accept only if (dx>=0, dy>=0) or (dx<=0,dy<=0)
                if (dx*dy < 0) return false;
            } else {
                // TRBL corner. Accept only if (dx>=0, dy<=0) or (dx<=0,dy>=0)
                if (dx*dy > 0) return false;
            }
        }
        
        return graph.lineOfSight(x1,y1,x2,y2);*/
    }
    
    protected final float computeWeight(int x1, int y1, int x2, int y2) {
        int dx = x2-x1;
        int dy = y2-y1;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    
    protected final void addEdge(int fromI, int toI, float weight) {
        ArrayList<Edge> edgeList = outgoingEdgeList.get(fromI);
        edgeList.add(new Edge(fromI, toI, weight));
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

    
    public Point coordinateOf(int index) {
        return nodeList.get(index);
    }

    public int size() {
        return nodeList.size();
    }
    
    public int maxSize() {
        return maxSize;
    }
    
    public int computeSumDegrees() {
        int sum = 0;
        for (ArrayList<Edge> list : outgoingEdgeList) {
            sum += list.size();
        }
        return sum;
    }
    
    public Iterator<Edge> edgeIterator(int source) {
        return outgoingEdgeList.get(source).iterator();
    }

    public Edge getEdge(int source, int dest) {
        
        ArrayList<Edge> edges = outgoingEdgeList.get(source);
        for (Edge edge : edges) {
            if (edge.dest == dest) {
                return edge;
            }
        }
        return new Edge(source, dest, Float.POSITIVE_INFINITY);
    }

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
        oldGraph.removeStartAndEnd();
        SparseVisibilityGraph newGraph = new SparseVisibilityGraph(oldGraph.graph, sx, sy, ex, ey);
        newGraph.nodeIndex = oldGraph.nodeIndex;
        newGraph.startIndex = oldGraph.startIndex;
        newGraph.startIsNewNode = oldGraph.startIsNewNode;
        newGraph.endIndex = oldGraph.endIndex;
        newGraph.endIsNewNode = oldGraph.endIsNewNode;
        newGraph.nodeList = oldGraph.nodeList;
        newGraph.outgoingEdgeList = oldGraph.outgoingEdgeList;
        newGraph.maxSize = oldGraph.maxSize;
        
        newGraph.addStartAndEnd(sx, sy, ex, ey);
        
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
    
}
