package draw;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

public class KeyToggler implements KeyListener {
    private final LinkedList<GridObjects> gridObjectsList;
    private final DrawCanvas drawCanvas;
    
    public KeyToggler(DrawCanvas drawCanvas, LinkedList<GridObjects> gridObjectsList) {
        this.drawCanvas = drawCanvas;
        this.gridObjectsList = gridObjectsList;
        System.out.println(gridObjectsList.size());
    }
    
    /*public void addGridLineSet(GridLineSet gridLineSet) {
        lineSetList.add(gridLineSet);
        displayLast();
    }*/
    
    private void goLeft() {
        gridObjectsList.addFirst(gridObjectsList.removeLast());
        displayLast();
    }
    
    private void goRight() {
        gridObjectsList.addLast(gridObjectsList.removeFirst());
        displayLast();
    }
    
    private void displayLast() {
        GridObjects gridObjects = gridObjectsList.getLast();
        GridLineSet gridLineSet = gridObjects.gridLineSet;
        GridPointSet gridPointSet = gridObjects.gridPointSet;
        drawCanvas.changeSet(gridLineSet, gridPointSet);

        /*if (gridLineSet != null)
            System.out.println("lines = " + gridObjects.gridLineSet.getLineList().size());
        if (gridPointSet != null)
            System.out.println("points = " + gridObjects.gridPointSet.getPointList().size());*/
    }
    
    @Override
    public void keyPressed(KeyEvent arg0) {
        switch(arg0.getKeyCode()) {
            case KeyEvent.VK_RIGHT :
                goRight();
                break;
            case KeyEvent.VK_LEFT :
                goLeft();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
        
    }

}
