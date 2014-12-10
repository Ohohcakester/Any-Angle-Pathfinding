package draw;

import grid.GridGraph;

public class VariableSizePointDrawer extends GridPointDrawer {

    public VariableSizePointDrawer(GridGraph gridGraph,
            GridPointSet gridPointSet, int resX, int resY, float relativeSize) {
        
        super(gridGraph, gridPointSet, resX, resY);
        circleSize *= relativeSize;
        halfCircleSize *= relativeSize;
    }

}
