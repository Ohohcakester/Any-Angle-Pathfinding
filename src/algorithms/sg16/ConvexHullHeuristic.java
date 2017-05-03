package algorithms.sg16;

import java.awt.Color;
import java.util.ArrayList;
import algorithms.datatypes.SnapshotItem;

public class ConvexHullHeuristic {

    // All edge positions are stored relative to (ex, ey);
    private final int[] edgex1;
    private final int[] edgey1;
    private final int[] edgex2;
    private final int[] edgey2;
    private final int nEdges;

    private final int ex;
    private final int ey;

    public ConvexHullHeuristic(SG16VisibilityGraph.ConvexHull[] convexHulls, int nHulls, int ex, int ey) {
        this.ex = ex;
        this.ey = ey;

        this.edgex1 = new int[nHulls];
        this.edgey1 = new int[nHulls];
        this.edgex2 = new int[nHulls];
        this.edgey2 = new int[nHulls];
        this.nEdges = nHulls;

        {
            int currIndex = 0;
            for (int hi=0; hi<nHulls; ++hi) {
                SG16VisibilityGraph.ConvexHull hull = convexHulls[hi];

                {
                    boolean isOnVector = false;
                    int prevdx = hull.xVertices[hull.size-1] - ex;
                    int prevdy = hull.yVertices[hull.size-1] - ey;
                    for (int j=0; j<hull.size; ++j) {
                        int currdx = hull.xVertices[j] - ex;
                        int currdy = hull.yVertices[j] - ey;
                        int crossProd = prevdx*currdy - prevdy*currdx;
                        int dotProd = prevdx*currdx + prevdy*currdy;
                        if (crossProd == 0 && dotProd < 0) {
                            isOnVector = true;

                            edgex1[currIndex] = prevdx;
                            edgey1[currIndex] = prevdy;
                            edgex2[currIndex] = currdx;
                            edgey2[currIndex] = currdy;
                            ++currIndex;
                            break;
                        }

                        prevdx = currdx;
                        prevdy = currdy;
                    }
                    if (isOnVector) continue;
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
                    int dx = hull.xVertices[j] - ex;
                    int dy = hull.yVertices[j] - ey;
                    if (dx == 0 && dy == 0) continue;

                    int dist = dx*dx*dy*dy;
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

                edgex1[currIndex] = mindx;
                edgey1[currIndex] = mindy;
                edgex2[currIndex] = maxdx;
                edgey2[currIndex] = maxdy;
                ++currIndex;
            }
        }
    }

    public final double heuristic(int px, int py) {
        px -= ex;
        py -= ey;
        if (px == 0 && py == 0) return 0;

        int maxDistanceLeft = Integer.MIN_VALUE;
        int closestLeftX = 0;
        int closestLeftY = 0;

        int maxDistanceRight = Integer.MIN_VALUE;
        int closestRightX = 0;
        int closestRightY = 0;

        for (int i=0; i<nEdges; ++i) {
            if (intersectsEdge(px, py, i)) {
                int leftX = edgex2[i];
                int leftY = edgey2[i];
                int leftDistance = Math.abs(leftX*py - leftY*px);
                if (leftDistance > maxDistanceLeft) {
                    closestLeftX = leftX;
                    closestLeftY = leftY;
                    maxDistanceLeft = leftDistance;
                }

                int rightX = edgex1[i];
                int rightY = edgey1[i];
                int rightDistance = Math.abs(rightX*py - rightY*px);
                if (rightDistance > maxDistanceRight) {
                    closestRightX = rightX;
                    closestRightY = rightY;
                    maxDistanceRight = rightDistance;
                }
            }
        }

        return Math.min(triangleDistance(px, py, closestLeftX, closestLeftY),
                        triangleDistance(px, py, closestRightX, closestRightY));
    }

    private final double triangleDistance(int px, int py, int vx, int vy) {
        int dx = px-vx;
        int dy = py-vy;
        return Math.sqrt(dx*dx+dy*dy) + Math.sqrt(vx*vx+vy*vy);
    }

    private final boolean intersectsEdge(int px, int py, int edgeIndex) {
        return linesIntersect(0,0,px,py,edgex1[edgeIndex],edgey1[edgeIndex],edgex2[edgeIndex],edgey2[edgeIndex]);
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


    public final ArrayList<SnapshotItem> snapshotLines(int px, int py) {
        ArrayList<SnapshotItem> snapshotItemList = new ArrayList<>();
        
        {
            Integer[] path = new Integer[] {ex,ey,px,py};

            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.ORANGE);
            snapshotItemList.add(snapshotItem);
        }

        px -= ex;
        py -= ey;

        int maxDistanceLeft = Integer.MIN_VALUE;
        int closestLeftX = 0;
        int closestLeftY = 0;

        int maxDistanceRight = Integer.MIN_VALUE;
        int closestRightX = 0;
        int closestRightY = 0;

        for (int i=0; i<nEdges; ++i) {
            boolean intersects = intersectsEdge(px, py, i);

            Color colour = intersects ? Color.BLUE : Color.CYAN;
            Integer[] path = new Integer[] {ex+edgex1[i], ey+edgey1[i], ex+edgex2[i], ey+edgey2[i]};

            SnapshotItem snapshotItem = SnapshotItem.generate(path, colour);
            snapshotItemList.add(snapshotItem);

            if (intersects) {
                int leftX = edgex2[i];
                int leftY = edgey2[i];
                int leftDistance = Math.abs(leftX*py - leftY*px);
                if (leftDistance > maxDistanceLeft) {
                    closestLeftX = leftX;
                    closestLeftY = leftY;
                    maxDistanceLeft = leftDistance;
                }

                int rightX = edgex1[i];
                int rightY = edgey1[i];
                int rightDistance = Math.abs(rightX*py - rightY*px);
                if (rightDistance > maxDistanceRight) {
                    closestRightX = rightX;
                    closestRightY = rightY;
                    maxDistanceRight = rightDistance;
                }
            }
        }

        {
            Integer[] path = new Integer[] {ex,ey,closestLeftX+ex,closestLeftY+ey};
            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.ORANGE);
            snapshotItemList.add(snapshotItem);
        }
        {
            Integer[] path = new Integer[] {ex,ey,closestRightX+ex,closestRightY+ey};
            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.ORANGE);
            snapshotItemList.add(snapshotItem);
        }
        {
            Integer[] path = new Integer[] {px+ex,py+ey,closestLeftX+ex,closestLeftY+ey};
            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.ORANGE);
            snapshotItemList.add(snapshotItem);
        }
        {
            Integer[] path = new Integer[] {px+ex,py+ey,closestRightX+ex,closestRightY+ey};
            SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.ORANGE);
            snapshotItemList.add(snapshotItem);
        }

        return snapshotItemList;
    }
}
