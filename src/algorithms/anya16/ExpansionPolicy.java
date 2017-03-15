package pgraph.base;

import org.jgrapht.traverse.Heuristic;

// ExpansionPolicy.java
// 
// Expands a given vertex v (from a graph G) in order to generate
// its successors. The set of successors are iterated over 
// using the functions ::next and ::hasNext.
//  
// @author: dharabor
// @created: 2015-04-13
//


public interface ExpansionPolicy<V>
{
	// return true if the start and target are valid location
	// and false otherwise. this method is intended to be invoked
	// once; at the commencement of search.
	public boolean validate_instance(V start, V target);
	
	// generate all the immediate neighbours of a node 
	public void expand(V vertex);
	
	// return the next neighbour of the node currently being
	// expanded; and null if there are no neighbours or if all
	// neighbours have been exhausted
	public V next();
	
	// return true until all neighbours have been iterated over
	public boolean hasNext();
	
	// return the distance (g-value) from the node being expanded
	// to the current neighbour
	public double step_cost();
	
	// A heuristic for evaluating cost-to-go
	Heuristic<V> heuristic();
	
	// Sometimes it is desirable for the expander to compute a hash
	// value for each node it generates (cf. the node computing its own
	// hash value).
	int hashCode(V v);
}

