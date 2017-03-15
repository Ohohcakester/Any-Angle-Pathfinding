package org.jgrapht.traverse;

import pgraph.base.BaseVertex;

// A heuristic for computing Euclidean distances in the plane.
//
// @author: dharabor
// @created: 2015-05-25
//

public class EuclideanDistanceHeuristic implements Heuristic<BaseVertex> 
{
	@Override
	public double getValue(BaseVertex n) 
	{
		return 0;
	}

	@Override
	public double getValue(BaseVertex n, BaseVertex t) 
	{
		if(n == null || t == null) { return 0; }
		return h(n.pos.x, n.pos.y, t.pos.x, t.pos.y);
	}

	public double h(double x1, double y1, double x2, double y2)
	{
		double delta_x = x1 - x2;
		double delta_y = y1 - y2;
		return Math.sqrt(delta_x*delta_x + delta_y*delta_y);
	}
}
