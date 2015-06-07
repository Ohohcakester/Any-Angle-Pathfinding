package algorithms.visibilitygraph;

import grid.GridGraph;

import java.util.ArrayList;

/**
 * STEP 1: Use Theta* (or some other algo) to find an upper bound L for the
 *  shortest path length from s to t.
 * 
 * STEP 2: Draw an ellipse with s and t as the focal point and "string length" L.
 * 
 * STEP 3: Select all points within the ellipse and construct a visibility graph.
 * 
 * STEP 4: Use Dijkstra's algorithm on the visibility graph.
 */
public class RestrictedVisibilityGraph extends VisibilityGraph {
    // nodeIndex[y][x] refers to grid point (x+xOffsets[y], y+yOffset)
    protected int[] xOffsets;
    protected int yOffset;
    protected final float pathLength;

    public RestrictedVisibilityGraph(GridGraph graph, int sx, int sy, int ex,
            int ey, float pathLength) {
        super(graph, sx, sy, ex, ey);
        this.pathLength = pathLength;
    }

    @Override
    public void initialise() {
        if (nodeList != null) {
            //("already initialised.");
            return;
        }
        
        nodeList = new ArrayList<>();
        outgoingEdgeList = new ArrayList<>();
        
        addNodes();
        addAllEdges();
        addStartAndEnd(sx, sy, ex, ey);
    }

    @Override
    protected void addNodes() {
        GridEllipse ellipse = new GridEllipse(sx, sy, ex, ey, pathLength);
        
        yOffset = ellipse.yMin;
        
        nodeIndex = new int[ellipse.yMax - ellipse.yMin + 1][];
        xOffsets = new int[nodeIndex.length];
        
        for (int y=0;y<nodeIndex.length;y++) {
            nodeIndex[y] = new int[ellipse.xRight[y] - ellipse.xLeft[y] + 1];
            xOffsets[y] = ellipse.xLeft[y];
            for (int x=0; x<nodeIndex[y].length; x++) {
                int xAbs = x + xOffsets[y];
                int yAbs = y + yOffset;
                
                if (isCorner(xAbs, yAbs)) {
                    nodeIndex[y][x] = assignNode(xAbs, yAbs);
                } else {
                    nodeIndex[y][x] = -1;
                }
            }
        }
    }
    
    @Override
    protected int indexOf(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]];
    }
    
    @Override
    protected boolean isNode(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]] != -1;
    }

    /**
     * Assumption: start and end are the last two nodes, if they exist.
     */
    @Override
    protected void removeStartAndEnd() {
        if (startIsNewNode) {
            int index = nodeList.size()-1;
            int yRel = sy-yOffset;
            nodeIndex[yRel][sx-xOffsets[yRel]] = -1;
            nodeList.remove(index);
            outgoingEdgeList.remove(index);
            removeInstancesOf(index);

            startIsNewNode = false;
        }
        if (endIsNewNode) {
            int index = nodeList.size()-1;
            int yRel = ey-yOffset;
            nodeIndex[yRel][ex-xOffsets[yRel]] = -1;
            nodeList.remove(index);
            outgoingEdgeList.remove(index);
            removeInstancesOf(index);

            endIsNewNode = false;
        }
    }
    
    @Override
    protected void addStartAndEnd(int sx, int sy, int ex, int ey) {
        if (isNode(sx, sy)) {
            startIndex = indexOf(sx, sy);
            startIsNewNode = false;
        } else {
            int yRel = sy-yOffset;
            startIndex = nodeIndex[yRel][sx-xOffsets[yRel]] = assignNodeAndConnect(sx, sy);
            startIsNewNode = true;
        }

        if (isNode(ex, ey)) {
            endIndex = indexOf(ex, ey);
            endIsNewNode = false;
        } else {
            int yRel = ey-yOffset;
            endIndex = nodeIndex[yRel][ex-xOffsets[yRel]] = assignNodeAndConnect(ex, ey);
            endIsNewNode = true;
        }
    }

}


class GridEllipse {
    private static final float BUFFER = 0.001f;
    
    public int yMin;
    public int yMax;
    public int[] xLeft;
    public int[] xRight;

    public GridEllipse(int sx, int sy, int ex, int ey, float pathLength) {
        double LSquare = (double)pathLength + BUFFER;
        LSquare *= LSquare;
        int dy = ey-sy;
        int dx = ex-sx;
        
        double m = (double)(dy)/dx;
        double mSquare = m*m;
        
        double onePlusmSquare = 1+mSquare;
        double cosSquare = 1/onePlusmSquare;
        double sinSquare = mSquare/onePlusmSquare;
        if (m == Double.POSITIVE_INFINITY || m == Double.NEGATIVE_INFINITY) {
            cosSquare = 0;
            sinSquare = 1;
        }
        
        double DSquare = (dy*dy + dx*dx);
        double aSquare = LSquare/4; // major axis radius
        double bSquare = (LSquare - DSquare) / 4; // minor axis radius
        
        double A = cosSquare/aSquare + sinSquare/bSquare;

        /*
         * x = +/- sqrt( P - Qy^2 ) - Ry 
         */        
        double P = 1/A;
        double Q = 1/(aSquare * bSquare * A*A);
        double R = (m/onePlusmSquare) * (1/aSquare - 1/bSquare) / A;
        if (m == Double.POSITIVE_INFINITY || m == Double.NEGATIVE_INFINITY) {
            R = 0;
        }
        
        double yHeight = 2*Math.sqrt(aSquare*sinSquare + bSquare*cosSquare);
        
        yMin = ceil( (sy+ey - yHeight)/2 );
        yMax = floor( (sy+ey + yHeight)/2 );
        int totalYs = yMax-yMin+1;
        xLeft = new int[totalYs];
        xRight = new int[totalYs];

        double midX = (double)(sx + ex)/2;
        double midY = (double)(sy + ey)/2;
        for (int i=0; i<totalYs; i++) {
            // x = +/- sqrt( P - Qy^2 ) - Ry 
            double y = yMin+i - midY;
            double insideSqrt = P - Q*(y*y);
            double Ry = R*y;
            if (insideSqrt > 0) {
                double sqrt = Math.sqrt(insideSqrt);
                xLeft[i] = ceil(midX - sqrt - Ry);
                xRight[i] = floor(midX + sqrt - Ry);
            } else {
                xLeft[i] = floor(midX - Ry);
                xRight[i] = ceil(midX - Ry);
            }
        }
    }
    
    private int floor(double a) {
        return (int)a;
    }
    
    private int ceil(double a) {
        return (int)(Math.ceil(a));
    }
    
    
}