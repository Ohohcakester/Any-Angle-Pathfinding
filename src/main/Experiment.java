package main;

import grid.GridGraph;
import grid.ReachableNodes;
import grid.StartGoalPoints;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import main.graphgeneration.DefaultGenerator;
import main.utility.Utility;
import uiandio.FileIO;
import uiandio.GraphImporter;
import algorithms.AStar;
import algorithms.Anya;
import algorithms.BasicThetaStar;
import algorithms.JumpPointSearch;
import algorithms.VisibilityGraphAlgorithm;
import algorithms.datatypes.Point;
import algorithms.strictthetastar.RecursiveStrictThetaStar;
import algorithms.strictthetastar.StrictThetaStar;
import algorithms.visibilitygraph.VisibilityGraph;
import draw.GridLineSet;

public class Experiment {
    
    public static void run() {
//        testVisibilityGraphSize();
//        testAbilityToFindGoal();
//        findStrictThetaStarIssues();
//        findUpperBound();
        testAlgorithmOptimality();
        //testAgainstReferenceAlgorithm();
        //countTautPaths();
//        other();
    }
    
    /**
     * Custom code for experiments.
     */
    public static void other() {
        // This is how to generate test data for the grid. (Use the VisibilityGraph algorithm to generate optimal path lengths)
//      ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
//      System.out.println(points.size());
//
//      generateRandomTestDataAndPrint(gridGraph);

      //This is how to conduct a running time / path length test for tha algorithm:
//      TestResult test1 = testAlgorithm(gridGraph, sx, sy, ex, ey, 1, 1);
//      System.out.println(test1);
//      TestResult test2 = testAlgorithm(gridGraph, sx, sy, ex, ey, 30, 25);
//      System.out.println(test2);
    }

    /**
     * Generates and prints out random test data for the gridGraph in question. <br>
     * Note: the algorithm used is the one specified in the algoFunction.
     * Use setDefaultAlgoFunction to choose the algorithm.
     * @param gridGraph the grid to test.
     */
    private static void generateRandomTestDataAndPrint(GridGraph gridGraph) {
        AlgoFunction algo = AnyAnglePathfinding.setDefaultAlgoFunction();
        ArrayList<Point> points = ReachableNodes.computeReachable(gridGraph, 5, 5);
    
        LinkedList<Integer> startX = new LinkedList<>();
        LinkedList<Integer> startY = new LinkedList<>();
        LinkedList<Integer> endX = new LinkedList<>();
        LinkedList<Integer> endY = new LinkedList<>();
        LinkedList<Double> length = new LinkedList<>();
        
        int size = points.size();
        System.out.println("Points: " + size);
        
        for (int i=0; i<100; i++) {
            Random random = new Random();
            int first = random.nextInt(size);
            int last = random.nextInt(size-1);
            if (last == first) last = size-1; // prevent first and last from being the same
    
            Point s = points.get(first);
            Point f = points.get(last);
            int[][] path = Utility.generatePath(algo, gridGraph, s.x, s.y, f.x, f.y);
            if (path.length >= 2) {
                double len = Utility.computePathLength(gridGraph, path);
                startX.offer(s.x);
                startY.offer(s.y);
                endX.offer(f.x);
                endY.offer(f.y);
                length.offer(len);
            }
            if (i%10 == 0) System.out.println("Computed: " + i);
        }
        System.out.println(startX);
        System.out.println(startY);
        System.out.println(endX);
        System.out.println(endY);
        System.out.println(length);
    }

    /**
     * Returns true iff there is a path from the start to the end. Uses the current algorithm to check.<br>
     * Use setDefaultAlgoFunction to choose the algorithm.
     */
    private static boolean hasSolution(AlgoFunction algo, GridGraph gridGraph, StartGoalPoints p) {
        int[][] path = Utility.generatePath(algo, gridGraph, p.sx, p.sy, p.ex, p.ey);
        return path.length > 1;
    }

    /**
     * Generates random lines on the map. Used to test whether the line-of-sight
     * algorithm is correct. Returns a gridLineSet containing all the test lines.
     */
    private static GridLineSet generateRandomTestLines(GridGraph gridGraph,
            int amount) {
        GridLineSet gridLineSet = new GridLineSet();
        
        Random rand = new Random();
        for (int i=0; i<amount; i++) {
            int x1 = rand.nextInt(gridGraph.sizeX);
            int y1 = rand.nextInt(gridGraph.sizeY);
            int x2 = rand.nextInt(gridGraph.sizeX);
            int y2 = rand.nextInt(gridGraph.sizeY);
    
            Experiment.testAndAddLine(x1,y1,x2,y2,gridGraph,gridLineSet);
        }
        
        return gridLineSet;
    }

    /**
     * Tests a set of coordinates for line-of-sight. Adds a green line to the 
     * gridLineSet if there is line-of-sight between (x1,y1) and (x2,y2).
     * Adds a red line otherwise.
     */
    private static void testAndAddLine(int x1, int y1, int x2, int y2,
            GridGraph gridGraph, GridLineSet gridLineSet) {
        
        if (gridGraph.lineOfSight(x1, y1, x2, y2)) {
            gridLineSet.addLine(x1, y1, x2, y2, Color.GREEN);
        } else {
            gridLineSet.addLine(x1, y1, x2, y2, Color.RED);
        }
    }

    private static void testAgainstReferenceAlgorithm() {
        AnyAnglePathfinding.setDefaultAlgoFunction();
        
        AlgoFunction currentAlgo = AnyAnglePathfinding.setDefaultAlgoFunction();
        AlgoFunction referenceAlgo = AStar::new;
    
        Random seedRand = new Random(3);
        int initial = seedRand.nextInt();
        for (int i=0; i<500000; i++) {
            int sizeX = seedRand.nextInt(70) + 10;
            int sizeY = seedRand.nextInt(70) + 10;
            int seed = i+initial;
            int ratio = seedRand.nextInt(40) + 5;
            
            int max = (sizeX+1)*(sizeY+1);
            int p1 = seedRand.nextInt(max);
            int p2 = seedRand.nextInt(max-1);
            if (p2 == p1) {
                p2 = max-1;
            }
            
            int sx = p1%(sizeX+1);
            int sy = p1/(sizeX+1);
            int ex = p2%(sizeX+1);
            int ey = p2/(sizeX+1);
    
            GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnlyOld(seed, sizeX, sizeY, ratio);
            int[][] path = Utility.generatePath(referenceAlgo, gridGraph, sx, sy, ex, ey);
            double referencePathLength = Utility.computePathLength(gridGraph, path);
            boolean referenceValid = (referencePathLength > 0.00001f);
    
            path = Utility.generatePath(currentAlgo, gridGraph, sx, sy, ex, ey);
            double algoPathLength = Utility.computePathLength(gridGraph, path);
            boolean algoValid = (referencePathLength > 0.00001f);
            
            if (referenceValid != algoValid) {
                System.out.println("============");
                System.out.println("Validity Discrepancy Discovered!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                System.out.println("Reference: " + referenceValid + " , Current: " + algoValid);
                System.out.println("============");
                throw new UnsupportedOperationException("DISCREPANCY!!");
            } else {
                if (Math.abs(algoPathLength - referencePathLength) > 0.0001) {
                    System.out.println("============");
                    System.out.println("Path Length Discrepancy Discovered!");
                    System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                    System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                    System.out.println("Reference: " + referencePathLength + " , Current: " + algoPathLength);
                    System.out.println("============");
                    throw new UnsupportedOperationException("DISCREPANCY!!");
                }
                if (i%1000 == 0)
                    System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
            }
        }
        
    }

    /**
     * Tests random generated maps of various sizes and obstacle densities for
     * the size of the visibility graphs.<br>
     * The results are output to the file VisibilityGraphSizes.txt.
     */
    private static void testVisibilityGraphSize() {
        FileIO fileIO = new FileIO(AnyAnglePathfinding.PATH_TESTDATA + "VisibilityGraphSizes.txt");
        Random seedGenerator = new Random();
        
        fileIO.writeRow("Seed", "Size", "%Blocked", "Vertices", "Edges (Directed)");
        for (int i=0; i<30; i++) {
            for (int r=0; r<3; r++) {
                int currentRatio = (r == 0 ? 7 : (r == 1 ? 15 : 50));
                        
                int currentSize = 10 + i*10;
                int currentSeed = seedGenerator.nextInt();
                
                GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(currentSeed, currentSize, currentSize, currentRatio, 0, 0, currentSize, currentSize);
                VisibilityGraph vGraph = new VisibilityGraph(gridGraph, 0, 0, currentSize, currentSize);
                vGraph.initialise();
                
                String seedString = currentSeed + "";
                String sizeString = currentSize + "";
                String ratioString = gridGraph.getPercentageBlocked()*100f + "";
                String verticesString = vGraph.size() + "";
                String edgesString = vGraph.computeSumDegrees() + "";
                
                fileIO.writeRow(seedString, sizeString, ratioString, verticesString, edgesString);
                fileIO.flush();
            }
        }
        fileIO.close();
    }


    private static void findUpperBound() {
        System.out.println("Strict Theta Star");
        AlgoFunction testAlgo = (gridGraph, sx, sy, ex, ey) -> new RecursiveStrictThetaStar(gridGraph, sx, sy, ex, ey);
        AlgoFunction optimalAlgo = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);

        double upperBound = 1.5;
        double maxRatio = 1;
        
        int wins = 0;
        int ties = 0;
        Random seedRand = new Random(-2814121L);
        long initial = seedRand.nextLong();
        for (int i=0; i>=0; i++) {
            int sizeX = seedRand.nextInt(30 + (int)(Math.sqrt(i))) + 1;
            int sizeY = seedRand.nextInt(10 + (int)(Math.sqrt(i))) + 1;
            sizeX = seedRand.nextInt(50) + 1;
            sizeY = seedRand.nextInt(30) + 1;
            long seed = i+initial;
            int ratio = seedRand.nextInt(60) + 1;
            
            int max = (sizeX+1)*(sizeY+1);
            int p1 = seedRand.nextInt(max);
            int p2 = seedRand.nextInt(max-1);
            if (p2 == p1) {
                p2 = max-1;
            }
            
            int sx = p1%(sizeX+1);
            int sy = p1/(sizeX+1);
            int ex = p2%(sizeX+1);
            int ey = p2/(sizeX+1);

            //GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, ratio);
            GridGraph gridGraph = DefaultGenerator.generateSeededTrueRandomGraphOnly(seed, sizeX, sizeY, ratio);
            
            //gridGraph = GraphImporter.importGraphFromFile("custommaze4.txt");
            //sx = 0; sy=0;ex=10+i;ey=2;
            //if (ex > 22) break;
            
            
            int[][] path = Utility.generatePath(testAlgo, gridGraph, sx, sy, ex, ey);
            double testPathLength = Utility.computePathLength(gridGraph, path);

            path = Utility.generatePath(optimalAlgo, gridGraph, sx, sy, ex, ey);
            double optimalPathLength = Utility.computePathLength(gridGraph, path);
            
            if (testPathLength > optimalPathLength*upperBound) {
                System.out.println("============");
                System.out.println("Discrepancy Discovered!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                System.out.println("Test: " + testPathLength + " , Optimal: " + optimalPathLength);
                System.out.println("Ratio: " + (testPathLength/optimalPathLength));
                System.out.println("============");
                System.out.println("WINS: " + wins + ", TIES: " + ties);
                throw new UnsupportedOperationException("DISCREPANCY!!");
            } else {
                //System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                double lengthRatio = (double)testPathLength/optimalPathLength;
                if (lengthRatio > maxRatio) {
                    //System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                    System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                    System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                    System.out.println("Test: " + testPathLength + " , Optimal: " + optimalPathLength);
                    System.out.println("Ratio: " + (testPathLength/optimalPathLength));
                    maxRatio = lengthRatio;
                    System.out.println(maxRatio);
                }
            }
        }
        //System.out.println(maxRatio);
    }

    private static void findStrictThetaStarIssues() {
//        AlgoFunction basicThetaStar = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);;
//        AlgoFunction strictThetaStar = (gridGraph, sx, sy, ex, ey) -> new StrictThetaStarV1(gridGraph, sx, sy, ex, ey);
        AlgoFunction basicThetaStar = (gridGraph, sx, sy, ex, ey) -> RecursiveStrictThetaStar.setBuffer(gridGraph, sx, sy, ex, ey, 0.4f);
        AlgoFunction strictThetaStar = (gridGraph, sx, sy, ex, ey) -> RecursiveStrictThetaStar.setBuffer(gridGraph, sx, sy, ex, ey, 0.2f);

        int wins = 0;
        int ties = 0;
        Random seedRand = new Random(192213);
        int initial = seedRand.nextInt();
        for (int i=0; i<500000; i++) {
            int sizeX = seedRand.nextInt(5) + 8;
            int sizeY = seedRand.nextInt(5) + 8;
            int seed = i+initial;
            int ratio = seedRand.nextInt(40) + 5;
            
            int max = (sizeX+1)*(sizeY+1);
            int p1 = seedRand.nextInt(max);
            int p2 = seedRand.nextInt(max-1);
            if (p2 == p1) {
                p2 = max-1;
            }
            
            int sx = p1%(sizeX+1);
            int sy = p1/(sizeX+1);
            int ex = p2%(sizeX+1);
            int ey = p2/(sizeX+1);

            GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, ratio);
            int[][] path = Utility.generatePath(basicThetaStar, gridGraph, sx, sy, ex, ey);
            double basicPathLength = Utility.computePathLength(gridGraph, path);

            path = Utility.generatePath(strictThetaStar, gridGraph, sx, sy, ex, ey);
            double strictPathLength = Utility.computePathLength(gridGraph, path);
            
            if (basicPathLength < strictPathLength-0.01f) {
                System.out.println("============");
                System.out.println("Discrepancy Discovered!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                System.out.println("Basic: " + basicPathLength + " , Strict: " + strictPathLength);
                System.out.println("============");
                System.out.println("WINS: " + wins + ", TIES: " + ties);
                throw new UnsupportedOperationException("DISCREPANCY!!");
            } else {
                System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                if (strictPathLength < basicPathLength - 0.01f) {
                    wins += 1;
                } else {
                    ties += 1;
                }
            }
        }
    }
    
    private static void testAlgorithmOptimality() {
        AlgoFunction testAlgo = Anya::new;
        AlgoFunction refAlgo = VisibilityGraphAlgorithm::graphReuse;
        
        //printSeed = false; // keep this commented out.
        Random seedRand = new Random(-2059321351);
        int initial = seedRand.nextInt();
        for (int i=0; i<50000000; i++) {
            int sizeX = seedRand.nextInt(300) + 10;
            int sizeY = seedRand.nextInt(300) + 10;
            int seed = i+initial;
            int ratio = seedRand.nextInt(50) + 10;
            
            int max = (sizeX+1)*(sizeY+1);
            int p1 = seedRand.nextInt(max);
            int p2 = seedRand.nextInt(max-1);
            if (p2 == p1) {
                p2 = max-1;
            }
            
            int sx = p1%(sizeX+1);
            int sy = p1/(sizeX+1);
            int ex = p2%(sizeX+1);
            int ey = p2/(sizeX+1);

            double restPathLength = 0, normalPathLength = 0;
            try {
            GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, ratio);
            int[][] path = Utility.generatePath(testAlgo, gridGraph, sx, sy, ex, ey);
            path = Utility.removeDuplicatesInPath(path);
            restPathLength = Utility.computePathLength(gridGraph, path);

            path = Utility.generatePath(refAlgo, gridGraph, sx, sy, ex, ey);
            path = Utility.removeDuplicatesInPath(path);
            normalPathLength = Utility.computePathLength(gridGraph, path);
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("EXCEPTION OCCURRED!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                throw new UnsupportedOperationException("DISCREPANCY!!");
            }
            
            if (Math.abs(restPathLength - normalPathLength) > 0.000001f) {
                System.out.println("============");
                System.out.println("Discrepancy Discovered!");
                System.out.println("Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                System.out.println("Start = " + sx + "," + sy + "  End = " + ex + "," + ey);
                System.out.println("Actual: " + restPathLength + " , Expected: " + normalPathLength);
                System.out.println(restPathLength / normalPathLength);
                System.out.println("============");
                throw new UnsupportedOperationException("DISCREPANCY!!");
            } else {
                if (i%10000 == 9999) {
                    System.out.println("Count: " + (i+1));
                    System.out.println("OK: Seed = " + seed +" , Ratio = " + ratio + " , Size: x=" + sizeX + " y=" + sizeY);
                    System.out.println("Actual: " + restPathLength + " , Expected: " + normalPathLength);
                }
            }
        }
    }
    
    private static boolean testTautness(GridGraph gridGraph, AlgoFunction algo, int sx, int sy, int ex, int ey) {
        int[][] path = Utility.generatePath(algo, gridGraph, sx, sy, ex, ey);
        path = Utility.removeDuplicatesInPath(path);
        return Utility.isPathTaut(gridGraph, path);
    }
    
    private static boolean hasSolution(GridGraph gridGraph, AlgoFunction algo, int sx, int sy, int ex, int ey) {
        int[][] path = Utility.generatePath(algo, gridGraph, sx, sy, ex, ey);
        return path.length > 1;
    }
    
    private static void countTautPaths() {
        int nTaut1=0;int nTaut2=0; int nTaut3=0;
        AlgoFunction hasPathChecker = JumpPointSearch::new;
        AlgoFunction algo3 = BasicThetaStar::new;
        AlgoFunction algo2 = StrictThetaStar::new;
        AlgoFunction algo1 = RecursiveStrictThetaStar::new;

        //printSeed = false; // keep this commented out.
        int pathsPerGraph = 100;
        int nIterations = 100000;
        
        int nPaths = 0;

        String[] maps = {
                "obst10_random512-10-0",
                "obst10_random512-10-1",              
                "obst10_random512-10-2",
                "obst10_random512-10-3",              
                "obst10_random512-10-4",
                "obst10_random512-10-5",              
                "obst10_random512-10-6",
                "obst10_random512-10-7",              
                "obst10_random512-10-8",
                "obst10_random512-10-9",             
                "obst40_random512-40-0",
                "obst40_random512-40-1",             
                "obst40_random512-40-2",
                "obst40_random512-40-3",             
                "obst40_random512-40-4",
                "obst40_random512-40-5",             
                "obst40_random512-40-6",
                "obst40_random512-40-7",             
                "obst40_random512-40-8",
                "obst40_random512-40-9"
                };
        nIterations = maps.length;
        System.out.println(maps.length);
        
        
        Random seedRand = new Random(-14);
        int initial = seedRand.nextInt();
        for (int i=0;i<nIterations;++i) {
            String map = maps[i];
            System.out.println("Map: " + map);
            GridGraph gridGraph = GraphImporter.loadStoredMaze(map);
            int sizeX = gridGraph.sizeX;
            int sizeY = gridGraph.sizeY;
            
            /*int sizeX = seedRand.nextInt(20) + 300;
            int sizeY = seedRand.nextInt(20) + 300;
            int seed = i+initial;
            int ratio = seedRand.nextInt(30) + 5;
            
            GridGraph gridGraph = DefaultGenerator.generateSeededGraphOnly(seed, sizeX, sizeY, ratio);
            System.out.println("Ratio: " + ratio);*/
            
            for (int j=0;j<pathsPerGraph;++j) {

                int max = (sizeX+1)*(sizeY+1);
                int p1 = seedRand.nextInt(max);
                int p2 = seedRand.nextInt(max-1);
                if (p2 == p1) {
                    p2 = max-1;
                }
                
                int sx = p1%(sizeX+1);
                int sy = p1/(sizeX+1);
                int ex = p2%(sizeX+1);
                int ey = p2/(sizeX+1);

                if (hasSolution(gridGraph, hasPathChecker, sx, sy, ex, ey)) {
                    if (testTautness(gridGraph, algo1, sx,sy,ex,ey)) nTaut1++;
                    if (testTautness(gridGraph, algo2, sx,sy,ex,ey)) nTaut2++;
                    if (testTautness(gridGraph, algo3, sx,sy,ex,ey)) nTaut3++;
                    nPaths++;
                } else {
                    j--;
                }
            }
            System.out.println("Total = " + nPaths);
            System.out.println("1: " + ((float)nTaut1/nPaths));
            System.out.println("2: " + ((float)nTaut2/nPaths));
            System.out.println("3: " + ((float)nTaut3/nPaths));
        }
    }
    
}
