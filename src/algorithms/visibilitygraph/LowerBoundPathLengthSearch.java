package algorithms.visibilitygraph;

import algorithms.AStarStaticMemory;
import algorithms.priorityqueue.ReusableIndirectHeap;
import grid.GridGraph;

public class LowerBoundPathLengthSearch extends AStarStaticMemory {
    
    private static final float APPROXIMATION_RATIO = (float)Math.sqrt(4 - 2*Math.sqrt(2));
    private static final float BUFFER = 0.000001f;
    private final float upperBound;

    public LowerBoundPathLengthSearch(GridGraph graph, int sx, int sy, int ex, int ey, float existingPathLength) {
        super(graph, sx, sy, ex, ey);
        this.upperBound = existingPathLength;
    }

    
    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        //finish = toOneDimIndex(ex, ey);

        pq = new ReusableIndirectHeap(totalSize);
        this.initialiseMemory(totalSize, Float.POSITIVE_INFINITY, -1, false);
        
        initialise(start);

        float lastDist = -1;
        while (!pq.isEmpty()) {
            float dist = pq.getMinValue();
            
            int current = pq.popMinIndex();
            
            //maybeSaveSearchSnapshot();
            if (Math.abs(dist - lastDist) > 0.01f) { maybeSaveSearchSnapshot(); lastDist = dist;}
            
            if (distance(current) == Float.POSITIVE_INFINITY) {
                //maybeSaveSearchSnapshot();
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

            //maybeSaveSearchSnapshot();
        }
        
        maybePostSmooth();
    }
    
    protected void tryRelaxNeighbour(int current, int currentX, int currentY, int x, int y) {
        if (!graph.isValidCoordinate(x, y))
            return;
        
        int destination = toOneDimIndex(x,y);
        if (visited(destination))
            return;
        if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
            return;
        
        if (relax(current, destination, weight(currentX, currentY, x, y), x, y)) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance(destination));
        }
    }

    protected boolean relax(int u, int v, float weightUV, int currX, int currY) {
        // return true iff relaxation is done.
        
        float newWeight = distance(u) + weightUV;
        if (newWeight / APPROXIMATION_RATIO + graph.distance(currX, currY, ex, ey) > upperBound + BUFFER + 0.05) return false;
        
        if (newWeight < distance(v)) {
            setDistance(v, newWeight);
            setParent(v, u);
            //maybeSaveSearchSnapshot();
            return true;
        }
        return false;
    }
    
    public float heuristicValue(int x, int y) {
        int index = graph.toOneDimIndex(x, y);
        return distance(index)/APPROXIMATION_RATIO;
    }

}
