package main;
import grid.GridAndGoals;
import main.graphgeneration.DefaultGenerator;
import main.testgen.TestDataGenerator;
import uiandio.GraphImporter;
import algorithms.AStar;
import algorithms.AStarOctileHeuristic;
import algorithms.AStarStaticMemory;
import algorithms.AcceleratedAStar;
import algorithms.AdjustmentThetaStar;
import algorithms.Anya;
import algorithms.BasicThetaStar;
import algorithms.BreadthFirstSearch;
import algorithms.JumpPointSearch;
import algorithms.LazyThetaStar;
import algorithms.RecursiveThetaStar;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.strictthetastar.RecursiveStrictThetaStar;
import algorithms.strictthetastar.StrictThetaStar;
import algorithms.visibilitygraph.BFSVisibilityGraph;

/**
 * Instructions: Look for the main method.
 * We can either run tests or trace the algorithm.
 * 
 * To see a visualisation of an algorithm,
 * 1) Set choice = 0 in the first line of main();
 * 2) Choose a maze in the first line of loadMaze();
 * 3) Choose an algorithm in the first line of setDefaultAlgoFunction();
 * 
 * The tracing / experimentation functions are detailed in the traceAlgorithm() method.
 */
public class AnyAnglePathfinding {
    public static final String PATH_TESTDATA = "testdata/";
    public static final String PATH_MAZEDATA = "mazedata/";
    private static AlgoFunction algoFunction; // The algorithm is stored in this function.

    public static void main(String[] args) {
        int choice = 0; // Choose an operation. 0: Visualisation.run() should be the default.

        switch(choice) {
            case 0:
                Visualisation.run();
                break;
            case 1:
                AlgoTest.run();
                break;
            case 2:
                Experiment.run();
                break;
            case 3:
                TestDataGenerator.run();
                break;
            case 4:
                GridGraphVisualiser.run();
                break;
            case 5:
                TextOutputVisualisation.run();
                break;
        }
    }
    
    /**
     * Choose a maze. (a gridGraph setting)
     */
    static GridAndGoals loadMaze() {
        int choice = 1; // Adjust this to choose a maze.
        
        switch(choice) {
            case 0 : {// UNSEEDED
                int unblockedRatio = 10;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int sizeX = 20;               // x-axis size of grid
                int sizeY = 20;               // y-axis size of grid

                int sx = 10;                  // x-coordinate of start point
                int sy = 13;                  // y-coordinate of start point
                int ex = 6;                   // x-coordinate of goal point
                int ey = 8;                   // y-coordinate of goal point
                return DefaultGenerator.generateUnseeded(sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 1 : { // SEEDED
                int unblockedRatio = 17;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int seed = 1667327427;        // seed for the random.
                
                int sizeX = 40;               // x-axis size of grid
                int sizeY = 40;               // y-axis size of grid
                int sx = 6;                   // x-coordinate of start point
                int sy = 10;                  // y-coordinate of start point
                int ex = 39;                  // x-coordinate of goal point
                int ey = 32;                  // y-coordinate of goal point
                return DefaultGenerator.generateSeeded(seed, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 2 :
                return GraphImporter.importGraphFromFile("maze.txt", 25, 17, 2, 9);
            case 3 :
                return DefaultGenerator.generateSeededOld(-98783479, 40, 40, 7, 1, 4, 18, 18); // maze 3
            case 4 :
                return DefaultGenerator.generateSeededOld(-565315494, 15, 15, 9, 1, 2, 1, 13); // maze 2
            case 5 :
                return DefaultGenerator.generateSeededOld(53, 15, 15, 9, 0, 0, 10, 14); // maze 1
            case 6 :
                return DefaultGenerator.generateSeededOld(-159182402, 15, 15, 9, 1, 1, 13, 12); // anya previously gave incorrect path
            case 7 :
                return GraphImporter.importGraphFromFile("maze14x11.txt", 0, 0, 10, 10); // Maze to contradict Theta* / A* optimality
            case 8 :
                return GraphImporter.importGraphFromFile("mazeWCS.txt", 2, 0, 28, 25); // Worst Case Scenario path length.
            case 9 :
                return DefaultGenerator.generateSeededOld(-410889275, 15, 15, 7, 0, 1, 10, 12); // maze 4
            case 10 :
                return GraphImporter.importGraphFromFile("mazeThetaWCS.txt", 0, 0, 28, 13); // Worst Case Scenario for Theta*
            case 11 :
                return GraphImporter.importGraphFromFile("mazeReuseWCS.txt", 1, 28, 0, 27); // Worst Case Scenario for Visibility Graph reuse.
            case 12 :
                return GraphImporter.importGraphFromFile("anyaCont2.txt", 1, 6, 9, 1); // difficult case for anya
            case 13 :
                return DefaultGenerator.generateSeededOld(-1155797147, 47, 32, 38, 46, 30, 20, 1); // issue for Strict Theta*
            case 14 :
                return DefaultGenerator.generateSeededOld(-1155849806, 11, 13, 40, 7, 12, 9, 0); // Strict Theta* longer than Basic Theta*
            case 15 :
                return GraphImporter.loadStoredMaze("sc2_losttemple", "66-83_117-53");
            case 16 :
                return GraphImporter.importGraphFromFile("custommaze.txt", 1, 1, 7, 4);
            case 17 :
                return GraphImporter.importGraphFromFile("custommaze3.txt", 1, 19, 29, 2);
            case 18 :
                return GraphImporter.loadStoredMaze("baldursgate_AR0402SR", "9-45_44-22");
            case 19 :
                return DefaultGenerator.generateSeeded(-1131220420, 12, 13, 37, 5, 13, 2, 3); // Issue for Strict Theta* at goal
            case 20 :
                return GraphImporter.importGraphFromFile("custommaze4.txt", 2, 4, 10, 2);
            case 21 :
                return DefaultGenerator.generateSeededTrueRandomGraph(-1186265305, 15, 9, 12, 7,8, 4,2);
            case 22 :
                //return DefaultGenerator.generateSeededTrueRandomGraph(-1186644456, 6,5, 14, 0,2, 5,0);
                return DefaultGenerator.generateSeededTrueRandomGraph(-1185836518, 30,9, 3, 1,0, 7,9);
            case 23 :
                return DefaultGenerator.generateSeeded(138863256, 200, 200, 7, 59, 179, 160, 35); // Good large dense graph with indirect path
            case 24 :
                return DefaultGenerator.generateSeeded(138863256, 200, 200, 7, 160, 35, 59, 179); // Same graph, opposite direction
            case 25 :
                return DefaultGenerator.generateSeeded(-1878652012, 200, 200, 7, 59, 179, 160, 35); // Good large dense graph with indirect path
            case 26:
                return GraphImporter.loadStoredMaze("corr2_maze512-2-5", "171-149_313-324");
            case 27 :
                return DefaultGenerator.generateSeeded(-1270138724, 17, 16, 26, 12, 16, 8, 0); // Edge case for Incremental VG upper bound check
            case 28 :
                return DefaultGenerator.generateSeeded(250342248, 67, 33, 17, 3, 28, 47, 32); // Difficult case for Incremental VG lower bound check
            case 29 :
                return DefaultGenerator.generateSeeded(-13991511, 80, 80, 7, 26, 37, 52, 54); // Restricted VG Inefficient
            case 30:
                return GraphImporter.loadStoredMaze("def_iQCWUDHB_iED_iED_iP", "1-42_75-81");
            case 31:
                //return DefaultGenerator.generateSeeded(-1131088503, 8, 11, 27, 4, 11, 7, 4); // Strict Theta* with high buffer finds a much longer path.
                return GraphImporter.importGraphFromFile("mazehighbufferbad.txt", 4, 8, 7, 1); // Strict Theta* with high buffer finds a much longer path.
            case 32:
                return GraphImporter.loadStoredMaze("corr2_maze512-2-1", "219-187_186-334");
            case 33 :
                return GraphImporter.importGraphFromFile("anyaCont2b.txt", 9, 9, 9, 1); // difficult case for anya
            default :
                return null;
        }
    }
    
    /**
     * Choose an algorithm.
     */
    static AlgoFunction setDefaultAlgoFunction() {
        int choice = 8; // adjust this to choose an algorithm
        
        switch (choice) {
            case 1 :
                algoFunction = AStar::new;
                break;
            case 2 :
                algoFunction = BreadthFirstSearch::new;
                break;
            case 3 :
                algoFunction = BreadthFirstSearch::postSmooth;
                break;
            case 4 :
                algoFunction = AStar::postSmooth;
                break;
            case 5 :
                algoFunction = AStar::dijkstra;
                break;
            case 6 :
                algoFunction = Anya::new;
                break;
            case 7 :
                algoFunction = VisibilityGraphAlgorithm::new;
                break;
            case 8 :
                algoFunction = BasicThetaStar::new;
                break;
            case 9 :
                algoFunction = BasicThetaStar::noHeuristic;
                break;
            case 10 :
                algoFunction = BasicThetaStar::postSmooth;
                break;
            case 11 :
                algoFunction = AcceleratedAStar::new;
                break;
            case 12 :
                algoFunction = VisibilityGraphAlgorithm::graphReuse;
                break;
            case 13 :
                algoFunction = AdjustmentThetaStar::new;
                break;
            case 14 :
                algoFunction = StrictThetaStar::new;
                break;
            case 15 :
                algoFunction = RecursiveStrictThetaStar::new;
                break;
            case 16 :
                algoFunction = BFSVisibilityGraph::graphReuse;
                break;
            case 17 :
                algoFunction = null; // reserved
                break;
            case 18 :
                algoFunction = null; // reserved
                break;
            case 19 :
                algoFunction = LazyThetaStar::new;
                break;
            case 20 :
                algoFunction = AStarOctileHeuristic::new;
                break;
            case 21 :
                algoFunction = AStarOctileHeuristic::postSmooth;
                break;
            case 22 :
                algoFunction = JumpPointSearch::new;
                break;
            case 23 :
                algoFunction = JumpPointSearch::postSmooth;
                break;
            case 24 :
                algoFunction = AStarStaticMemory::new;
                break;
            case 25 :
                break;
            case 26 :
                break;
            case 27 :
                algoFunction = null; // reserved
                break;
            case 28 :
                algoFunction = RecursiveThetaStar::new;
                break;
        }
        
        return algoFunction;
    }
}
