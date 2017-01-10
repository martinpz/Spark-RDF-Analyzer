package models;

import java.util.ArrayList;

public class Nodes{

	private long id;
	private String name;
	private double closeness;
	private int in_degree;
	private int out_degree;
	
	//id's of other nodes
	private int[] in_neighbors;

	//id's of other nodes
	private int[] out_neighbors;

	//id's of predicates coming into this node
	private long[] in_predicates;

	//id's of predicates going out
	private long[] out_predicates;

	
	/*
	 *  the number of nodes currently at maximum distance from this node.
	 *  this will get updated each time we move further out of the graph
	 *  into a new node.
	 *
	 *  This will have the id's of the nods.
	 */
	private long[] farthest_nodes;
	
	/*
	 * these are distances for the above field. The indexes for both the
	 * fields are same i.e if a farthest node is at index 5 in above field.
	 * Than it's distance will be at index 5 in this field.
	 */
	private long[] farthest_distances;
	
	private ArrayList<ArrayList<Integer>> paths;
	
	public void setID(long id) 							{ this.id = id; }
	public void setName(String name) 					{ this.name = name; }
	public void setCloseness(double closeness) 			{ this.closeness = closeness; }
	public void setInDegree(int indegree) 				{ this.in_degree= indegree; }
	public void setOutDegree(int outdegree) 			{ this.out_degree = outdegree; }
	public void setInPredicate(long[] predicateid) 		{ this.in_predicates = predicateid; }
	public void setOutPredicate(long[] predicateid) 	{ this.out_predicates = predicateid; }

	public void setFarthestNodes(long[] nodes) 			{ this.farthest_nodes = nodes; }
	public void setFarthestDistances(long[] distances) 	{ this.farthest_distances = distances; }

	public void setPath(ArrayList<Integer> a_path) 			{ this.paths.add(a_path); }
}
