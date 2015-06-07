package algorithms;

import algorithms.priorityqueue.IndirectHeap;
import grid.GridGraph;

public class LazyThetaStar extends BasicThetaStar {

    public LazyThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);

        distance = new Float[totalSize];
        parent = new int[totalSize];

        initialise(start);
        visited = new boolean[totalSize];

        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();

        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            if (parent[current] != -1) {
                int parX = toTwoDimX(parent[current]);
                int parY = toTwoDimY(parent[current]);

                if (!graph.lineOfSight(x,y,parX,parY)) {
                    findPath1Parent(current, x, y);
                }
            }

            if (current == finish || distance[current] == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            visited[current] = true;

            tryRelaxNeighbour(current, x, y, x-1, y-1);
            tryRelaxNeighbour(current, x, y, x, y-1);
            tryRelaxNeighbour(current, x, y, x+1, y-1);

            tryRelaxNeighbour(current, x, y, x-1, y);
            tryRelaxNeighbour(current, x, y, x+1, y);

            tryRelaxNeighbour(current, x, y, x-1, y+1);
            tryRelaxNeighbour(current, x, y, x, y+1);
            tryRelaxNeighbour(current, x, y, x+1, y+1);

            maybeSaveSearchSnapshot();
        }

        maybePostSmooth();
    }

    private void findPath1Parent(int current, int x, int y) {
        distance[current] = Float.POSITIVE_INFINITY;
        for (int i=-1;i<=1;++i) {
            for (int j=-1;j<=1;++j) {
                if (i == 0 && j == 0) continue;
                int px = x+i;
                int py = y+j;
                if (!graph.isValidBlock(px,py)) continue;
                int index = graph.toOneDimIndex(px, py);
                if (!visited[index]) continue;
                if (!graph.neighbourLineOfSight(x,y,px,py)) continue;

                float gValue = distance[index] + graph.distance(x,y,px,py);
                if (gValue < distance[current]) {
                    distance[current] = gValue;
                    parent[current] = index;
                }
            }
        }
    }

    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        if (parent[u] != -1) {
            u = parent[u];
        }

        float newWeight = distance[u] + physicalDistance(u, v);
        if (newWeight < distance[v]) {
            distance[v] = newWeight;
            parent[v] = u;
            return true;
        }
        return false;

        /*if (lineOfSight(parent[u], v)) {
            u = parent[u];

            float newWeight = distance[u] + physicalDistance(u, v);
            if (newWeight < distance[v]) {
                distance[v] = newWeight;
                parent[v] = u;
                return true;
            }
            return false;
        } else {
            float newWeight = distance[u] + weightUV;
            if (newWeight < distance[v]) {
                distance[v] = newWeight;
                parent[v] = u;
                return true;
            }
            return false;
        }*/
    }
}
