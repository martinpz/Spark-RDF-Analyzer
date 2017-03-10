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
		
			
//			RunAllPairShortestPathForAllNodes(nodeName);
//			graphFrame.select("subject","object").toJavaRDD().mapToPair(new PairFunction<Row, String, String>() {
//
//				@Override
//				public Tuple2<String, String> call(Row arg0) throws Exception {
//					System.out.println("subject = "+arg0.getString(0) + " object = "+arg0.getString(1));
//					return new Tuple2<String,String>(arg0.getString(0),arg0.getString(1));
//				}
//			});

//			DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
//					 + "Random10Nodes1.parquet");
//			uniqueNodes.show();

			
//			return10RandomNodes();
			getFarthestNodes();
//			findNodeMaxCloseness();
//			CreateUniqueNodesWithoutQuotes();
		} else if (metricType.equals("5")) {
			System.out.println("[LOGS] Present in metric type 5");
			return "<h1>" + calculateStartNode() + "</h1>";
		}
		return "none";
	}
	
	
	
	
	
	
	
	
	
	public static void findNodeMaxCloseness(){
		
		DataFrame df = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage() +
				 "closenessList.parquet");
		df.registerTempTable("closeness");
		
		DataFrame df2 = df.sqlContext().sql("SELECT object,COUNT(subject) as intersection FROM closeness "
				+ "GROUP BY object ORDER BY intersection DESC LIMIT 5");

		Row[] finalNodes = df2.collect();
		
		// send these nodes to bfs algo.
		
	}
	
	
	
	
	
	
	
	
	
	
	public static void getFarthestNodes(){
		
		/*
		 *  Top 5 nodes with maximum outdegrees. Add a column in the front with 1 that represents them as grey nodes.
		 *  i.e the ones to be expanded next.
		 */
		DataFrame subjectOfObjectDF1 = Service.sqlCtx().sql("SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject"
				+ " ORDER BY OutdegreeCount DESC LIMIT 1");

//		DataFrame completeDummyGraph = getDummyGraphFrame();
//		completeDummyGraph.registerTempTable("DummyGraph");
//
//		DataFrame subjectOfObjectDF1 = Service.sqlCtx().sql("SELECT subject,COUNT(object) AS OutdegreeCount FROM DummyGraph GROUP BY subject"
//		+ " ORDER BY OutdegreeCount DESC LIMIT 3");
		
		subjectOfObjectDF1.registerTempTable("sourceNodes");
		subjectOfObjectDF1.show();
		
		
		int distancepath = 1;
		
		DataFrame subjectOfObjectOfDF1 = initEmptyDF();

//		for(Row r: listofsource){
			
//			source = Service.sqlCtx().sql("SELECT subject,OutdegreeCount FROM sourceNodes WHERE subject='"+r.getString(0)+"'");
			
			subjectOfObjectOfDF1 = initEmptyDF();
			
			// get names of subjects in terms of list.
			Object[] subjects = getSubjectNames(subjectOfObjectDF1);
			distancepath = 1;
			
			subjectOfObjectDF1 = graphFrame.select("subject","object")
					.filter(col("object").isin(subjects))
					.filter(col("object").notEqual(col("subject")))
					.withColumn("distance", lit(distancepath));
			
			subjectOfObjectDF1.registerTempTable("frame1");

			subjectOfObjectDF1.show();
			boolean firstIteration = true;
			
			long breaker = 0;
			long lastcount = 0;
			
			while(true){

				if(firstIteration){
					firstIteration = false;
					subjects = getSubjectNames(subjectOfObjectDF1);
				}
				else{
					subjects = getSubjectNames(subjectOfObjectOfDF1);
				}
				
				distancepath += 1;
				
				for(Object r: subjects){
					System.out.println("the string of subjects is"+ (String)r);
				}
				subjectOfObjectOfDF1 = graphFrame.select("subject","object")
						.filter(col("object").isin(subjects))
						.filter(col("object").notEqual(col("subject")))
						.withColumn("distance", lit(distancepath));

				subjectOfObjectOfDF1.registerTempTable("frame2");
				subjectOfObjectOfDF1 = Service.sqlCtx().sql("SELECT DISTINCT * FROM frame2");

				

				/*
				 *  To avoid loops. We remove three kinds of rows from the dataframe.
				 *  
				 *  Case 1: When there are similar values of subject and object in a dataframe ( yes this case can occur too )
				 *  Case 2: If we have a loop i.e a dataframe somehow ends up pointing to itself.
				 *  
				 */

				
				subjectOfObjectDF1 = Service.sqlCtx().sql("SELECT f2.subject,f1.object,f2.distance FROM frame1 f1 INNER JOIN frame2 f2 ON f1.subject=f2.object").unionAll(subjectOfObjectDF1);
				subjectOfObjectDF1.registerTempTable("unionedFrame");

				
				/*
				 * case 1 : distinct clause.
				 * case 2 : in where clause part two
				 */
				subjectOfObjectDF1 = Service.sqlCtx().sql("SELECT subject,object,MIN(distance) as distance FROM unionedFrame uf WHERE uf.subject!=uf.object GROUP BY"
						+ " subject,object ");
				subjectOfObjectDF1.show();
				subjectOfObjectDF1.registerTempTable("frame1");
				

				
				
				System.out.println("subject after case 2");
				/*
				 *  The breaker will break us out of the loop if we get the same rows for 3 consecutive times.
				 */
				if(subjectOfObjectDF1.count() == lastcount){
					
						break;
				}
				
				
				lastcount = subjectOfObjectDF1.count();
			}
			
		subjectOfObjectDF1.show();
			
//			masterDF = masterDF.unionAll(subjectOfObjectDF1);
//		masterDF.write().parquet(rdfanalyzer.spark.Configuration.storage() +
//				 "closenessList.parquet");
//		masterDF.show();
	}
	
	public static Object[] getSubjectNames(DataFrame subjectRows){
		
		return subjectRows.select("subject").toJavaRDD().map(new Function<Row,String>() {

			@Override
			public String call(Row arg0) throws Exception {
				
				return arg0.getString(0);
			}
		}).collect().stream().toArray();
	}
	
	public static DataFrame initEmptyDF(){
		
		// Edge column Creation with dataType:
		List<StructField> EdgFields = new ArrayList<StructField>();
		EdgFields.add(DataTypes.createStructField("subject", DataTypes.StringType, true));
		EdgFields.add(DataTypes.createStructField("object", DataTypes.StringType, true));
		EdgFields.add(DataTypes.createStructField("distance", DataTypes.IntegerType, true));

		// Creating Schema:
		StructType edgSchema = DataTypes.createStructType(EdgFields);
		// Creating vertex DataFrame and edge DataFrame:
		return Service.sqlCtx().createDataFrame(Service.sparkCtx().emptyRDD(), edgSchema);		
	}

	public static DataFrame getDummyGraphFrame(){
		
		JavaRDD<Row> relationsRow = Service.sparkCtx()
				.parallelize(Arrays.asList(RowFactory.create("3L","4L"),
						RowFactory.create("4L","100L"), RowFactory.create("2L","3L"),RowFactory.create("3L","2L"),
//						RowFactory.create("4L","100L"), RowFactory.create("12L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("1L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("6L","5L"),RowFactory.create("1L","6L"),
						RowFactory.create("4L","100L"), RowFactory.create("6L","1L"),
						RowFactory.create("4L","100L"), RowFactory.create("1L","8L"),RowFactory.create("8L","9L"),
						RowFactory.create("4L","100L"),

//						RowFactory.create("4L","100L"), RowFactory.create("1L","8L"), RowFactory.create("1L","10L"),
//						RowFactory.create("4L","100L"), RowFactory.create("8L","9L"),RowFactory.create("3L","2L"),
//						RowFactory.create("4L","100L"), RowFactory.create("11L","1L"),RowFactory.create("6L","1L"),
//						RowFactory.create("4L","100L"), RowFactory.create("13L","11L"),RowFactory.create("9L","1L"),
//						RowFactory.create("4L","100L"), RowFactory.create("10L","1L"),RowFactory.create("8L","6L"),
//						RowFactory.create("5L","200L"), RowFactory.create("14L","10L"), RowFactory.create("10L","15L"),
//						RowFactory.create("5L","200L"), RowFactory.create("16L","14L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("5L","200L"), RowFactory.create("5L","200L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L")
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
//						RowFactory.create("15L","400L"), RowFactory.create("15L","400L")
						));

		// Edge column Creation with dataType:
		List<StructField> EdgFields = new ArrayList<StructField>();
		EdgFields.add(DataTypes.createStructField("subject", DataTypes.StringType, true));
		EdgFields.add(DataTypes.createStructField("object", DataTypes.StringType, true));

		// Creating Schema:
		StructType edgSchema = DataTypes.createStructType(EdgFields);

		// Creating vertex DataFrame and edge DataFrame:
		return Service.sqlCtx().createDataFrame(relationsRow, edgSchema);		
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
		RunAllPairShortestPathForAllNodes(nodeName);
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
		SSSP apsp = new SSSP();
		apsp.test();
	}	
	
	public static void return10RandomNodes(){
		
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "UniqueNodes.parquet");
		uniqueNodes.registerTempTable("UniqueNodes");
		uniqueNodes.show();

		List<Long> ids = new ArrayList<Long>();
		ids.add(197568495626L);
		ids.add(850403524644L);
		ids.add(927712936032L);
		ids.add(575525618367L);
		ids.add(1185410974699L);
		ids.add(369367187909L);
		ids.add(197568495640L);
		ids.add(197568496296L);
		
		DataFrame top10 = uniqueNodes.select("*").filter(uniqueNodes.col("id").isin(ids.toArray()));
		top10.write().parquet(rdfanalyzer.spark.Configuration.storage() + "Random10Nodes1.parquet");
		
		top10.show();
	}
	
	public static void CreateUniqueNodesWithoutQuotes(){
		
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "UniqueNodes.parquet");
		uniqueNodes.registerTempTable("UniqueNodes");

		DataFrame relations = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "relations.parquet");

		relations.registerTempTable("relations");
		
		System.out.println(relations.count() +" is the count of relations.");
		
		uniqueNodes.show();

//		JavaRDD<UniqueNodeCase> withoutquotesNodes = uniqueNodes.select("nodes","id").toJavaRDD().map(new Function<Row, UniqueNodeCase>() {
//
//			@Override
//			public UniqueNodeCase call(Row arg0) throws Exception {
//				
//				
//				String nodeName = arg0.getString(0);
//				System.out.println("nodeNmae ye hai = "+nodeName);
//				nodeName = nodeName.replace("\"", "");
//				nodeName = nodeName.replace("'", "");
//				System.out.println("hilgai = "+nodeName);
//
//				UniqueNodeCase casee = new UniqueNodeCase();
//				casee.setNodeID(arg0.getLong(1));
//				casee.setNodeName(nodeName);
//				
//				return casee;
//			}
//		});

//		System.out.println("creating Encoder with lines " + withoutquotesNodes.count());
//		Encoder<UniqueNodeCase> encoder = Encoders.bean(UniqueNodeCase.class);
//		System.out.println("created Encoder");
//		Dataset<UniqueNodeCase> javaBeanDS = Service.sqlCtx().createDataset(
//		  withoutquotesNodes.collect(),
//		  encoder
//		);
//		System.out.println("creating dataset");
//		javaBeanDS.toDF().write().parquet(rdfanalyzer.spark.Configuration.storage() + "uniqueNodesWithoutQuotes.parquet");
	}
	
	public static void ApplyHopBFS() throws Exception{

		
		DataFrame top10 = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "Random10Nodes1.parquet");
		
		top10.show();
		
		Row[] rows = top10.collect();
		
		List<ClosenessBean> bean = new ArrayList<ClosenessBean>();
		
		System.out.println("Starting looper mozi " + rows[0].getString(0));
		int i= 0;
		for(Row r:rows){
			if (i==0){
				bean.add(CalculateClosenessByHop(r.getString(0)));
			}
		}
		
		
		for(ClosenessBean b:bean){
			System.out.println("NodeName = "+b.getNode() + " ||||||||||||   6 hop Centrality = "+b.getCloseness());
		}
		
	}
	
	private static String readNodeFullCentrality(){
		
		DataFrame fullCentrality = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "sib200SSSP10Nodes.parquet");
		
		fullCentrality.registerTempTable("fullCentrality");
		
		DataFrame explodedCol = fullCentrality.withColumn("explodednodeDistancess", org.apache.spark.sql.functions.explode(fullCentrality.col("nodeDistances")));
		explodedCol.registerTempTable("registeredTable");
		DataFrame summedData = explodedCol.sqlContext().sql("SELECT sum(explodednodeDistancess),sourceNodes FROM registeredTable group by sourceNodes");
		
		summedData.show();
		
		
		
		return "";
	}

	public static void RunAllPairShortestPathForAllNodes(String nodeName) throws Exception{
		// responsible for dividing the number unique nodes into subnodes.
		// so that we can solve the problem individually and merge all 10 in the end together.
		
		
		// [ nodes, ids ]
		DataFrame uniqueNodes = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "UniqueNodes.parquet");
		uniqueNodes.registerTempTable("UniqueNodes");
		
		// [ subids, objids ]
		DataFrame relations = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "relations.parquet");
		relations.registerTempTable("relations");
		
		
//		DataFrame selectedNodeid = Service.sqlCtx().sql("SELECT * FROM UniqueNodes WHERE nodes='"+nodeName+"'");
//		selectedNodeid.show();
		
		
//		DataFrame uniqueNodes = DummyUniqueDataForInterimFile();
//		DataFrame relations = DummyRelationDataForInterimFile();

//		uniqueNodes.registerTempTable("UniqueNodes");


//		DataFrame sortedUniqueNodes = uniqueNodes.sqlContext().sql("SELECT * FROM UniqueNodes ORDER BY id DESC");
//		sortedUniqueNodes.show();
//
//		// this column will help us distinguish the keys in bfs because we assigned a unique constant to each key.
//		sortedUniqueNodes = sortedUniqueNodes.withColumn("randomConstants", functions.monotonically_increasing_id());
//		sortedUniqueNodes.registerTempTable("SortedUniqueNodes");
		
		
//		DataFrame subNodes;
//		Row[] lastRow = null;
		
		
		/*
		 *  we need to find the first row id. This will help us set the initial condition on the query i.e from which
		 *  id should be our id less than
		 */
		



		Row[] uniqueNodesRows = uniqueNodes.collect();
		DataFramePartitionLooper partitionerLoop  = new DataFramePartitionLooper(relations,uniqueNodesRows);

//		double differenceDouble = (uniqueNodes.count()/partitionerLoop.NODE_DIVIDER);
//		int difference = (int) differenceDouble;
//
//		Row firstRow = sortedUniqueNodes.first();
//
//		long lastId = firstRow.getLong(1);

//		DataFrame ff = relations.sqlContext().sql("SELECT * FROM relations WHERE id = 214748366883");
		
		DataFrame top10 = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
				 + "Random10Nodes1.parquet");

//		Row[] rows = top10.collect();
//		
//		int i = 0;
//		for(Row r:rows){
//
//			if(i==0){
//				partitionerLoop.run(r.getLong(1),i);
//			}
//			i++;
//		}
		
		
		top10.show();
		
//		partitionerLoop.WriteDataToFile();

//		for(int i=0;i<partitionerLoop.NODE_DIVIDER;i++){
//			
//			/*
//			 *  Suppose we've total 1000 nodes which means adjacency matrix has 1000 rows and we've to populate an RDD
//			 *  of 1000*1000. So what we did is that we will divide 1000 by 10 i.e 1000/10 = 100. And solve bfs for those
//			 *  100 nodes this makes the number of rows in the rdd to 1000*100. Once done we generate a parquet file with 
//			 *  it and than we generate the next 100 and so on until all the files are generated. In the end we merge all 
//			 *  the data to get our final result.
//			 */
//			
//			
//			subNodes = sortedUniqueNodes.sqlContext().sql("SELECT * FROM SortedUniqueNodes WHERE id < "+ lastId +" LIMIT "+ difference);
//			subNodes.show();
//			/*
//			 *  Calculate the bfs for these subNodes and generate a parquet file of the result.
//			 */
//			partitionerLoop.CreateInterimFilesForBFS(subNodes,i);
//			
//			
//			/*
//			 *  Set the last id as the last record in the subNodes DF
//			 */
//			
//			System.out.print("coming here");
//			lastRow = subNodes.collect();
//
//			lastId = lastRow[lastRow.length - 1].getLong(1);
//			break;
//		}
		
		
		
//		uniqueNodes.show();
		

//		for(int i=0;i<uniqueNodesRows.length;i++){
//			long nodeid =  uniqueNodesRows[i].getLong(1);
//			partitionerLoop.ApplyBFS(nodeid);
//			System.out.println("roribaba"+nodeid);
//			break;
//		}
		
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
