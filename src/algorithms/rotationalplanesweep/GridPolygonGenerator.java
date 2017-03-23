package algorithms.rotationalplanesweep;

import java.util.Arrays;
import grid.GridGraph;

public class GridPolygonGenerator {

    private final GridGraph graph;
    private final int sizeX;
    private final int sizeY;
    
    public GridPolygonGenerator(GridGraph graph) {
        this.graph = graph;
        this.sizeX = graph.sizeX;
        this.sizeY = graph.sizeY;
    }

    public static RPSScanner createRpsScannerFromGrid(GridGraph graph) {
        GridPolygonGenerator generator = new GridPolygonGenerator(graph);
        GridRPSPolygonTracer tracer = new GridRPSPolygonTracer(graph);

        int sizeX = graph.sizeX;
        int sizeY = graph.sizeY;

        for (int y=0; y<sizeY; ++y) {
            for (int x=1; x<=sizeX; ++x) {
                boolean bottomRightOfBlocked = graph.isBlockedRaw(x-1,y);
                boolean bottomLeftOfBlocked = (x != sizeX && graph.isBlockedRaw(x,y));
                boolean topRightOfBlocked = (y != 0 && graph.isBlockedRaw(x-1,y-1));
                boolean topLeftOfBlocked = (y != 0 && x != sizeX && graph.isBlockedRaw(x,y-1));

                if (bottomRightOfBlocked && !bottomLeftOfBlocked && !(topRightOfBlocked && !topLeftOfBlocked)) {
                    tracer.traceFromVertex(x, y);
                }
            }
        }
        tracer.postProcess();

        return new RPSScanner(tracer.vertices, tracer.edges, graph);
    }
}

class GridRPSPolygonTracer {
    // visited[] is used for corners that turn upwards.
    // visited[sx, sy] == true iff the edge above it has been visited.
    private boolean[] visited;
    
    public RPSScanner.Vertex[] vertices;
    public RPSScanner.Edge[] edges;
    public int nVertices;
    public int nEdges;

    private RPSScanner.Vertex prevVertex;
    private RPSScanner.Vertex firstVertex;

    private final GridGraph grid;
    private final int sizeXPlusOne;
    private final int sizeX;
    private final int sizeY;

    public GridRPSPolygonTracer(GridGraph grid) {
        this.grid = grid;
        this.sizeX = grid.sizeX;
        this.sizeY = grid.sizeY;
        this.sizeXPlusOne = grid.sizeX+1;
        vertices = new RPSScanner.Vertex[11];
        edges = new RPSScanner.Edge[11];
        visited = new boolean[(sizeX+1)*(sizeY+1)];
    }

    // Condition: Vertex (sx, sy) must be a bottom-right corner.
    public void traceFromVertex(int sx, int sy) {
        if (isVisited(sx, sy)) return;
        addFirstVertex(sx, sy);
        int prevX = prevVertex.x;
        int prevY = prevVertex.y;
        int nextX = sx;
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
                        markVisited(nextX, nextY);
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
                        markVisited(nextX, nextY);
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
                        markVisited(nextX, nextY);
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
                        markVisited(nextX, nextY);
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

    private final void markVisited(int x, int y) {
        visited[y*sizeXPlusOne + x] = true;
    }

    private final boolean isVisited(int x, int y) {
        return visited[y*sizeXPlusOne + x];
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
        }
    }
}