package algorithms.rotationalplanesweep;

import java.util.Arrays;
import grid.GridGraph;

public class GridPolygonGenerator {

    private int[] queueX;
    private int[] queueY;
    private int queueStart;
    private int queueEnd;
    
    private final GridGraph graph;
    private final int sizeX;
    private final int sizeY;
    
    public GridPolygonGenerator(GridGraph graph) {
        this.graph = graph;
        this.sizeX = graph.sizeX;
        this.sizeY = graph.sizeY;
        queueX = new int[11];
        queueY = new int[11];
    }

    /**
     * Marking blocked tiles: rightmost tile is representative
     * Marking unblocked tiles: leftmost tile is representative
     *
     */
    public void markAndGetRepresentativeTile(boolean[] visited, int sx, int sy, int[] tile, boolean isBlocked) {
        tile[0] = sx;
        tile[1] = sy;

        queueStart = 0;
        queueEnd = 0;
        
        enqueue(sx, sy);

        while (queueStart != queueEnd) {
            int cx = queueX[queueStart];
            int cy = queueY[queueStart];
            dequeue();

            if (canGoUp(cx,cy,isBlocked)) explore(visited, cx, cy+1, tile, isBlocked);
            if (canGoDown(cx,cy,isBlocked)) explore(visited, cx, cy-1, tile, isBlocked);
            if (canGoLeft(cx,cy,isBlocked)) explore(visited, cx-1, cy, tile, isBlocked);
            if (canGoRight(cx,cy,isBlocked)) explore(visited, cx+1, cy, tile, isBlocked);
        }
    }

    private final void explore(boolean[] visited, int px, int py, int[] tile, boolean isBlocked) {
        int index = py*sizeX + px;
        if (visited[index]) return;
        visited[index] = true;
        enqueue(px, py);

        // isBlocked: rightmost tile
        // !isBlocked: leftmost tile
        if ((px > tile[0]) == isBlocked) {
            tile[0] = px;
            tile[1] = py;
        }
    }

    private final boolean canGoDown(int x, int y, boolean isBlocked) {
        return (y > 0) && (graph.isBlocked(x,y-1) == isBlocked);
    }

    private final boolean canGoUp(int x, int y, boolean isBlocked) {
        return (y+1 < graph.sizeY) && (graph.isBlocked(x,y+1) == isBlocked);
    }
    
    private final boolean canGoLeft(int x, int y, boolean isBlocked) {
        return (x > 0) && (graph.isBlocked(x-1,y) == isBlocked);
    }
    
    private final boolean canGoRight(int x, int y, boolean isBlocked) {
        return (x+1 < graph.sizeX) && (graph.isBlocked(x+1,y) == isBlocked);
    }
    
    private final void enqueue(int x, int y) {
        queueX[queueEnd] = x;
        queueY[queueEnd] = y;
        
        queueEnd = (queueEnd + 1)%queueX.length;
        if (queueStart == queueEnd) {
            // queue is full. Need to expand.
            int currLength = queueX.length;
            
            int[] newQueueX = new int[currLength*2];
            int[] newQueueY = new int[currLength*2];
            
            int size = 0;
            for (int i=queueStart;i<currLength;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                ++size;
            }
            for (int i=0;i<queueEnd;++i) {
                newQueueX[size] = queueX[i];
                newQueueY[size] = queueY[i];
                ++size;
            }
            //assert size == currLength;
            queueX = newQueueX;
            queueY = newQueueY;
            
            queueStart = 0;
            queueEnd = currLength+1;
        }
    }
    
    private final void dequeue() {
        queueStart = (queueStart + 1)%queueX.length;
    }


    public static RPSScanner createRpsScannerFromGrid(GridGraph graph) {
        GridPolygonGenerator generator = new GridPolygonGenerator(graph);
        GridRPSPolygonTracer tracer = new GridRPSPolygonTracer(graph);

        int sizeX = graph.sizeX;
        int sizeY = graph.sizeY;

        boolean[] visited = new boolean[sizeX*sizeY];

        int[] tile = new int[2]; // x, y

        int index = -1;
        for (int y=0; y<sizeY; ++y) {
            for (int x=0; x<sizeX; ++x) {
                ++index;
                if (!visited[index]) {
                    generator.markAndGetRepresentativeTile(visited, x, y, tile, graph.isBlocked(x,y));
                    if (graph.isBlocked(x,y)) {
                        tracer.traceFromBlock(tile[0], tile[1]);
                    } else {
                        if (tile[0] > 0) tracer.traceFromBlock(tile[0]-1, tile[1]);
                    }
                }
            }
        }
        tracer.postProcess();

        return new RPSScanner(tracer.vertices, tracer.edges, graph);
    }
}

class GridRPSPolygonTracer {
    public RPSScanner.Vertex[] vertices;
    public RPSScanner.Edge[] edges;
    public int nVertices;
    public int nEdges;

    private RPSScanner.Vertex prevVertex;
    private RPSScanner.Vertex firstVertex;

    private final GridGraph grid;
    private final int sizeX;
    private final int sizeY;

    public GridRPSPolygonTracer(GridGraph grid) {
        this.grid = grid;
        this.sizeX = grid.sizeX;
        this.sizeY = grid.sizeY;
        vertices = new RPSScanner.Vertex[11];
        edges = new RPSScanner.Edge[11];
    }

    public void traceFromBlock(int sx, int sy) {
        addFirstVertex(sx+1,sy);
        int prevX = prevVertex.x;
        int prevY = prevVertex.y;
        int nextX = sx+1;
        int nextY = sy+1;

        boolean replaceLastVertex = false;
        while (addNextVertex(nextX, nextY, replaceLastVertex)) {
            replaceLastVertex = false;

            // set nextX, nextY
            if (nextY > prevY) {
                // Up
                if (topRightOfBlockedTile(nextX, nextY)) {
                    // Blocked tile on left side.
                    if (!bottomRightOfBlockedTile(nextX, nextY)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else if (!bottomLeftOfBlockedTile(nextX, nextY)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                        replaceLastVertex = true;
                    } else {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    }

                } else {
                    // Blocked tile on right side
                    if (!bottomLeftOfBlockedTile(nextX, nextY)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else if (!bottomRightOfBlockedTile(nextX, nextY)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                        replaceLastVertex = true;
                    } else {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    }
                }

            } else if (nextY < prevY) {
                // Down
                if (bottomRightOfBlockedTile(nextX, nextY)) {
                    // Blocked tile on left side.
                    if (!topRightOfBlockedTile(nextX, nextY)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    } else if (!topLeftOfBlockedTile(nextX, nextY)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                        replaceLastVertex = true;
                    } else {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    }

                } else {
                    // Blocked tile on right side
                    if (!topLeftOfBlockedTile(nextX, nextY)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                    } else if (!topRightOfBlockedTile(nextX, nextY)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                        replaceLastVertex = true;
                    } else {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                    }
                }

            } else if (nextX > prevX) {
                // Right
                if (bottomRightOfBlockedTile(nextX, nextY)) {
                    // Blocked tile above.
                    if (!bottomLeftOfBlockedTile(nextX, nextY)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else if (!topLeftOfBlockedTile(nextX, nextY)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                        replaceLastVertex = true;
                    } else {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    }

                } else {
                    // Blocked tile below
                    if (!topLeftOfBlockedTile(nextX, nextY)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else if (!bottomLeftOfBlockedTile(nextX, nextY)) {
                        // Go right next
                        nextX = nextX + 1;
                        nextY = nextY;
                        replaceLastVertex = true;
                    } else {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    }
                }

            } else if (nextX < prevX) {
                // Left
                if (bottomLeftOfBlockedTile(nextX, nextY)) {
                    // Blocked tile above.
                    if (!bottomRightOfBlockedTile(nextX, nextY)) {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    } else if (!topRightOfBlockedTile(nextX, nextY)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                        replaceLastVertex = true;
                    } else {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    }

                } else {
                    // Blocked tile below
                    if (!topRightOfBlockedTile(nextX, nextY)) {
                        // Go down next
                        nextX = nextX;
                        nextY = nextY - 1;
                    } else if (!bottomRightOfBlockedTile(nextX, nextY)) {
                        // Go left next
                        nextX = nextX - 1;
                        nextY = nextY;
                        replaceLastVertex = true;
                    } else {
                        // Go up next
                        nextX = nextX;
                        nextY = nextY + 1;
                    }
                }

            } else {
                System.out.println("ERROR");
            }

            // set prevX, prevY
            prevX = prevVertex.x;
            prevY = prevVertex.y;
        }
    }

    private final boolean topRightOfBlockedTile(int x, int y) {
        return x > 0 && y > 0 && grid.isBlockedRaw(x-1, y-1);
    }

    private final boolean topLeftOfBlockedTile(int x, int y) {
        return x < sizeX && y > 0 && grid.isBlockedRaw(x, y-1);
    }

    private final boolean bottomRightOfBlockedTile(int x, int y) {
        return x > 0 && y < sizeY && grid.isBlockedRaw(x-1, y);
    }

    private final boolean bottomLeftOfBlockedTile(int x, int y) {
        return x < sizeX && y < sizeY && grid.isBlockedRaw(x, y);
    }

    private final void addFirstVertex(int x, int y) {
        prevVertex = new RPSScanner.Vertex(x, y);
        firstVertex = prevVertex;

        if (nVertices >= vertices.length) vertices = Arrays.copyOf(vertices, vertices.length*2);
        vertices[nVertices++] = prevVertex;
    }

    private final boolean addNextVertex(int x, int y, boolean replaceLastVertex) {
        if (x == firstVertex.x && y == firstVertex.y) {
            // Close the loop
            if (replaceLastVertex) {
                --nVertices;
                edges[nEdges-1].v = firstVertex;
            } else {
                if (nEdges >= edges.length) edges = Arrays.copyOf(edges, edges.length*2);
                edges[nEdges++] = new RPSScanner.Edge(prevVertex, firstVertex);
            }
            return false;
        }

        RPSScanner.Vertex nextVertex = new RPSScanner.Vertex(x,y);

        if (replaceLastVertex) {
            vertices[nVertices-1] = nextVertex;
            edges[nEdges-1].v = nextVertex;
        } else {
            RPSScanner.Edge edge = new RPSScanner.Edge(prevVertex, nextVertex);

            if (nVertices >= vertices.length) vertices = Arrays.copyOf(vertices, vertices.length*2);
            vertices[nVertices++] = nextVertex;

            if (nEdges >= edges.length) edges = Arrays.copyOf(edges, edges.length*2);
            edges[nEdges++] = edge;
        }
        prevVertex = nextVertex;

        return true;
    }

    public void postProcess() {
        // Shrink to size
        vertices = Arrays.copyOf(vertices, nVertices);
        edges = Arrays.copyOf(edges, nEdges);

        for (int i=0; i<edges.length; ++i) {
            RPSScanner.Edge e = edges[i];
            e.u.edge1 = e;
            e.v.edge2 = e;
            e.originalU = e.u;
        }
    }
}