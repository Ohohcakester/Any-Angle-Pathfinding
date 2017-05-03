package algorithms.sg16;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import algorithms.rotationalplanesweep.ConvexHullRPSScanner;
import grid.GridGraph;

import algorithms.datatypes.SnapshotItem;

public class SG16VisibilityGraph {

    private Runnable snapshotAction;

    private final GridGraph graph;
    private final int sizeXPlusOne;
    private final int sizeYPlusOne;
    private ConvexHullRPSScanner scanner;
    private ConvexHullHeuristic convexHullHeuristic;

    private final int[] nodeIndex; // Flattened 2D Array
    private int[] nodeX;
    private int[] nodeY;

    private ConvexHull[] convexHulls;
    private int nNodes;
    
    public static class ConvexHull {
        public int[] xVertices;
        public int[] yVertices;
        public int size;
        public int obstacleIndex;
    }
    
    public SG16VisibilityGraph(GridGraph graph) {
        this.graph = graph;
        this.sizeXPlusOne = graph.sizeX+1;
        this.sizeYPlusOne = graph.sizeY+1;
        nodeIndex = new int[sizeYPlusOne*sizeXPlusOne];
        Arrays.fill(nodeIndex, -1);
    }

    public final void setSnapshotAction(Runnable action) {
        snapshotAction = action;
    }

    public void initialise(int sx, int sy, int ex, int ey) {
        initialiseConvexHulls(sx, sy, ex, ey);
        if (snapshotAction != null) snapshotAction.run();
        initialiseNodes();
        scanner = new ConvexHullRPSScanner(graph, convexHulls, convexHulls.length);
        convexHullHeuristic = new ConvexHullHeuristic(convexHulls, convexHulls.length, ex, ey);
    }
    
    private void initialiseConvexHulls(int sx, int sy, int ex, int ey) {
        convexHulls = ConvexHullSplitGenerator.generate(graph, sx, sy, ex, ey);
    }

    private void initialiseNodes() {
        nNodes = 0;
        for (int i=0; i<convexHulls.length; ++i) {
            ConvexHull hull = convexHulls[i];
            for (int j=0; j<hull.size; ++j) {
                int x = hull.xVertices[j];
                int y = hull.yVertices[j];
                if (nodeIndex[y*sizeXPlusOne +x] != -1) continue;

                nodeIndex[y*sizeXPlusOne + x] = nNodes;
                ++nNodes;
            }
        }

        nodeX = new int[nNodes];
        nodeY = new int[nNodes];

        for (int i=0; i<convexHulls.length; ++i) {
            ConvexHull hull = convexHulls[i];
            for (int j=0; j<hull.size; ++j) {
                int x = hull.xVertices[j];
                int y = hull.yVertices[j];
                int index = nodeIndex[y*sizeXPlusOne + x];

                nodeX[index] = x;
                nodeY[index] = y;
            }
        }
    }

    public final ConvexHullRPSScanner computeAllVisibleSuccessors(int currX, int currY, int ex, int ey) {
        scanner.computeAllVisibleSuccessors(currX, currY, ex, ey);
        return scanner;
    }

    public final ConvexHullHeuristic getConvexHullHeuristic() {
        return convexHullHeuristic;
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


    public final List<SnapshotItem> generateConvexHullSnapshot() {
        List<SnapshotItem> snapshotItemList = new ArrayList<>();

        for (int i=0; i<convexHulls.length; ++i) {
            ConvexHull hull = convexHulls[i];
            int size = hull.size;
            int prevX = hull.xVertices[size-1];
            int prevY = hull.yVertices[size-1];

            for (int j=0; j<size; ++j) {
                int currX = hull.xVertices[j];
                int currY = hull.yVertices[j];

                Integer[] path = new Integer[]{prevX, prevY, currX, currY};
                
                SnapshotItem snapshotItem = SnapshotItem.generate(path, Color.GREEN);
                snapshotItemList.add(snapshotItem);

                prevX = currX;
                prevY = currY;
            }
        }

        return snapshotItemList;
    }
    
}
