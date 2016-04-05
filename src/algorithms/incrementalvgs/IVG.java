package algorithms.incrementalvgs;

import grid.GridGraph;

import java.util.Arrays;

import algorithms.sparsevgs.LineOfSightScanner;

public class IVG {

    // Visibility Graph Data Structure
    protected int[][] nodeIndex;
    private float[] heuristicList;
    private int[] nodesX;
    private int[] nodesY;
    private boolean[] isVisibleFromGoal;
    private int nNodes;
    

    private final IVGJPS lowerBoundSearch;
    private final GridGraph graph;
    private final float upperBoundLength;
    private final int sx, sy, ex, ey;
    private int startIndex;
    private int endIndex;

    // Grid Ellipse Data Structure
    protected int[] xOffsets;
    protected int yOffset;
    
    public IVG(GridGraph graph, int sx, int sy,
            int ex, int ey, float upperBoundLength, IVGJPS lowerBoundSearch) {
        this.graph = graph;
        this.sx = sx;
        this.sy = sy;
        this.ex = ex;
        this.ey = ey;
        this.upperBoundLength = upperBoundLength;
        this.lowerBoundSearch = lowerBoundSearch;
    }
    
    public void initialise() {
        nodesX = new int[11];
        nodesY = new int[11];
        heuristicList = new float[11];
        isVisibleFromGoal = new boolean[11];
        
        addNodes();
        addStartAndEnd(sx, sy, ex, ey);
        computeHeuristics();
    }

    protected void addNodes() {
        GridEllipse ellipse = new GridEllipse(sx, sy, ex, ey, upperBoundLength);

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
    
    private final void addStartAndEnd(int sx, int sy, int ex, int ey) {
        if (isNode(sx, sy)) {
            startIndex = indexOf(sx, sy);
        } else {
            int yRel = sy-yOffset;
            startIndex = nodeIndex[yRel][sx-xOffsets[yRel]] = assignNode(sx, sy);
        }

        if (isNode(ex, ey)) {
            endIndex = indexOf(ex, ey);
        } else {
            int yRel = ey-yOffset;
            endIndex = nodeIndex[yRel][ex-xOffsets[yRel]] = assignNode(ex, ey);
        }
    }

    private final int assignNode(int x, int y) {
        int index = nNodes;
        if (index >= nodesX.length) {
            nodesX = Arrays.copyOf(nodesX, nodesX.length*2);
            nodesY = Arrays.copyOf(nodesY, nodesY.length*2);
        }
        nodesX[index] = x;
        nodesY[index] = y;
        ++nNodes;
        return index;
    }
    
    private final void computeHeuristics() {
        heuristicList = new float[nNodes];
        isVisibleFromGoal = new boolean[nNodes];
        for (int i=0;i<nNodes;++i) {
            int x = nodesX[i];
            int y = nodesY[i];
            heuristicList[i] = computeHeuristic(x,y);
        }
    }
    
    public final void findPointsReachableFromGoal(LineOfSightScanner losScanner) {
        losScanner.computeAllVisibleTautSuccessors(ex, ey);
        int nSuccessors = losScanner.nSuccessors;
        for (int i=0;i<nSuccessors;++i) {
            int x = losScanner.successorsX[i];
            int y = losScanner.successorsY[i];
            int index = tryGetIndexOf(x,y);
            if (index != -1) {
                isVisibleFromGoal[index] = true;
            }
        }
    }
    
    public final boolean isVisibleFromGoal(int x, int y) {
        return isVisibleFromGoal[indexOf(x,y)];
    }

    private final float computeHeuristic(int x, int y) {
        float sld = graph.distance(x, y, ex, ey);
        float lowerBoundDistance = lowerBoundSearch.heuristicValue(x, y);
        return sld < lowerBoundDistance ? lowerBoundDistance : sld; // Return max(sld, lowerBoundDistance);
    }

    public final float lowerBoundRemainingDistance(int x, int y) {
        return heuristicList[indexOf(x, y)];
    }
    
    /**
     * Returns -1 on failure.
     */
    public final int tryGetIndexOf(int x, int y) {
        int yRel = y-yOffset;
        if (yRel < 0 || yRel >= nodeIndex.length) return -1;
        int xRel = x-xOffsets[yRel];
        if (xRel < 0 || xRel >= nodeIndex[yRel].length) return -1;
        
        return nodeIndex[yRel][xRel];
    }
    
    public final int indexOf(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]];
    }

    public final int xCoordinateOf(int index) {
        return nodesX[index];
    }
    
    public final int yCoordinateOf(int index) {
        return nodesY[index];
    }

    protected final boolean isNode(int x, int y) {
        int yRel = y-yOffset;
        return nodeIndex[yRel][x-xOffsets[yRel]] != -1;
    }
    
    protected final boolean isCorner(int x, int y) {
        boolean a = graph.isBlocked(x-1, y-1);
        boolean b = graph.isBlocked(x, y-1);
        boolean c = graph.isBlocked(x, y);
        boolean d = graph.isBlocked(x-1, y);
        
        return ((!a && !c) || (!d && !b)) && (a || b || c || d);
        
        /* NOTE
         *   ___ ___
         *  |   |||||
         *  |...X'''| <-- this is considered a corner in the above definition
         *  |||||___|
         *  
         *  The definition below excludes the above case.
         */
        
        /*int results = 0;
        if(a)results++;
        if(b)results++;
        if(c)results++;
        if(d)results++;
        return (results == 1);*/
    }

    public final int size() {
        return nNodes;
    }
    
    public final int startNode() {
        return startIndex;
    }
    
    public final int endNode() {
        return endIndex;
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
