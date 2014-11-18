package uiandio;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class CloseOnExitWindowListener implements WindowListener {

    public CloseOnExitWindowListener() {
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub
        
    }
}
