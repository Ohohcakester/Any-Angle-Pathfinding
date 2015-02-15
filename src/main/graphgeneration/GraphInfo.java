package main.graphgeneration;

public class GraphInfo {
	public int seed;
	public int sizeX;
	public int sizeY;
	public int ratio;

	public GraphInfo(int seed, int sizeX, int sizeY, int ratio, int nTrials) {
        this.seed = seed;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.ratio = ratio;
    }
}