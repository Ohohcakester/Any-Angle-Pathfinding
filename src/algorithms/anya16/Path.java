package algorithms.anya16;

// Path.java
// 
// Describes a path in a graph in terms of its constituent vertices
// and their associated cumulative cost. i.e. the cost to step from 
// the start vertex to the current vertex, i.
//
// @author: dharabor
// @created: 2015-05-20
// 

public class Path<V> 
{
	private double path_cost;
	private V vertex;
	private Path<V> next;
	private Path<V> prev;
		
	Path(V vertex, Path<V> next, double path_cost)
	{
		this.vertex = vertex;
		this.path_cost = path_cost;
		this.next = next;
		if (next != null) next.prev = this;
	}
	
	public double getPathCost() { return path_cost; }
	public Path<V> getNext() { return next; }
	public Path<V> getPrev() { return prev; }
	public V getVertex() { return vertex; }
}
