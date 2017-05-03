package algorithms.rotationalplanesweep;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import draw.GridLineSet;
import draw.GridPointSet;

import algorithms.sg16.SG16VisibilityGraph;
import algorithms.datatypes.SnapshotItem;
import grid.GridGraph;


public class ConvexHullRPSScanner {

    public static ArrayList<List<SnapshotItem>> snapshotList = new ArrayList<>();

    private final void saveSnapshot(int sx, int sy, RPSScanner.Vertex v) {
        ArrayList<SnapshotItem> snapshot = new ArrayList<>();

        snapshot.add(SnapshotItem.generate(new Integer[]{sx, sy, v.x, v.y}, Color.RED));

        // Snapshot current state of heap
        int heapSize = edgeHeap.size();
        RPSScanner.Edge[] edges = edgeHeap.getEdgeList();
        for (int k=0; k<heapSize; ++k) {
            Color colour = (k == 0) ? Color.CYAN : Color.GREEN;
            RPSScanner.Edge e = edges[k];
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

    private final RPSScanner.Vertex[] verticesUnsorted;
    private final RPSScanner.Vertex[] vertices;
    private int nVertices;
    private final RPSScanner.Edge[] edges;
    private final RPSEdgeHeap edgeHeap;
    private final GridGraph graph;
    private final int FOCUSED_SEARCH_RANGE;

    private final SG16VisibilityGraph.ConvexHull[] convexHulls;
    private final int nHulls;

    private final boolean[] obstacleIsMarked;

    public ConvexHullRPSScanner(GridGraph graph, SG16VisibilityGraph.ConvexHull[] convexHulls, int nHulls) {
        successorsX = new int[11];
        successorsY = new int[11];
        this.nHulls = nHulls;
        this.convexHulls = convexHulls;
        this.vertices = new RPSScanner.Vertex[nHulls*2];
        this.verticesUnsorted = new RPSScanner.Vertex[nHulls*2];
        this.edges = new RPSScanner.Edge[nHulls];
        nSuccessors = 0;
        this.graph = graph;

        setupInitialVertices();
        this.edgeHeap = new RPSEdgeHeap(edges);

        int maxObstacleIndex = -1;
        for (int i=0;i<nHulls;++i) {
            maxObstacleIndex = Math.max(convexHulls[i].obstacleIndex, maxObstacleIndex);
        }
        obstacleIsMarked = new boolean[maxObstacleIndex+1];

        int focusedSearchRadius = (graph.sizeX+graph.sizeY)/20;
        FOCUSED_SEARCH_RANGE = focusedSearchRadius*focusedSearchRadius;
    }

    private void setupInitialVertices() {
        for (int i=0; i<edges.length; ++i) {
            RPSScanner.Vertex u = new RPSScanner.Vertex(0,0);
            RPSScanner.Vertex v = new RPSScanner.Vertex(0,0);
            RPSScanner.Edge e = new RPSScanner.Edge(u, v);
            u.edge1 = e;
            v.edge2 = e;

            vertices[2*i] = u;
            vertices[2*i+1] = v;
            verticesUnsorted[2*i] = u;
            verticesUnsorted[2*i+1] = v;
            edges[i] = e;
        }
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

    /**
     * returns number of vertices.
     */
    private final void setupVerticesAndEdges(int sx, int sy, int ex, int ey) {

        // Use vertices array for temporary storage.
        System.arraycopy(verticesUnsorted, 0, vertices, 0, verticesUnsorted.length);

        for (int oi=0; oi<obstacleIsMarked.length; ++oi) {
            obstacleIsMarked[oi] = false;
        }

        int currIndex = 0;
        for (int hi=0; hi<nHulls; ++hi) {
            SG16VisibilityGraph.ConvexHull hull = convexHulls[hi];
            int oi = hull.obstacleIndex;

            // Special case: Check whether you are on a vector
            {
                boolean isOnVector = false;
                int prevdx = hull.xVertices[hull.size-1] - sx;
                int prevdy = hull.yVertices[hull.size-1] - sy;
                for (int j=0; j<hull.size; ++j) {
                    int currdx = hull.xVertices[j] - sx;
                    int currdy = hull.yVertices[j] - sy;
                    int crossProd = prevdx*currdy - prevdy*currdx;
                    int dotProd = prevdx*currdx + prevdy*currdy;
                    //if (crossProd == 0 && dotProd < 0) {
                    if (crossProd == 0 && dotProd <= 0) {
                        isOnVector = true;
                        RPSScanner.Vertex minVertex = vertices[currIndex++];
                        RPSScanner.Vertex maxVertex = vertices[currIndex++];

                        if (dotProd < 0) {
                            minVertex.x = prevdx + sx;
                            minVertex.y = prevdy + sy;
                            maxVertex.x = currdx + sx;
                            maxVertex.y = currdy + sy;

                        } else { // dotProd == 0
                            if (currdx == 0 && currdy == 0) {
                                // (sx, sy) == curr
                                // pick prev and next
                                int next = (j+1)%hull.size;
                                int nextX = hull.xVertices[next];
                                int nextY = hull.yVertices[next];

                                minVertex.x = prevdx + sx;
                                minVertex.y = prevdy + sy;
                                maxVertex.x = nextX;
                                maxVertex.y = nextY;
                            } else {
                                // (sx, sy) == prev
                                // pick curr and prev.prev
                                int prevprev = (j+hull.size-2)%hull.size;
                                int prevprevX = hull.xVertices[prevprev];
                                int prevprevY = hull.yVertices[prevprev];

                                minVertex.x = prevprevX;
                                minVertex.y = prevprevY;
                                maxVertex.x = currdx + sx;
                                maxVertex.y = currdy + sy;
                            }
                        }
                        break;
                    }

                    prevdx = currdx;
                    prevdy = currdy;
                }
                if (isOnVector) {
                    obstacleIsMarked[oi] = true;
                    continue;
                }
            }

            // mindx/mindx/mindist: point with minimum angle.
            // maxdx/maxdx/maxdist: point with maximum angle.
            // Initial values: crossProd will always be 0, so we will tiebreak by distance.
            // Because initial distance is MAX_INT, the initial values will be replaced immediately.
            int mindx = 0;
            int mindy = 0;
            int mindist = Integer.MAX_VALUE;

            int maxdx = 0;
            int maxdy = 0;
            int maxdist = Integer.MAX_VALUE;

            // We use cross products, due to periodic boundary conditions.
            for (int j=0; j<hull.size; ++j) {
                int dx = hull.xVertices[j] - sx;
                int dy = hull.yVertices[j] - sy;
                if (dx == 0 && dy == 0) continue;

                int dist = dx*dx+dy*dy;
                int crossProdMin = dx*mindy - dy*mindx;
                int crossProdMax = dx*maxdy - dy*maxdx;
                // tiebreak by choosing the nearer one.
                if (crossProdMin < 0 || (crossProdMin == 0 && (dist < mindist))) {
                    mindx = dx;
                    mindy = dy;
                    mindist = dist;
                }

                // tiebreak by choosing the nearer one.
                if (crossProdMax > 0 || (crossProdMax == 0 && (dist < maxdist))) {
                    maxdx = dx;
                    maxdy = dy;
                    maxdist = dist;
                }
            }
            obstacleIsMarked[oi] = obstacleIsMarked[oi] || onLineToGoal(0, 0, ex-sx, ey-sy, mindx, mindy, maxdx, maxdy);

            RPSScanner.Vertex minVertex = vertices[currIndex++];
            RPSScanner.Vertex maxVertex = vertices[currIndex++];
            minVertex.x = mindx + sx;
            minVertex.y = mindy + sy;
            maxVertex.x = maxdx + sx;
            maxVertex.y = maxdy + sy;
        }

        int backIndex = vertices.length;
        currIndex = 0;
        // Assumption: vertices has the same sort order as convexHulls.
        // Endpoints of convexHulls[i] is vertices[2*i] and vertices[2*i+1]
        for (int hi=0; hi<nHulls; ++hi) {
            SG16VisibilityGraph.ConvexHull hull = convexHulls[hi];
            int index = hi*2;

            int mindx = vertices[index].x - sx;
            int mindy = vertices[index].y - sy;
            int maxdx = vertices[index+1].x - sx;
            int maxdy = vertices[index+1].y - sy;
            int minDist = mindx*mindx + mindy*mindy;
            int maxDist = maxdx*maxdx + maxdy*maxdy;

            // Note: we can't set the x,y coordinates because it will affect the vertices array too.
            // So all we can do is rearrange the vertex pointers in verticesUnsorted.
            if (minDist > FOCUSED_SEARCH_RANGE && maxDist > FOCUSED_SEARCH_RANGE && !obstacleIsMarked[hull.obstacleIndex]) {
                // Exclude vertex
                verticesUnsorted[--backIndex] = vertices[index+1];
                verticesUnsorted[--backIndex] = vertices[index];
            } else {
                // Include vertex
                verticesUnsorted[currIndex++] = vertices[index];
                verticesUnsorted[currIndex++] = vertices[index+1];
            }
        }
        if (currIndex != backIndex) throw new UnsupportedOperationException("Counting Error!");

        nVertices = currIndex;
    }

    private final void initialiseScan(int sx, int sy) {
        System.arraycopy(verticesUnsorted, 0, vertices, 0, nVertices);
        
        // Compute angles
        for (int i=0; i<nVertices; ++i) {
            RPSScanner.Vertex v = vertices[i];
            if (v.x != sx || v.y != sy) {
                v.angle = Math.atan2(v.y-sy, v.x-sx);
                if (v.angle < 0) v.angle += 2*Math.PI;
            } else {
                v.angle = -1;
                /*RPSScanner.Vertex n1 = v.edge1.v;
                RPSScanner.Vertex n2 = v.edge2.u;
                if (graph.isOuterCorner(n1.x, n1.y)) addNeighbour(n1.x, n1.y);
                if (graph.isOuterCorner(n2.x, n2.y)) addNeighbour(n2.x, n2.y);*/
            }
        }
        sortVertices(sx, sy);

        edgeHeap.clear();
        for (int i=0; i<nVertices; i+=2) {
            RPSScanner.Edge edge = verticesUnsorted[i].edge1;
            if (intersectsPositiveXAxis(sx, sy, edge)) {
                edgeHeap.insert(edge, sx, sy);
            }
        }
    }

    public final void computeAllVisibleSuccessors(int sx, int sy, int ex, int ey) {
        clearNeighbours();
        if (nHulls == 0) return;
        if (!graph.isUnblockedCoordinate(sx, sy)) return;

        setupVerticesAndEdges(sx, sy, ex, ey);
        initialiseScan(sx, sy);

        // This queue is used to enforce the order:
        // INSERT TO EDGEHEAP -> ADD AS NEIGHBOUR -> DELETE FROM EDGEHEAP
        //     for all vertices with the same angle from (sx,sy).
        RPSScanner.Vertex[] vertexQueue = new RPSScanner.Vertex[11];
        int vertexQueueSize = 0;

        int i = 0;
        // Skip vertex if it is (sx,sy).
        while (vertices[i].x == sx && vertices[i].y == sy) ++i;

        for (; i<nVertices; ++i) {
            if (vertexQueueSize >= vertexQueue.length) {
                vertexQueue = Arrays.copyOf(vertexQueue, vertexQueue.length*2);
            }
            vertexQueue[vertexQueueSize++] = vertices[i];

            if (i+1 == vertices.length || !isSameAngle(sx, sy, vertices[i], vertices[i+1])) {
                // Clear queue

                // Insert all first
                for (int j=0; j<vertexQueueSize; ++j) {
                    RPSScanner.Vertex v = vertexQueue[j];
                    maybeAddEdge(sx, sy, v, v.edge1);
                    maybeAddEdge(sx, sy, v, v.edge2);
                }

                // Add all
                for (int j=0; j<vertexQueueSize; ++j) {
                    RPSScanner.Vertex v = vertexQueue[j];
                    //saveSnapshot(sx, sy, v); // UNCOMMENT FOR TRACING

                    RPSScanner.Edge edge = edgeHeap.getMin();
                    if (!linesIntersect(sx, sy, v.x, v.y, edge.u.x, edge.u.y, edge.v.x, edge.v.y)) {
                        addNeighbour(v.x, v.y);
                    }
                }

                // Delete all
                for (int j=0; j<vertexQueueSize; ++j) {
                    RPSScanner.Vertex v = vertexQueue[j];
                    maybeDeleteEdge(sx, sy, v, v.edge1);
                    maybeDeleteEdge(sx, sy, v, v.edge2);
                }

                // Clear queue
                vertexQueueSize = 0;
            }
        }
    }

    private final void sortVertices(int sx, int sy) {
        Arrays.sort(vertices, 0, nVertices, (a,b) -> Double.compare(a.angle, b.angle));
    }

    private final boolean isSameAngle(int sx, int sy, RPSScanner.Vertex u, RPSScanner.Vertex v) {
        int dx1 = u.x - sx;
        int dy1 = u.y - sy;
        int dx2 = v.x - sx;
        int dy2 = v.y - sy;

        return dx1*dx2 + dy1*dy2 > 0 && dx1*dy2 == dx2*dy1;
    }

    private final void maybeAddEdge(int sx, int sy, RPSScanner.Vertex curr, RPSScanner.Edge edge) {
        if (edge == null || curr != edge.v) return;

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
                //edgeHeap.insert(edge, sx, sy);
                //edgeHeap.insert(edge.u.edge2, sx, sy);
            }
        }
    }

    private final void maybeDeleteEdge(int sx, int sy, RPSScanner.Vertex curr, RPSScanner.Edge edge) {
        if (edge == null || curr != edge.u) return;

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
                //edgeHeap.delete(edge, sx, sy);
                //edgeHeap.delete(edge.v.edge1, sx, sy);
            }
        }
    }

    private final boolean intersectsPositiveXAxis(int sx, int sy, RPSScanner.Edge edge) {
        return intersectsPositiveXAxis(sx, sy, edge.u, edge.v);
    }

    private final boolean intersectsPositiveXAxis(int sx, int sy, RPSScanner.Vertex edgeU, RPSScanner.Vertex edgeV) {
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
                    //throw new UnsupportedOperationException("This should not happen");
                }
            }
        }
        return false;
    }

    private final boolean anglesIntersectPositiveXAxis(int sx, int sy, RPSScanner.Vertex edgeU, RPSScanner.Vertex edgeV) {
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
                return false;
                /*int minX1; int minY1; int maxX1; int maxY1;
                int minX2; int minY2; int maxX2; int maxY2;
                
                if (sx < tx) {minX1 = sx; maxX1 = tx;}
                else {minX1 = tx; maxX1 = sx;}

                if (sy < ty) {minY1 = sy; maxY1 = ty;}
                else {minY1 = ty; maxY1 = sy;}

                if (ux < vx) {minX2 = ux; maxX2 = vx;}
                else {minX2 = vx; maxX2 = ux;}

                if (uy < vy) {minY2 = uy; maxY2 = vy;}
                else {minY2 = vy; maxY2 = uy;}

                return !(maxX1 < minX2 || maxY1 < minY2 || maxX2 < minX1 || maxY2 < minY1);*/
            }

            return (prod1 <= 0 && prod2 <= 0);
        }
    }

    private final boolean onLineToGoal(int sx, int sy, int ex, int ey, int ux, int uy, int vx, int vy) {
        int line1dx = ex - sx;
        int line1dy = ey - sy;
        int cross1 = (ux-sx)*line1dy - (uy-sy)*line1dx;
        int cross2 = (vx-sx)*line1dy - (vy-sy)*line1dx;

        int line2dx = vx - ux;
        int line2dy = vy - uy;
        int cross3 = (sx-ux)*line2dy - (sy-uy)*line2dx;
        int cross4 = (ex-ux)*line2dy - (ey-uy)*line2dx;

        //return cross1*cross2 <= 0 && cross3*cross4 <= 0;
        return ((cross1<=0 && cross2 >=0) || (cross1>=0 && cross2<=0)) &&
               ((cross3<=0 && cross4 >=0) || (cross3>=0 && cross4<=0));
    }


    public void drawLines(GridLineSet gridLineSet, GridPointSet gridPointSet) {
        for (int i=0; i<vertices.length; ++i) {
            RPSScanner.Vertex v = vertices[i];
            gridPointSet.addPoint(v.x, v.y, Color.YELLOW);
        }

        RPSScanner.Edge[] edges = edgeHeap.getEdgeList();
        for (int i=0; i<edges.length; ++i) {
            RPSScanner.Edge e = edges[i];
            gridLineSet.addLine(e.u.x, e.u.y, e.v.x, e.v.y, Color.RED);
        }
    }

    public final ArrayList<SnapshotItem> snapshotLines() {
        ArrayList<SnapshotItem> snapshotItemList = new ArrayList<>();

        for (int i=0; i<nVertices; i+=2) {
            RPSScanner.Edge e = verticesUnsorted[i].edge1;
            Integer[] path = new Integer[] {e.u.x, e.u.y, e.v.x, e.v.y};

            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.CYAN);
            snapshotItemList.add(snapshotItem);
        }

        return snapshotItemList;
    }

    public final ArrayList<SnapshotItem> snapshotLinesAndSuccessors(int currX, int currY) {
        ArrayList<SnapshotItem> snapshotItemList = snapshotLines();
        
        for (int i=0; i<nSuccessors; ++i) {
            int succX = successorsX[i];
            int succY = successorsY[i];
            if (!graph.lineOfSight(currX, currY, succX, succY)) continue;
            Integer[] path = new Integer[] {currX, currY, succX, succY};

            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.MAGENTA);
            snapshotItemList.add(snapshotItem);
        }

        return snapshotItemList;
    }

    public void snapshotHeap(GridLineSet gridLineSet) {
        RPSScanner.Edge[] edges = edgeHeap.getEdgeList();
        for (int i=0; i<edgeHeap.size(); ++i) {
            Color colour = (i == 0) ? Color.ORANGE : Color.RED;
            RPSScanner.Edge e = edges[i];
            gridLineSet.addLine(e.u.x, e.u.y, e.v.x, e.v.y, colour);
        }
    }
}
