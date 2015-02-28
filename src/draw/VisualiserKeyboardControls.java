package draw;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

public class VisualiserKeyboardControls implements KeyListener {
    private final HashMap<Integer,Runnable> functions;
    private final ArrayList<String> descriptions;
    private final EditorUI editorUI;
    
    public VisualiserKeyboardControls(EditorUI editorUI) {
        super();
        this.editorUI = editorUI;
        functions = new HashMap<>();
        descriptions = new ArrayList<>();
        initialiseFunctions();
    }
    
    private void initialiseFunctions() {
        add(KeyEvent.VK_C, "C: Generates the path file from the currently selected points.",
                () -> editorUI.generatePath());
        add(KeyEvent.VK_A, "A: Generates the maze analysis for the maze.",
                () -> editorUI.generateMazeAnalysis());
        add(KeyEvent.VK_P, "P: Prints the path analysis for the current selected.",
                () -> editorUI.printPathAnalysis());
    }
    
    private void add(int keyCode, String description, Runnable function) {
        functions.put(keyCode, function);
        descriptions.add(description);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Runnable function = functions.get(e.getKeyCode());
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
