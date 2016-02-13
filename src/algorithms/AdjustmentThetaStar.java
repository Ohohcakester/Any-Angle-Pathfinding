package algorithms;

import grid.GridGraph;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 * @author Oh
 *
 */
public class AdjustmentThetaStar extends BasicThetaStar {

    public AdjustmentThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        boolean updated = false;
        if (lineOfSight(parent[u], v)) {
            u = parent[u];
            
            float newWeight = distance(u) + physicalDistance(u, v);
            if (newWeight < distance(v)) {
                setDistance(v, newWeight);
                setParent(v, u);
                updated = true;
            }
        } else {
            float newWeight = distance(u) + weightUV;
            if (newWeight < distance(v)) {
                setDistance(v, newWeight);
                setParent(v, u);
                updated = true;
            }
        }
        
        if (tryUpdateWithNeighbouringNodes(parent[u], v)) {
            updated = true;
        }
        return updated;
    }

    private boolean tryUpdateWithNeighbouringNodes(int u, int v) {
        if (u == -1) return false;
        
        int ux = toTwoDimX(u);
        int uy = toTwoDimY(u);
        boolean updated = false;

        if (tryUpdateWithNode(ux+1, uy, v)) updated = true;
        if (tryUpdateWithNode(ux-1, uy, v)) updated = true;
        if (tryUpdateWithNode(ux, uy+1, v)) updated = true;
        if (tryUpdateWithNode(ux, uy-1, v)) updated = true;
        
        return updated;
    }
    
    private boolean tryUpdateWithNode(int ux, int uy, int v) {
        int u = toOneDimIndex(ux, uy);
        float newWeight = distance(u) + physicalDistance(ux, uy, v);
        if (newWeight < distance(v)) {
            if (lineOfSight(u, v)) {
                setDistance(v, newWeight);
                setParent(v, u);
                return true;
            }
        }
        return false;
    }

}
