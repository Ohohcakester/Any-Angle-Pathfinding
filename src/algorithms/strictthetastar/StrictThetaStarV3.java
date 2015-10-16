package algorithms.strictthetastar;

import grid.GridGraph;
import algorithms.BasicThetaStar;
import algorithms.priorityqueue.ReusableIndirectHeap;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 * Comes from V2f.
 * V3: Lazy ver
 * @author Oh
 * 
 * Ideas:
 * Heuristic trap:
 *  - The heuristic value of the final node is 1.1f instead of 0.
 *  - A lot of inoptimality comes because the algorithm is too eager to relax
 *    the final vertex. The slightly higher heuristic encourages the algorithm
 *    to explore a little more first.
 *
 */
public class StrictThetaStarV3 extends BasicThetaStar {
    private int DEPTH_LIMIT = -1;
    private static final float BUFFER_VALUE = 0.36f; // 0.33f is enough.

    public StrictThetaStarV3(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static StrictThetaStarV3 noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        StrictThetaStarV3 algo = new StrictThetaStarV3(graph, sx, sy, ex, ey);
        algo.heuristicWeight = 0f;
        return algo;
    }
    
    public static StrictThetaStarV3 depthLimit(GridGraph graph, int sx, int sy, int ex, int ey, int depthLimit) {
        StrictThetaStarV3 algo = new StrictThetaStarV3(graph, sx, sy, ex, ey);
        algo.DEPTH_LIMIT = depthLimit;
        return algo;
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);

        pq = new ReusableIndirectHeap(totalSize);
        this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);
        
        initialise(start);
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            tryFixBufferValue(current);

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            int parentIndex = parent(current);
            if (parentIndex != -1) {
                int parX = toTwoDimX(parentIndex);
                int parY = toTwoDimY(parentIndex);

                if (!graph.lineOfSight(x,y,parX,parY)) {
                    findPath1Parent(current, x, y);
                }
            }
            
            if (current == finish || distance(current) == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            setVisited(current, true);
            

            tryRelaxNeighbour(current, x, y, x-1, y-1);
            tryRelaxNeighbour(current, x, y, x, y-1);
            tryRelaxNeighbour(current, x, y, x+1, y-1);
            
            tryRelaxNeighbour(current, x, y, x-1, y);
            tryRelaxNeighbour(current, x, y, x+1, y);
            
            tryRelaxNeighbour(current, x, y, x-1, y+1);
            tryRelaxNeighbour(current, x, y, x, y+1);
            tryRelaxNeighbour(current, x, y, x+1, y+1);

            maybeSaveSearchSnapshot();
        }
        
        maybePostSmooth();
    }
    
    protected float heuristic(int x, int y) {
        return heuristicWeight*graph.distance(x, y, ex, ey);
        // MOD 2 :: Increased Goal Heuristic
        /*if (x == ex && y == ey) {
            return 0.18f; // 0.18f
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }*/
    }

    private void findPath1Parent(int current, int x, int y) {
        setDistance(current, Float.POSITIVE_INFINITY);
        for (int i=-1;i<=1;++i) {
            for (int j=-1;j<=1;++j) {
                if (i == 0 && j == 0) continue;
                int px = x+i;
                int py = y+j;
                if (!graph.isValidBlock(px,py)) continue;
                int index = graph.toOneDimIndex(px, py);
                if (!visited(index)) continue;
                if (!graph.neighbourLineOfSight(x,y,px,py)) continue;

                float gValue = distance(index) + graph.distance(x,y,px,py);
                if (gValue < distance(current)) {
                    setDistance(current, gValue);
                    setParent(current, index);
                }
            }
        }
    }

    private void tryFixBufferValue(int current) {
        if (parent(current) < 0 && parent(current) != -1) {
            setParent(current, parent(current) - Integer.MIN_VALUE);
            setDistance(current, distance(parent(current)) + physicalDistance(current, parent(current)));
        }
    }
    

    protected void tryRelaxNeighbour(int current, int currentX, int currentY, int x, int y) {
        if (!graph.isValidCoordinate(x, y))
            return;
        
        int destination = toOneDimIndex(x,y);
        if (visited(destination))
            return;
        if (parent(current) != -1 && parent(current) == parent(destination)) // OPTIMISATION: [TI]
            return; // Idea: don't bother trying to relax if parents are equal. using triangle inequality.
        if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
            return;
        
        if (relax(current, destination, weight(currentX, currentY, x, y))) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance(destination) + heuristic(x,y));
        }
    }
    
    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        return tautRelax(u,v,0);
    }
    
    // This gives very good paths... but the recursion level is too deep.
    private boolean tautRelax(int u, int v, int depth) {
        if (isTaut(v, u)) {
            return tryRelaxVertex(u, v, false);
        } else {
            int par = parent(u);
            //if (lineOfSight(par, v)) {
                if (depth == 0) {
                    return tryRelaxVertex(par,v,!isTaut(v,par));
                }
                return tautRelax(par, v, depth-1);
            /*} else {
                return tryRelaxVertex(u, v, true);
            }*/
        }
    }
    
    private boolean tryRelaxVertex(int u, int v, boolean addBuffer) {
        int newParent = u;
        float newWeight = distance(u) + physicalDistance(u,v);
        if (addBuffer) {
            newWeight += BUFFER_VALUE;
            newParent += Integer.MIN_VALUE;
        }
        if (newWeight < distance(v)) {
            if (isMergeableWithParent(u, v)) {
                newParent = parent(u);
            }
            setDistance(v, newWeight);
            setParent(v, newParent);
            return true;
        }
        return false;
    }
    
    // If parent(u),u,v collinear, remove u from path, except when u is at an outer corner.
    private boolean isMergeableWithParent(int u, int v) {
        if (u == -1) return false;
        int p = parent(u);
        if (p == -1) return false; // u is start point.
        int ux = toTwoDimX(u);
        int uy = toTwoDimY(u);
        if (isOuterCorner(ux, uy)) return false; // u is outer corner
        
        int vx = toTwoDimX(v);
        int vy = toTwoDimY(v);
        int px = toTwoDimX(p);
        int py = toTwoDimY(p);
        
        return isCollinear(px,py,ux,uy,vx,vy);
    }
    
    protected final boolean isOuterCorner(int x, int y) {
        boolean a = graph.isBlocked(x-1, y-1);
        boolean b = graph.isBlocked(x, y-1);
        boolean c = graph.isBlocked(x, y);
        boolean d = graph.isBlocked(x-1, y);
        
        return ((!a && !c) || (!d && !b)) && (a || b || c || d);
        
        /* NOTE:
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *  
         *  The definition below excludes the above case.
         */
    }
    
    protected final boolean isCollinear(int x1, int y1, int x2, int y2, int x3, int y3) {
        // (y2-y1)/(x2-x1) == (y3-y2)/(x3-x2)
        // <=>
        return (y2-y1)*(x3-x2) == (y3-y2)*(x2-x1);
    }

    /**
     * Checks whether the path v, u, p=parent(u) is taut.
     */
    private boolean isTaut(int v, int u) {
        int p = parent(u); // assert u != -1
        if (p == -1) return true;
        int x1 = toTwoDimX(v);
        int y1 = toTwoDimY(v);
        int x2 = toTwoDimX(u);
        int y2 = toTwoDimY(u);
        int x3 = toTwoDimX(p);
        int y3 = toTwoDimY(p);
        
        if (x1 < x2) {
            if (y1 < y2) {
                return isTautFromBottomLeft(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTopLeft(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                return isTautFromLeft(x1, y1, x2, y2, x3, y3);
            }
        } else if (x2 < x1) {
            if (y1 < y2) {
                return isTautFromBottomRight(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTopRight(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                return isTautFromRight(x1, y1, x2, y2, x3, y3);
            }
        } else { // x2 == x1
            if (y1 < y2) {
                return isTautFromBottom(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTop(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                throw new UnsupportedOperationException("v == u?");
            }
        }
    }

    
    private boolean isTautFromBottomLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2 || y3 < y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return bottomRightOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return topLeftOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromTopLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2 || y3 > y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return bottomLeftOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return topRightOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }
    
    private boolean isTautFromBottomRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2 || y3 < y2) return false;
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return topRightOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromTopRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2 || y3 > y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return topLeftOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return bottomRightOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2) return false;
        
        int dy = y3 - y2;
        if (dy < 0) { // y3 < y2
            return topRightOfBlockedTile(x2, y2);
        } else if (dy > 0) { // y3 > y2
            return bottomRightOfBlockedTile(x2, y2);
        } else { // y3 == y2
            return true;
        }
    }

    private boolean isTautFromRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2) return false;
        
        int dy = y3 - y2;
        if (dy < 0) { // y3 < y2
            return topLeftOfBlockedTile(x2, y2);
        } else if (dy > 0) { // y3 > y2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // y3 == y2
            return true;
        }
    }

    private boolean isTautFromBottom(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (y3 < y2) return false;
        
        int dx = x3 - x2;
        if (dx < 0) { // x3 < x2
            return topRightOfBlockedTile(x2, y2);
        } else if (dx > 0) { // x3 > x2
            return topLeftOfBlockedTile(x2, y2);
        } else { // x3 == x2
            return true;
        }
    }

    private boolean isTautFromTop(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (y3 > y2) return false;
        
        int dx = x3 - x2;
        if (dx < 0) { // x3 < x2
            return bottomRightOfBlockedTile(x2, y2);
        } else if (dx > 0) { // x3 > x2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // x3 == x2
            return true;
        }
    }


    
    private boolean topRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y-1);
    }

    private boolean topLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y-1);
    }

    private boolean bottomRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y);
    }

    private boolean bottomLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y);
    }

}
