package draw;

import grid.GridGraph;

import java.awt.Color;
import java.awt.Graphics;

public class GridPointDrawer implements Drawer {

    private final GridPointSet gridPointSet;
    private final GridGraph gridGraph;
    private final int resX;
    private final int resY;

    protected int circleSize;
    protected int halfCircleSize;
    private final float width;
    private final float height;
    
    
    public GridPointDrawer(GridGraph gridGraph, GridPointSet gridPointSet, int resX, int resY) {
        this.resX = resX;
        this.resY = resY;
        width = (float)resX/gridGraph.sizeX;
        height = (float)resY/gridGraph.sizeY;
        circleSize = (int)(width/4);
        halfCircleSize = circleSize/2;
        
        this.gridGraph = gridGraph;
        this.gridPointSet = gridPointSet;
    }
    
    @Override
    public void draw(Graphics g) {
        if (gridPointSet == null) {
            return;
        }
        for (GridPointSet.ColourPoint point : gridPointSet.getPointList()) {
            drawPoint(g, point);
        }
    }
    
    private void drawPoint(Graphics g, GridPointSet.ColourPoint point) {
        int x = (int)(width*point.x);
        int y = (int)(height*point.y);
        
        g.setColor(point.color);
        g.fillOval(x-halfCircleSize, y-halfCircleSize, circleSize, circleSize);
        g.setColor(Color.BLACK);
    }
}
