package draw;

import grid.GridGraph;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class VisualiserMouseControls implements MouseListener {
    private final GridGraph gridGraph;
    private final EditorUI editorUI;
    
    protected VisualiserMouseControls() {
        gridGraph = null;
        editorUI = null;
    }
    
    public VisualiserMouseControls(GridGraph gridGraph, EditorUI editorUI) {
        this.gridGraph = gridGraph;
        this.editorUI = editorUI;
    }
    
    public int toPointX(int px) {
        return (px + (editorUI.resX/gridGraph.sizeX/2)) * gridGraph.sizeX / editorUI.resX;
    }
    
    public int toPointY(int py) {
        //py = editorUI.resY - py;
        return (py + (editorUI.resY/gridGraph.sizeY/2)) * gridGraph.sizeY / editorUI.resY;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = toPointX(e.getX());
        int y = toPointY(e.getY());
        switch(e.getButton()) {
            case MouseEvent.BUTTON1:
                editorUI.addStartPoint(x, y);
                break;
            case MouseEvent.BUTTON2:
            case MouseEvent.BUTTON3:
                editorUI.addEndPoint(x, y);
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

}
  