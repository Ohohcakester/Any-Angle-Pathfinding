package draw;
import grid.GridGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;


public class DrawCanvas extends JPanel implements MouseListener{
    
    private GridGraphDrawer gridGraphDrawer;
    private GridLineDrawer gridLineDrawer;
    
    public DrawCanvas(GridGraph gridGraph, GridLineSet gridLineSet) {
        super();
        setPreferredSize(new Dimension(600,600));
        gridGraphDrawer = new GridGraphDrawer(gridGraph, 600, 600);
        gridLineDrawer = new GridLineDrawer(gridGraph, gridLineSet, 600, 600);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(Color.BLACK);
        gridGraphDrawer.draw(g);
        gridLineDrawer.draw(g);
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