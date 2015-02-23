package draw;
import grid.GridGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;


public class DrawCanvas extends JPanel {

    private static final int MAX_RES = 700;
    public final int resY;
    public final int resX;
    
    private final Drawer gridGraphDrawer;
    private Drawer gridLineDrawer;
    private Drawer gridPointDrawer;
    private Drawer startEndPointDrawer;
    protected final GridGraph gridGraph;
    
    public DrawCanvas(GridGraph gridGraph, GridLineSet gridLineSet) {
        super();
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        if (sizeX < sizeY) {
            resY = MAX_RES;
            resX = resY*sizeX/sizeY;
        } else {
            resX = MAX_RES;
            resY = resX*sizeY/sizeX;
        }
        
        setPreferredSize(new Dimension(resX,resY));
        
        this.gridGraph = gridGraph;
        gridGraphDrawer = new GridGraphDrawer(gridGraph, resX, resY);
        if (gridLineSet != null) {
            gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, resX, resY);
        }
    }
    
    public DrawCanvas(GridGraph gridGraph) {
        this(gridGraph, null);
    }

    public void setStartAndEnd(int sx, int sy, int ex, int ey) {
        GridPointSet gridPointSet = new GridPointSet();
        gridPointSet.addPoint(sx,  sy, Color.ORANGE);
        gridPointSet.addPoint(ex,  ey, Color.YELLOW);
        startEndPointDrawer = new VariableSizePointDrawer(gridGraph,
                gridPointSet, resX, resY, 1.3f);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.BLACK);
        gridGraphDrawer.draw(g);
        if (gridLineDrawer != null) {
            gridLineDrawer.draw(g);
        }
        if (gridPointDrawer != null) {
            gridPointDrawer.draw(g);
        }
        if (startEndPointDrawer != null) {
            startEndPointDrawer.draw(g);
        }
    }
    
    public void changeSet(GridLineSet gridLineSet, GridPointSet gridPointSet) {
        gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, resX, resY);
        gridPointDrawer = new GridPointDrawer(gridGraph, gridPointSet, resX, resY);
        repaint();
    }
    
    public void changeSet(GridPointSet gridPointSet) {
        gridPointDrawer = new GridPointDrawer(gridGraph, gridPointSet, resX, resY);
        repaint();
    }

}