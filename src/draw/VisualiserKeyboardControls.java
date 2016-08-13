package draw;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import draw.EditorUI.PathComputeMode;

public class VisualiserKeyboardControls implements KeyListener {
    private final HashMap<Integer,Runnable> functionsRelease;
    private final HashMap<Integer,Runnable> functionsPress;
    private final ArrayList<String> descriptions;
    private final EditorUI editorUI;
    
    public VisualiserKeyboardControls(EditorUI editorUI) {
        super();
        this.editorUI = editorUI;
        functionsRelease = new HashMap<>();
        functionsPress = new HashMap<>();
        descriptions = new ArrayList<>();
        initialiseFunctions();
    }
    
    private void initialiseFunctions() {
        add(KeyEvent.VK_ESCAPE, "ESC: Close the window.",
                () -> System.exit(0));
        add(KeyEvent.VK_9, "9: Generates the path file from the currently selected points.",
                editorUI::generatePath);
        add(KeyEvent.VK_0, "0: Generates the maze analysis for the maze.",
                editorUI::generateMazeAnalysis);
        add(KeyEvent.VK_A, "A: Prints the maze analysis for the maze.",
                editorUI::printMazeAnalysis);
        add(KeyEvent.VK_P, "P: Prints the path analysis for the current selected path.",
                editorUI::printPathAnalysis);
        add(KeyEvent.VK_S, "S: Generates a .map and a .scen file from the maze.",
                editorUI::generateScen);
        add(KeyEvent.VK_Z, "Z: Switch mode: Automatically display path between points.",
                () -> editorUI.setPathComputeMode(PathComputeMode.PATH_ONLY));
        add(KeyEvent.VK_X, "X: Switch mode: Automatically display search tree between points.",
                () -> editorUI.setPathComputeMode(PathComputeMode.SEARCH_TREE));
        add(KeyEvent.VK_C, "C: Switch mode: Disable path computation.",
                () -> editorUI.setPathComputeMode(PathComputeMode.NO_COMPUTE));
        addHold(KeyEvent.VK_V, "V: Hold down for real-time pathfinding to mouse location",
                editorUI::onRealTimePathfind, editorUI::offRealTimePathfind);
    }
    
    private void add(int keyCode, String description, Runnable function) {
        functionsRelease.put(keyCode, function);
        descriptions.add(description);
    }
    
    private void addHold(int keyCode, String description, Runnable press, Runnable release) {
        functionsPress.put(keyCode, press);
        functionsRelease.put(keyCode, release);
        descriptions.add(description);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Runnable function = functionsPress.get(e.getKeyCode());
        if (function != null) function.run();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Runnable function = functionsRelease.get(e.getKeyCode());
        if (function != null)
            function.run();
        else
            printHelp();
    }
    
    private void printHelp() {
        for (String description : descriptions) {
            System.out.println(description);
        }
    }

}
