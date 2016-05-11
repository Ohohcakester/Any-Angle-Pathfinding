package draw;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class KeyToggler implements KeyListener {
    private final ArrayDeque<GridObjects> gridObjectsList;
    private final DrawCanvas drawCanvas;
    private static int imgCount = 0;
    
    public KeyToggler(DrawCanvas drawCanvas, ArrayList<GridObjects> gridArrayList) {
        this.drawCanvas = drawCanvas;
        gridObjectsList = new ArrayDeque<GridObjects>(gridArrayList);
        gridObjectsList.addLast(GridObjects.nullObject());
        System.out.println(gridObjectsList.size());
        goRight(1, false);
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
    
    private void takeSnapShot() {
        imgCount++;
        JPanel panel = drawCanvas;
        
        (new File("snapshots/")).mkdirs();
        BufferedImage bufImage = new BufferedImage(panel.getSize().width, panel.getSize().height,BufferedImage.TYPE_INT_RGB);
        panel.paint(bufImage.createGraphics());
        File imageFile = new File("snapshots/"+imgCount+".png");
        try {
            imageFile.createNewFile();
            ImageIO.write(bufImage, "png", imageFile);
            System.out.println("Snapshot " + imgCount);
        } catch(Exception ex) {
            System.out.println("Unable to take snapshot: " + ex.getMessage());
        }
        
     }

    private void rotateLeft(boolean stopAtEnd) {
        if (peekSecondLast(gridObjectsList).isNull()) {
            if (!stopAtEnd) {
                gridObjectsList.addFirst(gridObjectsList.removeLast());
                gridObjectsList.addFirst(gridObjectsList.removeLast());
            }
        } else {
            gridObjectsList.addFirst(gridObjectsList.removeLast());
        }
    }
    
    private GridObjects peekSecondLast(ArrayDeque<GridObjects> list) {
        GridObjects top = list.removeLast();
        GridObjects peek = list.peekLast();
        list.addLast(top);
        return peek;
    }
    
    private boolean goRight(int amount, boolean stopAtEnd) {
        boolean moved = false;
        for (int i=0; i<amount; i++) {
            moved = rotateRight(stopAtEnd) ? true : moved;
        }
        displayLast();
        return moved;
    }

    private boolean rotateRight(boolean stopAtEnd) {
        if (gridObjectsList.getFirst().isNull()) {
            if (!stopAtEnd) {
                gridObjectsList.addLast(gridObjectsList.removeFirst());
                gridObjectsList.addLast(gridObjectsList.removeFirst());
                return true;
            }
            return false;
        } else {
            gridObjectsList.addLast(gridObjectsList.removeFirst());
            return true;
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
            case KeyEvent.VK_D :
                goRight(3, true);
                break;
            case KeyEvent.VK_A :
                goLeft(3, true);
                break;
            case KeyEvent.VK_W :
                goRight(5, true);
                break;
            case KeyEvent.VK_S :
                goLeft(5, true);
                break;
                

                // O: Go right one step + take screenshot, loops around
            case KeyEvent.VK_O :
                if (goRight(1, false))
                    takeSnapShot();
                break;

                // P: Go right one step + take screenshot, stop at end
            case KeyEvent.VK_P :
                if (goRight(1, true))
                    takeSnapShot();
                break;

                // L: Go right multiple steps + take screenshot, stop at end
            case KeyEvent.VK_L :
                if (goRight(13, true))
                    takeSnapShot();
                break;
                // Esc: Close Window
            case KeyEvent.VK_ESCAPE :
                System.exit(0);
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
