package grid;


public class AStar {

    protected boolean postSmoothingOn() {
        return true;
    }
    protected float heuristicWeight() {
        return 1f;
    }
    
    protected GridGraph graph;
    protected Float distance[];
    protected int parent[];
    
    IndirectHeap<Float> pq; 
    
    private final int sizeX;
    private final int sizeY;

    private int finish;
    private int ex;
    private int ey;


    private int toOneDimIndex(int x, int y) {
        return y*sizeX + x;
    }
    
    private int toTwoDimX(int index) {
        return index%sizeX;
    }
    
    private int toTwoDimY(int index) {
        return index/sizeX;
    }
    
    public AStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        this.graph = graph;
        sizeX = graph.sizeX;
        sizeY = graph.sizeY;
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        this.ex = ex;
        this.ey = ey;
        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);
        
        distance = new Float[totalSize];
        parent = new int[totalSize];
        
        initialise(start);
        boolean[] visited = new boolean[totalSize];
        
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            if (current == finish) {
                maybePostSmooth();
                return;
            }
            visited[current] = true;

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);

            tryRelax(visited, current, x, y, x-1, y-1);
            tryRelax(visited, current, x, y, x, y-1);
            tryRelax(visited, current, x, y, x+1, y-1);
            
            tryRelax(visited, current, x, y, x-1, y);
            tryRelax(visited, current, x, y, x+1, y);
            
            tryRelax(visited, current, x, y, x-1, y+1);
            tryRelax(visited, current, x, y, x, y+1);
            tryRelax(visited, current, x, y, x+1, y+1);
            
        }
        
        maybePostSmooth();
    }
    
    private void tryRelax(boolean[] visited, int current, int currentX, int currentY, int x, int y) {
        if (!graph.isValidCoordinate(x, y))
            return;
        
        int destination = toOneDimIndex(x,y);
        if (visited[destination])
            return;
        if (!graph.lineOfSight(currentX, currentY, x, y))
            return;
        
        if (relax(current, destination, weight(currentX, currentY, x, y))) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance[destination] + heuristic(x,y));
        }
    }

    private float heuristic(int x, int y) {
        //return 0;
        return heuristicWeight()*graph.distance(x, y, ex, ey);
    }


    private float weight(int x1, int y1, int x2, int y2) {
        return graph.distance(x1, y1, x2, y2);
    }
    

    public Float[] getDistance(){return distance;}
    public int[] getParent(){return parent;}
    
    
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        
        float newWeight = distance[u] + weightUV;
        if (newWeight < distance[v]) {
            distance[v] = newWeight;
            parent[v] = u;
            return true;
        }
        return false;
    }
    
    /*protected final boolean relax(Edge edge) {
        // return true iff relaxation is done.
        return relax(edge.getSource(), edge.getDest(), edge.getWeight());
    }*/
    
    protected final void initialise(int s) {
        for (int i=0; i<distance.length; i++) {
            distance[i] = Float.POSITIVE_INFINITY;
            parent[i] = -1;
        }
        distance[s] = 0f;
    }
    
    private int pathLength() {
        int length = 0;
        int current = finish;
        while (current != -1) {
            current = parent[current];
            length++;
        }
        return length;
    }
    
    protected boolean lineOfSight(int node1, int node2) {
        int x1 = toTwoDimX(node1);
        int y1 = toTwoDimY(node1);
        int x2 = toTwoDimX(node2);
        int y2 = toTwoDimY(node2);
        return graph.lineOfSight(x1, y1, x2, y2);
    }

    protected float physicalDistance(int node1, int node2) {
        int x1 = toTwoDimX(node1);
        int y1 = toTwoDimY(node1);
        int x2 = toTwoDimX(node2);
        int y2 = toTwoDimY(node2);
        return graph.distance(x1, y1, x2, y2);
    }
    
    private void maybePostSmooth() {
        if (postSmoothingOn()) {
            postSmooth();
        }
    }
    
    private void postSmooth() {

        int current = finish;
        while (current != -1) {
            int next = parent[current];
            if (next != -1) {
                next = parent[current];
                while (next != -1) {
                    if (lineOfSight(current,next)) {
                        parent[current] = next;
                        next = parent[next];
                    } else {
                        next = -1;
                    }
                }
            }
            
            current = parent[current];
        }
    }
    

    public int[][] getPath() {
        int length = pathLength();
        int[][] path = new int[length][];
        int current = finish;
        
        int index = length-1;
        while (current != -1) {
            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            
            path[index] = new int[2];
            path[index][0] = x;
            path[index][1] = y;
            
            index--;
            current = parent[current];
        }
        
        return path;
        
    }
}