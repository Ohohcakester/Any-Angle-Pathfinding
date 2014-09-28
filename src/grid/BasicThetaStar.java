package grid;


public class BasicThetaStar extends AStar {

    @Override
    protected boolean postSmoothingOn() {
        return false;
    }
    
    @Override
    protected float heuristicWeight() {
        return 1f;
    }
    
    public BasicThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        
        if (lineOfSight(parent[u], v)) {
            u = parent[u];
            
            float newWeight = distance[u] + physicalDistance(u, v);
            if (newWeight < distance[v]) {
                distance[v] = newWeight;
                parent[v] = u;
                return true;
            }
            return false;
        } else {
            float newWeight = distance[u] + weightUV;
            if (newWeight < distance[v]) {
                distance[v] = newWeight;
                parent[v] = u;
                return true;
            }
            return false;
        }
    }
    
}