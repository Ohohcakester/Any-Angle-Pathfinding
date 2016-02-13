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
public class StrictThetaStarV1 extends BasicThetaStar {

    private static final float BUFFER_VALUE = 1;

    public StrictThetaStarV1(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static StrictThetaStarV1 noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        StrictThetaStarV1 algo = new StrictThetaStarV1(graph, sx, sy, ex, ey);
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
        int par = parent(u);
        if (lineOfSight(parent(u), v)) {
            float newWeight = distance(par) + physicalDistance(par, v);
            return relaxTarget(v, par, newWeight);
        } else {
            float newWeight = distance(u) + physicalDistance(u, v);
            return relaxTarget(v, u, newWeight);
        }
    }

    private boolean relaxTarget(int v, int par, float newWeight) {
        if (newWeight < distance(v)) {
            if (!isTaut(v, par)) {
                newWeight += BUFFER_VALUE;
                par += Integer.MIN_VALUE;
            }
            setDistance(v, newWeight);
            setParent(v, par);
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
