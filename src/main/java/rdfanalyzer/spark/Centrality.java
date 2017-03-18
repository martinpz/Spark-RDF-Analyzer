package rdfanalyzer.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;


import ranking.DataFramePartitionLooper;
import scala.Tuple2;
import ranking.ClosenessBean;
import ranking.ClosenessNodes;

import static org.apache.spark.sql.functions.*;

public class Centrality implements Serializable{

	public static ConnAdapter objAdapter = new ConnAdapter();
	public static DataFrame graphFrame;
	public static List<ClosenessBean> closenessbean;
	public static List<Tuple2<Long,Long>> vertices = new ArrayList<>();
	public static List<Long> Uniquevertices = new ArrayList<>();

	/**
	 * this means that when we find the maximum in degree in the whole graph.
	 * Lets say if the max in degree for a node in the graph is 100. Than
	 * LIMIT_DELTA = 80 means only consider nodes with indegree <= 80. This
	 * parameter can be tuned to suit your requirements.
	 */
	public static final int LIMIT_DELTA = 80;
	
	public static String dataset;

	public static String main(String metricType, String dataset, String nodeName) throws Exception {
		
		Centrality.dataset = dataset;
		graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + dataset +".parquet");
		graphFrame.cache().registerTempTable("Graph");

		for (int i = 0; i < graphFrame.schema().fieldNames().length; i++) {
			System.out.println("[LOG]Schema FieldName : " + graphFrame.schema().fieldNames()[i]);
		}
		nodeName = nodeName.replace("$", "/");
		System.out.println("[LOGS] Node name= " + nodeName);

		if (metricType.equals("1")) {
			return "<h1>" + CalculateInDegree(nodeName) + "</h1>";
		} else if (metricType.equals("2")) {
			return "<h1>" + CalculateOutDegree(nodeName) + "</h1>";
		} else if (metricType.equals("3")) {
		} else if (metricType.equals("4")) {
			GenerateTopNodesCloseness();
		} 
		return "none";
	}
	
	// convert subjects from DF to an array.
	public static Object[] getSubjectNames(DataFrame subjectRows){
		
		return subjectRows.select("subject").toJavaRDD().map(new Function<Row,Long>() {

			@Override
			public Long call(Row arg0) throws Exception {
				
				return arg0.getLong(0);
			}
		}).collect().stream().toArray();
	}
	

	// Determine the top nodes and then calculate their closeness
	public static void GenerateTopNodesCloseness() throws Exception{
		
		// generate id based uniqeNodes parquet files and relation parquet files.
		generateDataFrame();
		
		// retrieve the generated files
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 dataset + "UniqueNodes.parquet");

		DataFrame relations = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 dataset + "relations.parquet");
				
		uniqueNodes.registerTempTable("UniqueNodes");

		System.out.println("Running the ClosenessNodes");
		// get nodes which will have the most closeness
		DataFrame topCandidatesForCloseness = ClosenessNodes.run(graphFrame,dataset);

		System.out.println("getting ids of ClosenessNodes");

		// get ids of those nodes.
		DataFrame topCandidatesForClosenessIDs = uniqueNodes.select("id","nodes")
				.filter(col("nodes").isin(getNodeNames(topCandidatesForCloseness)));
		
		/**
		 *  Now we got the topCandidates for having highest closeness.
		 *  We can pass them to our BFS algo to find centralities for them.
		 */
		System.out.println("creating the AdjacencyMatrix");

		DataFramePartitionLooper looper = new DataFramePartitionLooper(relations);
		
		Row[] items = topCandidatesForClosenessIDs.collect();
		
		boolean firstTime = true;
		
		System.out.println("looping the bfs.");
		for(Row r:items){
			
			looper.run(r.getLong(0), firstTime);
			
			if(firstTime){
				firstTime = false;
			}
		}
		
		
		// will write our final results to a parquet file.
		looper.WriteDataToFile(dataset);
	}
	
	
	/**
	 * 
	 * @param subjectRows
	 * @return Object[]
	 * 
	 * This returns the subject of all the objects defined in the DataFrame @subjectRows
	 */
	
	public static Object[] getNodeNames(DataFrame subjectRows){
		
		return subjectRows.select("subject").toJavaRDD().map(new Function<Row,String>() {

			@Override
			public String call(Row arg0) throws Exception {
				
				return arg0.getString(0);
			}
		}).collect().stream().toArray();
	}
	
	/**
	 *  @return String
	 *  
	 *  returns the number of in-degree of a particular node.
	 */
	
	public static String CalculateInDegree(String node) {

		String result = "";
		// Run SQL over loaded Graph.

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(subject) FROM Graph WHERE object = '" + node + "'");
		resultsFrame.select("").filter(resultsFrame.col("").isin());
		List<Row> rows = resultsFrame.collectAsList();

		result = Long.toString(rows.get(0).getLong(0));

		return result;
	}

	/**
	 *  @return String
	 *  
	 *  returns the number of out-degree of a particular node.
	 */
	
	public static String CalculateOutDegree(String node) {
		String result = "";

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(object) from Graph where subject='" + node + "'");
		List<Row> rows = resultsFrame.collectAsList();

		result = Long.toString(rows.get(0).getLong(0));

		return result;
	}

	/**
	 * 
	 * @return DataFrame
	 * 
	 * This function generates the uniqueNodes.parquet and relations.parquet.
	 * 
	 * UniqueNodes.parquet : has uniqueids assigned to all the nodes in the graph with nodes,ids as columns.
	 * relations.parquet   : has the subject,object relations defined for the graph in terms of ids we generated for UniqueNodes.
	 */
	
	public static DataFrame generateDataFrame(){
		
		DataFrame uniqueFrame = Service.sqlCtx().sql(""
		 + "SELECT DISTINCT"
		 + " a.nodes FROM "
		 + "(SELECT subject as nodes from Graph "
		 + " UNION ALL "
		 + " SELECT object as nodes FROM Graph) a").withColumn("id", functions.monotonically_increasing_id());

		uniqueFrame.write().parquet(rdfanalyzer.spark.Configuration.storage() +
		 dataset+ "UniqueNodes.parquet");
		
		uniqueFrame.registerTempTable("UniqueNodes");

		
		// step 2
		 DataFrame relationsFrame = Service.sqlCtx().sql(""
		 + "SELECT unSub.id as subId,unObj.id as objId FROM Graph g "
		 + "INNER JOIN UniqueNodes unSub ON unSub.nodes=g.subject "
		 + "INNER JOIN UniqueNodes unObj ON unObj.nodes=g.object "
		 + "WHERE g.subject != g.object");
		 relationsFrame.write().parquet(rdfanalyzer.spark.Configuration.storage()
		 + dataset + "relations.parquet");
		 
		 return relationsFrame;
	}
	/**
	 *  
	 * @return long
	 * 
	 * Gets the node with highest In-degree
	 */
	public static long getHighestIndegree() {
		DataFrame maxInDegreeFrame = Service.sqlCtx().sql("SELECT MAX(tbl1.InDegreeCount) FROM "
				+ "(SELECT object,COUNT(subject) AS InDegreeCount FROM Graph GROUP BY object)tbl1");

		List<Row> rowMaxInDegree = maxInDegreeFrame.collectAsList();

		return rowMaxInDegree.get(0).getLong(0);
	}

	/**
	 *  
	 * @return long
	 * 
	 * Gets the node with highest out-degree
	 */
	public static long getHighestOutDegree() {
		DataFrame maxOutDegreeFrame = Service.sqlCtx().sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
				+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

		List<Row> rowMaxOutDegree = maxOutDegreeFrame.collectAsList();
		return rowMaxOutDegree.get(0).getLong(1);
	}
	
	
	/**
	 * We calculate 4 different values because. It is not necessary that the
	 * node which has highest in-degree also has highest outdegree. So if a
	 * node has highest out-degree we also calculate it's in-degree and sum. And the
	 * other way around. Hence we end up with 4 values
	 */
	public static String calculateStartNode() {
		// node with highest out-degree
		DataFrame maxOutDegreeFrame = Service.sqlCtx().sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
				+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

		List<Row> rowMaxOutDegree = maxOutDegreeFrame.collectAsList();

		// in-degree of node with highest out-degree
		String maxInDegreeOfOutDegree = CalculateInDegree(rowMaxOutDegree.get(0).getString(0));

		// node with highest in-degree
		DataFrame maxInDegreeFrame = Service.sqlCtx().sql("SELECT first(tbl1.object),MAX(tbl1.OutdegreeCount) FROM"
				+ "(SELECT object,COUNT(subject) AS OutdegreeCount FROM Graph GROUP BY object)tbl1");

		List<Row> rowMaxInDegree = maxInDegreeFrame.collectAsList();
		// out-degree of node with highest in-degree
		String maxOutDegreeOfInDegree = CalculateOutDegree(rowMaxInDegree.get(0).getString(0));

        /*
         *  Perform sum of the values corresponding to each node and that node
         *  which has larger sum is returned as start node
         */
		long maxOutdegreeTotal = rowMaxOutDegree.get(0).getLong(1) + Integer.parseInt(maxInDegreeOfOutDegree);
		long maxIndegreeTotal = rowMaxInDegree.get(0).getLong(1) + Integer.parseInt(maxOutDegreeOfInDegree);
		if (maxOutdegreeTotal < maxIndegreeTotal) {
			return rowMaxOutDegree.get(0).getString(0);
		}
		return rowMaxInDegree.get(0).getString(0);

	}

}
