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
            Color colour = (k == 0) ? Color.CYAN : Color.GREEN;
            Edge e = edges[k];
            snapshot.add(SnapshotItem.generate(new Integer[]{e.u.x, e.u.y, e.v.x, e.v.y}, colour));
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
        public Edge edge1; // edge1 goes forward
        public Edge edge2; // edge2 goes backward

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
        private static final double EPSILON = 0.00000001;

        public Vertex u; // u goes backward
        public Vertex v; // v goes forward
        public int heapIndex;
        //public double distance;

        public Edge(RPSScanner.Vertex u, RPSScanner.Vertex v) {
            this.u = u;
            this.v = v;
        }

        // Condition: e2 comes after e1 in rotational plane sweep order.
        private static final boolean isLessThan(Edge e1, Edge e2, int sx, int sy) {
            // compare u and e.v
            if (e1.u.x == e2.v.x && e1.u.y == e2.v.y) {
                // have to do flipping
                double m1x = e1.midX();
                double m1y = e1.midY();
                double e1_distance_sx_sy_m1x_m1y = e1.distance(sx, sy, m1x, m1y);
                double diff1 = e1_distance_sx_sy_m1x_m1y - e2.distance(sx, sy, m1x, m1y);

                double m2x = e2.midX();
                double m2y = e2.midY();
                double diff2 = e1.distance(sx, sy, m2x, m2y) - e2.distance(sx, sy, m2x, m2y);

                if (diff1*diff2 < 0) {
                    // opposite signs
                    // need to flip m1 to the opposite side.
                    // u1 == v2
                    int du1x = e1.u.x - sx;
                    int du1y = e1.u.y - sy;
                    double dm1x = m1x - sx;
                    double dm1y = m1y - sy;

                    // m1r is the reflected midpoint about the line s,u1
                    // m1r = m1 - 2*(dm1 - <dm1,du1>/<du1,du1> du1)
                    double dotprodRatio = (dm1x*du1x + dm1y*du1y) / (du1x*du1x + du1y*du1y);
                    double m1rx = m1x - 2*(dm1x - dotprodRatio*du1x);
                    double m1ry = m1y - 2*(dm1y - dotprodRatio*du1y);
                    return e1_distance_sx_sy_m1x_m1y < e2.distance(sx, sy, m1rx, m1ry);
                }
                return diff1 < 0;
            } else {
                // Find a ray (sx,sy)->(tx,ty) that crosses both line segments
                int du1x = e1.u.x - sx;
                int du1y = e1.u.y - sy;
                int du2x = e2.u.x - sx;
                int du2y = e2.u.y - sy;
                int crossProd = du1x*du2y - du1y*du2x;

                double tx, ty;
                if (crossProd >= 0) {
                    tx = (double)(e1.u.x + e2.v.x)/2;
                    ty = (double)(e1.u.y + e2.v.y)/2;
                } else {
                    tx = (double)(e2.u.x + e2.v.x)/2;
                    ty = (double)(e2.u.y + e2.v.y)/2;
                }
                //System.out.println("Ray -> " + tx + ", " + ty);

                return e1.distance(sx, sy, tx, ty) < e2.distance(sx, sy, tx, ty);
            }
        }

        public final boolean isLessThan(Edge e, int sx, int sy) {
            if ((u.x == sx && u.y == sy) || (v.x == sx && v.y == sy)) return true;
            if ((e.u.x == sx && e.u.y == sy) || (e.v.x == sx && e.v.y == sy)) return false;

            int dv1x = v.x - sx;
            int dv1y = v.y - sy;
            int dv2x = e.v.x - sx;
            int dv2y = e.v.y - sy;

            int crossProd = dv1x*dv2y - dv1y*dv2x;
            if (crossProd >= 0) {
                // this -> e
                return isLessThan(this, e, sx, sy);
            } else {
                // e -> this
                return !isLessThan(e, this, sx, sy);
            }
        }

        private final double midX() {
            return (double)(u.x + v.x)/2;
        }

        private final double midY() {
            return (double)(u.y + v.y)/2;
        }

        private final double distance(int sx, int sy, double tx, double ty) {
            double dx = tx - sx;
            double dy = ty - sy;
            int dex = v.x - u.x;
            int dey = v.y - u.y;
            int dux = u.x - sx;
            int duy = u.y - sy;

            double denom = dey*dx - dex*dy;
            if (Math.abs(denom) < EPSILON) {
                // collinear (degenerate case)
                int dvx = v.x - sx;
                int dvy = v.y - sy;
                // (sx,sy) lies between u and v
                if (dux*dvx + duy*dvy <= 0) return 0;
                return Math.min(dux*dux + duy*duy, dvx*dvx + dvy*dvy);
            } else {
                // not collinear
                int numer = dux*dey - duy*dex;
                return (dx*dx+dy*dy)*numer*numer/(denom*denom);
            }
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

    private final void initialiseScan(int sx, int sy) {
        
        // Compute angles
        for (int i=0; i<vertices.length; ++i) {
            Vertex v = vertices[i];
            if (v.x != sx || v.y != sy) {
                v.angle = Math.atan2(v.y-sy, v.x-sx);
                if (v.angle < 0) v.angle += 2*Math.PI;
            } else {
                v.angle = -1;
                Vertex n1 = v.edge1.v;
                Vertex n2 = v.edge2.u;
                if (graph.isOuterCorner(n1.x, n1.y)) addNeighbour(n1.x, n1.y);
                if (graph.isOuterCorner(n2.x, n2.y)) addNeighbour(n2.x, n2.y);
            }
        }
        sortVertices(sx, sy);

        edgeHeap.clear();
        Edge[] edges = edgeHeap.getEdgeList();
        // Note: iterating through the edges like this is a very dangerous operation.
        // That's because the edges array changes as you insert the edges into the heap.
        // Reason why it works: When we swap two edges when inserting, both edges have already been checked.
        //                      That's because we only swap with edges in the heap, which have a lower index than i.
        for (int i=0; i<edges.length; ++i) {
            Edge edge = edges[i];
            if (intersectsPositiveXAxis(sx, sy, edge)) {
                edgeHeap.insert(edge, sx, sy);
            }
        }
    }

    public final void computeAllVisibleSuccessors(int sx, int sy) {
        clearNeighbours();
        if (vertices.length == 0) return;
        if (!graph.isUnblockedCoordinate(sx, sy)) return;

        initialiseScan(sx, sy);

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
                    maybeAddEdge(sx, sy, v, v.edge1);
                    maybeAddEdge(sx, sy, v, v.edge2);
                }

                // Add all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];
                    //saveSnapshot(sx, sy, v); // UNCOMMENT FOR TRACING

                    Edge edge = edgeHeap.getMin();
                    if (!linesIntersect(sx, sy, v.x, v.y, edge.u.x, edge.u.y, edge.v.x, edge.v.y)) {
                        if (graph.isOuterCorner(v.x, v.y)) {
                            addNeighbour(v.x, v.y);
                        }
                    }
                }

                // Delete all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];
                    maybeDeleteEdge(sx, sy, v, v.edge1);
                    maybeDeleteEdge(sx, sy, v, v.edge2);
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }
    }

    public final void computeAllVisibleTautSuccessors(int sx, int sy) {
        clearNeighbours();
        if (vertices.length == 0) return;
        if (!graph.isUnblockedCoordinate(sx, sy)) return;

        initialiseScan(sx, sy);


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
                    maybeAddEdge(sx, sy, v, v.edge1);
                    maybeAddEdge(sx, sy, v, v.edge2);
                }

                // Add all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];
                    if (!isTautSuccessor(sx, sy, v.x, v.y)) continue;
                    //saveSnapshot(sx, sy, v); // UNCOMMENT FOR TRACING

                    Edge edge = edgeHeap.getMin();
                    if (!linesIntersect(sx, sy, v.x, v.y, edge.u.x, edge.u.y, edge.v.x, edge.v.y)) {
                        addNeighbour(v.x, v.y);
                    }
                }

                // Delete all
                for (int j=0; j<vertexQueueSize; ++j) {
                    Vertex v = vertexQueue[j];
                    maybeDeleteEdge(sx, sy, v, v.edge1);
                    maybeDeleteEdge(sx, sy, v, v.edge2);
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }
    }


    public final void computeAllVisibleTwoWayTautSuccessors(int sx, int sy) {
        clearNeighbours();
        if (vertices.length == 0) return;
        if (!graph.isUnblockedCoordinate(sx, sy)) return;

        initialiseScan(sx, sy);

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
                    maybeAddEdge(sx, sy, v, v.edge1);
                    maybeAddEdge(sx, sy, v, v.edge2);
                }

                // Add all (if it doesn't fall within the interval (excludeStart, excludeEnd) )
                if (currentAngle <= excludeStart || excludeEnd <= currentAngle) {
                    for (int j=0; j<vertexQueueSize; ++j) {
                        Vertex v = vertexQueue[j];
                        if (!isTautSuccessor(sx, sy, v.x, v.y)) continue;
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
                    maybeDeleteEdge(sx, sy, v, v.edge1);
                    maybeDeleteEdge(sx, sy, v, v.edge2);
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }
    }

    // Assumptions:
    // 1. (sx, sy) != (nx, ny)
    // 2. (sx, sy) has line of sight to (nx, ny)
    // 3. (nx, ny) is an outer corner tile.
    private final boolean isTautSuccessor(int sx, int sy, int nx, int ny) {
        int dx = nx - sx;
        int dy = ny - sy;
        if (dx == 0 || dy == 0) return graph.isOuterCorner(nx, ny);

        if (dx > 0) {
            if (dy > 0) {
                return !graph.bottomLeftOfBlockedTile(nx, ny);
            } else { // (dy < 0)
                return !graph.topLeftOfBlockedTile(nx, ny);
            }
        } else { // (dx < 0)
            if (dy > 0) {
                return !graph.bottomRightOfBlockedTile(nx, ny);
            } else { // (dy < 0)
                return !graph.topRightOfBlockedTile(nx, ny);
            }
        }
    }

    private final void sortVertices(int sx, int sy) {
        Arrays.sort(vertices, (a,b) -> Double.compare(a.angle, b.angle));
    }

    private final boolean isSameAngle(int sx, int sy, Vertex u, Vertex v) {
        int dx1 = u.x - sx;
        int dy1 = u.y - sy;
        int dx2 = v.x - sx;
        int dy2 = v.y - sy;

        return dx1*dx2 + dy1*dy2 > 0 && dx1*dy2 == dx2*dy1;
    }

    private final void maybeAddEdge(int sx, int sy, Vertex curr, Edge edge) {
        if (curr != edge.v) return;

        int dux = edge.u.x - sx;
        int duy = edge.u.y - sy;
        int dvx = edge.v.x - sx;
        int dvy = edge.v.y - sy;

        int crossProd = dux*dvy - dvx*duy;
        if (crossProd < 0) {
            // Add/delete
            edgeHeap.insert(edge, sx, sy);
        } else if (crossProd == 0) {
            int dotProd = dux*dvx + duy*dvy;
            if (dotProd > 0) {
                // Don't add
            } else if (dotProd < 0) {
                // Add/delete
                edgeHeap.insert(edge, sx, sy);
            } else { // dotProd == 0
                // Add edge and neighbour
                edgeHeap.insert(edge, sx, sy);
                edgeHeap.insert(edge.u.edge2, sx, sy);
            }
        }
    }

    private final void maybeDeleteEdge(int sx, int sy, Vertex curr, Edge edge) {
        if (curr != edge.u) return;

        int dux = edge.u.x - sx;
        int duy = edge.u.y - sy;
        int dvx = edge.v.x - sx;
        int dvy = edge.v.y - sy;

        int crossProd = dux*dvy - dvx*duy;
        if (crossProd < 0) {
            // Add/delete
            edgeHeap.delete(edge, sx, sy);
        } else if (crossProd == 0) {
            int dotProd = dux*dvx + duy*dvy;
            if (dotProd > 0) {
                // Don't add
            } else if (dotProd < 0) {
                // Add/delete
                edgeHeap.delete(edge, sx, sy);
            } else { // dotProd == 0
                // Delete edge and neighbour
                edgeHeap.delete(edge, sx, sy);
                edgeHeap.delete(edge.v.edge1, sx, sy);
            }
        }
    }

    private final boolean intersectsPositiveXAxis(int sx, int sy, Edge edge) {
        if (sx == edge.u.x && sy == edge.u.y) {
            return anglesIntersectPositiveXAxis(sx, sy, edge.u.edge2.u, edge.v);
        } else if (sx == edge.v.x && sy == edge.v.y) {
            return anglesIntersectPositiveXAxis(sx, sy, edge.u, edge.v.edge1.v);
        } else  {
            return intersectsPositiveXAxis(sx, sy, edge.u, edge.v);
        }
    }

    private final boolean intersectsPositiveXAxis(int sx, int sy, Vertex edgeU, Vertex edgeV) {
        if (anglesIntersectPositiveXAxis(sx, sy, edgeU, edgeV)) {
            int dux = edgeU.x - sx;
            int duy = edgeU.y - sy;
            int dvx = edgeV.x - sx;
            int dvy = edgeV.y - sy;

            int crossProd = dux*dvy - dvx*duy;
            if (crossProd < 0) {
                return true;
            } else if (crossProd == 0) {
                int dotProd = dux*dvx + duy*dvy;
                if (dotProd > 0) {
                    // Don't add
                } else if (dotProd < 0) {
                    return true;
                } else { // (dotProd == 0)
                    // Never happens.
                    throw new UnsupportedOperationException("This should not happen");
                }
            }
        }
        return false;
    }

    private final boolean anglesIntersectPositiveXAxis(int sx, int sy, Vertex edgeU, Vertex edgeV) {
        return edgeU.angle <= edgeV.angle && !isSameAngle(sx, sy, edgeU, edgeV);
    }

    private final boolean linesIntersect(int sx, int sy, int tx, int ty, int ux, int uy, int vx, int vy) {
        int line1dx = tx - sx;
        int line1dy = ty - sy;
        int cross1 = (ux-sx)*line1dy - (uy-sy)*line1dx;
        int cross2 = (vx-sx)*line1dy - (vy-sy)*line1dx;

        int line2dx = vx - ux;
        int line2dy = vy - uy;
        int cross3 = (sx-ux)*line2dy - (sy-uy)*line2dx;
        int cross4 = (tx-ux)*line2dy - (ty-uy)*line2dx;

        if (cross1 != 0 && cross2 != 0 && cross3 != 0 && cross4 != 0) {
            return ((cross1 > 0) != (cross2 > 0)) && ((cross3 > 0) != (cross4 > 0));
        }

        // There exists a cross product that is 0. One of the degenerate cases.
        // Not possible: (sx == ux && sy == uy) or (sx == vx && sy == vy)
        if (tx == ux && ty == uy) {
            if (sx == vx && sy == vy) return true;
            int dx1 = sx-tx;
            int dy1 = sy-ty;
            int dx2 = vx-tx;
            int dy2 = vy-ty;
            int dx3 = sx-vx;
            int dy3 = sy-vy;
            return (dx1*dx2 + dy1*dy2 > 0) && (dx1*dx3 + dy1*dy3 > 0) && (dx1*dy2 == dx2*dy1);
        } else if (tx == vx && ty == vy) {
            if (sx == ux && sy == uy) return true;
            int dx1 = sx-tx;
            int dy1 = sy-ty;
            int dx2 = ux-tx;
            int dy2 = uy-ty;
            int dx3 = sx-ux;
            int dy3 = sy-uy;
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
                
                if (sx < tx) {minX1 = sx; maxX1 = tx;}
                else {minX1 = tx; maxX1 = sx;}

                if (sy < ty) {minY1 = sy; maxY1 = ty;}
                else {minY1 = ty; maxY1 = sy;}

                if (ux < vx) {minX2 = ux; maxX2 = vx;}
                else {minX2 = vx; maxX2 = ux;}

                if (uy < vy) {minY2 = uy; maxY2 = vy;}
                else {minY2 = vy; maxY2 = uy;}

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
            Color colour = (i == 0) ? Color.ORANGE : Color.RED;
            Edge e = edges[i];
            gridLineSet.addLine(e.u.x, e.u.y, e.v.x, e.v.y, colour);
        }
    }
}
