package uiandio;
import grid.GridAndGoals;
import grid.GridGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * How to create a grid file.<br>
 * The grid is stored in a .txt file in the following format:<br>
 * <pre>
 * 6 3
 * 0 1 0 0 1 0
 * 0 1 1 1 1 0
 * 0 1 0 0 1 0</pre>
 * 
 * The first number specifies the number of columns of the grid.<br>
 * The second number specifies the number of rows of the grid.<br>
 * The rest of the numbers specify the status of the tiles. 1 means a blocked
 * tile, 0 means an unblocked tile.
 */
public class GraphImporter {
    private GridGraph gridGraph;
    
    private GraphImporter(String fileName) {
        boolean[][] result = null;

        File file = new File(fileName);
        try {
            FileReader fileReader = new FileReader(file);
            Scanner sc = new Scanner(fileReader);
            int x = sc.nextInt();
            int y = sc.nextInt();
            result = new boolean[y][];
            for (int i=0; i<y; i++) {
                result[i] = new boolean[x];
                for (int j=0; j<x; j++) {
                    result[i][j] = (sc.nextInt() != 0);
                }
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        create(result);          
    }

    private void create(boolean[][] result) {
        gridGraph = new GridGraph(result[0].length, result.length);
        for (int y=0;y<result.length;y++) {
            for (int x=0;x<result[0].length;x++) {
                gridGraph.setBlocked(x, y, result[y][x]);
            }
        }
    }

    private void createDoubleSize(boolean[][] result) {
        int size = 2;
        
        gridGraph = new GridGraph(result[0].length*size, result.length*size);
        for (int y=0;y<result.length*size;y++) {
            for (int x=0;x<result[0].length*size;x++) {
                gridGraph.setBlocked(x, y, result[y/size][x/size]);
            }
        }
    }
    
    private GridGraph retrieve() {
        return gridGraph;
    }

    /**
     * Import a graph from a file in the AnyAnglePathFinding directory.
     * Look into the GraphImporter documentation for details on how to create a grid file.
     */
    public static GridGraph importGraphFromFile(String filename) {
        GridGraph gridGraph;
        GraphImporter graphImporter = new GraphImporter(filename);
        gridGraph = graphImporter.retrieve();
        return gridGraph;
    }

    /**
     * Import a graph from a file in the AnyAnglePathFinding directory,
     * and also set the start and goal points.
     */
    public static GridAndGoals importGraphFromFile(String filename, int sx, int sy, int ex, int ey) {
    	GridGraph gridGraph = GraphImporter.importGraphFromFile(filename);
    	return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
}
