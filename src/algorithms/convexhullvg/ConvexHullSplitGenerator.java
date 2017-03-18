package algorithms.convexhullvg;

import java.util.Arrays;

import grid.GridGraph;

public class ConvexHullSplitGenerator {

    private ConvexHullVG.ConvexHull[] convexHulls;    
    private int nHulls;

    public ConvexHullSplitGenerator(GridGraph graph) {

    }



    public static ConvexHullVG.ConvexHull[] generate(GridGraph graph) {
        ConvexHullSplitGenerator generator = new ConvexHullSplitGenerator(graph);
        return generator.convexHulls;
    }
}