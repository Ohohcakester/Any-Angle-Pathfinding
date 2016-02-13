package algorithms.strictthetastar;

import grid.GridGraph;
import algorithms.BasicThetaStar;
import algorithms.priorityqueue.ReusableIndirectHeap;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 * V2: With recursive taut-parent finding.
 * V2b: With collinear point merging to reduce depth of taut-parent searches.
 * V2c: Fix bug with distance comparison. It should be a heuristic, not an actual distance!
 *   '-> But this seems to perform worse than V2b. Why was V2b right...?
 * 
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
public class StrictThetaStarV2c extends BasicThetaStar {
    private static final int ADD_BUFFER = 2;
    private static final int RELAX = 1;
    private static final int NO_RELAX = 0;

    private static final float BUFFER_VALUE = 1f;

    public StrictThetaStarV2c(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static StrictThetaStarV2c noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        StrictThetaStarV2c algo = new StrictThetaStarV2c(graph, sx, sy, ex, ey);
        algo.heuristicWeight = 0f;
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
            
            if (current == finish || distance(current) == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            setVisited(current, true);

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            

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
            return 1.1f;
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }*/
    }

    private void tryFixBufferValue(int current) {
        if (parent(current) < 0 && parent(current) != -1) {
            //System.out.println("FIXCOUNT " + fixCount + " OUT OF " + outOf);

            setParent(current, parent(current) - Integer.MIN_VALUE);
            setDistance(current, distance(parent(current)) + physicalDistance(current, parent(current)));
            
            //parent(current) = wrap(current);
            
            /*int target = parent(current);
            while (target != -1) {
                if (isTaut(current, target) && lineOfSight(current, target)) {
                    setDistance(current, distance(target) + physicalDistance(current, target));
                    parent(current) = target;
                    return;
                }
                target = parent(target);
            }*/
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
        
        int result = tautRelax(current, destination);
        if (result == NO_RELAX) return;
        else if (result == RELAX) {
            // Decreasekey
            pq.decreaseKey(destination, distance(destination) + heuristic(x,y));
        } else if (result == ADD_BUFFER) {
            // DecreaseKey with buffer
            pq.decreaseKey(destination, distance(destination) + heuristic(x,y) + BUFFER_VALUE);
        }
    }
    
    // This gives very good paths... but the recursion level is too deep.
    private int tautRelax(int u, int v) {
        if (isTaut(v, u)) {
            return tryRelaxVertex(u, v, false);
        } else {
            int par = parent(u);
            if (lineOfSight(par, v)) {
                return tautRelax(par, v);
            } else {
                return tryRelaxVertex(u, v, true);
            }
        }
    }
    
    private int tryRelaxVertex(int u, int v, boolean addBuffer) {
        float newWeight = distance(u) + physicalDistance(u,v);
        if (newWeight < distance(v)) {
            if (isMergeableWithParent(u, v)) {
                u = parent(u);
            }
            setDistance(v, newWeight);
            setParent(v, u);
            return addBuffer ? ADD_BUFFER : RELAX;
        }
        return NO_RELAX;
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
        return graph.isTaut(x1, y1, x2, y2, x3, y3);
    }
}
