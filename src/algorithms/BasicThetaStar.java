package algorithms;

import grid.GridGraph;


public class BasicThetaStar extends AStarStaticMemory {
    
    public BasicThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }
    
    public static BasicThetaStar postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
        BasicThetaStar bts = new BasicThetaStar(graph, sx, sy, ex, ey);
        bts.postSmoothingOn = true;
        return bts;
    }
    
    public static BasicThetaStar noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        BasicThetaStar bts = new BasicThetaStar(graph, sx, sy, ex, ey);
        bts.heuristicWeight = 0;
        return bts;
    }

    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        
        if (lineOfSight(parent(u), v)) {
            u = parent(u);
            
            float newWeight = distance(u) + physicalDistance(u, v);
            if (newWeight < distance(v)) {
                setDistance(v, newWeight);
                setParent(v, u);
                return true;
            }
            return false;
        } else {
            float newWeight = distance(u) + weightUV;
            if (newWeight < distance(v)) {
                setDistance(v, newWeight);
                setParent(v, u);
                return true;
            }
            return false;
        }
    }
    
}