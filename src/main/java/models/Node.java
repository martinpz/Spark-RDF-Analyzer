package models;

import java.util.ArrayList;

public class Node{

	private long id;
	private String name;
	private double closeness = 0;
	private int in_degree = 0;
	private int out_degree = 0;
	

	//id's of predicates coming into this node
	private ArrayList<Long> in_predicates = new ArrayList<Long>();

	//id's of predicates going out
	private ArrayList<Long> out_predicates = new ArrayList<Long>();

	
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
	
	/*
	 * The length of the parent ArrayList here represents the number of outgoing nodes
	 * from this node.
	 */
	private ArrayList<ArrayList<Long>> paths = new ArrayList<ArrayList<Long>>();
	
	
	
	public Node(long id, String name){
		this.id = id;
		this.name = name;
	}
	
	public void setID(long id) 							{ this.id = id; }
	public void setName(String name) 					{ this.name = name; }
	public void setCloseness(double closeness) 			{ this.closeness = closeness; }
	public void setInDegree(int indegree) 				{ this.in_degree= indegree; }
	public void setOutDegree(int outdegree) 			{ this.out_degree = outdegree; }
	public void AddInPredicate(long predicateid) 		{ this.in_predicates.add(predicateid); }
	public void AddOutPredicate(long predicateid) 		{ this.out_predicates.add(predicateid); }


	public void setFarthestNodes(long[] nodes) 			{ this.farthest_nodes = nodes; }
	public void setFarthestDistances(long[] distances) 	{ this.farthest_distances = distances; }

	
	public void AddOuterPath(Long path_from_id,Long path_to_id) 		
	{ 
		boolean added = false;
		
		// we already have atleast entries from the base node to one hop towards its child
		if(paths.size()>0){
			
			for(ArrayList<Long> path:paths){
				
				for(Long ids:path){

					if(ids == path_from_id){
						path.add(path_to_id);

						added = true;
						break;
					}
				}
				
				if(added){
					break;
				}
			}
		}

		/*
		 *  this case will happen when u have some paths create but more
		 *  to be created on the first hop towards base(this) nodes child
		 */
		
		if(!added){
			ArrayList<Long> newpath = new ArrayList<Long>();
			newpath.add(path_to_id);
			paths.add(newpath);
		}

	}
	
	public int getPathLength() { return this.paths.size(); }
	
	
	public long getID() { return this.id; }
	public String getName() { return this.name; }
}
