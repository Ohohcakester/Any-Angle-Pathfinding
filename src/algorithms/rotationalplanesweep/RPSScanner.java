package algorithms.rotationalplanesweep;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import draw.GridLineSet;
import draw.GridPointSet;

import algorithms.datatypes.SnapshotItem;
import grid.GridGraph;


public class RPSScanner {

    public static ArrayList<List<SnapshotItem>> snapshotList = new ArrayList<>();

    private final void saveSnapshot(int sx, int sy, Vertex v) {
        ArrayList<SnapshotItem> snapshot = new ArrayList<>();

        snapshot.add(SnapshotItem.generate(new Integer[]{sx, sy, v.x, v.y}, Color.RED));

        // Snapshot current state of heap
        int heapSize = edgeHeap.size();
        Edge[] edges = edgeHeap.getEdgeList();
        for (int k=0; k<heapSize; ++k) {
            Edge e = edges[k];
            snapshot.add(SnapshotItem.generate(new Integer[]{e.u.x, e.u.y, e.v.x, e.v.y}, Color.GREEN));
        }

        snapshotList.add(new ArrayList<SnapshotItem>(snapshot));
    }
    
    public static final void clearSnapshots() {
        snapshotList.clear();
    }


    public int nSuccessors;
    public int[] successorsX;
    public int[] successorsY;

    private final Vertex[] vertices;
    private final RPSEdgeHeap edgeHeap;
    private final GridGraph graph;

    public static class Vertex {
        public int x;
        public int y;
        public double angle;
        public Edge edge1;
        public Edge edge2;

        public Vertex(int x, int y) {
            this.x = x;
            this.y = y;            
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

    public static class Edge {
        public Vertex u;
        public Vertex v;
        public Vertex originalU;
        public int heapIndex;
        public double distance;

        public Edge(RPSScanner.Vertex u, RPSScanner.Vertex v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString() {
            return u.x + ", " + u.y + ", " + v.x + ", " + v.y;
        }
    }

    public RPSScanner(Vertex[] vertices, Edge[] edges, GridGraph graph) {
        successorsX = new int[11];
        successorsY = new int[11];
        nSuccessors = 0;
        this.vertices = vertices;
        this.edgeHeap = new RPSEdgeHeap(edges);
        this.graph = graph;
    }

    private final void clearNeighbours() {
        nSuccessors = 0;
    }

    private final void addNeighbour(int x, int y) {
        if (nSuccessors >= successorsX.length) {
            successorsX = Arrays.copyOf(successorsX, successorsX.length*2);
            successorsY = Arrays.copyOf(successorsY, successorsY.length*2);
        }
        successorsX[nSuccessors] = x;
        successorsY[nSuccessors] = y;
        ++nSuccessors;
    }

    private final double getAngle(int sx, int sy, Vertex v) {
        if (v.x == sx && v.y == sy) return -1;
        double angle = Math.atan2(v.y-sy, v.x-sx);
        if (angle < 0) angle += 2*Math.PI;
        return angle;
    }

    private class VertexShortcutter {
        private Vertex s;
        private Edge edge1;
        private Edge edge2;
        private Vertex end1;
        private Vertex end2;
        private boolean end2FirstEdge;

        public VertexShortcutter(Vertex s) {
            this.s = s;
            /*         edge1
                    s ------- end1
                    |
              edge2 |
                    |
                   end2            */
            edge1 = s.edge1;
            edge2 = s.edge2;

            if (edge1.u == s) end1 = edge1.v;
            else end1 = edge1.u;
            if (edge2.u == s) end1 = edge2.v;
            else end2 = s.edge2.u;

            end2FirstEdge = (end2.edge1 == edge2);
        }

        public void shortcut() {
            // Removal
            edge1.u = end1;
            edge1.v = end2;
            if (end2FirstEdge) end2.edge1 = edge1;
            else end2.edge2 = edge1;
        }

        public void restore() {
            // Restore
            edge1.u = s;
            edge1.v = end1;
            if (end2FirstEdge) end2.edge1 = edge2;
            else end2.edge2 = edge2;
        }
    }

    private final void initialiseScan(int sx, int sy, ArrayList<VertexShortcutter> shortcutters) {
        
        // Compute angles, apply shortcutting to cut (sx, sy).
        // The shortcutters arraylist has size at most 2.
        for (int i=0; i<vertices.length; ++i) {
            Vertex v = vertices[i];
            if (v.x != sx || v.y != sy) {
                v.angle = Math.atan2(v.y-sy, v.x-sx);
                if (v.angle < 0) v.angle += 2*Math.PI;
            } else {
                v.angle = -1;
                VertexShortcutter shortcutter = new VertexShortcutter(v);
                shortcutter.shortcut();
                shortcutters.add(shortcutter);
            }
        }
        sortVertices(sx, sy);

        edgeHeap.clear();
        Edge[] edges = edgeHeap.getEdgeList();
        reorderEdgeEndpoints(sx, sy, edges);
        // Note: iterating through the edges like this is a very dangerous operation.
        // That's because the edges array changes as you insert the edges into the heap.
        // Reason why it works: When we swap two edges when inserting, both edges have already been checked.
        //                      That's because we only swap with edges in the heap, which have a lower index than i.
        for (int i=0; i<edges.length; ++i) {
            Edge edge = edges[i];
            if (intersectsPositiveXAxis(sx, sy, edge)) {
                edgeHeap.insert(edge, computeDistance(sx, sy, edge));
            }
        }
    }

    private final void restoreShortcuttedVertices(ArrayList<VertexShortcutter> shortcutters) {
        // Restore shortcutted vertices
        for (VertexShortcutter shortcutter : shortcutters) {
            shortcutter.restore();
        }
    }

    public final void computeAllVisibleSuccessors(int sx, int sy) {
        clearNeighbours();
        if (vertices.length == 0) return;

        // This arraylist has size at most 2.
        ArrayList<VertexShortcutter> shortcutters = new ArrayList<>();
        initialiseScan(sx, sy, shortcutters);


        // This queue is used to enforce the order:
        // INSERT TO EDGEHEAP -> ADD AS NEIGHBOUR -> DELETE FROM EDGEHEAP
        //     for all vertices with the same angle from (sx,sy).
        Vertex[] vertexQueue = new Vertex[11];
        int vertexQueueSize = 0;

        int i = 0;
        // Skip vertex if it is (sx,sy).
        while (vertices[i].x == sx && vertices[i].y == sy) ++i;

        for (; i<vertices.length; ++i) {
            if (vertexQueueSize >= vertexQueue.length) {
                vertexQueue = Arrays.copyOf(vertexQueue, vertexQueue.length*2);
            }
            vertexQueue[vertexQueueSize++] = vertices[i];

            if (i+1 == vertices.length || !isSameAngle(sx, sy, vertices[i], vertices[i+1])) {
                // Clear queue

                // Insert all first
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];

                    if (isStartOrOriginEdge(sx, sy, v, v.edge1)) {
                        edgeHeap.insert(v.edge1, computeDistance(sx, sy, v.edge1));
                    }
                    if (isStartOrOriginEdge(sx, sy, v, v.edge2)) {
                        edgeHeap.insert(v.edge2, computeDistance(sx, sy, v.edge2));
                    }
                }

                // Add all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];
                    //saveSnapshot(sx, sy, v); // UNCOMMENT FOR TRACING

                    Edge edge = edgeHeap.getMin();
                    if (!linesIntersect(sx, sy, v.x, v.y, edge.u.x, edge.u.y, edge.v.x, edge.v.y)) {
                        addNeighbour(v.x, v.y);
                    }
                }

                // Delete all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];

                    if (!isStartOrOriginEdge(sx, sy, v, v.edge1)) {
                        edgeHeap.delete(v.edge1);
                    }
                    if (!isStartOrOriginEdge(sx, sy, v, v.edge2)) {
                        edgeHeap.delete(v.edge2);
                    }
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }

        restoreShortcuttedVertices(shortcutters);
    }


    public final void computeAllVisibleTautSuccessors(int sx, int sy) {
        clearNeighbours();
        if (vertices.length == 0) return;

        // This arraylist has size at most 2.
        ArrayList<VertexShortcutter> shortcutters = new ArrayList<>();
        initialiseScan(sx, sy, shortcutters);

        // We exclude the non-taut region (excludeStart, excludeEnd)
        double EPSILON = 0.00000001;
        double excludeStart = 99999;
        double excludeEnd = 99998;
        // Setting the interval (excludeStart, excludeEnd)
        if (graph.bottomLeftOfBlockedTile(sx, sy)) {
            if (!graph.topRightOfBlockedTile(sx, sy)) {
                excludeStart = Math.PI + EPSILON;
                excludeEnd = 3*Math.PI/2 - EPSILON;
            }
        } else if (graph.bottomRightOfBlockedTile(sx, sy)) {
            if (!graph.topLeftOfBlockedTile(sx, sy)) {
                excludeStart = 3*Math.PI/2 + EPSILON;
                excludeEnd = 2*Math.PI - EPSILON;
            }
        } else if (graph.topRightOfBlockedTile(sx, sy)) {
            excludeStart = 0 + EPSILON;
            excludeEnd = Math.PI/2 - EPSILON;
        } else if (graph.topLeftOfBlockedTile(sx, sy)) {
            excludeStart = Math.PI/2 + EPSILON;
            excludeEnd = Math.PI - EPSILON;
        }


        // This queue is used to enforce the order:
        // INSERT TO EDGEHEAP -> ADD AS NEIGHBOUR -> DELETE FROM EDGEHEAP
        //     for all vertices with the same angle from (sx,sy).
        Vertex[] vertexQueue = new Vertex[11];
        int vertexQueueSize = 0;

        int i = 0;
        // Skip vertex if it is (sx,sy).
        while (vertices[i].x == sx && vertices[i].y == sy) ++i;

        for (; i<vertices.length; ++i) {
            if (vertexQueueSize >= vertexQueue.length) {
                vertexQueue = Arrays.copyOf(vertexQueue, vertexQueue.length*2);
            }
            vertexQueue[vertexQueueSize++] = vertices[i];
            double currentAngle = vertices[i].angle;

            if (i+1 == vertices.length || !isSameAngle(sx, sy, vertices[i], vertices[i+1])) {
                // Clear queue

                // Insert all first
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];

                    if (isStartOrOriginEdge(sx, sy, v, v.edge1)) {
                        edgeHeap.insert(v.edge1, computeDistance(sx, sy, v.edge1));
                    }
                    if (isStartOrOriginEdge(sx, sy, v, v.edge2)) {
                        edgeHeap.insert(v.edge2, computeDistance(sx, sy, v.edge2));
                    }
                }

                // Add all (if it doesn't fall within the interval (excludeStart, excludeEnd) )
                if (currentAngle <= excludeStart || excludeEnd <= currentAngle) {
                    for (int j=0; j<vertexQueueSize; ++j) {
                        Vertex v = vertexQueue[j];
                        //saveSnapshot(sx, sy, v); // UNCOMMENT FOR TRACING

                        Edge edge = edgeHeap.getMin();
                        if (!linesIntersect(sx, sy, v.x, v.y, edge.u.x, edge.u.y, edge.v.x, edge.v.y)) {
                            addNeighbour(v.x, v.y);
                        }
                    }
                }

                // Delete all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];

                    if (!isStartOrOriginEdge(sx, sy, v, v.edge1)) {
                        edgeHeap.delete(v.edge1);
                    }
                    if (!isStartOrOriginEdge(sx, sy, v, v.edge2)) {
                        edgeHeap.delete(v.edge2);
                    }
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }

        restoreShortcuttedVertices(shortcutters);
    }

    private final void sortVertices(int sx, int sy) {
        Arrays.sort(vertices, (a,b) -> Double.compare(a.angle, b.angle));
    }

    private boolean isSameAngle(int sx, int sy, Vertex u, Vertex v) {
        int dx1 = u.x - sx;
        int dy1 = u.y - sy;
        int dx2 = v.x - sx;
        int dy2 = v.y - sy;

        return dx1*dx2 + dy1*dy2 > 0 && dx1*dy2 == dx2*dy1;
    }

    private final void reorderEdgeEndpoints(int sx, int sy, Edge[] edges) {
        // Reorders the endpoints of each edge so that v is anticlockwise from u.
        for (int i=0; i<edges.length; ++i) {
            if (isEdgeOrderReversed(sx, sy, edges[i])) {
                Vertex temp = edges[i].u;
                edges[i].u = edges[i].v;
                edges[i].v = temp;
            }
        }
    }

    private final boolean isEdgeOrderReversed(int sx, int sy, Edge edge) {
        // Note: No need special case with vertex shortcutting.
        // Special case: if an endpoint is (sx,sy), then instantly u is (sx,sy)
        int dx1 = edge.u.x - sx;
        int dy1 = edge.u.y - sy;
        int dx2 = edge.v.x - sx;
        int dy2 = edge.v.y - sy;

        int crossProd = dx1*dy2 - dx2*dy1;
        if (dx1*dx2 + dy1*dy2 < 0 && crossProd == 0) {
            // (sx, sy) is on the edge (but not at the endpoints)
            // Revert to canonical ordering.
            return edge.u == edge.originalU;
        }
        return crossProd < 0;
    }

    private final boolean isStartOrOriginEdge(int sx, int sy, Vertex v, Edge e) {
        return v == e.u || (e.u.x == sx && e.u.y == sy);
    }

    private final double computeDistance(int sx, int sy, Edge edge) {
        // a = v - u
        // b = s - u

        int ax = edge.v.x - edge.u.x;
        int ay = edge.v.y - edge.u.y;

        int bx = sx - edge.u.x;
        int by = sy - edge.u.y;

        int aDotb = ax*bx + ay*by;
        int aDota = ax*ax + ay*ay;
        if (0 <= aDotb && aDotb <= aDota) {
            // use perpendicular distance.
            double perpX = bx - (double)(ax * aDotb)/aDota;
            double perpY = by - (double)(ay * aDotb)/aDota;

            return perpX*perpX + perpY*perpY;
        } else {
            // use distance to closer point.
            int cx = sx - edge.v.x;
            int cy = sy - edge.v.y;

            return Math.min(bx*bx + by*by, cx*cx + cy*cy);
        }
    }

    private final boolean intersectsPositiveXAxis(int sx, int sy, Edge e) {
        return !(e.u.x == sx && e.u.y == sy) && !(e.v.x == sx && e.v.y == sy) &&
                 e.u.angle >= Math.PI && e.v.angle < Math.PI;
    }

    private final boolean linesIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        int line1dx = x2 - x1;
        int line1dy = y2 - y1;
        int cross1 = (x3-x1)*line1dy - (y3-y1)*line1dx;
        int cross2 = (x4-x1)*line1dy - (y4-y1)*line1dx;

        int line2dx = x4 - x3;
        int line2dy = y4 - y3;
        int cross3 = (x1-x3)*line2dy - (y1-y3)*line2dx;
        int cross4 = (x2-x3)*line2dy - (y2-y3)*line2dx;

        if (cross1 != 0 && cross2 != 0 && cross3 != 0 && cross4 != 0) {
            return ((cross1 > 0) != (cross2 > 0)) && ((cross3 > 0) != (cross4 > 0));
        }

        // There exists a cross product that is 0. One of the degenerate cases.
        // Not possible: (x1 == x3 && y1 == y3) or (x1 == x4 && y1 == y4)
        if (x2 == x3 && y2 == y3) {
            int dx1 = x1-x2;
            int dy1 = y1-y2;
            int dx2 = x4-x2;
            int dy2 = y4-y2;
            int dx3 = x1-x4;
            int dy3 = y1-y4;
            return (dx1*dx2 + dy1*dy2 > 0) && (dx1*dx3 + dy1*dy3 > 0) && (dx1*dy2 == dx2*dy1);
        } else if (x2 == x4 && y2 == y4) {
            int dx1 = x1-x2;
            int dy1 = y1-y2;
            int dx2 = x3-x2;
            int dy2 = y3-y2;
            int dx3 = x1-x3;
            int dy3 = y1-y3;
            return (dx1*dx2 + dy1*dy2 > 0) && (dx1*dx3 + dy1*dy3 > 0) && (dx1*dy2 == dx2*dy1);
        } else {
            // No equalities whatsoever.
            // We consider this case an intersection if they intersect.

            int prod1 = cross1*cross2;
            int prod2 = cross3*cross4;

            if (prod1 == 0 && prod2 == 0) {
                // All four points collinear.
                int minX1; int minY1; int maxX1; int maxY1;
                int minX2; int minY2; int maxX2; int maxY2;
                
                if (x1 < x2) {minX1 = x1; maxX1 = x2;}
                else {minX1 = x2; maxX1 = x1;}

                if (y1 < y2) {minY1 = y1; maxY1 = y2;}
                else {minY1 = y2; maxY1 = y1;}

                if (x3 < x4) {minX2 = x3; maxX2 = x4;}
                else {minX2 = x4; maxX2 = x3;}

                if (y3 < y4) {minY2 = y3; maxY2 = y4;}
                else {minY2 = y4; maxY2 = y3;}

                return !(maxX1 < minX2 || maxY1 < minY2 || maxX2 < minX1 || maxY2 < minY1);
            }

            return (prod1 <= 0 && prod2 <= 0);
        }
    }


    public void drawLines(GridLineSet gridLineSet, GridPointSet gridPointSet) {
        for (int i=0; i<vertices.length; ++i) {
            Vertex v = vertices[i];
            gridPointSet.addPoint(v.x, v.y, Color.YELLOW);
        }

        Edge[] edges = edgeHeap.getEdgeList();
        for (int i=0; i<edges.length; ++i) {
            Edge e = edges[i];
            gridLineSet.addLine(e.u.x, e.u.y, e.v.x, e.v.y, Color.RED);
        }
    }

    public void snapshotHeap(GridLineSet gridLineSet) {
        Edge[] edges = edgeHeap.getEdgeList();
        for (int i=0; i<edgeHeap.size(); ++i) {
            Edge e = edges[i];
            gridLineSet.addLine(e.u.x, e.u.y, e.v.x, e.v.y, Color.RED);
        }
    }
}
