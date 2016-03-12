package main;

import grid.GridGraph;
import algorithms.PathFindingAlgorithm;

public interface AlgoFunction {
    public abstract PathFindingAlgorithm getAlgo(GridGraph gridGraph, int sx, int sy, int ex, int ey);
}