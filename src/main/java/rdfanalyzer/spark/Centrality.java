package rdfanalyzer.spark;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.google.common.io.Resources;

import ranking.DataFramePartitionLooper;
import ranking.RepeatedRowsCase;
import ranking.SSSP;
import ranking.UniqueNodeCase;
import scala.Tuple2;
import scala.Tuple4;
import ranking.ClosenessBean;
import ranking.ClosenessCentrality;
import ranking.ClosenessNodes;

import static org.apache.spark.sql.functions.*;

public class Centrality implements Serializable{
	private final static Logger logger = Logger.getLogger(Centrality.class);

	public static ConnAdapter objAdapter = new ConnAdapter();
	public static DataFrame graphFrame;
	public static List<ClosenessBean> closenessbean;
	public static List<Tuple2<Long,Long>> vertices = new ArrayList<>();
	public static List<Long> Uniquevertices = new ArrayList<>();

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
			GenerateTopNodesCloseness();
			CalculateCentralityFromDistances();
			
			
		} else if (metricType.equals("5")) {
			System.out.println("[LOGS] Present in metric type 5");
			return "<h1>" + calculateStartNode() + "</h1>";
		}
		return "none";
	}
	
	// convert subjects from DF to an array.
	public static Object[] getSubjectNames(DataFrame subjectRows){
		
		return subjectRows.select("subject").toJavaRDD().map(new Function<Row,String>() {

			@Override
			public String call(Row arg0) throws Exception {
				
				return arg0.getString(0);
			}
		}).collect().stream().toArray();
	}

	public static void CalculateCentralityFromDistances() throws Exception{

		
		DataFrame topClosenessNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 "sib200TopClosenessNodes.parquet");

		
		topClosenessNodes.withColumn("nodeDistances", explode(topClosenessNodes.col("nodeDistances"))).registerTempTable("explodedNodes");
		
		topClosenessNodes.sqlContext().sql("SELECT sourceNodes,SUM(nodeDistances) FROM explodedNodes GROUP BY sourceNodes").write()
		.parquet(rdfanalyzer.spark.Configuration.storage() +
				 "sib200FinalclosenessCentralNodes.parquet");
		
		
	}
	
	public static void GenerateTopNodesCloseness() throws Exception{
		
		// generate id based parquet files for this graph
		generateDataFrame();
		
		// retrive them
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 "UniqueNodes.parquet");

		DataFrame relations = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 "relations.parquet");
				
		uniqueNodes.registerTempTable("UniqueNodes");

		System.out.println("Running the ClosenessNodes");
		// get nodes which will have the most closeness
		DataFrame topCandidatesForCloseness = ClosenessNodes.run(graphFrame);

		System.out.println("getting ids of ClosenessNodes");
		// get ids of those nodes.
		DataFrame topCandidatesForClosenessIDs = uniqueNodes.select("id","nodes")
				.filter(col("nodes").isin(getNodeNames(topCandidatesForCloseness)));
		

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
		
		looper.WriteDataToFile();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// convert subjects from DF to an array.
	public static Object[] getNodeNames(DataFrame subjectRows){
		
		return subjectRows.select("subject").toJavaRDD().map(new Function<Row,String>() {

			@Override
			public String call(Row arg0) throws Exception {
				
				return arg0.getString(0);
			}
		}).collect().stream().toArray();
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

	
	public static DataFrame generateDataFrame(){
		
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
		 
		 return relationsFrame;
	}


	public static void RunAllPairShortestPathWithTestData() throws Exception{
		// This is for testing with small graph
		SSSP apsp = new SSSP();
		apsp.test();
	}	
	

	
	
	
	public static DataFrame DummyUniqueDataForInterimFile(){
		
		JavaRDD<Row> verRow = Service.sparkCtx()
				.parallelize(Arrays.asList(RowFactory.create("node1",1L), RowFactory.create("node2",2L),
						RowFactory.create("node3",3L), RowFactory.create("node4","4L")));

		// Creating column and declaring dataType for vertex:
		List<StructField> verFields = new ArrayList<StructField>();
		verFields.add(DataTypes.createStructField("nodes", DataTypes.StringType, true));
		verFields.add(DataTypes.createStructField("id", DataTypes.LongType, true));
		StructType verSchema = DataTypes.createStructType(verFields);
		return Service.sqlCtx().createDataFrame(verRow, verSchema);


	}
	public static DataFrame DummyRelationDataForInterimFile(){
	
		JavaRDD<Row> relationsRow = Service.sparkCtx()
				.parallelize(Arrays.asList(RowFactory.create(1L,2L),
						RowFactory.create(1L,3L), RowFactory.create(2L,4L),
						RowFactory.create(3L,1L), RowFactory.create(4L,1L)));

		// Edge column Creation with dataType:
		List<StructField> EdgFields = new ArrayList<StructField>();
		EdgFields.add(DataTypes.createStructField("subId", DataTypes.LongType, true));
		EdgFields.add(DataTypes.createStructField("objId", DataTypes.LongType, true));

		// Creating Schema:
		StructType edgSchema = DataTypes.createStructType(EdgFields);

		// Creating vertex DataFrame and edge DataFrame:
		return Service.sqlCtx().createDataFrame(relationsRow, edgSchema);		
	}

	public static ClosenessBean CalculateClosenessByHop(String nodeName) throws Exception {

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT * from Graph");
		resultsFrame.cache();
		resultsFrame.show();
		System.out.println("logo = "+nodeName);
		
		ClosenessCentrality path = new ClosenessCentrality();
		return path.calculateCloseness(resultsFrame, nodeName);
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
