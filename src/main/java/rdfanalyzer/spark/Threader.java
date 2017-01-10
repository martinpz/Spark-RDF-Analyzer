package rdfanalyzer.spark;

import java.util.ArrayList;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class Threader implements Runnable{
	
	
	
	class Tracker {

		private int level;
		private Tracker[] childs;
		private String node;
		private String parent;
		
		public Tracker(String node,String parent,Tracker[] childs, int level){
			this.level = level;
			this.node = node;
			this.childs = childs;
			this.parent = parent;
		}
		
		public String getNode() { return this.node; }
		public String getParent() { return this.parent; }
		public Tracker[] getChilds() { return this.childs; }
		public int getLevel() { return this.level; }
	}
	
	private Thread t;
	
	private long id_counter = 0;

	// the sum to calculate closeness centrality
	private int sum = 0;

	// check if we're above RADIUS
	private int check = 0;
	
	// the first node from where we will start expanding
	private Tracker father;
	
	// how far we want to expand from the start node.
	private final int RADIUS = 2;


	// this represents an object(subject,predicate,object) of the selected node.
	private String ObjectNode;
	
	public Threader(String parent){
		

		Row[] childs = Threader.GetNodeObjects(parent);

//		Tracker[] childsTracker = ConvertRowsToTracker(parent,childs);

		// this is the first node from where we will start expanding
//		this.father = new Tracker(parent,"",childsTracker,0);

	}
	
	public void CreateNodeInformation(String parent,String child){
		
	}
	
//	public Tracker[] ConvertRowsToTracker(String node,Row[] childs){
//
//		for(int i=0;i<childs.length;i++){
//			Tracker tracker = new Tracker()
//		}
//	}
	
	@Override
	public void run() {
		
		recursiveFunction(this.father,1);
	}

	// level represents the hops we're from the father node
	public void recursiveFunction(Tracker father, int level){

		// if we're going out of the radius, go back.
		if(level > RADIUS){
			return;
		}
		
		Row[] childs = performBFS(father);

		
//		for(int i=0;i<father.getChilds().length;i++){
//			
//		}
//		Row[] rows2 = GetNodeObjects(father.getChilds()[1].getString(0));
//		Tracker track = new Tracker(father.getChilds()[1].getString(0),rows2, level);
//		recursiveFunction(track,++level);

		

	}
	
	
	// returns all the children nodes of all the child nodes of previous level
	public Row[] performBFS(Tracker nodeToExpand){

		String query="";
		
		query = "SELECT subject,predicate,object from Graph where subject IN (";

		for(int i=0;i<nodeToExpand.getChilds().length;i++){
		
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
//			if(i==(nodeToExpand.getChilds().length-1)){
//				query = query + "'" + nodeToExpand.childs[i].getString(0) + "')";
//			}
//			else{
//				query = query + "'" + nodeToExpand.childs[i].getString(0) + "',";
//			}
		}
		
		DataFrame resultsFrame = Service.sqlCtx().sql(query);
		Row[] rows = resultsFrame.collect();
		return rows;
	}

	public static Row[] GetNodeObjects(String node){

		String result = "";

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT object from Graph where subject='"+node+"'");
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
