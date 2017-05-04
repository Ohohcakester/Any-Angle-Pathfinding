package algorithms.anya16;

import grid.GridGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;

// A simple uniform-cost lattice / cell grid.
// This implementation uses a bitpacked representation
// in order to improve time and space efficiency.
//
// @author: dharabor
// @created: 2015-04-16
//

public class BitpackedGrid 
{
	// a very small number; smaller than the smallest step size
	// provided no individual grid dimension is larger than epsilon^(-1)
	public static final double epsilon = 0.0000001;
	
	// we frame the map with some extra obstacles 
	// this helps us avoid unnecessary boundary testing
	private static final int padding_ = 2;  
	
	// we use single int words when packing the grid;
	// each vertex is assigned one bit per word
	private static final int BITS_PER_WORD = 32;
	private static final int LOG2_BITS_PER_WORD = 5;
	private static final int INDEX_MASK = BITS_PER_WORD - 1;
		
	// the original dimensions of the gridmap
	private int map_height_original_;
	private int map_width_original_;	

	// the padded dimensions of the gridmap
	// we round each row to the nearest number of words (+1)
	// and we add one row of padding before the first row
	// one row of padding after the last row
	private int map_height_;
	private int map_width_;
	private int map_size_;
	private int map_width_in_words_; // for convenience
	
	// data describing the cell grid
	private int[] map_cells_; 
	
	// data relating to the discrete set of lattice points that 
	// together form the cell grid; the data is redundant but 
	// we keep it anyway for better performance.
	private int[] visible_;
	private int[] corner_;
	private int[] double_corner_;
	
	// there are a finite number of places where an edge can
	// be intersected. This variable stores the smallest 
	// distance between any two (adjacent) such points.
	public double smallest_step;
	public double smallest_step_div2;
	
    public BitpackedGrid(String mapfile) throws Exception
    {
        this.load(mapfile);
    }

    public BitpackedGrid(GridGraph gridGraph) throws Exception
    {
        this.load(gridGraph);
    }
	
	// @param width: the horizontal size of the lattice, as 
	// 				 measured in square cells
	// @param height: the vertical size of the lattice, as 
	//				 measured in square cells
	public BitpackedGrid(int width, int height)
	{
		init(width, height);
	}
	
	private void init(int width, int height)
	{
		this.map_height_original_ = height;
		this.map_width_original_ = width;
		this.map_width_in_words_ = ((width >> LOG2_BITS_PER_WORD)+1);
		this.map_width_ = map_width_in_words_ <<LOG2_BITS_PER_WORD;
		this.map_height_ = height+2*padding_;

		this.map_size_ = ((map_height_ * map_width_) 
				>> LOG2_BITS_PER_WORD);
		this.map_cells_ = new int[map_size_];
		this.visible_ = new int[map_size_];
		this.corner_ = new int[map_size_];
		this.double_corner_ = new int[map_size_];
		
		this.smallest_step = 
				Math.min(
					1/(double)this.get_padded_width(),
					1/(double)this.get_padded_height());
		this.smallest_step_div2 = smallest_step / 2.0;
	}
		
	// returns true if the point (x, y) is visible from
	// another discrete point on the grid (i.e. (x, y) is 
	// not adjacent to 4 obstacle tiles).
	public boolean get_point_is_visible(int x, int y)
	{
		return get_bit_value(x, y, visible_);
	}
	
	// returns true if the point (x, y) is adjacent to 
	// (i) exactly one obstacle tile OR (ii) exactly two 
	// diagonally adjacent obstacle tiles
	public boolean get_point_is_corner(int x, int y)
	{
		return get_bit_value(x, y, corner_);
	}
		
	// returns true if the point (x, y) is adjacent to 
	// exactly two diagonally adjacent obstacle tiles.
	public boolean get_point_is_double_corner(int x, int y)
	{
		return get_bit_value(x, y, double_corner_);
	}
	
	// returns true if the cell (x, y) is not an obstacle 
	public boolean get_cell_is_traversable(int cx, int cy)
	{
		return get_bit_value(cx, cy, map_cells_);
	}

	
	public void set_point_is_visible(int x, int y, boolean value)
	{
		set_bit_value(x, y, value, visible_);
	}
	
	public void set_point_is_corner(int x, int y, boolean value)
	{
		set_bit_value(x, y, value, corner_);
	}
	
	public void set_point_is_double_corner(int x, int y, boolean value)
	{
		set_bit_value(x, y, value, double_corner_);
	}
	
	public boolean get_point_is_discrete(double x, int y)
	{
		return Math.abs((int)(x+this.smallest_step_div2) - x) 
				< this.smallest_step;
	}
		
	public void set_cell_is_traversable(int cx, int cy, boolean value)
	{
		set_bit_value(cx, cy, value, map_cells_);
		update_point(cx, cy);
		update_point(cx+1, cy);
		update_point(cx, cy+1);
		update_point(cx+1, cy+1);		
	}
	
	private void update_point(int px, int py)
	{
		boolean cellNW = get_cell_is_traversable(px-1, py-1);
		boolean cellNE = get_cell_is_traversable(px, py-1);
		boolean cellSW = get_cell_is_traversable(px-1, py);
		boolean cellSE = get_cell_is_traversable(px, py);
		
		boolean corner = 
				((!cellNW | !cellSE) & cellSW & cellNE) |
				((!cellNE | !cellSW) & cellNW & cellSE);
		
		boolean double_corner = 
				((!cellNW & !cellSE) & cellSW & cellNE) ^
				((!cellSW & !cellNE) & cellNW & cellSE);
		
		boolean visible = 
				cellNW | cellNE | cellSW | cellSE;
		
		set_point_is_corner(px, py, corner);
		set_point_is_double_corner(px, py, double_corner);
		set_point_is_visible(px, py, visible);
	}
	
	// TODO: pass int instead of bool (removes one branch instruction)
	private void set_bit_value(int x, int y, boolean value, int[] elts)
	{
		int map_id = get_map_id(x, y);
		int word_index = map_id >> LOG2_BITS_PER_WORD;
		int mask = (1 << ((map_id & INDEX_MASK)));
		int tmp = elts[word_index];
		elts[word_index] = value ? (tmp | mask) : (tmp & ~mask);				
	}
	
	// TODO: pass int instead of bool (removes one branch instruction)
	private boolean get_bit_value(int x, int y, int[] elts)
	{
		int map_id =  get_map_id(x, y);
		int word_index = map_id >> LOG2_BITS_PER_WORD;					
		int mask = 1 << ((map_id & INDEX_MASK));  
		return (elts[word_index] & mask) != 0; 
	}
	
	private int get_map_id(int x, int y)
	{
		x += padding_;
		y += padding_;
		return (y*map_width_ + x);
	}
		
	// returns true if we can traverse along the lattice from the point
	// (x, y) in direction d  until the next row or column.
	public boolean can_step_from_point(double fromx, double fromy, 
			AnyaVertex.VertexDirections d)
	{
		// We test whether (fromx, fromy) is a discrete point. 
		// Note that we only consider the x-dimension as when 
		// traveling along the y-axis we always move from row to row
//		boolean discrete_x = Math.abs(fromx - (int)fromx) < 
//				BitpackedGrid.epsilon;

		boolean discrete_x = ((fromx + this.smallest_step_div2) - (int)fromx)
				< this.smallest_step;
		
		// computing traversability differs slightly depending on whether
		// (x, y) is discrete or not. If (x, y) is discrete, we need to look
		// at two adjacent cells and ensure at least one is not an obstacle.
		// On the other hand, if (x, y) is not discrete, we might only need
		// to look a single cell (e.g. moving up from point (x.z, y) depends
		// only on cell (x, y)
		boolean retval = false;
		switch(d)
		{
			case VD_LEFT:
			{
				int cx = (int)((discrete_x ? fromx - 1 : fromx));
				int cy = (int)fromy;
				retval = 
					(this.get_cell_is_traversable(cx, cy) | 
					this.get_cell_is_traversable(cx, cy-1));
				break;
			}
			case VD_RIGHT:
			{
				int cx = (int)fromx;
				int cy = (int)fromy;
				retval = 
					(this.get_cell_is_traversable(cx, cy) | 
					this.get_cell_is_traversable(cx, cy-1));
				break;
			}
			case VD_UP:
			{
				int cx = (int)fromx;
				int cy = (int)(fromy-1);
				retval = 
					this.get_cell_is_traversable(cx, cy) |
					(discrete_x && this.get_cell_is_traversable(cx-1, cy));
				break;
			}
			case VD_DOWN:
			{
				int cx = (int)fromx;
				int cy = (int)fromy;
				retval = 
					this.get_cell_is_traversable(cx, cy) |
					(discrete_x && this.get_cell_is_traversable(cx-1, cy));
				break;
			}
		}
		return retval;
	}	
	
	// scan the cells of the grid, starting at (@param x, @param y)
	// and heading in the negative x direction.  
	//
	// @return the x-coordinate of the first obstacle node reached by 
	// traveling right from (x, y). if @param x is an obstacle, the return 
	// value is equal to @param x
	public int scan_cells_right(int x, int y)
	{
        int tile_id = get_map_id(x, y);
        int t_index = tile_id >> LOG2_BITS_PER_WORD;
    
    	// ignore obstacles in bit positions < (i.e. to the left of) the starting cell
    	// (NB: big endian order means the leftmost cell is in the lowest bit)
    	int obstacles = ~this.map_cells_[t_index];
    	int start_bit_index = tile_id & INDEX_MASK;
    	int mask = ~((1 << start_bit_index)-1);
    	obstacles &= mask;

    	int stop_pos;
    	int start_index = t_index;
    	while(true)
        { 
        	if(obstacles != 0 )
        	{
        		stop_pos = Integer.numberOfTrailingZeros(obstacles);
        		break;
        	}
        	t_index++;
        	obstacles = ~this.map_cells_[t_index];
        }
        
        int retval = ((t_index - start_index)*BITS_PER_WORD);
        retval += (stop_pos - start_bit_index);
        return x + retval;
	}
	
	// scan the cells of the grid, starting at (@param x, @param y)
	// and heading in the negative x direction.  
	//
	// @return the x-coordinate of the first obstacle node reached by 
	// traveling left from (x, y). if @param x is an obstacle, the return 
	// value is equal to @param x
    public int scan_cells_left(int x,int y)
    {                    	
        int tile_id = get_map_id(x, y);
        int t_index = tile_id >> LOG2_BITS_PER_WORD;
    	
        // scan adjacent cells from the current row and the row above
    	int obstacles = this.map_cells_[t_index];
    	obstacles = ~obstacles;

    	// ignore cells in bit positions > (i.e. to the right of) the starting cell
    	// (NB: big endian order means the rightmost cell is in the highest bit)
    	int start_bit_index = tile_id & INDEX_MASK;
    	int opposite_index = (BITS_PER_WORD - (start_bit_index+1));
    	int mask = (1 << start_bit_index);
    	mask = (mask | (mask-1));
    	obstacles &= mask;

    	int stop_pos;
    	int start_index = t_index;
    	while(true)
        {
        	if(obstacles != 0 )
        	{
        		stop_pos = Integer.numberOfLeadingZeros(obstacles);
        		break;
        	}
        	t_index--;
        	obstacles = ~this.map_cells_[t_index];
        }
        
        int retval = ((start_index - t_index)*BITS_PER_WORD);
        retval += (stop_pos - opposite_index);
        return x - retval;
    }
	
	// scan right along the lattice from location (@param x, @param row).
	// @return the next discrete point that is corner or which is 
	// the last traversable point before an obstacle. 
	// If no such point exists, the return value is equal to x.
	// NB: we assume 
	public int scan_right(double x, int row)
	{
        int left_of_x = (int)(x+this.smallest_step_div2);
        int tile_id = get_map_id(left_of_x, row);
        int t_index = tile_id >> LOG2_BITS_PER_WORD;
        int ta_index = t_index - map_width_in_words_; 
    	
        // scan adjacent cells from the current row and the row above
    	int cells = this.map_cells_[t_index];
    	int cells_above = this.map_cells_[ta_index];
    	
    	int obstacles = ~cells & ~cells_above;
    	int corners = this.corner_[t_index];

    	// ignore corners in bit positions <= (i.e. to the left of) the starting cell
    	// (NB: big endian order means the leftmost cell is in the lowest bit)
    	int start_bit_index = tile_id & INDEX_MASK;
    	int mask = (1 << start_bit_index);
    	corners &= ~(mask | (mask-1));
    	// ignore obstacles in bit positions < (i.e. strictly left of) the starting cell    	
    	// Because we scan cells (and not corners) we cannot ignore the current location. 
    	// To do so might result in intervals that pass through obstacles 
    	// (e.g. current location is a double corner)
    	obstacles &= ~(mask-1); 

    	int stop_pos;
    	int start_index = t_index;
    	while(true)
        {
        	int value = corners | obstacles; 
        	if(value != 0 )
        	{
        		// Each point (x, y) is associated with the top-left 
        		// corner of tile (x, y). When traveling right (cf. left)
        		// we need to stop exactly at the position of the first 
        		// (corner or obstacle) bit. 
        		stop_pos = Integer.numberOfTrailingZeros(value);
        		break;
        	}
        	t_index++;
        	ta_index++;
        	corners = this.corner_[t_index];
        	obstacles = ~this.map_cells_[t_index] & ~this.map_cells_[ta_index];
        }
        
        int retval = left_of_x + ((t_index - start_index)*BITS_PER_WORD + stop_pos);
        retval -= start_bit_index;
        return retval;
	}
	
	// scan left along the lattice from location (@param x, @param row).
	// @return the next discrete point that is corner or which is the last 
	// traversable point before an obstacle. 
	// If no such point exists, the return value is equal to @param x.
    public int scan_left(double x,int row)
    {
        // early return if the next discrete point 
        // left of x is a corner
        int left_of_x = (int)(x);
    	if((x - left_of_x) >= this.smallest_step && 
        	get_point_is_corner(left_of_x, row))
        {
        	return left_of_x;
        }

    	int tile_id = get_map_id(left_of_x, row);
        int t_index = tile_id >> LOG2_BITS_PER_WORD;
        int ta_index = t_index - map_width_in_words_; 
    	
        // scan adjacent cells from the current row and the row above
    	int cells = this.map_cells_[t_index];
    	int cells_above = this.map_cells_[ta_index];
    	
    	int obstacles = ~cells & ~cells_above;
    	int corners = this.corner_[t_index];

    	// ignore cells in bit positions >= (i.e. to the right of) the starting cell
    	// (NB: big endian order means the rightmost cell is in the highest bit)
    	// Because we scan cells (and not just corners) we can safely ignore
    	// the current position. The traversability of its associated cell has
    	// no impact on deciding whether we can travel left, away from the cell.
    	int start_bit_index = tile_id & INDEX_MASK;
    	int mask = (1 << start_bit_index) - 1;
    	corners &= mask;
    	obstacles &= mask;

    	int stop_pos;
    	int start_index = t_index;
    	while(true)
        {
        	int value = corners | obstacles; 
        	if(value != 0 )
        	{
        		// Each point (x, y) is associated with the top-left 
        		// corner of tile (x, y). When counting zeroes to figure
        		// out how far we can travel we end up stopping one 
        		// position before the first set bit. This approach prevents
        		// us from traveling through an obstacle (we stop right before)
        		// but in in the case of corner tiles, we need to stop exactly 
        		// at the position of the set bit. Hence, +1 below.
        		stop_pos = Math.min(
        				Integer.numberOfLeadingZeros(corners)+1, 
        				Integer.numberOfLeadingZeros(obstacles));
        		break;
        	}
        	t_index--;
        	ta_index--;
        	corners = this.corner_[t_index];
        	obstacles = ~this.map_cells_[t_index] & ~this.map_cells_[ta_index];
        }
        
        int retval = left_of_x - ((start_index - t_index)*BITS_PER_WORD + stop_pos);
        retval += ((BITS_PER_WORD) - start_bit_index);
        return retval;
    }
	
	// Load up map files in MovingAI format
	public void load(String mapfile) throws Exception
	{
        FileReader fstream = null;
        fstream = new FileReader(mapfile);
        BufferedReader in = new BufferedReader(fstream);

        String mapType = in.readLine().trim();
        String dim1 = in.readLine().trim();
        String dim2 = in.readLine().trim();
        String mapToken = in.readLine().trim();
        
        if(!mapToken.equals("map"))
        {
        	in.close();
        	throw new Exception(
        			this.getClass().getName()+": " +
        			"Could not load map; unrecognised format.");
        }
        
        if(mapType.equals("octile"))
        {
        	in.close();
        	throw new Exception(
        			this.getClass().getName() + ": "+
        			"Could not load map; only octile types are supported.");
        }
        
        int height, width;
        try
        {
        
        	String[] tmp1 = dim1.split(" ");
        	String[] tmp2 = dim2.split(" ");
        	if(!(
        			(tmp1[0].equals("height") && tmp2[0].equals("width")) || 
        			(tmp1[0].equals("width") && tmp2[0].equals("height"))))
			{
        		in.close();
    			throw new Exception();
			}
        	if(tmp1[0].equals("height"))
        	{
        		height = Integer.parseInt(tmp1[1]);
    			width = Integer.parseInt(tmp2[1]);
        	}
        	else
        	{
    			height = Integer.parseInt(tmp2[1]);
        		width = Integer.parseInt(tmp1[1]);
        	}
        }
        catch(Exception e)
        {
        	in.close();
        	throw new Exception(
        			this.getClass().getName() + ": "+
        			"Could not load map; invalid height/width.");

        }

        this.init(width, height);
        for (int y = 0; y < height; y++)
        {
            String mapLine = in.readLine();
            for (int x = 0 ; x < width; x++)
            {
                this.set_cell_is_traversable(x, y, mapLine.charAt(x)=='.');
            }
        }
        in.close();
	}
	
	
	public String print_binary_cells()
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.map_size_; i++)
		{
			buf.append(Integer.toBinaryString(map_cells_[i])+ " ");
		}
		return buf.toString();

	}
	
	public String debug_cells(int myx, int myy)
	{
		StringBuffer buf = new StringBuffer();
		for(int y = 0; y < this.map_height_original_; y++)
		{
			for(int x = 0; x < this.map_width_original_; x++)
			{
				if(myx == x && myy == y)
				{
					buf.append("X");
				}
				else
				{
					char c = get_cell_is_traversable(x, y) ? '.' : '@';
					buf.append(c);
				}
			}			
			buf.append("\n");
		}
		return buf.toString();	
	}
	
	public String print_cells()
	{
		StringBuffer buf = new StringBuffer();
		for(int y = 0; y < this.map_height_original_; y++)
		{
			for(int x = 0; x < this.map_width_original_; x++)
			{
				char c = get_cell_is_traversable(x, y) ? '.' : '@';
				buf.append(c);
			}			
			buf.append("\n");
		}
		return buf.toString();
	}
	
	public String print_visibility()
	{
		StringBuffer buf = new StringBuffer();
		for(int y = 0; y < map_height_original_+1; y++)
		{
			for(int x = 0; x < map_width_original_+1; x++)
			{
				char c = get_point_is_visible(x, y) ? '.' : '@';
				buf.append(c);
			}			
			buf.append("\n");
		}
		return buf.toString();
	}
	
	public String print_corners()
	{
		StringBuffer buf = new StringBuffer();
		for(int y = 0; y < map_height_original_+1; y++)
		{
			for(int x = 0; x < map_width_original_+1; x++)
			{
				char c = get_point_is_corner(x, y) ? '.' : '@';
				buf.append(c);
			}			
			buf.append("\n");
		}
		return buf.toString();
	}

	public String print_double_corners()
	{
		StringBuffer buf = new StringBuffer();
		for(int y = 0; y < map_height_original_+1; y++)
		{
			for(int x = 0; x < map_width_original_+1; x++)
			{
				char c = get_point_is_double_corner(x, y) ? '.' : '@';
				buf.append(c);
			}			
			buf.append("\n");
		}
		return buf.toString();
	}
	
	public int get_padded_width()
	{
		return this.map_width_;
	}
	
	public int get_padded_height()
	{
		return this.map_height_;
	}
	
	public int get_num_cells()
	{
		return this.map_height_ * map_width_;
	}
	
	// print a portion of the grid cells around location (x, y)
	// @param offset specifies how many cells around (x, y) to print
	// i.e. an offset of 10 prints 21x21 cells with (x,y) in the middle
	// (10 cells above (x, y), 10 below, 10 left and 10 right)
	public void print_cells(int x, int y, int offset, PrintStream stream)
	{
		for(int j = y-offset; j < y+offset; j++)
		{
			stream.print(j+" ");
			for(int i = x-offset; i < x+offset; i++)
			{
				stream.print(this.get_cell_is_traversable(i, j) ? "." : "@");											
			}
			stream.println("");
		}
	}






    //====================================================================


    // Load up map files in MovingAI format
    public void load(GridGraph gridGraph) throws Exception
    {
        int height = gridGraph.sizeY;
        int width = gridGraph.sizeX;

        this.init(width, height);
        for (int y = 0; y < height; y++)
        {
            for (int x = 0 ; x < width; x++)
            {
                this.set_cell_is_traversable(x, y, !gridGraph.isBlocked(x, y));
            }
        }
    }



}