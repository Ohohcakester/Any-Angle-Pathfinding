package uiandio;
import grid.GridGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class GraphImporter {
    private GridGraph gridGraph;
    
    public GraphImporter(String fileName) {
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

    protected void create(boolean[][] result) {
        gridGraph = new GridGraph(result[0].length, result.length);
        for (int y=0;y<result.length;y++) {
            for (int x=0;x<result[0].length;x++) {
                gridGraph.setBlocked(x, y, result[y][x]);
            }
        }
    }

    protected void createDoubleSize(boolean[][] result) {
        int size = 2;
        
        gridGraph = new GridGraph(result[0].length*size, result.length*size);
        for (int y=0;y<result.length*size;y++) {
            for (int x=0;x<result[0].length*size;x++) {
                gridGraph.setBlocked(x, y, result[y/size][x/size]);
            }
        }
    }
    
    public GridGraph retrieve() {
        return gridGraph;
    }
}
