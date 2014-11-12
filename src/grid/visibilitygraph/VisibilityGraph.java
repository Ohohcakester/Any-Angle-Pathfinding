package grid.visibilitygraph;

import grid.GridGraph;
import grid.anya.Point;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class VisibilityGraph {
    private final GridGraph graph;
    private int[][] nodeIndex;
    private int startIndex;
    private int endIndex;
    
    ArrayList<Point> nodeList;
    ArrayList<LinkedList<Edge>> outgoingEdgeList;
    
    public VisibilityGraph(GridGraph graph, int sx, int sy, int ex, int ey) {
        this.graph = graph;
        
        nodeList = new ArrayList<>();
        outgoingEdgeList = new ArrayList<>();
        
        addNodes();
        addStartAndEnd(sx, sy, ex, ey);
        addAllEdges();
    }

    private void addNodes() {
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

    private int assignNode(int x, int y) {
        int index = nodeList.size();
        nodeList.add(new Point(x,y));
        outgoingEdgeList.add(new LinkedList<Edge>());
        return index;
    }
    
    private void addStartAndEnd(int sx, int sy, int ex, int ey) {
        if (isNode(sx, sy)) {
            startIndex = indexOf(sx, sy);
        } else {
            startIndex = nodeIndex[sy][sx] = assignNode(sx, sy);
        }

        if (isNode(ex, ey)) {
            endIndex = indexOf(ex, ey);
        } else {
            endIndex = nodeIndex[ey][ex] = assignNode(ex, ey);
        }
    }
    
    private void addAllEdges() {
        for (int i=0; i<nodeList.size(); i++) {
            Point fromPoint = coordinateOf(i);
            for (int j=i+1; j<nodeList.size(); j++) {
                Point toPoint = coordinateOf(j);
                if (graph.lineOfSight(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y)) {
                    float weight = computeWeight(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                    addEdge(i, j, weight);
                    addEdge(j, i, weight);
                }
            }
        }
    }
    
    private float computeWeight(int x1, int y1, int x2, int y2) {
        int dx = x2-x1;
        int dy = y2-y1;
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    
    private void addEdge(int fromI, int toI, float weight) {
        LinkedList<Edge> edgeList = outgoingEdgeList.get(fromI);
        edgeList.add(new Edge(fromI, toI, weight));
    }
    
    
    private int indexOf(int x, int y) {
        return nodeIndex[y][x];
    }
    
    private boolean isNode(int x, int y) {
        return nodeIndex[y][x] != -1;
    }
    

    private boolean isCorner(int x, int y) {
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
    
    public Iterator<Edge> edgeIterator(int source) {
        return outgoingEdgeList.get(source).iterator();
    }

    public Edge getEdge(int source, int dest) {
        
        LinkedList<Edge> edges = outgoingEdgeList.get(source);
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
}