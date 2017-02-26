package rdfanalyzer.spark;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import com.google.common.io.Resources;

import ranking.DataFramePartitionLooper;
import ranking.APSP;
import ranking.ClosenessBean;
import ranking.ClosenessCentrality;

public class Centrality {
	private final static Logger logger = Logger.getLogger(Centrality.class);

	public static ConnAdapter objAdapter = new ConnAdapter();
	public static DataFrame graphFrame;
	public static List<ClosenessBean> closenessbean;

	public static final String closenessParquetPath = rdfanalyzer.spark.Configuration.storage() + "closeness.parquet";
	/*
	 * this means that when we find the maximum in degree in the whole graph.
	 * Lets say if the max in degree for a node in the graph is 100. Than
	 * LIMIT_DELTA = 80 means only consider nodes with indegree <= 80. This
	 * parameter can be tuned to suit your requirements.
	 */
	public static final int LIMIT_DELTA = 80;

	public static String main(String metricType, String dataset, String nodeName) throws Exception {
		graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + dataset + ".parquet");
		graphFrame.cache().registerTempTable("Graph");
		/**********************************************************************/
		// DataFrame resultsFrame = Service.sqlCtx().sql("SELECT * FROM Graph");
		// Row[] rows = resultsFrame.collect();
		//
		// for(int i = 0 ; i < rows.length; i++)
		// {
		// System.out.println("The node name is "+rows[i].getString(0));
		// System.out.println("The node importance is "+rows[i].getDouble(1));
		// }
		// return "";
		/**********************************************************************/

		for (int i = 0; i < graphFrame.schema().fieldNames().length; i++) {
			System.out.println("[LOG]Schema FieldName : " + graphFrame.schema().fieldNames()[i]);
		}
		nodeName = nodeName.replace("$", "/");
		System.out.println("[LOGS] Node name= " + nodeName);

		if (metricType.equals("1")) {
			System.out.println("[LOGS] Present in metric type 1");
			return "<h1>" + CalculateInDegree(nodeName) + "</h1>";
		} else if (metricType.equals("2")) {
			System.out.println("[LOGS] Present in metric type 2");
			return "<h1>" + CalculateOutDegree(nodeName) + "</h1>";
		} else if (metricType.equals("3")) {
			System.out.println("[LOGS] Present in metric type 3");
			return CalculateBetweenness(nodeName);
		} else if (metricType.equals("4")) {

			return CalculateCloseness(nodeName);
		} else if (metricType.equals("5")) {
			System.out.println("[LOGS] Present in metric type 5");
			return "<h1>" + calculateStartNode() + "</h1>";
		}
		return "none";
	}

	public static String CalculateInDegree(String node) {

		String result = "";
		// Run SQL over loaded Graph.

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(subject) FROM Graph WHERE object = '" + node + "'");
		resultsFrame.select("").filter(resultsFrame.col("").isin());
		List<Row> rows = resultsFrame.collectAsList();

		result = Long.toString(rows.get(0).getLong(0));

		return result;
	}

	public static String CalculateOutDegree(String node) {
		String result = "";

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(object) from Graph where subject='" + node + "'");
		List<Row> rows = resultsFrame.collectAsList();

		result = Long.toString(rows.get(0).getLong(0));

		return result;
	}

	public static String CalculateBetweenness(String node) {
		return "betweenness";
	}

	public static String CalculateCloseness(String nodeName)  throws Exception{

		
//		relations.registerTempTable("relations");
//		Service.sqlCtx().sql("select * from relations where subId=1460288880641").show();;

		/*
		 *  We'll loop the unique nodes to find APSP w.r.t each node
		 *  
		 *  but we'll create the adjacency matrix using the relations data.
		 */

		
//		ReadAllPairShortestPathSingleNode();
//
//		ReadAllPairShortestPathAllNode();
//		generateDataFrame();
		RunAllPairShortestPathForAllNodes();
//		RunAllPairShortestPathWithTestData();
		
//		uniqueNodes.foreachPartition(partitionerLoop);
		
		// step 1
		 
		
		
//		relationsFrame.foreachPartition(new DataFramePartitionLooper());		

		
		
//		RDFAnalyzerPageRank rank  = new RDFAnalyzerPageRank();
//		rank.PerformPageRank(resultFrame);
		return "";
	}
	
	public static void generateDataFrame(){
		
		DataFrame uniqueFrame = Service.sqlCtx().sql(""
		 + "SELECT DISTINCT"
		 + " a.nodes FROM "
		 + "(SELECT subject as nodes from Graph "
		 + " UNION ALL "
		 + " SELECT object as nodes FROM Graph) a").withColumn("id", functions.monotonically_increasing_id());

		uniqueFrame.write().parquet(rdfanalyzer.spark.Configuration.storage() +
		 "UniqueNodes.parquet");
		
		uniqueFrame.registerTempTable("UniqueNodes");

		
		// step 2
		 DataFrame relationsFrame = Service.sqlCtx().sql(""
		 + "SELECT unSub.id as subId,unObj.id as objId FROM Graph g "
		 + "INNER JOIN UniqueNodes unSub ON unSub.nodes=g.subject "
		 + "INNER JOIN UniqueNodes unObj ON unObj.nodes=g.object "
		 + "WHERE g.subject != g.object");
		 relationsFrame.write().parquet(rdfanalyzer.spark.Configuration.storage()
		 + "relations.parquet");
	}

	public static void ReadAllPairShortestPathSingleNode() throws Exception{
		DataFrame singleNodeAPSP = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "sib200APSP.parquet");
		singleNodeAPSP.show();
		System.out.println("The count of single node is "+singleNodeAPSP.count());
	}	
	public static void ReadAllPairShortestPathAllNode() throws Exception{
		DataFrame allNodeAPSP = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "sib200APSPAll.parquet");
		allNodeAPSP.show();
		
		System.out.println("The count of all nodes is "+allNodeAPSP.count());
	}	

	public static void RunAllPairShortestPathWithTestData() throws Exception{
		// This is for testing with small graph
		APSP apsp = new APSP();
		apsp.test();
	}	
	
	
	public static void RunAllPairShortestPathForAllNodes(){
		// [ nodes, ids ]
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "UniqueNodes.parquet");

		// [ subids, objids ]
		DataFrame relations = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "relations.parquet");
		
		Row[] uniqueNodesRows = uniqueNodes.collect();
		System.out.println("number of unique nodes " + uniqueNodesRows.length);
		
//		uniqueNodes.show();
		
		DataFramePartitionLooper partitionerLoop  = new DataFramePartitionLooper(relations,uniqueNodesRows);
		partitionerLoop.run();
		partitionerLoop.WriteDataToFile();

//		for(int i=0;i<uniqueNodesRows.length;i++){
//			long nodeid =  uniqueNodesRows[i].getLong(1);
//			partitionerLoop.ApplyBFS(nodeid);
//			System.out.println("roribaba"+nodeid);
//			break;
//		}
		
	}

	public static String CalculateClosenessByHop(String nodeName) throws Exception {

		closenessbean = new ArrayList<ClosenessBean>();
		List<String> node = new ArrayList<String>();
		node.add(nodeName);

		DataFrame resultFrame = Service.sqlCtx()
				.sql("SELECT subject,object FROM Graph WHERE subject='" + nodeName + "' LIMIT 5");

		List<String> neighbours = getListFromDatasetRows(resultFrame);
		for (String string : neighbours) {
			node.add(string);
		}

		System.out.println("List Size: " + node.size());

		System.out.println("[LOGS] Present in metric type 4");

		
		List<String> tobeQueried = new ArrayList<>();
		tobeQueried.add(nodeName);

		// All The records of parquet.
		DataFrame existingParquetData = null;

		// The records which already exists in parquet sent through arg of this
		// function.
		DataFrame existingcurrentNodesData = null;

		File f = new File(closenessParquetPath);

		System.out.println("query 1");

		if (f.exists()) {
			System.out.println("file exists reading...");

			// Read existing closeness data from Parquet
			existingParquetData = Service.sqlCtx().read().parquet(closenessParquetPath); // 1
																							// record
			existingcurrentNodesData = Service.sqlCtx().read().parquet(closenessParquetPath);

			System.out.println("query 222");

			// Getting All the nodes from Parquet Table which are present in the
			// queried list passed as an arg to this function.
//			existingcurrentNodesData
//					.select(existingcurrentNodesData.col("closeness"), existingcurrentNodesData.col("node"))
//					.where(existingcurrentNodesData.col("node").isin(node.stream().toArray(String[]::new))).show(); // 1
//																													// record

			// Convert the parquet dataset nodesName col into List<String>
			List<String> existingNodes = getListFromDatasetRows(existingcurrentNodesData);
			System.out.println("query 2");

			System.out.println("Passed nodes " + node.size());
			/*
			 * Subtract the existingNodes from the ones which are queried, since
			 * the existing ones are already available through parquet.
			 */
			tobeQueried = getUniqueValues(node, existingNodes);
		}

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT * from Graph");
		System.out.println("query 3");

		resultsFrame.cache();
		System.out.println("query 4");

		ClosenessCentrality path = new ClosenessCentrality();
		System.out.println("query 5");

		for (String anode : tobeQueried) {
			// closenessbean.add(path.calculateCloseness(resultsFrame,anode));
		}
		System.out.println("query 6");

		DataFrame newDataset = Service.sqlCtx().createDataFrame(closenessbean, ClosenessBean.class);

		System.out.println("Parquet data");
		if (existingcurrentNodesData != null) {
			existingcurrentNodesData.show();
		}

		System.out.println("Before");
		newDataset.show();

		if (f.exists()) {
			// newDataset = existingParquetData.union(newDataset);
			System.out.println("query 7");
		}

		System.out.println("After");
		newDataset.show();

		System.out.println("To Be Queried size = " + tobeQueried.size());

		// deleteDirectory(f);

		System.out.println("saving file");
		newDataset.write().parquet(closenessParquetPath);

		// DataFrame vertFrame = Service.sqlCtx().sql("select *,row_number()
		// OVER(ORDER BY(SELECT 0)) as id from Graph");

		// DataFrame vertFrame = Service.sqlCtx().sql(""
		// + "SELECT DISTINCT row_number() OVER(ORDER BY(SELECT 0)) as
		// id,a.nodes FROM "
		// + "(SELECT subject as nodes from Graph"
		// + " UNION ALL"
		// + " SELECT object as nodes FROM Graph) a");

		// String nodee = node;
		// System.out.println("query 0 success");
		// DataFrame hop1Nodes = Service.sqlCtx().sql("SELECT subject,object
		// FROM Graph WHERE subject='"+nodee+"' AND object!='"+nodee+"'");
		// System.out.println("query 1 success");
		//
		// List<String> hop1Objects = getListFromDatasetRows(hop1Nodes);
		//
		//
		// DataFrame hop2Nodes =
		// graphFrame.select(graphFrame.col("subject"),graphFrame.col("object"))
		// .where(graphFrame.col("subject").isin(hop1Objects.stream().toArray(String[]::new))
		// .and(graphFrame.col("object").notEqual(hop1Nodes.col("object"))
		// .and(graphFrame.col("object").notEqual(nodee))));
		// System.out.println("query 3 success");
		//
		// List<String> hop2Objects = getListFromDatasetRows(hop2Nodes);
		// System.out.println("query 4 success");
		//
		// DataFrame hop3Nodes =
		// graphFrame.select(graphFrame.col("subject"),graphFrame.col("object"))
		// .where(graphFrame.col("subject").isin(hop2Objects.stream().toArray(String[]::new))
		// .and(graphFrame.col("object").notEqual(hop2Nodes.col("object")))
		// .and(graphFrame.col("object").notEqual(hop1Nodes.col("object")))
		// .and(graphFrame.col("object").notEqual(nodee)));
		//
		// System.out.println("query 5 success");
		//
		// System.out.println("hop1 = "+ hop1Nodes.count());
		// System.out.println("hop2 = "+ hop2Nodes.count());
		// System.out.println("hop3 = "+ hop3Nodes.count());
		//
		// hop1Nodes = hop1Nodes.union(hop2Nodes).union(hop3Nodes);
		// hop1Nodes.show();

		// vertFrame.write().parquet(rdfanalyzer.spark.Configuration.storage() +
		// "UniqueNodes.parquet");
		// vertFrame.createOrReplaceTempView("UniqueNodes");

		// DataFrame vertFrame =
		// Service.spark().read().parquet(rdfanalyzer.spark.Configuration.storage()
		// + "UniqueNodes.parquet");
		// vertFrame.show();
		// vertFrame.createOrReplaceTempView("UniqueNodes");
		//
		// DataFrame relationsFrame = Service.sqlCtx().sql(""
		// + "SELECT unSub.id as subId,unObj.id as objId,predicate FROM Graph g
		// "
		// + "INNER JOIN UniqueNodes unSub ON unSub.nodes=g.subject "
		// + "INNER JOIN UniqueNodes unObj ON unObj.nodes=g.object");
		// relationsFrame.write().parquet(rdfanalyzer.spark.Configuration.storage()
		// + "relations.parquet");
		// relationsFrame.show();

		// DataFrame relationsFrame = Service.sqlCtx().sql("SELECT
		// subject,predicate,object from Graph");

		// // this give us the value of max indegree of a particular node.
		// long highestIndegree = getHighestIndegree();
		//
		//
		// long inDegreeignoreLimit = (highestIndegree * LIMIT_DELTA)/ 100;
		//
		// String query = "SELECT g.subject,g.object FROM Graph g INNER JOIN "
		// + "(SELECT object FROM Graph GROUP BY object HAVING "
		// + "COUNT(subject)<"+inDegreeignoreLimit+") ss ON ss.object =
		// g.object";
		//
		// DataFrame allSubjects = Service.sqlCtx().sql(query);
		//
		// RDFAnalyzerPageRank analyzer = new RDFAnalyzerPageRank();
		// analyzer.PerformPageRank(allSubjects);
		return "";
	}

	private static void deleteDirectory(File f) {
		if (f.exists()) {
			System.out.println("File exists and is a directory");
			try {
				FileUtils.deleteDirectory(f);
				System.out.println("Directory deleted");
			} catch (IOException | SecurityException e) {
				System.out.println("Error is: " + e.getMessage());
			}
		}
	}

	private static List<String> getUniqueValues(List<String> bList, List<String> aList) {

		List<String> union = new ArrayList<String>(aList);
		union.addAll(bList);

		System.out.println("Union list = " + union.size());

		List<String> intersection = new ArrayList<String>(aList);
		intersection.retainAll(bList);
		intersection.addAll(aList);
		System.out.println("Intersec list = " + intersection.size());

		List<String> symmetricDifference = new ArrayList<String>(union);
		symmetricDifference.removeAll(intersection);
		System.out.println("SymmetricDiff list = " + symmetricDifference.size());

		return symmetricDifference;
	}

	public static List<String> getListFromDatasetRows(DataFrame rows) {
		return rows.toJavaRDD().map(new Function<Row, String>() {

			@Override
			public String call(Row row) throws Exception {
				// TODO Auto-generated method stub
				return row.getString(1);
			}
		}).collect();

	}

	public static String readResource(final String fileName, Charset charset) throws IOException {
		return Resources.toString(Resources.getResource(fileName), charset);
	}

	public static long getHighestIndegree() {
		DataFrame maxInDegreeFrame = Service.sqlCtx().sql("SELECT MAX(tbl1.InDegreeCount) FROM "
				+ "(SELECT object,COUNT(subject) AS InDegreeCount FROM Graph GROUP BY object)tbl1");

		List<Row> rowMaxInDegree = maxInDegreeFrame.collectAsList();

		return rowMaxInDegree.get(0).getLong(0);
	}

	public static long getHighestOutDegree() {
		DataFrame maxOutDegreeFrame = Service.sqlCtx().sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
				+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

		List<Row> rowMaxOutDegree = maxOutDegreeFrame.collectAsList();
		return rowMaxOutDegree.get(0).getLong(1);
	}

	/*
	 * BullShit
	 */
	public static String calculateStartNode() {
		/*
		 * We calculate 4 different values because. It is not necessary that the
		 * node which has highest in-degree also has highest outdegree. So if a
		 * node has highest out-degree we also calculate it's in-degree. And the
		 * other way around. Hence we end up with 4 values
		 */
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
		String maxOutDegreeOfInDegree = CalculateInDegree(rowMaxInDegree.get(0).getString(0));

		System.out.println("[LOG]Working until here yuppie");
		long maxOutdegreeTotal = rowMaxOutDegree.get(0).getLong(1) + Integer.parseInt(maxInDegreeOfOutDegree);
		long maxIndegreeTotal = rowMaxInDegree.get(0).getLong(1) + Integer.parseInt(maxOutDegreeOfInDegree);
		if (maxOutdegreeTotal < maxIndegreeTotal) {
			return rowMaxOutDegree.get(0).getString(0);
		}
		return rowMaxInDegree.get(0).getString(0);

	}

}
