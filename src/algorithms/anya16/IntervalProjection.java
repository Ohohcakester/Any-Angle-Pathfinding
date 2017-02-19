package pgraph.anya;

import pgraph.grid.BitpackedGrid;

// IntervalProjection.java
//
// This class is used to project intervals from one location on the
// grid onto another. There are two types of projections:
// 	- Flat projections 
// 	- Conical projections
// 
// The projection types correspond to the two types of nodes
// used by Anya.
// 
// @author: dharabor
// @created: 2015-05-04
//

public class IntervalProjection 
{	
	// the actual endpoints of the projected interval
	double left;
	double right;
	
	// the furthest point left (resp. right) which is
	// visible from the the left (resp. right) endpoint
	// of the projected interval.
	double max_left;
	double max_right;
	int row;
	
	// a projection is valid if it is possible to move the endpoints
	// of the interval to the adjacent row (up or down) without
	// intersecting an obstacle
	boolean valid;
	
	// A projection is observable if the projected left endpoint is 
	// strictly smaller than the projected right endpoint.
	// NB: a projection can be valid but non-observable.
	boolean observable;

	// these variables only used for conical projection
	// some terminology:
	// we analyse the cells of this row in order to determine if 
	// the successors inside a conical projection are sterile or not. 
	int sterile_check_row;
	int check_vis_row;
	
	// used when generating type iii non-observable conical successors
	int type_iii_check_row; 
			
	// these variables only used for flat projection
	// some terminology:
	// a deadend flat node is one that cannot be projected further.
	// an intermediate flat node is one that does not hug any walls.
	boolean deadend;  
	boolean intermediate;
	
	public IntervalProjection()
	{
		valid = false;
	}
	
	// project the interval associated with @param node. the type of projection
	// (conical or flat) depends on the location of the interval of @param node
	// relative to its root. 
	public void project(AnyaNode node, BitpackedGrid grid)
	{
		project(node.interval.getLeft(), node.interval.getRight(), 
				(int)node.interval.getRow(),
				(int)node.root.x, (int)node.root.y, grid);
	}
	
	public void project(double ileft, double iright, int irow,
			int rootx, int rooty, BitpackedGrid grid)
	{
		observable = false;
		valid = false;
    	if(rooty == irow)
    	{
    		project_flat(ileft, iright,	rootx, rooty, grid);
    	}
    	else
    	{
        	project_cone(ileft, iright, irow, rootx, rooty, grid); 
    	}
	}
		
	public void project_cone(double ileft, double iright, int irow, 
			int rootx, int rooty, BitpackedGrid grid)
	{
    	if(rooty < irow) // project down
    	{
    		check_vis_row = irow;
    		sterile_check_row = row = irow + 1;
    		type_iii_check_row = irow - 1;
    	}
    	else // project up
    	{
    		assert(rooty > irow);
    		sterile_check_row = irow - 2;
    		row = check_vis_row = irow - 1;
    		type_iii_check_row = irow;
    	}
    	
    	
    	valid = grid.get_cell_is_traversable(
    				(int)(ileft+grid.smallest_step_div2), check_vis_row) &&
    			grid.get_cell_is_traversable(
					(int)(iright-grid.smallest_step_div2), check_vis_row);
    	
    	if(!valid) { return; } 
    	
		// interpolate the endpoints of the new interval onto the next row.
    	// TODO: cache rise, lrun, rrun and y_delta with the root
    	// to avoid branch instructions here?
    	double rise = Math.abs(irow - rooty);
    	double lrun = rootx - ileft;
    	double rrun = iright - rootx;    	    	

		// clip the interval if visibility from the root is obstructed.
		// NB: +1 because we convert from tile coordinates to point coords
    	max_left =  grid.scan_cells_left((int)ileft, check_vis_row) + 1;
    	left = Math.max(ileft - lrun/rise, max_left);    	

    	max_right = grid.scan_cells_right((int)iright, check_vis_row);
    	right = Math.min(iright + rrun/rise, max_right);
    	
    	observable = (left < right);
    	
    	// sanity checking; sometimes an interval cannot be projected 
    	// all the way to the next row without first hitting an obstacle.
    	// in these cases we need to reposition the endpoints appropriately
    	if(left >= max_right) 
    	{ 
    		left = grid.get_cell_is_traversable(
    				(int)(ileft-grid.smallest_step_div2), check_vis_row) ?
    						right : max_left;
		}
    	if(right <= max_left) 
    	{ 
    		right = grid.get_cell_is_traversable(
    					(int)iright, check_vis_row) ? 
    						left : max_right;
    	}
	}
	
	// @param ileft: the left endpoint of the interval being projected
	// @param iright: the right endpoint of the interval being projected
	// @param rootx, @param rooty: the coordinates of the root point
	public void project_flat(double ileft, double iright,  
			int rootx, int rooty, BitpackedGrid grid)
	{
		if(rootx <= ileft)
		{
			left = iright;
			right = grid.scan_right(left, rooty);
			deadend = !( 
				grid.get_cell_is_traversable((int)right, rooty) &&
			    grid.get_cell_is_traversable((int)right, rooty-1));	
		}
		else
		{
			right = ileft;
			left = grid.scan_left(right, rooty);
			deadend = !(
				grid.get_cell_is_traversable((int)(left-grid.smallest_step_div2), rooty) &&
				grid.get_cell_is_traversable((int)(left-grid.smallest_step_div2), rooty-1));
		}
		
		intermediate = 
				grid.get_cell_is_traversable((int)left, rooty) &&
			    grid.get_cell_is_traversable((int)left, rooty-1);

		row = rooty;
		valid = (left != right);
	}
	
	// project through a flat node and onto an adjacent grid row
	public void project_f2c(AnyaNode node, BitpackedGrid grid)
	{
		assert(node.interval.getRow() == node.root.y);
		project_f2c(node.interval.getLeft(), node.interval.getRight(), 
				(int)node.interval.getRow(),
				(int)node.root.x, (int)node.root.y, grid);
	}

	
	private void project_f2c(double ileft, double iright, int irow,
			int rootx, int rooty, BitpackedGrid grid)
	{
		// look to the right for successors
    	// recall that each point (x, y) corresponds to the
    	// top-left corner of a tile at location (x, y)
		if(rootx <= ileft)
		{	
			// can we make a valid turn? valid means 
			// (i) the path bends around a corner; 
			// (ii) we do not step through any obstacles or through 
			// double-corner points.
			boolean can_step = 
					grid.get_cell_is_traversable((int)iright, irow) && 
					grid.get_cell_is_traversable((int)iright, irow-1);
			if(!can_step) { valid = false; observable = false; return; }
			
			// if the tile below is free, we must be going up
			// else we round the corner and go down			
			if(!grid.get_cell_is_traversable((int)iright-1, irow))
			{	// going down
				sterile_check_row = row = irow+1;
				check_vis_row = irow;
			}
			else
			{ 	// going up
				row = check_vis_row = irow-1;
				sterile_check_row = irow-2;
			}
			
			left = max_left = iright;
			right = max_right = grid.scan_cells_right((int)left, check_vis_row);
		}	
		else
		{ // look to the left for successors
			assert(rootx >= iright);
			boolean can_step = 
					grid.get_cell_is_traversable((int)ileft-1, irow) && 
					grid.get_cell_is_traversable((int)ileft-1, irow-1);
			if(!can_step) { valid = false; observable = false; return; }
			
			// if the tiles below are free, we must be going up
			// else we round the corner and go down		
			if(!grid.get_cell_is_traversable((int)ileft, irow))
			{ 	// going down
				check_vis_row = irow;
				sterile_check_row = row = irow+1;  
			}
			else
			{ 	// going up
				row = check_vis_row = irow-1;
				sterile_check_row = irow-2;
			}	
			
			right = max_right = ileft;
			left = max_left = grid.scan_cells_left((int)right-1, check_vis_row)+1;
		}
		valid = true;
		observable = false;			
	}
	
	public boolean getValid() { return valid; }
	
}
