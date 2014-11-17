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
        gridObjectsList.addFirst(null);
        System.out.println(gridObjectsList.size());
    }
    
    /*public void addGridLineSet(GridLineSet gridLineSet) {
        lineSetList.add(gridLineSet);
        displayLast();
    }*/
    
    private void goLeft(int amount, boolean stopAtEnd) {
        for (int i=0; i<amount; i++) {
            rotateLeft(stopAtEnd);
        }
        displayLast();
    }

    private void rotateLeft(boolean stopAtEnd) {
        if (gridObjectsList.get(gridObjectsList.size()-2) == null) {
            if (!stopAtEnd) {
                gridObjectsList.addFirst(gridObjectsList.removeLast());
                gridObjectsList.addFirst(gridObjectsList.removeLast());
            }
        } else {
            gridObjectsList.addFirst(gridObjectsList.removeLast());
        }
    }
    
    private void goRight(int amount, boolean stopAtEnd) {
        for (int i=0; i<amount; i++) {
            rotateRight(stopAtEnd);
        }
        displayLast();
    }

    private void rotateRight(boolean stopAtEnd) {
        if (gridObjectsList.getFirst() == null) {
            if (!stopAtEnd) {
                gridObjectsList.addLast(gridObjectsList.removeFirst());
                gridObjectsList.addLast(gridObjectsList.removeFirst());
            }
        } else {
            gridObjectsList.addLast(gridObjectsList.removeFirst());
        }
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
                goRight(1, false);
                break;
            case KeyEvent.VK_LEFT :
                goLeft(1, false);
                break;
            case KeyEvent.VK_UP :
                goRight(1, true);
                break;
            case KeyEvent.VK_DOWN :
                goLeft(1, true);
                break;
            case KeyEvent.VK_PAGE_UP :
                goRight(10, false);
                break;
            case KeyEvent.VK_PAGE_DOWN :
                goLeft(10, false);
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
