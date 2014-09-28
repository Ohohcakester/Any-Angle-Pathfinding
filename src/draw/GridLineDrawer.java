package draw;

import grid.GridGraph;

import java.awt.Color;
import java.awt.Graphics;

public class GridLineDrawer {
    
    private final GridLineSet gridLineSet;
    private final GridGraph gridGraph;
    private final int resX;
    private final int resY;
    
    
    public GridLineDrawer(GridGraph gridGraph, GridLineSet gridLineSet, int resX, int resY) {
        this.resX = resX;
        this.resY = resY;
        this.gridGraph = gridGraph;
        this.gridLineSet = gridLineSet;
    }
    
    public void draw(Graphics g) {
        for (GridLineSet.Line line : gridLineSet.getLineList()) {
            drawLine(g, line);
        }
    }
    
    private void drawLine(Graphics g, GridLineSet.Line line) {
        float width = (float)resX/gridGraph.sizeX;
        float height = (float)resY/gridGraph.sizeY;
        int x1 = (int)(width*line.x1);
        int y1 = (int)(height*line.y1);
        int x2 = (int)(width*line.x2);
        int y2 = (int)(height*line.y2);
        
        g.setColor(line.color);
        g.drawLine(x1, y1, x2, y2);
        g.setColor(Color.BLACK);
    }
}
