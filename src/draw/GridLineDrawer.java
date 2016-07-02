package draw;

import grid.GridGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import draw.GridLineSet.FractionLine;

public class GridLineDrawer implements Drawer {
    
    private static final int LINE_THICKNESS = 2;
    
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
    
    /* (non-Javadoc)
     * @see draw.Drawer#draw(java.awt.Graphics)
     */
    @Override
    public void draw(Graphics g) {
        if (gridLineSet == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(LINE_THICKNESS));

        for (GridLineSet.FractionLine line : gridLineSet.getFractionLineList()) {
            drawFractionLine(g, line);
        }
        for (GridLineSet.Line line : gridLineSet.getLineList()) {
            drawLine(g, line);
        }
        
        g2.setStroke(new BasicStroke(1));
    }
    
    private void drawFractionLine(Graphics g, FractionLine line) {
        float width = (float)resX/gridGraph.sizeX;
        float height = (float)resY/gridGraph.sizeY;
        int x1 = (int)(width*line.x1.n/line.x1.d);
        int y1 = (int)(height*line.y1.n/line.y1.d);
        int x2 = (int)(width*line.x2.n/line.x2.d);
        int y2 = (int)(height*line.y2.n/line.y2.d);
        
        g.setColor(line.color);

        //g2.draw(new Line2D.Float(x1, y1, x2, y2));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(Color.BLACK);
    }

    private void drawLine(Graphics g, GridLineSet.Line line) {
        float width = (float)resX/gridGraph.sizeX;
        float height = (float)resY/gridGraph.sizeY;
        int x1 = (int)(width*line.x1);
        int y1 = (int)(height*line.y1);
        int x2 = (int)(width*line.x2);
        int y2 = (int)(height*line.y2);
        
        g.setColor(line.color);

        //g2.draw(new Line2D.Float(x1, y1, x2, y2));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(Color.BLACK);
    }
}
