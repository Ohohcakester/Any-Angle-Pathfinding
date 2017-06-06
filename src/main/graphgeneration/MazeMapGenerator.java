package main.graphgeneration;

import grid.GridAndGoals;
import grid.GridGraph;

import java.util.Random;
import java.util.ArrayList;

public class MazeMapGenerator {

    /**
     * connectednessRatio is between 0 and 1.
     *   0: n-1 edges
     *   1: max possible number of edges.
     */
    public static GridAndGoals generateUnseeded(int sizeX, int sizeY,
            int corridorWidth, float connectednessRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, corridorWidth, connectednessRatio);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    public static GridGraph generateUnseededGraphOnly(int sizeX, int sizeY,
            int corridorWidth, float connectednessRatio) {
        GridGraph gridGraph = generate(false, 0, sizeX, sizeY, corridorWidth, connectednessRatio);
        return gridGraph;
    }

    public static GridAndGoals generateSeeded(long seed, int sizeX, int sizeY,
            int corridorWidth, float connectednessRatio, int sx, int sy, int ex, int ey) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, corridorWidth, connectednessRatio);
        return new GridAndGoals(gridGraph, sx, sy, ex, ey);
    }

    public static GridGraph generateSeededGraphOnly(long seed, int sizeX, int sizeY,
            int corridorWidth, float connectednessRatio) {
        GridGraph gridGraph = generate(true, seed, sizeX, sizeY, corridorWidth, connectednessRatio);
        return gridGraph;
    }

    private static GridGraph generate(boolean seededRandom, long seed,
            int sizeX, int sizeY, int corridorWidth, float connectednessRatio) {
        GridGraph gridGraph = new GridGraph(sizeX, sizeY);

        Random rand = new Random();
        if (!seededRandom) {
            seed = rand.nextInt();
            System.out.println("Maze Map with random seed = " + seed);
        } else {
            System.out.println("Maze Map with predefined seed = " + seed);
        }
        rand = new Random(seed);

        MazeMapGenerator generator = new MazeMapGenerator(rand, sizeX, sizeY, corridorWidth, connectednessRatio);
        generator.writeToGridGraph(gridGraph);
        return gridGraph;
    }

    /* Diagram of how we index vertices, horizontal edges and vertical edges
     * used in the algorithm below.
     *
     *(0,0) ___________________ (2,0)
     *     |  [0,0]  |  [1,0]  |
     *     |         |         |
     *     |{0,0}    |{1,0}    |{2,0}
     *     |         |         |
     *(0,1)|_________|_________|(2,1)
     *     |  [0,1]  |  [1,1]  |
     *     |         |         |
     *     |{0,1}    |{1,1}    |{1,2}
     *     |         |         |
     *     |_________|_________|
     *(0,2)   [0,2]     [1,2]   (2,2)
     */

    private final int nNodesX;
    private final int nNodesY;
    private final int nVertEdgesX;
    private final int nVertEdgesY;
    private final int nVertEdges;
    private final int nHoriEdgesX;
    private final int nHoriEdgesY;

    private final int[] nodeCurrentSet;
    private final ArrayList<ArrayList<Integer>> sets;
    private final int[] edges;

    private final int nAdditionalEdges;

    private final int corridorWidth;
    private final Random rand;

    private int firstEdge = -1;

    private MazeMapGenerator(Random rand, int sizeX, int sizeY, int corridorWidth, float connectednessRatio) {
        this.rand = rand;
        this.corridorWidth = corridorWidth;

        nNodesX = (sizeX/corridorWidth - 1)/2;
        nNodesY = (sizeY/corridorWidth - 1)/2;

        nVertEdgesX = nNodesX;
        nVertEdgesY = nNodesY-1;
        nHoriEdgesX = nNodesX-1;
        nHoriEdgesY = nNodesY;

        nVertEdges = nVertEdgesX*nVertEdgesY;

        nodeCurrentSet = new int[nNodesX*nNodesY];
        sets = new ArrayList<ArrayList<Integer>>();
        edges = new int[nVertEdgesX*nVertEdgesY + nHoriEdgesX*nHoriEdgesY];

        for (int i=0; i<nodeCurrentSet.length; ++i) {
            nodeCurrentSet[i] = i;
            sets.add(new ArrayList<>());
            sets.get(i).add(i);
        }

        for (int i=0; i<edges.length; ++i) {
            edges[i] = i;
        }

        int minEdges = nNodesX*nNodesY - 1;
        int maxEdges = edges.length;

        nAdditionalEdges = (int)(connectednessRatio*(maxEdges-minEdges));

        generateRandomMap();
    }

    private final void generateRandomMap() {
        // First we pick n-1 edges to form a spanning tree.
        // Wont pick: edges that link two nodes from the same component.
        // Picked: edges in the spanning tree.
        int nMinusOne = nNodesX*nNodesY - 1;
        int start = 0;
        int end = edges.length;

        /*
         *           start                  end
         *  ___________|_____________________|________
         * |___________|_____________________|________|
         *  '----.----' '---------.---------' '---.---'
         *   Won't pick      Unprocessed        Picked
         */

        // STEP 1: Pick n-1 edges to form a spanning tree.
        int[] endPoints = new int[4];
        while (edges.length - end < nMinusOne) {
            int index = rand.nextInt(end-start) + start;
            int edge = edges[index];

            getEndpoints(edge, endPoints);
            int v1 = indexOf(endPoints[0], endPoints[1]);
            int v2 = indexOf(endPoints[2], endPoints[3]);

            if (nodeCurrentSet[v1] == nodeCurrentSet[v2]) {
                // Same set. Don't pick edge.
                swapEdge(index, start);
                ++start;
            } else {
                // Different set. Merge sets and pick edge.
                swapEdge(index, end-1);
                --end;
                mergeSets(v1, v2);
            }
        }

        // STEP 2: Pick random additional edges.
        for (int i=0; i<nAdditionalEdges; ++i) {
            // We can pick any edge before end. (it doesn't have to be unprocessed)
            int index = rand.nextInt(end);

            swapEdge(index, end-1);
            --end;
        }

        firstEdge = end;
    }

    private final void swapEdge(int e1, int e2) {
        int temp = edges[e1];
        edges[e1] = edges[e2];
        edges[e2] = temp;
    }

    private final void mergeSets(int v1, int v2) {
        if (sets.get(nodeCurrentSet[v1]).size() < sets.get(nodeCurrentSet[v2]).size()) {
            mergeSets(v2, v1);
            return;
        }

        // wtf lol. I wrote c++ instead of java. fix this!

        // Assume v2 is smaller than v1.
        // We merge v2 into v1.
        int v1SetIndex = nodeCurrentSet[v1];
        int v2SetIndex = nodeCurrentSet[v2];
        ArrayList<Integer> v1Set = sets.get(v1SetIndex);
        ArrayList<Integer> v2Set = sets.get(v2SetIndex);

        for (int vertex : v2Set) {
            nodeCurrentSet[vertex] = v1SetIndex;
        }

        // add v2Set to v1Set, clear v2Set.
        v1Set.addAll(v2Set);
        v2Set.clear();
    }

    private final int indexOf(int px, int py) {
        return py*nNodesX + px;
    }

    private final void getEndpoints(int index, int[] return_coords) {
        if (index < nVertEdges) {
            // Vertical edge
            int indexX = index%nVertEdgesX;
            int indexY = index/nVertEdgesX;

            // Example: Edge {1,0} bridges points (1,0) and (1,1)
            return_coords[0] = indexX;
            return_coords[1] = indexY;
            return_coords[2] = indexX;
            return_coords[3] = indexY+1;
        } else {
            // Horizontal edge
            index -= nVertEdges;
            int indexX = index%nHoriEdgesX;
            int indexY = index/nHoriEdgesX;

            // Example: Edge [0,1] bridges points (0,1) and (1,1)
            return_coords[0] = indexX;
            return_coords[1] = indexY;
            return_coords[2] = indexX+1;
            return_coords[3] = indexY;
        }
    }

    private final void getEdgeTile(int index, int[] return_edgeTile) {
        if (index < nVertEdges) {
            // Vertical edge
            int indexX = index%nVertEdgesX;
            int indexY = index/nVertEdgesX;

            // Example: Edge {1,0} bridges points (1,0) and (1,1)
            // point (x,y) is at tile (2*x+1, 2*y+1)
            return_edgeTile[0] = 2*indexX+1;
            return_edgeTile[1] = 2*indexY+2;
        } else {
            // Horizontal edge
            index -= nVertEdges;
            int indexX = index%nHoriEdgesX;
            int indexY = index/nHoriEdgesX;

            // Example: Edge [0,1] bridges points (0,1) and (1,1)
            // point (x,y) is at tile (2*x+1, 2*y+1)
            return_edgeTile[0] = 2*indexX+2;
            return_edgeTile[1] = 2*indexY+1;
        }
    }

    private final void writeToGridGraph(GridGraph grid) {
        for (int y=0; y<grid.sizeY; ++y) {
            for (int x=0; x<grid.sizeX; ++x) {
                grid.setBlocked(x, y, true);
            }
        }

        for (int y=0; y<nNodesY; ++y) {
            for (int x=0; x<nNodesX; ++x) {
                int baseX = (2*x+1)*corridorWidth;
                int baseY = (2*y+1)*corridorWidth;
                int endX = baseX + corridorWidth;
                int endY = baseY + corridorWidth;
                for (int py=baseY; py<endY; ++py) {
                    for (int px=baseX; px<endX; ++px) {
                        grid.setBlocked(px, py, false);
                    }
                }
            }
        }

        int[] edgeTile = new int[2];
        for (int i=firstEdge; i<edges.length; ++i) {
            getEdgeTile(edges[i], edgeTile);

            int baseX = edgeTile[0]*corridorWidth;
            int baseY = edgeTile[1]*corridorWidth;
            int endX = baseX + corridorWidth;
            int endY = baseY + corridorWidth;
            for (int py=baseY; py<endY; ++py) {
                for (int px=baseX; px<endX; ++px) {
                    grid.setBlocked(px, py, false);
                }
            }
        }
    }
}
