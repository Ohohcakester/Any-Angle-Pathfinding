package grid;

import java.util.ArrayList;

/**
 * A problem instance - a GridGraph and the Start, Goal points.
 */
public class GridAndGoals {
	public final GridGraph gridGraph;
	public final StartGoalPoints startGoalPoints;
	
	public GridAndGoals(GridGraph gridGraph, StartGoalPoints startGoalPoints) {
		this.gridGraph = gridGraph;
		this.startGoalPoints = startGoalPoints;
	}
	
	public GridAndGoals(GridGraph gridGraph, int sx, int sy, int ex, int ey) {
		this.gridGraph = gridGraph;
		this.startGoalPoints = new StartGoalPoints(sx, sy, ex, ey);
	}

    public final void validateStartAndEndPoints() {
        int sx = startGoalPoints.sx;
        int sy = startGoalPoints.sy;
        int ex = startGoalPoints.ex;
        int ey = startGoalPoints.ey;
        int sizeX = gridGraph.sizeX;
        int sizeY = gridGraph.sizeY;

        ArrayList<String> issues = new ArrayList<>();

        if (sx < 0) issues.add("sx < 0");
        if (sy < 0) issues.add("sy < 0");
        if (sx > sizeX) issues.add("sx > sizeX");
        if (sy > sizeY) issues.add("sy > sizeY");

        if (ex < 0) issues.add("ex < 0");
        if (ey < 0) issues.add("ey < 0");
        if (ex > sizeX) issues.add("ex > sizeX");
        if (ey > sizeY) issues.add("ey > sizeY");
        
        if (issues.size() > 0) {
            throw new UnsupportedOperationException("INVALID START/END POINTS: " + issues);
        }
    }
}
