package rdfanalyzer.spark;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class Centrality {

	public static ConnAdapter objAdapter = new ConnAdapter();

	public static String main(String metricType,String dataset, String nodeName){

		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + dataset + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		nodeName = nodeName.replace("$", "/");
		
		System.out.println("[LOGS] Node name= "+nodeName);

		if(metricType.equals("1")){
			System.out.println("[LOGS] Present in metric type 1");
			return CalculateInDegree(nodeName);
		}
		else if(metricType.equals("2")){
			System.out.println("[LOGS] Present in metric type 2");
			return CalculateOutDegree(nodeName);
		}
		else if(metricType.equals("3")){
			System.out.println("[LOGS] Present in metric type 3");
			return CalculateBetweenness(nodeName);
		}
		else if(metricType.equals("4")){
			System.out.println("[LOGS] Present in metric type 4");
			return CalculateCloseness(nodeName);
		}
		
		return "none";
	}
	
	public static String CalculateInDegree(String node){

		String result = "";
		// Run SQL over loaded Graph.
		

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(object) FROM Graph WHERE object = '"+node+"'");
		Row[] rows = resultsFrame.collect();

		result = Long.toString(rows[0].getLong(0));

		return "<h1>"+result+"</h1>";
	}
	public static String CalculateOutDegree(String node){
		
		String result = "";

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(*) from Graph where subject='"+node+"'");
		Row[] rows = resultsFrame.collect();

		result = Long.toString(rows[0].getLong(0));
		
		return "<h1>"+result+"</h1>";
	}
	
	public static String CalculateBetweenness(String node){
		
		return "betweenness";
	}
	public static String CalculateCloseness(String node){
		
		/*
		 * 
		 * 	Calculate steps this node needs to take to reach other nodes. 
		 *  Suppose this node take 2,3,4 steps to reach 3 other nodes in the graph.
		 *  Than we sum these values and take an inverse of it to find the closeness of this node.
		 *  
		 *  In this case 2+3+4 = 9 and than inverse of 9 is 1/9 = 0.1111.
		 *
		 * 	Rules:
		 * 	
		 * 	Be careful about the predicate directions i.e
		 *  Node1 --> Node2 --> Node3 <--- Node4 In this case Node3 is unreachable from Node1
		 *  Hence the distance from Node1 to Node4 is 0 while Node1 to Node3 is 2.
		 * 
		 */
		
		
		
		return "closeness";
	}

}
