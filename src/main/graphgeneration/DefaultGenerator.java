package main.graphgeneration;

import grid.GridAndGoals;
import grid.GridGraph;

import java.util.Random;

/**
 * Generates a random "block map".
 * A block map is made out of small 2x2 blocks and is guaranteed to have no squeezable corners.<br>
 * Exception: the removed block at sx,sy and ex,ey.
 */
public class DefaultGenerator {
    
    /**
     * Generates a graph using the parameters specified in the arguments.
     * @param _seed The random seed to be used
     * @param _sizeX x-axis size
     * @param _sizeY y-axis size
     * @param _ratio chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
     * @param _sx x-coordinate of start point
     * @param _sy y-coordinate of start point
     * @param _ex x-coordinate of goal point
     * @param _ey y-coordinate of goal point
     * @return the generated gridGraph.
     */
    public static GridAndGoals generateSeededOld(int seed, int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
    
    public static GridAndGoals generateUnseededOld(int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
    
    /**
     * Does not remove the block at 0,0
     */
    public static GridAndGoals generateSeeded(int seed, int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, -1,-1,-1,-1);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    /**
     * Does not remove the block at 0,0
     */
    public static GridAndGoals generateUnseeded(int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, unblockedRatio, -1,-1,-1,-1);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    /**
     * For backwards compatibility. Removes the block at 0,0.
     */
    public static GridGraph generateSeededGraphOnlyOld(int seed, int sizeX, int sizeY, int unblockedRatio) {
        return generate(true, seed, sizeX, sizeY, unblockedRatio, 0,0,0,0);
    }

    /**
     * Does not remove the block at 0,0
     */
    public static GridGraph generateSeededGraphOnly(int seed, int sizeX, int sizeY, int unblockedRatio) {
        return generate(true, seed, sizeX, sizeY, unblockedRatio, -1,-1,-1,-1);
    }

    /**
     * Removes the blocks at sx,sy and ex,ey.
     */
    public static GridGraph generateSeededGraphOnly(int seed, int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        return generate(true, seed, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
    }

    public static GridAndGoals generateSeededTrueRandomGraph(long seed, int sizeX, int sizeY, int frequency, int sx, int sy, int ex, int ey) {
        return new GridAndGoals(generateSeededTrueRandomGraphOnly(seed, sizeX, sizeY, frequency), sx, sy, ex, ey);
    }
    
    public static GridGraph generateSeededTrueRandomGraphOnly(long seed, int sizeX, int sizeY, int frequency) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);
        //System.out.println("True Random Graph with predefined seed = " + seed);
        Random rand = new Random(seed);
        generateRandomMap(rand, gridGraph, frequency);
        return gridGraph;
    }
    
    /**
     * Generates a graph using the static parameters defined at the beginning of the class.
     */
    private static GridGraph generate(boolean seededRandom, int seed, int sizeX, int sizeY, int unblockedRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        Random rand = new Random();
        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomBlockMap(rand, gridGraph, unblockedRatio);
        fillCorners(rand, gridGraph);

        gridGraph.trySetBlocked(sx, sy, false);
        gridGraph.trySetBlocked(ex, ey, false);
        return gridGraph;
    }
    
    
    /**
     * Generates a truly random map for the gridGraph.
     * No longer used as this does not generate very good or realistic grids.
     */
    private static void generateRandomMap(Random rand, GridGraph gridGraph, int frequency) {
        for (int x = 0; x < gridGraph.sizeX; x++) {
            for (int y = 0; y < gridGraph.sizeY; y++) {
                gridGraph.setBlocked(x, y, rand.nextInt()%frequency == 0);
            }
        }
    }

    /**
     * Post-processing step to remove situations like this:
     * <pre>
     *   ___ ___
     *  |   |||||
     *  |...X'''|
     *  |||||___|</pre>
     * An unblocked path can pass through x from the top left to the bottom right.<br>
     * We fix this by filling up the gaps with additional blocks.
     */
    private static void fillCorners(Random rand, GridGraph gridGraph) {
        boolean didSomething = true;;
        while (didSomething) {
            didSomething = false;
            for (int x = 0; x < gridGraph.sizeX; x++) {
                for (int y = 0; y < gridGraph.sizeY; y++) {
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

    /**
     * Generates a random "block" map for the gridGraph.<br>
     * This generates more realistic grids by spawning clusters of blocks
     * instead of simply spawning random blocks.
     */
    private static void generateRandomBlockMap(Random rand, GridGraph gridGraph, int frequency) {
        for (int x = 0; x < gridGraph.sizeX; x++) {
            for (int y = 0; y < gridGraph.sizeY; y++) {
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
            }
        }
    }

}
