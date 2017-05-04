package algorithms.anya16;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class AnyaExpansionPolicy implements ExpansionPolicy<AnyaNode> {

	private BitpackedGrid grid_;
	private AnyaHeuristic heuristic_;
	private EuclideanDistanceHeuristic euclidean_; 

	private int idx_succ_;
	private AnyaNode start;
	private AnyaNode target;
	private AnyaNode cnode_;
	private AnyaNode csucc_;
	private ArrayList<AnyaNode> successors_;
				
	// reduces branching by eliminating nodes that cannot have successors
	private boolean prune_ = true;
	
	// the location of the target
	protected double tx_, ty_;
	
	public AnyaExpansionPolicy(BitpackedGrid grid) throws Exception
	{
		this(grid, true);
	}
	
	public AnyaExpansionPolicy(BitpackedGrid grid, boolean prune) throws Exception
	{
		this.grid_ = grid;
		
		this.prune_ = prune;
		successors_ = new ArrayList<AnyaNode>(32);
		heuristic_ = new AnyaHeuristic();
		euclidean_ = new EuclideanDistanceHeuristic();
	}
	
	// need to work with nodes, not vertices and edges
	@Override
	public void expand(AnyaNode vertex) 
	{
		this.cnode_ = vertex;
		this.csucc_ = null;
		this.idx_succ_ = 0;
		successors_.clear();
		
		if(vertex.equals(start))
		{
			generate_start_successors(cnode_, successors_);
		}
		else
		{
			generate_successors(cnode_, successors_);
		}
    }

	@Override
	public AnyaNode next() 
	{
		csucc_ = null;
		if(idx_succ_ < successors_.size())
		{
			csucc_ = successors_.get(idx_succ_++);
		}
		return csucc_;
	}

	@Override
	public boolean hasNext() 
	{
		return idx_succ_ < successors_.size();
	}
		
	@Override
	public double step_cost() 
	{
		assert(cnode_ != null && csucc_ != null);
		double retval = euclidean_.h(cnode_.root.x, cnode_.root.y, 
					csucc_.root.x, csucc_.root.y); 
		return retval;
	}

	@Override
	public Heuristic<AnyaNode> heuristic() 
	{
		return heuristic_;
	}
	
    // we require that the cells (s.x, s.y) and (t.x, t.y) are 
    // non-obstacle locations; i.e. the instances are valid
    // both for the corner graph that we search and also on
    // the cell-based graph representation of the grid.
    // We make this decision to keep compatibility with 
    // Nathan Sturtevant's benchmarks (http://movingai.com)
    // i.e. every benchmark problem should be solvable and 
    // any that isn't should also fail here
    //@Override
    public boolean validate_instance_old(AnyaNode start, AnyaNode target) 
    {
        this.start = start;
        this.target = target;
        tx_ = this.target.root.x;
        ty_ = this.target.root.y;
        boolean result = 
                grid_.get_cell_is_traversable((int)start.root.x, (int)start.root.y) &&
                grid_.get_cell_is_traversable((int)target.root.x, (int)target.root.y);
        return result;
    }

    // Revised validation to validate cases only based on the corner graph.
    @Override
    public boolean validate_instance(AnyaNode start, AnyaNode target) 
    {
        this.start = start;
        this.target = target;
        tx_ = this.target.root.x;
        ty_ = this.target.root.y;

        int startX = (int)start.root.x;
        int startY = (int)start.root.y;
        int targetX = (int)target.root.x;
        int targetY = (int)target.root.y;

        boolean startResult = grid_.get_cell_is_traversable(startX, startY) ||
                            grid_.get_cell_is_traversable(startX-1, startY) ||
                            grid_.get_cell_is_traversable(startX, startY-1) ||
                            grid_.get_cell_is_traversable(startX-1, startY-1);

        boolean targetResult = grid_.get_cell_is_traversable(targetX, targetY) ||
                            grid_.get_cell_is_traversable(targetX-1, targetY) ||
                            grid_.get_cell_is_traversable(targetX, targetY-1) ||
                            grid_.get_cell_is_traversable(targetX-1, targetY-1);

        boolean result = startResult && targetResult;
        return result;
    }
	
	public BitpackedGrid getGrid() { return grid_; }
		
    protected void generate_successors(
    		AnyaNode node, ArrayList<AnyaNode> retval)
    {
    	IntervalProjection projection = new IntervalProjection();
    	
    	if(node.root.y == node.interval.getRow())
    	{
        	projection.project(node, grid_);
	    	flat_node_obs(node, retval, projection);	    	
	    	projection.project_f2c(node, grid_);
	    	flat_node_nobs(node, retval, projection);
    	}
    	else
    	{
        	projection.project(node, grid_);
        	cone_node_obs(node, retval, projection);
        	cone_node_nobs(node, retval, projection);
    	}
    }
    
    protected void generate_start_successors(AnyaNode node, 
    		ArrayList<AnyaNode> retval)
    {
    	assert(node.interval.getLeft() == node.interval.getRight() &&
    			node.interval.getLeft() == node.root.x &&
    			node.interval.getRow() == node.root.y);

    	
    	// certain successors will be ignored if the start is a double-corner
    	boolean start_dc = grid_.get_point_is_double_corner(
    			(int)node.root.x, (int)node.root.y);
    	
    	// certain start locations are ambiguous; we don't try to solve these
    	/*if(start_dc && !grid_.get_cell_is_traversable(
    			(int)node.root.x, (int)node.root.y)) 
    	{ return;  }*/
    	
    	int rootx = (int)node.root.x;
    	int rooty = (int)node.root.y;
    		
    	// generate flat observable successors left of the start point
    	// NB: hacky implementation; we use a fake root for the projection
    	IntervalProjection projection = new IntervalProjection();
    	if(!start_dc)
    	{
	    	projection.project(rootx, rootx, rooty, 
	    			rootx+1, rooty, grid_);
	    	this.generate_observable_flat__(projection, 
	    			rootx, rooty, node, retval);
    	}

    	// generate flat observable successors right of the start point
    	// NB: hacky implementation; we use a fake root for the projection
    	projection.project(rootx, rootx, rooty, 
    			rootx-1, rooty, grid_);
    	this.generate_observable_flat__(projection, 
    			rootx, rooty, node, retval);
    	
    	// generate conical observable successors below the start point 
        int max_left = grid_.scan_cells_left(rootx-1, rooty)+1;
        int max_right = grid_.scan_cells_right(rootx, rooty);
    	if(max_left != rootx && !start_dc)
    	{
    		split_interval_make_successors(max_left, rootx, rooty+1, 
    				rootx, rooty, rooty+1, node, retval);
    	}    	
    	if(max_right  != rootx)
    	{
    		split_interval_make_successors(rootx, max_right, rooty+1, 
    				rootx, rooty, rooty+1, node, retval);
    	}

    	// generate conical observable successors above the start point
    	max_left = grid_.scan_cells_left(rootx-1, rooty-1)+1;
    	max_right = grid_.scan_cells_right(rootx, rooty-1);
    	if(max_left != rootx && !start_dc)
    	{
    		split_interval_make_successors(max_left, rootx, rooty-1, 
    				rootx, rooty, rooty-2, node, retval);
    	}    	
        
    	if(max_right != rootx)
    	{
    		split_interval_make_successors(rootx, max_right, rooty-1, 
    				rootx, rooty, rooty-2, node, retval);
    	}
    }
    
    private void split_interval_make_successors(
    		double max_left, double max_right, int irow,
    		int rootx, int rooty, int sterile_check_row, 
    		AnyaNode parent, ArrayList<AnyaNode> retval)
    {
    	if(max_left == max_right) { return; }
    	
    	double succ_left = max_right;
    	double succ_right;
    	int num_successors = retval.size();
    	boolean target_node = contains_target(max_left, max_right, irow);
    	boolean forced_succ = !prune_ || target_node;
    	
    	AnyaNode successor = null;
    	do
    	{
    		succ_right = succ_left;
    		succ_left = grid_.scan_left(succ_right, irow);
    		if(forced_succ || 
    		   !sterile(succ_left, succ_right, sterile_check_row))
    		{
    			successor = new AnyaNode(parent, 
    					new AnyaInterval(succ_left, succ_right, irow), 
    					rootx, rooty);
    			successor.interval.setLeft(
    					succ_left < max_left ? max_left : succ_left);
    			retval.add(successor);
    		}
    	}while((succ_left != succ_right) && (succ_left > max_left));
    	
    	
    	// TODO: recurse over every node (NB: intermediate check includes goal check) 
    	// TODO: recurse until we start heading e.g. up instead of down (flat is ok)
    	if(!forced_succ && retval.size() == (num_successors+1) && 
    			intermediate(successor.interval, rootx, rooty))
    	{
    		retval.remove(retval.size()-1);
    		// TODO: optimise this new call out?
    		IntervalProjection proj = new IntervalProjection();
    		proj.project_cone(
    				successor.interval.getLeft(), 
    				successor.interval.getRight(), 
    				successor.interval.getRow(),
    				rootx, rooty, grid_);
    		if(proj.valid && proj.observable)
    		{
    			split_interval_make_successors(proj.left, proj.right, proj.row, 
    					rootx, rooty, proj.sterile_check_row, parent, retval);
    		}
    	}
    }
    
    // return true if the non-discrete points of the interval 
    // [@param left, @param right] on @param row sit adjacent to
    // any obstacle cells.
    //
    // TODO: can we do this better/faster by keeping track of whether the
    // interval endpoints are open or closed? i.e. only generate
    // intervals with two closed endpoints and ignore the rest unless
    // they contain the start or goal. Should work because for every semi-open 
    // interval (a, b] we can create two new intervals (a, b) and [b, c]
    private boolean sterile(double left, double right, int row)
    {
    	int r = (int)(right-BitpackedGrid.epsilon);
    	int l = (int)(left+BitpackedGrid.epsilon);
    	boolean result = !(
			(grid_.get_cell_is_traversable(l, row) && 
			 grid_.get_cell_is_traversable(r, row)));
    	return result;
    }
    
    // return true if the interval [@param left, @param right] on
    // has no adjacent successors on @param row.
    // NB: This code is not inside IntervalProjection because the
    // area inside a projection needs to be split into individual 
    // intervals.
    private boolean intermediate(AnyaInterval interval, int rootx, int rooty)
    {       	    	
       	// intermediate nodes have intervals that are not taut; i.e.
    	// their endpoints are not adjacent to any location that cannot be 
    	// directly observed from the root.
    	
    	double left = interval.getLeft();
    	double right = interval.getRight();
    	int row = interval.getRow();
    	
    	int tmp_left = (int)left;
    	int tmp_right = (int)right;
    	boolean discrete_left = interval.discrete_left;
       	boolean discrete_right = interval.discrete_right; 
       	
       	boolean rightroot = ((tmp_right - rootx) >>> 31) == 1;
       	boolean leftroot = ((rootx - tmp_left) >>> 31) == 1;
       	boolean right_turning_point = false;
		boolean left_turning_point = false;
       	if(rooty < row)
       	{
       		left_turning_point = discrete_left && 
       				grid_.get_point_is_corner(tmp_left, row) &&
       				(!grid_.get_cell_is_traversable(tmp_left-1, row-1) ||
       						leftroot);
       		right_turning_point = discrete_right && 
       				grid_.get_point_is_corner(tmp_right, row) &&
       				(!grid_.get_cell_is_traversable(tmp_right, row-1) ||
       						rightroot);   				       	
   		}
       	else
       	{
       		left_turning_point = discrete_left && 
       				grid_.get_point_is_corner(tmp_left, row) &&
       				(!grid_.get_cell_is_traversable(tmp_left-1, row) ||
       						leftroot);
       		right_turning_point = discrete_right && 
       				grid_.get_point_is_corner(tmp_right, row) &&
       				(!grid_.get_cell_is_traversable(tmp_right, row) ||
       						rightroot);   				
       	}
       	       	
       return !((discrete_left && left_turning_point) || 
       		    (discrete_right && right_turning_point));
    }
    
    
    private boolean contains_target(double left, double right, int row)
    {
		return (row == ty_) && (tx_ >= left-BitpackedGrid.epsilon) && (tx_ <= right+BitpackedGrid.epsilon);    	
    }    
    
    // TODO: assumes vertical move to the next row is always valid.
    // there is an inductive argument here: if the move is not valid
    // the node should have been pruned. check this is always true.
    protected void cone_node_obs(AnyaNode node, 
    		ArrayList<AnyaNode> retval, IntervalProjection projection)
    {    	
    	assert(node.root.y != node.interval.getRow());

    	Point2D.Double root = node.root;
    	generate_observable_cone__(projection, (int)root.x, (int)root.y,
			node, retval);
    }
    
    private void generate_observable_cone__(IntervalProjection projection,
		int rootx, int rooty, AnyaNode parent, ArrayList<AnyaNode> retval)
	{
    	if(!(projection.valid && projection.observable)) { return; }
    	split_interval_make_successors(projection.left, projection.right, 
    			(int)projection.row, rootx, rooty, 
    			projection.sterile_check_row, parent, retval);
    }

    // there are two kinds of non-observable successors
    // (i) conical successors that are adjacent to an observable projection
    // (ii) flat successors that are adjacent to the current interval
    // (iii) conical successors that are not ajdacent to to any observable
    // projection or the current interval (i.e the angle from the root
    // to the interval is too low to observe any point from the next row)
    // TODO: seems like too many branching statements in this function. consolidate?
    protected void cone_node_nobs(AnyaNode node, 
    		ArrayList<AnyaNode> retval, IntervalProjection projection)
    {
    	if(!projection.valid) { return; }
    	
		double ileft = node.interval.getLeft();
		double iright = node.interval.getRight();
		int irow = (int)node.interval.getRow();

		// non-observable successor type (iii)
		if(!projection.observable)
		{
			if(node.root.x > iright && node.interval.discrete_right &&
					grid_.get_point_is_corner((int)iright, irow))
			{
				split_interval_make_successors(
		    			projection.max_left, iright, projection.row,
		    			(int)iright, irow, projection.sterile_check_row, node, retval);
			}
			else if(node.root.x < ileft && node.interval.discrete_left &&
					grid_.get_point_is_corner((int)ileft, irow))
			{
				split_interval_make_successors(
		    			ileft, projection.max_right, projection.row,
		    			(int)ileft, irow, projection.sterile_check_row, node, retval);
			}
			// non-observable successors to the left of the current interval
			if( node.interval.discrete_left &&
					!grid_.get_cell_is_traversable(
							(int)ileft-1, projection.type_iii_check_row) &&
					grid_.get_cell_is_traversable(
							(int)ileft-1, projection.check_vis_row))
			{
		    	projection.project_flat(ileft-grid_.smallest_step_div2, ileft,
		    			(int)ileft, (int)irow, grid_); 
		    	generate_observable_flat__(projection, (int)ileft, irow, 
		    			node, retval);	 	    	
			}
			// non-observable successors to the right of the current interval
			if( node.interval.discrete_right &&
					!grid_.get_cell_is_traversable(
							(int)iright, projection.type_iii_check_row) &&
					grid_.get_cell_is_traversable(
							(int)iright, projection.check_vis_row))
			{
		    	projection.project_flat(iright, iright+grid_.smallest_step_div2, 
		    			(int)iright, (int)irow, grid_); // NB: dummy root
		    	generate_observable_flat__(projection, (int)iright, irow, 
		    			node, retval);	 	    	
			}			
			return;
		}
		
		// non-observable successors type (i) and (ii)
    	IntervalProjection flatprj = new IntervalProjection();    	
    	int corner_row = irow - (((int)node.root.y - irow) >>> 31);
		
		// non-observable successors to the left of the current interval
		if( node.interval.discrete_left &&
				grid_.get_point_is_corner((int)ileft, irow))
		{
			// flat successors from the interval row
			if(!grid_.get_cell_is_traversable((int)(ileft-1), corner_row))
			{
		    	flatprj.project(ileft-BitpackedGrid.epsilon, iright, (int)irow, 
		    			(int)ileft, (int)irow, grid_);
		    	generate_observable_flat__(flatprj, (int)ileft, irow, 
		    			node, retval);	 	    	
			}				

			// conical successors from the projected row
			split_interval_make_successors(
	    			projection.max_left, projection.left, projection.row,
	    			(int)ileft, irow, projection.sterile_check_row, node, retval);
		}
		
		// non-observable successors to the right of the current interval
		if( node.interval.discrete_right && 
				grid_.get_point_is_corner((int)iright, irow))
		{
			// flat successors from the interval row
			if(!grid_.get_cell_is_traversable((int)(iright), corner_row))
			{				
		    	flatprj.project(ileft, iright+BitpackedGrid.epsilon, (int)irow, 
		    			(int)ileft, (int)irow, grid_);
		    	generate_observable_flat__(flatprj, (int)iright, irow, 
		    			node, retval);	 	    	
			}
			
			// conical successors from the projected row
			split_interval_make_successors(
	    			projection.right, projection.max_right, projection.row,
	    			(int)iright, irow, projection.sterile_check_row, node, retval);
		}
    }
    
    protected void flat_node_obs(AnyaNode node, 
    		ArrayList<AnyaNode> retval, IntervalProjection projection)
    {
    	Point2D.Double root = node.root;
    	generate_observable_flat__(projection, (int)root.x, (int)root.y,
			node, retval);
    }
    
    private void generate_observable_flat__(IntervalProjection projection,
		int rootx, int rooty, AnyaNode parent, ArrayList<AnyaNode> retval)
	{
		assert(projection.row == rooty);
		if(!projection.valid) { return; }
		
		boolean goal_interval = 
			contains_target(projection.left, projection.right, projection.row);
		if(projection.intermediate && prune_ && !goal_interval)
		{
			// ignore intermediate nodes and project further along the row
			projection.project(projection.left, projection.right, 
					projection.row, rootx, rooty, grid_);
			// check if the projection contains the goal
			goal_interval = 
				contains_target(projection.left, projection.right, 
						projection.row);
		}
		
		if(!projection.deadend || !prune_ || goal_interval)
		{
			retval.add(
				new AnyaNode(parent,
					new AnyaInterval(projection.left, projection.right, 
							projection.row), rootx, rooty));
		}
    }
    
    protected void flat_node_nobs(AnyaNode node, 
    		ArrayList<AnyaNode> retval, IntervalProjection projection)
    {
    	if(!projection.valid) { return; }
		// conical successors from the projected row

    	int new_rootx;
		int new_rooty = node.interval.getRow();
    	if(node.root.x <= node.interval.getLeft())
    	{
    		new_rootx = (int)node.interval.getRight();
    	}
    	else
    	{
    		new_rootx = (int)node.interval.getLeft();
    	}
    	
		split_interval_make_successors(
    			projection.left, projection.right, projection.row,
    			new_rootx, new_rooty, projection.sterile_check_row, node, retval);
    }
        
    public int hash(AnyaNode n)
    {
    	int x = (int)n.root.x;
    	int y = (int)n.root.y;
    	return y*grid_.get_padded_width() + x;
    }

	@Override
	public int hashCode(AnyaNode v) 
	{
		return v.hashCode();
	}
}

