import grid.AStar;
import grid.BasicThetaStar;
import grid.GridGraph;

import java.awt.Color;
import java.util.Random;

import javax.swing.JFrame;

import draw.DrawCanvas;
import draw.GridLineSet;

/**
 * 5000 Runs, seed 51, frequency 11 on 40x40 BlockMap with fillCorners.
 * Generate path from 15,25 to 34,3
 * 
 * Dijkstra (heuristic 0) : 9434 +/- 87
 * A* (heuristic 1) : 2573 +/- 55
 * 
 * 
 * 
 * @author Oh
 *
 */
public class AnyAnglePathfinding {
    
    private static boolean seededRandom = true;;
    private static int seed = 5311;
    
    public static Random rand = new Random();
    private static int sizeX = 80;
    private static int sizeY = 80;
    
    public static void main(String[] args) {
        
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);
        GridLineSet gridLineSet = new GridLineSet();

        if (seededRandom) {
            rand = new Random(seed);    
        }
        
        generateRandomBlockMap(gridGraph, 11);
        fillCorners(gridGraph);
        
        //generateRandomTestLines(gridGraph, gridLineSet, 100);

        int sx = 26;
        int sy = 57;
        int ex = 56;
        int ey = 18;
        gridGraph.setBlocked(sx, sy, false);
        gridGraph.setBlocked(ex, ey, false);

        //testSpeed(gridGraph, sx, sy, ex, ey);
        //testSpeed(gridGraph, sx, sy, ex, ey);
        
        int[][] path = generatePath(gridGraph, sx, sy, ex, ey);
        
        float pathLength = 0;
        for (int i=0; i<path.length-1; i++) {
            gridLineSet.addLine(path[i][0], path[i][1],
                    path[i+1][0], path[i+1][1], Color.BLUE);
            
            pathLength += gridGraph.distance(path[i][0], path[i][1],
                            path[i+1][0], path[i+1][1]);
        }
        System.out.println("Path Length: " + pathLength);
        
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        setupMainFrame(drawCanvas);
    }


    private static void testAlgorithmSpeed(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        new BasicThetaStar(gridGraph, sx, sy, ex, ey);
    }

    private static int[][] generatePath(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        AStar aStar = new BasicThetaStar(gridGraph, sx, sy, ex, ey);
        int[][] path = aStar.getPath();
        return path;
    }

    private static void testSpeed(GridGraph gridGraph,
            int sx, int sy, int ex, int ey) {

        int sampleSize = 30;
        int[] data = new int[sampleSize];
        
        int sum = 0;
        long sumSquare = 0;
        
        for (int s = 0; s < sampleSize; s++) {
            long start = System.currentTimeMillis();
            for (int i=0; i<500; i++) {
                testAlgorithmSpeed(gridGraph, sx, sy, ex, ey);
            }
            long end = System.currentTimeMillis();
            
            data[s] = (int)(end-start);
            
            sum += data[s];
            sumSquare += data[s]*data[s];
        }
        
        double expectation = (double)sum / sampleSize;
        double secondMomentTimesN = (double)sumSquare;
        double varianceTimesN = secondMomentTimesN - sampleSize*(expectation*expectation);
        double standardDeviation = Math.sqrt(varianceTimesN / (sampleSize-1));
        
        System.out.println(expectation*10 + " (+/-" + standardDeviation*10 + ")");
        
    }

    private static void generateRandomTestLines(GridGraph gridGraph,
            GridLineSet gridLineSet, int amount) {
        
        for (int i=0; i<amount; i++) {
            int x1 = rand.nextInt(sizeX);
            int y1 = rand.nextInt(sizeY);
            int x2 = rand.nextInt(sizeX);
            int y2 = rand.nextInt(sizeY);

            testAndAddLine(x1,y1,x2,y2,gridGraph,gridLineSet);
        }
    }

    private static void generateRandomMap(GridGraph gridGraph, int frequency) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                gridGraph.setBlocked(x, y, rand.nextInt()%frequency == 0);
               
                /*if ((x + y) % 5 == 0 && (x % 4 == 0 || y % 5 == 0)) {
                    gridGraph.setBlocked(x, y, true);
                }*/
            }
        }
    }

    private static void fillCorners(GridGraph gridGraph) {
        boolean didSomething = true;;
        while (didSomething) {
            didSomething = false;
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if (gridGraph.isBlocked(x, y)) {
                        if (gridGraph.isValidBlock(x+1, y+1) && gridGraph.isBlocked(x+1, y+1)) {
                            if (!gridGraph.isBlocked(x+1, y) && !gridGraph.isBlocked(x, y+1)) {
                                if (rand.nextBoolean())
                                    gridGraph.setBlocked(x+1, y, true);
                                else
                                    gridGraph.setBlocked(x, y+1, true);
                                didSomething = true;
                            }
                        }

                        if (gridGraph.isValidBlock(x-1, y+1) && gridGraph.isBlocked(x-1, y+1)) {
                            if (!gridGraph.isBlocked(x-1, y) && !gridGraph.isBlocked(x, y+1)) {
                                if (rand.nextBoolean())
                                    gridGraph.setBlocked(x-1, y, true);
                                else
                                    gridGraph.setBlocked(x, y+1, true);
                                didSomething = true;
                            }
                        }
                    }
                }
            }
        }
    }


    private static void generateRandomBlockMap(GridGraph gridGraph, int frequency) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (rand.nextInt(frequency) == 0) {
                    switch(rand.nextInt(3)) {
                        case 0:
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x, y+1, true);
                            gridGraph.trySetBlocked(x+1, y, true);
                            gridGraph.trySetBlocked(x+1, y+1, true);
                            break;
                        case 1:
                            gridGraph.trySetBlocked(x, y-1, true);
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x, y+1, true);
                            break;
                        case 2:
                            gridGraph.trySetBlocked(x-1, y, true);
                            gridGraph.trySetBlocked(x, y, true);
                            gridGraph.trySetBlocked(x+1, y, true);
                            break;
                    }
                }
               
                /*if ((x + y) % 5 == 0 && (x % 4 == 0 || y % 5 == 0)) {
                    gridGraph.setBlocked(x, y, true);
                }*/
            }
        }
    }

    private static void testAndAddLine(int x1, int y1, int x2, int y2,
            GridGraph gridGraph, GridLineSet gridLineSet) {
        
        if (gridGraph.lineOfSight(x1, y1, x2, y2)) {
            gridLineSet.addLine(x1, y1, x2, y2, Color.GREEN);
        } else {
            gridLineSet.addLine(x1, y1, x2, y2, Color.RED);
        }
        
    }
    
    private static void setupMainFrame(DrawCanvas drawCanvas) {
        JFrame mainFrame = new JFrame();
        mainFrame.add(drawCanvas);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
