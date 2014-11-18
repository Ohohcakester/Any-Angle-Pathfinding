package algorithms;

import grid.GridGraph;

import java.util.LinkedList;
import java.util.Queue;

public class BreadthFirstSearch extends AStar {

    private Queue<Integer> queue;
    
    public BreadthFirstSearch(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
        postSmoothingOn = false;
    }

    public static BreadthFirstSearch postSmooth(GridGraph graph, int sx, int sy, int ex, int ey) {
        BreadthFirstSearch bfs = new BreadthFirstSearch(graph, sx, sy, ex, ey);
        bfs.postSmoothingOn = true;
        return bfs;
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);

        visited = new boolean[totalSize];
        parent = new int[totalSize];
        for (int i=0; i<parent.length; i++) {
            parent[i] = -1;
        }

        queue = new LinkedList<>();
        queue.offer(start);
        visited[start] = true;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currX = toTwoDimX(current);
            int currY = toTwoDimY(current);
            
            if (canGoDown(currX, currY)) {
                int index = toOneDimIndex(currX, currY-1);
                if (!visited[index]) {
                    if (addToQueue(current, index))
                        break;
                }
            }
            if (canGoUp(currX, currY)) {
                int index = toOneDimIndex(currX, currY+1);
                if (!visited[index]) {
                    if (addToQueue(current, index))
                        break;
                }
            }
            if (canGoLeft(currX, currY)) {
                int index = toOneDimIndex(currX-1, currY);
                if (!visited[index]) {
                    if (addToQueue(current, index))
                        break;
                }
            }
            if (canGoRight(currX, currY)) {
                int index = toOneDimIndex(currX+1, currY);
                if (!visited[index]) {
                    if (addToQueue(current, index))
                        break;
                }
            }

            maybeSaveSearchSnapshot();
        }
        
        maybePostSmooth();
    }

    /**
     * Returns true iff finish is found.
     */
    private boolean addToQueue(int current, int index) {
        parent[index] = current;
        queue.offer(index);
        visited[index] = true;
        if (index == finish) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean canGoUp(int x, int y) {
        return !bottomRightOfBlockedTile(x,y) || !bottomLeftOfBlockedTile(x,y);
    }
    
    private boolean canGoDown(int x, int y) {
        return !topRightOfBlockedTile(x,y) || !topLeftOfBlockedTile(x,y);
    }
    
    private boolean canGoLeft(int x, int y) {
        return !topRightOfBlockedTile(x,y) || !bottomRightOfBlockedTile(x,y);
    }
    
    private boolean canGoRight(int x, int y) {
        return !topLeftOfBlockedTile(x,y) || !bottomLeftOfBlockedTile(x,y);
    }
    
    private boolean topRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y-1);
    }

    private boolean topLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y-1);
    }

    private boolean bottomRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y);
    }

    private boolean bottomLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y);
    }

}
