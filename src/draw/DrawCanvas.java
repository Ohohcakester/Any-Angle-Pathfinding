package draw;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import grid.GridGraph;


public class DrawCanvas extends JPanel {

    // DRAWING CONFIGURATION
    private static final int MAX_RES = 700;
    
    private static final Color UNBLOCKEDTILE_COLOR = Color.WHITE;
    private static final Color BLOCKEDTILE_COLOR = new Color(127,127,127);
    
    private static final Color STARTPOINT_COLOR = new Color(0,160,0);
    private static final Color ENDPOINT_COLOR = Color.YELLOW;
    // DRAWING CONFIGURATION - END
    
    public final int resY;
    public final int resX;
    
    private BufferedImage cachedGridGraphImage;
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
        gridPointSet.addPoint(sx,  sy, STARTPOINT_COLOR);
        gridPointSet.addPoint(ex,  ey, ENDPOINT_COLOR);
        startEndPointDrawer = new VariableSizePointDrawer(gridGraph,
                gridPointSet, resX, resY, 1.3f);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        clearToColor(g);
        
        if (cachedGridGraphImage == null) {
            cachedGridGraphImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g2 = cachedGridGraphImage.getGraphics();
            g2.setColor(BLOCKEDTILE_COLOR);
            gridGraphDrawer.draw(g2);
        }
        g.drawImage(cachedGridGraphImage, 0, 0, this);
        
        
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
    
    private void clearToColor(Graphics g) {
        g.setColor(UNBLOCKEDTILE_COLOR);
        g.fillRect(0,0,resX,resY);
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

    public void changeSet(GridLineSet gridLineSet) {
        gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, resX, resY);
        repaint();
    }
    
}