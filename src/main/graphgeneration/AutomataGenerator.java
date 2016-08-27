package main.graphgeneration;

import java.util.Random;

import grid.GridAndGoals;
import grid.GridGraph;

public class AutomataGenerator {

    public static GridAndGoals generateUnseeded(int sizeX, int sizeY, int unblockedRatio, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, unblockedRatio, iterations, resolutionMultiplier, cutoffOffset, bordersAreBlocked);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    public static GridAndGoals generateSeeded(long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, iterations, resolutionMultiplier, cutoffOffset, bordersAreBlocked);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
    
    public static GridGraph generateSeededGraphOnly(long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, unblockedRatio, iterations, resolutionMultiplier, cutoffOffset, bordersAreBlocked);
        return gridGraph;
    }

    public static GridAndGoals generateUnseededDynamicCutoff(int sizeX, int sizeY, float initialPercentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generateDynamicCutoff(false, 0, sizeX, sizeY, initialPercentBlocked, iterations, resolutionMultiplier, bordersAreBlocked);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    public static GridAndGoals generateSeededDynamicCutoff(long seed, int sizeX, int sizeY, float initialPercentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generateDynamicCutoff(true, seed, sizeX, sizeY, initialPercentBlocked, iterations, resolutionMultiplier, bordersAreBlocked);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }
    
    public static GridGraph generateSeededGraphOnlyDynamicCutoff(long seed, int sizeX, int sizeY, float initialPercentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked) {
        GridGraph gridGraph = generateDynamicCutoff(true, seed, sizeX, sizeY, initialPercentBlocked, iterations, resolutionMultiplier, bordersAreBlocked);
        return gridGraph;
    }

    private static GridGraph generate(boolean seededRandom, long seed, int sizeX, int sizeY, int unblockedRatio, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        Random rand = new Random();
        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomMap(rand, gridGraph, unblockedRatio, iterations, resolutionMultiplier, cutoffOffset, bordersAreBlocked);
        
        return gridGraph;
    }

    private static GridGraph generateDynamicCutoff(boolean seededRandom, long seed, int sizeX, int sizeY, float initialPercentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        Random rand = new Random();
        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Starting random with random seed = " + seed);
        } else {
            System.out.println("Starting random with predefined seed = " + seed);
        }
        rand = new Random(seed);
        
        generateRandomMapDynamicCutoff(rand, gridGraph, initialPercentBlocked, iterations, resolutionMultiplier, bordersAreBlocked);
        
        return gridGraph;
    }
    
    
    private static void generateRandomMap(Random rand, GridGraph gridGraph, int frequency, int iterations, float resolutionMultiplier, int cutoffOffset, boolean bordersAreBlocked) {
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        int resolution = Math.max((int)((sizeX+sizeY)*resolutionMultiplier/150), 1);
        int cutoff = (int)(0.8f*resolution*resolution + 1.75f*resolution + 0.8f) + cutoffOffset;

        System.out.println("Resolution " + resolution + ", Cutoff " + cutoff);
        
        boolean[][] grid = new boolean[sizeY][];
        // Count: used for DP computation of number of blocked neighbours.
        //  Note: count includes the current tile as well. We subtract it when we compare with cutoff.
        int[][] count = new int[sizeY][];
        for (int y=0;y<sizeY;++y) {
            grid[y] = new boolean[sizeX];
            count[y] = new int[sizeX];
            for (int x=0;x<sizeX;++x) {
                grid[y][x] = rand.nextInt()%frequency == 0;
            }
        }
        
        for (int itr=0;itr<iterations;++itr) {
            if (bordersAreBlocked) runAutomataIterationBlockedBorders(sizeX, sizeY, resolution, grid, count);
            else runAutomataIterationUnblockedBorders(sizeX, sizeY, resolution, grid, count);
            
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    grid[y][x] = (count[y][x] - (grid[y][x] ? 1 : 0) >= cutoff);
                }
            }
        }
        
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                gridGraph.setBlocked(x, y, grid[y][x]);
            }
        }
    }


    private static void generateRandomMapDynamicCutoff(Random rand, GridGraph gridGraph, float percentBlocked, int iterations, float resolutionMultiplier, boolean bordersAreBlocked) {
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;
        int resolution = Math.max((int)((sizeX+sizeY)*resolutionMultiplier/150), 1);

        boolean[][] grid = new boolean[sizeY][];
        // Count: used for DP computation of number of blocked neighbours.
        //  Note: count includes the current tile as well. We subtract it when we compare with cutoff.
        int[][] count = new int[sizeY][];
        for (int y=0;y<sizeY;++y) {
            grid[y] = new boolean[sizeX];
            count[y] = new int[sizeX];
            for (int x=0;x<sizeX;++x) {
                grid[y][x] = rand.nextFloat() < percentBlocked;
            }
        }
        
        int maxCount = (resolution*2+1);
        maxCount = maxCount*maxCount - 1;
        
        for (int itr=0;itr<iterations;++itr) {
            if (bordersAreBlocked) runAutomataIterationBlockedBorders(sizeX, sizeY, resolution, grid, count);
            else runAutomataIterationUnblockedBorders(sizeX, sizeY, resolution, grid, count);
            
            // Adjust counts to exclude the center.
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    count[y][x] = (count[y][x] - (grid[y][x] ? 1 : 0));
                }
            }
            
            // Compute mean.
            long totalCount = 0;
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    totalCount += count[y][x];
                }
            }
            float mean = (float)totalCount / (sizeX*sizeY);
            
            // Compute approximately the value at the pth percentile. (where p = percentBlocked)
            int nBins = (int)Math.sqrt(sizeX*sizeY);
            
            // bin 0: <= low
            // bin n+1: >= low+range
            // bin i: low + d*(i-1) < x < low + d*(i)
            int[] bins = new int[nBins+2];
            float[] binAverage = new float[nBins+2];
            
            float low = mean*0.5f;
            float range = (mean + maxCount)/2 - low;
            float d = range/nBins;
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    int bin = (int)((count[y][x] - low)*nBins / range) + 1;
                    bin = Math.max(Math.min(bin, nBins+1), 0); // Clamp bin to [0,n+1]
                    binAverage[bin] = (binAverage[bin]*bins[bin] + count[y][x]) / (bins[bin]+1);
                    bins[bin]++;
                }
            }
            
            // cutoff = value at pth percentile (similar to median)
            // using the value at the pth percentile allows us to maintain the same percentageBlocked each iteration.
            int remainingCumSum = (int)(sizeX*sizeY * (1-percentBlocked));
            float cutoff = -1;
            for (int i=0;i<bins.length;++i) {
                remainingCumSum -= bins[i];
                if (remainingCumSum < 0) {
                    cutoff = binAverage[i];
                    break;
                }
            }
            
            for (int y=0;y<sizeY;++y) {
                for (int x=0;x<sizeX;++x) {
                    grid[y][x] = count[y][x] >= cutoff;
                }
            }
        }
        
        for (int y=0;y<sizeY;++y) {
            for (int x=0;x<sizeX;++x) {
                gridGraph.setBlocked(x, y, grid[y][x]);
            }
        }
    }
    
    protected static void runAutomataIterationUnblockedBorders(int sizeX, int sizeY, int resolution, boolean[][] grid, int[][] count) {
        /*
         * Note: for brevity, the following code:
         * nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
         * 
         * Is a shortened version of:
         * if (px < 0 || py < 0 || px >= sizeX || py >= sizeY) {
         *     nBlocked++;
         * } else {
         *     nBlocked += grid[py][px] ? 1 : 0;
         * }
         */
        
        { // Base case: y = 0
            int y = 0;
            { // Base case: x = 0
                int x = 0;
                int nBlocked = 0;
                for (int i=-resolution;i<=resolution;++i) {
                    for (int j=-resolution;j<=resolution;++j) {
                        int px = x + i;
                        int py = y + j;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                    }
                }
                
                count[y][x] = nBlocked;
            }

            // y = 0, x > 0
            for (int x=1;x<sizeX;++x) {
                int nBlocked = count[y][x-1];

                { // subtract for (x-1-r,?)
                    int px = x - resolution - 1;
                    for (int j=-resolution;j<=resolution;++j) {
                        int py = y + j;
                        nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                    }
                }
                
                { // add for (x+r,?)
                    int px = x + resolution;
                    for (int j=-resolution;j<=resolution;++j) {
                        int py = y + j;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                    }
                }
                
                count[y][x] = nBlocked;
            }
        }

        // y > 0
        for (int y=1;y<sizeY;++y) {
            // y > 0, x = 0
            {
                int x = 0;
                int nBlocked = count[y-1][x];

                { // subtract for (?,y-1-r)
                    int py = y - resolution - 1;
                    for (int i=-resolution;i<=resolution;++i) {
                        int px = x + i;
                        nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                    }
                }
                
                { // add for (?,y+r)
                    int py = y + resolution;
                    for (int i=-resolution;i<=resolution;++i) {
                        int px = x + i;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                    }
                }

                count[y][x] = nBlocked;
            }
            
            // y > 0, x > 0
            for (int x=1;x<sizeX;++x) {
                int nBlocked = count[y-1][x] + count[y][x-1] - count[y-1][x-1];

                { // add (x-1-r,y-1-r)
                    int px = x - resolution - 1;
                    int py = y - resolution - 1;
                    nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                }
                { // add (x+r,y+r)
                    int px = x + resolution;
                    int py = y + resolution;
                    nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                }
                { // subtract (x-1-r,y+r)
                    int px = x - resolution - 1;
                    int py = y + resolution;
                    nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                }
                { // subtract (x+r,y-1-r)
                    int px = x + resolution;
                    int py = y - resolution - 1;
                    nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || !grid[py][px]) ? 0 : 1;
                }
                
                count[y][x] = nBlocked;
            }
        }
    }

    private static void runAutomataIterationBlockedBorders(int sizeX, int sizeY, int resolution, boolean[][] grid, int[][] count) {
        /*
         * Note: for brevity, the following code:
         * nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
         * 
         * Is a shortened version of:
         * if (px < 0 || py < 0 || px >= sizeX || py >= sizeY) {
         *     nBlocked++;
         * } else {
         *     nBlocked += grid[py][px] ? 1 : 0;
         * }
         */
        
        { // Base case: y = 0
            int y = 0;
            { // Base case: x = 0
                int x = 0;
                int nBlocked = 0;
                for (int i=-resolution;i<=resolution;++i) {
                    for (int j=-resolution;j<=resolution;++j) {
                        int px = x + i;
                        int py = y + j;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                    }
                }
                
                count[y][x] = nBlocked;
            }

            // y = 0, x > 0
            for (int x=1;x<sizeX;++x) {
                int nBlocked = count[y][x-1];

                { // subtract for (x-1-r,?)
                    int px = x - resolution - 1;
                    for (int j=-resolution;j<=resolution;++j) {
                        int py = y + j;
                        nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                    }
                }
                
                { // add for (x+r,?)
                    int px = x + resolution;
                    for (int j=-resolution;j<=resolution;++j) {
                        int py = y + j;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                    }
                }
                
                count[y][x] = nBlocked;
            }
        }

        // y > 0
        for (int y=1;y<sizeY;++y) {
            // y > 0, x = 0
            {
                int x = 0;
                int nBlocked = count[y-1][x];

                { // subtract for (?,y-1-r)
                    int py = y - resolution - 1;
                    for (int i=-resolution;i<=resolution;++i) {
                        int px = x + i;
                        nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                    }
                }
                
                { // add for (?,y+r)
                    int py = y + resolution;
                    for (int i=-resolution;i<=resolution;++i) {
                        int px = x + i;
                        nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                    }
                }

                count[y][x] = nBlocked;
            }
            
            // y > 0, x > 0
            for (int x=1;x<sizeX;++x) {
                int nBlocked = count[y-1][x] + count[y][x-1] - count[y-1][x-1];

                { // add (x-1-r,y-1-r)
                    int px = x - resolution - 1;
                    int py = y - resolution - 1;
                    nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                }
                { // add (x+r,y+r)
                    int px = x + resolution;
                    int py = y + resolution;
                    nBlocked += (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                }
                { // subtract (x-1-r,y+r)
                    int px = x - resolution - 1;
                    int py = y + resolution;
                    nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                }
                { // subtract (x+r,y-1-r)
                    int px = x + resolution;
                    int py = y - resolution - 1;
                    nBlocked -= (px < 0 || py < 0 || px >= sizeX || py >= sizeY || grid[py][px]) ? 1 : 0;
                }
                
                count[y][x] = nBlocked;
            }
        }
    }

    /**
     * Generates a truly random map for the gridGraph.
     * No longer used as this does not generate very good or realistic grids.
     */
    private static void generateRandomMap_slow(Random rand, GridGraph gridGraph, int frequency, int iterations, int cutoffOffset) {
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
