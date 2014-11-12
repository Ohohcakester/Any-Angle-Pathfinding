package grid.visibilitygraph;

public class Edge {
    public final int source;
    public final int dest;
    public final float weight;
    
    public Edge(int source, int dest, float weight) {
        this.source = source;
        this.dest = dest;
        this.weight = weight;
    }
}
