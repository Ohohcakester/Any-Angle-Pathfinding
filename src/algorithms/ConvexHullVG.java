package algorithms;

import java.util.Arrays;

import algorithms.rotationalplanesweep.ConvexHullRPSScanner;
import grid.GridGraph;

public class ConvexHullVG {

    private final GridGraph graph;
    private final int sizeXPlusOne;
    private final int sizeYPlusOne;
    private ConvexHullRPSScanner scanner;

    private final int[] nodeIndex; // Flattened 2D Array
    private int[] nodeX;
    private int[] nodeY;

    private ConvexHull[] convexHulls;
    private int nNodes;
    
    public static class ConvexHull {
        public int[] xVertices;
        public int[] yVertices;
        public int size;
    }
    
    public ConvexHullVG(GridGraph graph) {
        this.graph = graph;
        this.sizeXPlusOne = graph.sizeX+1;
        this.sizeYPlusOne = graph.sizeY+1;
        nodeIndex = new int[sizeYPlusOne*sizeXPlusOne];
        scanner = new ConvexHullRPSScanner(graph, convexHulls, convexHulls.length);
    }

    public void initialise(int sx, int sy, int ex, int ey) {
        initialiseConvexHulls();
        initialiseNodes();
    }
    
    private void initialiseConvexHulls() {

    }

    private void initialiseNodes() {
        int nNodes = 0;
        for (int i=0; i<convexHulls.length; ++i) {
            ConvexHull hull = convexHulls[i];
            for (int j=0; j<hull.size; ++j) {
                int x = hull.xVertices[j];
                int y = hull.yVertices[j];
                nodeIndex[y*sizeXPlusOne + x] = nNodes;
                ++nNodes;
            }
        }

        nodeX = new int[nNodes];
        nodeY = new int[nNodes];
        int index = 0;
        for (int i=0; i<convexHulls.length; ++i) {
            ConvexHull hull = convexHulls[i];
            for (int j=0; j<hull.size; ++j) {
                nodeX[index] = hull.xVertices[j];
                nodeY[index] = hull.yVertices[j];

                if (nodeIndex[hull.yVertices[j]*sizeXPlusOne + hull.xVertices[j]] != index) throw new UnsupportedOperationException("ERROR");
                ++index;
            }
        }
    }

    public final ConvexHullRPSScanner computeAllVisibleSuccessors(int currX, int currY) {
        scanner.computeAllVisibleSuccessors(currX, currY);
        return scanner;
    }

    public final int indexOf(int x, int y) {
        return nodeIndex[y*sizeXPlusOne + x];
    }

    public final int size() {
        return nNodes;
    }

    public final int getX(int index) {
        return nodeX[index];
    }

    public final int getY(int index) {
        return nodeY[index];
    }
}
