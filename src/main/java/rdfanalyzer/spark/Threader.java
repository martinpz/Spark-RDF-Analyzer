package rdfanalyzer.spark;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class Threader implements Runnable{
	
	private Thread t;
	private int pathLength = 1;
	private int check = 0;

	private Row[] rows;

	// this represents an object(subject,predicate,object) of the selected node.
	private String ObjectNode;
	
	public Threader(Row[] rows){
		this.rows = rows;
	}
	
	@Override
	public void run() {
		recursiveFunction(this.rows);
		System.out.println("[LOG] The sum of the total distance from all nodes is =" + this.pathLength);
	}
	
	public void recursiveFunction(Row[] rows){
		check++;
		if(check >= 1)
			return;

		System.out.println("[LOG] Called Recusive Function; Objects =" + rows.length);
		
		for(int i=0;i<rows.length;i++){
			
			Row[] rows2 = GetNodeObjects(rows[i].getString(0));
			recursiveFunction(rows2);
		}
		
		if(rows.length == 0){
			pathLength++;
			System.out.println("[LOG] Pathlength added, now it is =" + pathLength);
		}
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
