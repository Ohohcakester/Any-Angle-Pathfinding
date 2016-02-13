package algorithms.strictthetastar;

import grid.GridGraph;
import algorithms.BasicThetaStar;
import algorithms.priorityqueue.ReusableIndirectHeap;

/**
 * An modification of Theta* that I am experimenting with. -Oh
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
public class StrictThetaStarV1b extends BasicThetaStar {

    private static final float BUFFER_VALUE = 1f;

    public StrictThetaStarV1b(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
        this.postSmoothingOn = true;
    }
    
    public static StrictThetaStarV1b noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        StrictThetaStarV1b algo = new StrictThetaStarV1b(graph, sx, sy, ex, ey);
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
            tryFixBufferValue(current);
            
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
        //return heuristicWeight*graph.distance(x, y, ex, ey);
        // MOD 2 :: Increased Goal Heuristic
        if (x == ex && y == ey) {
            return 1.1f;
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }
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
        
        if (relax(current, destination, weight(currentX, currentY, x, y))) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance(destination) + heuristic(x,y));
        }
    }
    
    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        float tempWeight = Float.POSITIVE_INFINITY;
        int tempParent = -1;
        if (tryLocateTautParent(v, u)) return true;
        return tryLocateVisibleParent(v, u);
        /*int par = parent(u);
        if (lineOfSight(parent(u), v)) {
            float newWeight = distance(par) + physicalDistance(par, v);
            return relaxTarget(v, par, newWeight, true);
        } else {
            float newWeight = distance(u) + physicalDistance(u, v);
            return relaxTarget(v, u, newWeight, true);
        }*/
    }

    private boolean relaxTarget(int v, int par, float newWeight, boolean EEE) {
        if (newWeight < distance(v)) {
            if (!isTaut(v, par)) {
                if (EEE && tryLocateTautParent(v, par)) {
                    return true;
                }
                newWeight += BUFFER_VALUE;
            }
            setDistance(v, newWeight);
            setParent(v, par);
            return true;
        }
        return false;
    }

    
    private boolean tryLocateTautParent(int current, int from) {
        int u = from;
        while (parent(u) != -1 && !isTaut(current, u)) {
            u = parent(u);
        }
        //if (isTaut(current, u)) {
        float newWeight = distance(u) + physicalDistance(current, u);
        if (newWeight < distance(current)) {
            if (lineOfSight(current, u)) {
                setParent(current, u);
                setDistance(current, newWeight);
                return true;
            }
        }
        //}
        return false;
    }
    
    private boolean tryLocateVisibleParent(int current, int from) {
        int u = from;
        while (parent(u) != -1 && lineOfSight(current, parent(u))) {
            u = parent(u);
        }
        float newWeight = distance(u) + physicalDistance(current, u);
        if (newWeight < distance(current)) {
            setParent(current, u);
            setDistance(current, distance(u) + physicalDistance(current, u) + BUFFER_VALUE);
            return true;
        }
        return false;
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
