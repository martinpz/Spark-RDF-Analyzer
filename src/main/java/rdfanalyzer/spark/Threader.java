package rdfanalyzer.spark;

import java.io.File;
import java.util.ArrayList;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import models.Node;
import models.Predicate;

public class Threader implements Runnable{
	
	
	
	class Tracker {

		private int level;
		private Row[] childs;
		private String node;
		private String parent;
		
		public Tracker(String node,String parent,Row[] childs, int level){
			this.level = level;
			this.node = node;
			this.childs = childs;
			this.parent = parent;
		}
		
		public String getNode() { return this.node; }
		public String getParent() { return this.parent; }
		public Row[] getChilds() { return this.childs; }
		public int getLevel() { return this.level; }
	}
	
	private Thread t;
	
	private long global_counter = 1;

	// the sum to calculate closeness centrality
	private int sum = 0;

	// check if we're above RADIUS
	private int level = 1;
	
	// the first node from where we will start expanding
	private Tracker father;
	
	// how far we want to expand from the start node.
	private final int RADIUS = 2;

	
	ArrayList<Tracker> trackerlist = new ArrayList<Tracker>();
	ArrayList<Node> nodeslist = new ArrayList<Node>();
	ArrayList<Predicate> predicateslist = new ArrayList<Predicate>();
	
	private final String jsonPath = "/home/data/analyzer.json";
	
	
	// step 1
	public boolean FileExists(){
		File jsonFile = new File(jsonPath);
		
		if(jsonFile.exists()){
			return true;
		}
		
		return false;
	}
	
	
	public Threader(String parent){
		
		if(FileExists()){
			
		}
		else{
			// get child of the topmost parent node
			Row[] parentonly = new Row[1];
			parentonly[0] =  RowFactory.create(parent);
			
			Row[] childs = performBFS(parentonly);

			Node parentNode = new Node(global_counter,parent);
//			createFirstChildNodes(parentNode,childs);
			performRecursion(parentNode,childs);
		}
	}
	
	public void performRecursion(Node parent,Row[] childs){
		
		ReorganizeChildsLevelNodes(parent,childs);
		Row[] morechilds = performBFS(childs);
	}
	public void ReorganizeChildsLevelNodes(Node parent,Row[] morechilds){

		/*
		 * We get results in the following manner
		 * 0.Subject1 , Pred1, Object1
		 * 1.Subject1 , Pred1, Object2
		 * 2.Subject1 , Pred1, Object3
		 * 3.Subject2 , Pred1, Object4
		 * 4.Subject2 , Pred1, Object5
		 * 5.Subject2 , Pred1, Object6
		 * 
		 * So in this function we consider the subject as parent and Object1,2,3 as childs of that.
		 * @startindex is used for starting from the next subject once we gathered one subject objects.
		 * In this case, on the second iteration of while loop @startindex will be 3.
		 */
		
		int lastindex = 0;
		int startindex = 0;
		String subject = "";
		ArrayList<Row> rows = new ArrayList<Row>();
		boolean nodecheckedIfAlreadyExists = false;
		boolean nodeAlreadyexists = false;

		while(startindex != (morechilds.length-1))
		{
		
			nodecheckedIfAlreadyExists = false;
			nodeAlreadyexists = false;

			for(int i=startindex;i<morechilds.length;i++){
	
				/*
				 *  @subject!="" because we want to set value of @subject so we let go this
				 *  condition in first iteration
				 *  
				 *  @subject != morechilds[i].getString(0)
				 *  because we want to gather objects for only one subject. So if subject changes
				 *  break out of the loop.
				 */
				
				/*
				 *  this check is for suppose if we found that this subject already has a node object
				 *  than we can keep continuing until the break condition defined below is met
				 *  
				 *  We do this because we don't want to mess up the @lastindex param here which will
				 *  help us start from the next subject.
				 */
				if(nodeAlreadyexists){
					continue;
				}
				
				if(subject != morechilds[i].getString(0) && subject!=""){
					break;
				}
				
				
				subject = morechilds[i].getString(0);

				// no need to run the function for the same name of subject again and again.
				if(!nodecheckedIfAlreadyExists){
				
					nodecheckedIfAlreadyExists = true;
					// if node with this name already exists in the list, break out.
					if(CheckIfNodeAlreadyExists(subject)){
						nodeAlreadyexists = true;
						continue;
					}
				}

				if(!nodeAlreadyexists){
					
				}
				rows.add(morechilds[i]);
				
				lastindex = i;
			}
			
			// we do this so next time we start from the rows of second subject
			startindex = lastindex+1;

			if(!nodeAlreadyexists){

				// convert arraylist to array to we can pass it to the createFirstChildNodes function
				Row[] arrayrowsChilds = rows.toArray(new Row[rows.size()]);

				global_counter+=1;
				

				createFirstChildNodes(parent, arrayrowsChilds);
			}

		}// while
	}
	
	
	public void createFirstChildNodes(Node parent,Row[] childs){

		nodeslist.add(parent);

		for(int i=0;i<childs.length;i++){
			
			if(CheckIfNodeAlreadyExists(childs[i].getString(2))){
				// move onto the next node, since we already have an object for this.
				continue;
			}

			global_counter += 1;

			/*
			 *  create node objects for all the childs of parent
			 *  this will help us assign them id's.
			 */
			
			Node n = new Node(global_counter,childs[i].getString(2));

			Predicate p = CheckIfPredicateAlreadyExists(childs[i].getString(1));
			
			// if we don't have this kind of predicate from before. create a new one.
			if( p == null){
				global_counter += 1;
				p = new Predicate(global_counter,parent.getID(),childs[i].getString(1),n.getID());
			}
			

			parent.AddOutPredicate(p.getPredicateID());
			parent.AddOuterPath(parent.getID(), n.getID());

			predicateslist.add(p);
			nodeslist.add(n);
		}
	}
	
	
	/*
	 * To save yourself from graph cycles.
	 */
	public boolean CheckIfNodeAlreadyExists(String node){
		for(int i=0;i<nodeslist.size();i++){
			if(nodeslist.get(i).getName() == node){
				// node already exists.
				return true;
			}
		}
		return false;
	}
	/*
	 * To save yourself from multiple predicate entries.
	 */
	public Predicate CheckIfPredicateAlreadyExists(String pred){
		for(int i=0;i<predicateslist.size();i++){
			if(predicateslist.get(i).getPredName() == pred){
				// predicate already exists.
				return predicateslist.get(i);
			}
		}
		return null;
	}
	
	/*
	 * 
	 *  startnode is the most important node we found using
	 *  'calculateStartNode' function defined in Centrality.java
	 */

//	public void RunTrackerAlgorithm(Row[] childs,int level){
//		
//		level++;
//
//		// performBFS gives us all the childs of the next depth of the tree.
//		Row[] depth = performBFS(childs);
//
//		
//		for(int i=0;i<depth.length;i++){
//			
//			// check if a tracker for this node already exist. This is to prevent cycles.
//			if(!checkIfNodeTrackerAlreadyExists(depth[i].getString(0))){
//				
//				/*
//				 * So suppose we have 2 object nodes for a 2 different subjects
//				 * If we loop to find that that the last value for subject 1 is
//				 * at index 2 than we keep the last index so that we start next
//				 * time from 2 onwards to get child nodes of second subject.
//				 */
//				int lastindex = 0;
//				
//				ArrayList<String> childsOfNode = new ArrayList<String>();
//				
//				for(int j = lastindex ; j < depth.length; j++){
//					childsOfNode.add(depth[j].getString(2));
//				}
//			}
//		}
//		
//
////		Tracker[] childsTracker = ConvertRowsToTracker(parent,childs);
//	}
	
//	public boolean checkIfNodeTrackerAlreadyExists(String node){
//		
//		for(int i=0;i<trackerlist.size();i++){
//			if(trackerlist.get(i).node == node){
//				return true;
//			}
//		}
//		return false;
//	}
	
//	public void ConvertRowsToTracker(String node,String parent,Row[] childs, int level){
//		
//		Tracker t = new Tracker(parent,"",childs,level);
//
//		trackerlist.add(t);
//	}
	
	
//	public Tracker[] ConvertRowsToTracker(String node,Row[] childs){
//
//		for(int i=0;i<childs.length;i++){
//			Tracker tracker = new Tracker()
//		}
//	}
	
	@Override
	public void run() {
		
//		recursiveFunction(this.father,1);
	}

	// level represents the hops we're from the father node
//	public void recursiveFunction(Tracker father, int level){
//
//		// if we're going out of the radius, go back.
//		if(level > RADIUS){
//			return;
//		}
//		
//		Row[] childs = performBFS(father);
//
//		
////		for(int i=0;i<father.getChilds().length;i++){
////			
////		}
////		Row[] rows2 = GetNodeObjects(father.getChilds()[1].getString(0));
////		Tracker track = new Tracker(father.getChilds()[1].getString(0),rows2, level);
////		recursiveFunction(track,++level);
//
//		
//
//	}
	
	
	// returns all the children nodes of all the child nodes of previous level
	public Row[] performBFS(Row[] childs){

		String query="";
		
		query = "SELECT subject,predicate,object from Graph where subject IN (";

		for(int i=0;i<childs.length;i++){
		
			// save information of child to text file here

			/*
			 * Here we can save 
			 * 
			 * outdegree,
			 * indegree, 
			 * out-neighbors,
			 * in-neighbors, 
			 * closeness 
			 * 
			 * w.r.t selected node
			 * 
			 */
			
			
			/* 
			 *  - This gives us the name of the object for the subject. 
			 *  - This was retrieved using the GetNodeObjects function 
			 *    defined below.
			 */
			if(i==(childs.length-1)){
				query = query + "'" + childs[i].getString(0) + "')";
			}
			else{
				query = query + "'" + childs[i].getString(0) + "',";
			}
		}
		
		DataFrame resultsFrame = Service.sqlCtx().sql(query);
		Row[] rows = resultsFrame.collect();
		return rows;
	}

	
	public void start () {
		
		
		if (t == null) {

			t = new Thread (this);
			t.start ();
		}
	}
}
