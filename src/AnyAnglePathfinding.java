import grid.AStar;
import grid.BasicThetaStar;
import grid.GridGraph;
import grid.PathFindingAlgorithm;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import draw.DrawCanvas;
import draw.GridLineSet;
import draw.GridObjects;
import draw.GridPointSet;
import draw.KeyToggler;

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
    
    private static boolean seededRandom = false;
    private static int seed = 5311;
    
    public static Random rand = new Random();
    private static int sizeX = 40;
    private static int sizeY = 40;

    private static int sx = 0;
    private static int sy = 0;
    private static int ex = 10;
    private static int ey = 10;
    /*private static int sx = 0;
    private static int sy = 0;
    private static int ex = 70;
    private static int ey = 42;*/
    
    public static void main(String[] args) {
        
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);
        GridLineSet gridLineSet = new GridLineSet();

        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomBlockMap(gridGraph, 9);
        fillCorners(gridGraph);
        
        GraphImporter graphImporter = new GraphImporter("maze.txt");
        gridGraph = graphImporter.retrieve();
        
        //generateRandomTestLines(gridGraph, gridLineSet, 100);

        //gridGraph.trySetBlocked(sx, sy, false);
        //gridGraph.trySetBlocked(ex, ey, false);

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

        LinkedList<GridObjects> lineSetList = recordAlgorithmOperation(gridGraph, sx, sy, ex, ey);
        lineSetList.addLast(new GridObjects(gridLineSet, null));
        DrawCanvas drawCanvas = new DrawCanvas(gridGraph, gridLineSet);
        
        setupMainFrame(drawCanvas, lineSetList);
    }
    
    private static AStar getAlgo(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        return new BasicThetaStar(gridGraph, sx, sy, ex, ey);
    }


    private static void testAlgorithmSpeed(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        PathFindingAlgorithm algo = getAlgo(gridGraph, sx, sy, ex, ey);
        algo.computePath();
    }

    private static int[][] generatePath(GridGraph gridGraph, int sx, int sy,
            int ex, int ey) {
        AStar aStar = getAlgo(gridGraph, sx, sy, ex, ey);
        aStar.computePath();
        int[][] path = aStar.getPath();
        return path;
    }
    
    private static LinkedList<GridObjects> recordAlgorithmOperation (
            GridGraph gridGraph, int sx, int sy, int ex, int ey) {
        PathFindingAlgorithm algo = getAlgo(gridGraph, sx, sy, ex, ey);
        algo.startRecording();
        algo.computePath();
        algo.stopRecording();
        LinkedList<List<Integer[]>> snapshotList = algo.retrieveSnapshotList();
        LinkedList<GridObjects> gridObjectsList = new LinkedList<>();
        for (List<Integer[]> snapshot : snapshotList) {
            gridObjectsList.add(createGridObjects(snapshot));
        }
        return gridObjectsList;
    }
    
    private static GridObjects createGridObjects(List<Integer[]> snapshot) {
        GridLineSet gridLineSet = new GridLineSet();
        GridPointSet gridPointSet = new GridPointSet();
        for (Integer[] edge : snapshot) {
            if (edge.length == 4) {
                gridLineSet.addLine(edge[0], edge[1], edge[2], edge[3], Color.GRAY);
            } else if (edge.length == 2) {
                gridPointSet.addPoint(edge[0], edge[1], Color.BLUE);
            }
        }
        return new GridObjects(gridLineSet,gridPointSet);
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
    
    private static void setupMainFrame(DrawCanvas drawCanvas, LinkedList<GridObjects> gridObjectsList) {
        KeyToggler keyToggler = new KeyToggler(drawCanvas, gridObjectsList);
        
        JFrame mainFrame = new JFrame();
        mainFrame.add(drawCanvas);
        mainFrame.addKeyListener(keyToggler);
        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
