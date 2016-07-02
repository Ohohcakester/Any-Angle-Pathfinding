package main.graphgeneration;

import java.util.Random;

import grid.GridAndGoals;
import grid.GridGraph;

public class AutomataGenerator {

    public static GridAndGoals generateUnseeded(int sizeX, int sizeY, int unblockedRatio, int iterations, int cutoffOffset, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, unblockedRatio, iterations, cutoffOffset);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    public static GridAndGoals generateSeeded(long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, int cutoffOffset, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, iterations, cutoffOffset);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
    
    public static GridGraph generateSeededGraphOnly(long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, int cutoffOffset) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, iterations, cutoffOffset);
        return gridGraph;
    }

    private static GridGraph generate(boolean seededRandom, long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, int cutoffOffset) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        Random rand = new Random();
        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomMap(rand, gridGraph, unblockedRatio, iterations, cutoffOffset);
        
        return gridGraph;
    }
    

    /**
     * Generates a truly random map for the gridGraph.
     * No longer used as this does not generate very good or realistic grids.
     */
    private static void generateRandomMap(Random rand, GridGraph gridGraph, int frequency, int iterations, int cutoffOffset) {
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        int resolution = Math.max((sizeX+sizeY)/150, 1);
        int cutoff = (int)(0.8f*resolution*resolution + 1.75f*resolution + 0.8f) + cutoffOffset;

System.out.println("Resolution " + resolution + ", Cutoff " + cutoff);
        boolean[][] grid = new boolean[sizeY][];
        boolean[][] grid2 = new boolean[sizeY][];
        for (int y=0;y<sizeY;++y) {
            grid[y] = new boolean[sizeX];
            grid2[y] = new boolean[sizeX];
            for (int x=0;x<sizeX;++x) {
                grid[y][x] = rand.nextInt()%frequency == 0;
            }
        }
        
        for (int itr=0;itr<iterations;++itr) {
            
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    int nBlocked = 0;
                    for (int i=-resolution;i<=resolution;++i) {
                        for (int j=-resolution;j<=resolution;++j) {
                            if (i == 0 && j == 0) continue;
                            int px = x + i;
                            int py = y + j;
                            if (px < 0 || py < 0 || px >= sizeX || py >= sizeY) {
                                nBlocked++;
                            } else {
                                nBlocked += grid[py][px] ? 1 : 0;
                            }
                        }
                    }
                    grid2[y][x] = nBlocked >= cutoff;
                }
            }
            
            boolean[][] temp = grid;
            grid = grid2;
            grid2 = temp;
        }
        
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                gridGraph.setBlocked(x, y, grid[y][x]);
            }
        }
    }
    
    
}
