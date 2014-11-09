package draw;
import grid.GridGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;


public class DrawCanvas extends JPanel implements MouseListener{
    
    private static final int RES_Y = 600;
    private static final int RES_X = 600;
    
    private final Drawer gridGraphDrawer;
    private Drawer gridLineDrawer;
    private Drawer gridPointDrawer;
    private final GridGraph gridGraph;
    
    public DrawCanvas(GridGraph gridGraph, GridLineSet gridLineSet) {
        super();
        setPreferredSize(new Dimension(RES_X,RES_Y));
        
        this.gridGraph = gridGraph;
        gridGraphDrawer = new GridGraphDrawer(gridGraph, RES_X, RES_Y);
        gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, RES_X, RES_Y);
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
    }
    
    public void changeSet(GridLineSet gridLineSet, GridPointSet gridPointSet) {
        gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, RES_X, RES_Y);
        gridPointDrawer = new GridPointDrawer(gridGraph, gridPointSet, RES_X, RES_Y);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}