package algorithms;

import grid.GridGraph;

public class RecursiveThetaStar extends BasicThetaStar {

    public RecursiveThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        
        if (lineOfSight(parent(u), v)) {
            u = parent(u);
            return relax(u, v, weightUV);
        } else {
            float newWeight = distance(u) + physicalDistance(u, v);
            if (newWeight < distance(v)) {
                setDistance(v, newWeight);
                setParent(v, u);
//                setParentGranular(v, u);
                return true;
            }
            return false;
        }
    }
    
    protected void setParentGranular(int v, int u) {
        int x1 = toTwoDimX(u);
        int y1 = toTwoDimY(u);
        int x2 = toTwoDimX(v);
        int y2 = toTwoDimY(v);
        
        int dx = x2-x1; int dy = y2-y1;
        int gcd = gcd(dx,dy);
        if (gcd < 0) gcd = -gcd;
        dx = dx/gcd;
        dy = dy/gcd;
        int par = u;
        for (int i=1;i<=gcd;++i) {
            x1 += dx;
            y1 += dy;
            int curr = toOneDimIndex(x1,y1);
            
            setParent(curr, par);
            par = curr;
        }
    }
    
    public static int gcd(int a, int b) {
        return a == 0 ? b : gcd(b%a, a);
    }
}
