package grid;

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
}
