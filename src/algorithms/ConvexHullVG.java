package algorithms;

import java.util.Arrays;

import algorithms.rotationalplanesweep.RPSScanner;
import grid.GridGraph;

public class ConvexHullVG {

    private final GridGraph graph;
    private final int sizeXPlusOne;
    private final int sizeYPlusOne;
    private RPSScanner scanner;

    private final int[] nodeIndex; // Flattened 2D Array

    private ConvexHull[] convexHulls;
    private int nNodes;
    
    class ConvexHull {
        int[] xVertices;
        int[] yVertices;
        int size;
    }
    
    public ConvexHullVG(GridGraph graph) {
        this.graph = graph;
        this.sizeXPlusOne = graph.sizeX+1;
        this.sizeYPlusOne = graph.sizeY+1;
        nodeIndex = new int[sizeYPlusOne*sizeXPlusOne];
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
    }

    public final RPSScanner getRPSScanner() {
        return scanner;
    }

    public final int indexOf(int x, int y) {
        return nodeIndex[y*sizeXPlusOne + x];
    }

    public final int size() {
        return nNodes;
    }

    public final int getX(int index) {
        return 0;
    }

    public final int getY(int index) {
        return 0;
    }
}
