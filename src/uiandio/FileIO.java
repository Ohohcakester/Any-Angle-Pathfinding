package uiandio;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * Use to write test data to a file.<br>
 * The writeLine method is convenient for writing text output that can be copied
 * into a spreadsheet easily, as strings are separated by tab (\t) characters.
 */
public class FileIO {
    private PrintWriter printWriter;
    private String SEPARATOR_COLUMN = "\t";
    private String SEPARATOR_ROW = System.lineSeparator();
    
    public static boolean makeDirs(String path) {
        return (new File(path)).mkdirs();
    }

    public FileIO(String filename) {
        try {
            printWriter = new PrintWriter(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static FileIO csv(String filename) {
        FileIO fileIO = new FileIO(filename);
        fileIO.SEPARATOR_COLUMN = ",";
        return fileIO;
    }
    
    public void writeLine(String string) {
        printWriter.write(string);
        printWriter.write(SEPARATOR_ROW);
    }
    
    public void writeRow(String...strings) {
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