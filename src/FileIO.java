import java.io.FileNotFoundException;
import java.io.PrintWriter;



public class FileIO {
    private PrintWriter printWriter;
    private String SEPARATOR_COLUMN = "\t";
    private String SEPARATOR_ROW = System.lineSeparator();
    
    public FileIO(String filename) {
        try {
            printWriter = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void writeLine(String...strings) {
        String separatorColumn = "";
        for (String string : strings) {
            printWriter.write(separatorColumn);
            printWriter.write(string);
            separatorColumn = SEPARATOR_COLUMN;
        }
        printWriter.write(SEPARATOR_ROW);
    }
    
    public void close() {
        printWriter.close();
    }
    
    public void flush() {
        printWriter.flush();
    }
}