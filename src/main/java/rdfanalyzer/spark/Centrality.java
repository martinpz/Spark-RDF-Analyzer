package rdfanalyzer.spark;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.MonotonicallyIncreasingID;

import com.google.common.io.Resources;

import ranking.ClosenessBean;
import ranking.ClosenessCentrality;
import ranking.RDFAnalyzerPageRank.PageRanksCase;
import ranking.oldTests;
import scala.Tuple2;


public class Centrality {
private final static Logger logger = Logger.getLogger(Centrality.class);

public static ConnAdapter objAdapter = new ConnAdapter();
public static Dataset<Row> graphFrame;
public static List<ClosenessBean> closenessbean = new ArrayList<ClosenessBean>();

public static final String closenessParquetPath = rdfanalyzer.spark.Configuration.storage() + "closeness.parquet";
/*
*  this means that when we find the maximum in degree in the whole graph.
*  Lets say if the max in degree for a node in the graph is 100.
*  Than LIMIT_DELTA = 80 means only consider nodes with indegree <= 80.
*  This parameter can be tuned to suit your requirements.
*/
public static final int LIMIT_DELTA = 80;
public static String main(String metricType,String dataset, String nodeName) throws Exception{
graphFrame = Service.spark().sqlContext().parquetFile(Configuration.storage() + dataset + ".parquet");
graphFrame.cache().createOrReplaceTempView("Graph");
/**********************************************************************/
// DataFrame resultsFrame = Service.sqlCtx().sql("SELECT * FROM Graph");
// Row[] rows = resultsFrame.collect();
//
// for(int i = 0 ; i < rows.length; i++)
// {
// System.out.println("The node name  is "+rows[i].getString(0));
// System.out.println("The node importance is "+rows[i].getDouble(1));
// }
// return "";
/**********************************************************************/

for(int i=0;i<graphFrame.schema().fieldNames().length;i++){
System.out.println("[LOG]Schema FieldName : "+graphFrame.schema().fieldNames()[i]);
}
nodeName = nodeName.replace("$", "/");
System.out.println("[LOGS] Node name= "+nodeName);

if(metricType.equals("1")){
System.out.println("[LOGS] Present in metric type 1");
return "<h1>"+ CalculateInDegree(nodeName) +"</h1>";
}
else if(metricType.equals("2")){
System.out.println("[LOGS] Present in metric type 2");
return "<h1>"+ CalculateOutDegree(nodeName) +"</h1>";
}
else if(metricType.equals("3")){
System.out.println("[LOGS] Present in metric type 3");
return CalculateBetweenness(nodeName);
}
else if(metricType.equals("4")){
	
	List<String> list = new ArrayList<String>();
	list.add(nodeName);
System.out.println("[LOGS] Present in metric type 4");
return CalculateCloseness(list);
}
else if(metricType.equals("5")){
System.out.println("[LOGS] Present in metric type 5");
return "<h1>"+calculateStartNode()+"</h1>";
}
return "none";
}
public static String CalculateInDegree(String node){

String result = "";
// Run SQL over loaded Graph.

Dataset<Row> resultsFrame = Service.spark().sqlContext().sql("SELECT COUNT(subject) FROM Graph WHERE object = '"+node+"'");
resultsFrame.select("").filter(resultsFrame.col("").isin());
List<Row> rows = resultsFrame.collectAsList();

result = Long.toString(rows.get(0).getLong(0));

return result;
}
public static String CalculateOutDegree(String node){
String result = "";

Dataset<Row> resultsFrame = Service.spark().sqlContext().sql("SELECT COUNT(object) from Graph where subject='"+node+"'");
List<Row> rows = resultsFrame.collectAsList();

result = Long.toString(rows.get(0).getLong(0));

return result;
}
public static String CalculateBetweenness(String node){
return "betweenness";
}
public static String CalculateCloseness(List<String> node) throws Exception{

	 List<String> tobeQueried = node;
	 Dataset<Row> existingData = null;

	 
	 File f = new File(closenessParquetPath);

	 if(f.exists()){
		 
		 // Read existing closeness data from Parquet
		 existingData = Service.spark().read().parquet(closenessParquetPath); 

		 System.out.println("ExistingData Size = "+existingData.count());
		 
		 // Getting All the nodes from Parquet Table which are present in the queried list passed as an arg to this function.
		 Dataset<Row> existingNodesCloseness = existingData.select(existingData.col("closeness"),existingData.col("node")).where(existingData.col("node").isin(node.stream().toArray(String[]::new)));	

		 // Convert the parquet dataset nodesName col into List<String>
		 List<String> existingNodes = getListFromDatasetRows(existingNodesCloseness);

		 /*
		  *  Subtract the existingNodes from the ones which are queried, since the existing ones are already available
		  *  through parquet.
		  */
		 tobeQueried = getUniqueValues(node,existingNodes);
	 }
 
 
 
	 Dataset<Row> resultsFrame = Service.spark().sqlContext().sql("SELECT * from Graph");
	 ClosenessCentrality path = new ClosenessCentrality();
	
	 for(String anode:tobeQueried){
		 closenessbean.add(path.calculateCloseness(resultsFrame,anode));
	 }
	 
	 Dataset<Row> newDataset = Service.spark().createDataFrame(closenessbean,ClosenessBean.class);
	 
	 if(f.exists()){
		 newDataset = existingData.union(newDataset);
	 }
	 
	 System.out.println("Count of data being saved in parquet = "+newDataset.count());
	 
	 
	 
	 
	 System.out.println("deleted = "+Files.deleteIfExists(f.toPath()));
	 newDataset.write().parquet(closenessParquetPath);

 // Dataset<Row> vertFrame = Service.spark().sqlContext().sql("select *,row_number() OVER(ORDER BY(SELECT 0)) as id from Graph");

//Dataset<Row> vertFrame = Service.spark().sqlContext().sql(""
//+ "SELECT DISTINCT row_number() OVER(ORDER BY(SELECT 0)) as id,a.nodes  FROM "
//+ "(SELECT subject as nodes from Graph"
//+ " UNION ALL"
//+ " SELECT object as nodes FROM Graph) a");

//	String nodee = node;
//	System.out.println("query 0 success");
//	Dataset<Row> hop1Nodes = Service.spark().sqlContext().sql("SELECT subject,object FROM Graph WHERE subject='"+nodee+"' AND object!='"+nodee+"'");
//	System.out.println("query 1 success");
//
//	List<String> hop1Objects = getListFromDatasetRows(hop1Nodes);
//	
//
//	Dataset<Row> hop2Nodes = graphFrame.select(graphFrame.col("subject"),graphFrame.col("object"))
//			.where(graphFrame.col("subject").isin(hop1Objects.stream().toArray(String[]::new))
//					.and(graphFrame.col("object").notEqual(hop1Nodes.col("object"))
//							.and(graphFrame.col("object").notEqual(nodee))));
//	System.out.println("query 3 success");
//
//	List<String> hop2Objects = getListFromDatasetRows(hop2Nodes);
//	System.out.println("query 4 success");
//	
//	Dataset<Row> hop3Nodes = graphFrame.select(graphFrame.col("subject"),graphFrame.col("object"))
//					.where(graphFrame.col("subject").isin(hop2Objects.stream().toArray(String[]::new))
//					.and(graphFrame.col("object").notEqual(hop2Nodes.col("object")))
//					.and(graphFrame.col("object").notEqual(hop1Nodes.col("object")))
//					.and(graphFrame.col("object").notEqual(nodee)));
//	
//	System.out.println("query 5 success");
//	
//	System.out.println("hop1 = "+ hop1Nodes.count());
//	System.out.println("hop2 = "+ hop2Nodes.count());
//	System.out.println("hop3 = "+ hop3Nodes.count());
//
//	hop1Nodes = hop1Nodes.union(hop2Nodes).union(hop3Nodes);
//	hop1Nodes.show();


//vertFrame.write().parquet(rdfanalyzer.spark.Configuration.storage() + "UniqueNodes.parquet");
//vertFrame.createOrReplaceTempView("UniqueNodes");

//	Dataset<Row> vertFrame = Service.spark().read().parquet(rdfanalyzer.spark.Configuration.storage() + "UniqueNodes.parquet");
//	vertFrame.show();
//	vertFrame.createOrReplaceTempView("UniqueNodes");
//
//Dataset<Row> relationsFrame = Service.spark().sqlContext().sql(""
//+ "SELECT unSub.id as subId,unObj.id as objId,predicate FROM Graph g "
//+ "INNER JOIN UniqueNodes unSub ON unSub.nodes=g.subject "
//+ "INNER JOIN UniqueNodes unObj ON unObj.nodes=g.object");
//relationsFrame.write().parquet(rdfanalyzer.spark.Configuration.storage() + "relations.parquet");
//relationsFrame.show();

// Dataset<Row> relationsFrame = Service.spark().sqlContext().sql("SELECT subject,predicate,object from Graph");


// // this give us the value of max indegree of a particular node.
// long highestIndegree = getHighestIndegree();
// 
// 
// long inDegreeignoreLimit = (highestIndegree * LIMIT_DELTA)/ 100;
//
// String query = "SELECT g.subject,g.object FROM Graph g INNER JOIN "
// + "(SELECT object FROM Graph GROUP BY object HAVING "
// + "COUNT(subject)<"+inDegreeignoreLimit+") ss ON ss.object = g.object";
//
// DataFrame allSubjects = Service.sqlCtx().sql(query);
//
// RDFAnalyzerPageRank analyzer = new RDFAnalyzerPageRank();
// analyzer.PerformPageRank(allSubjects);
return "";
}





private static List<String> getUniqueValues(List<String> objects, List<String> alreadyVisited){

	List<String> union = new ArrayList<String>(alreadyVisited);
	union.addAll(objects);

	List<String> intersection = new ArrayList<String>(alreadyVisited);
	intersection.retainAll(objects);

	List<String> symmetricDifference = new ArrayList<String>(union);
	symmetricDifference.removeAll(intersection);

	return symmetricDifference;
}



public static List<String> getListFromDatasetRows(Dataset<Row> rows){
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
public static long getHighestIndegree(){
Dataset<Row> maxInDegreeFrame = Service.spark().sqlContext()
.sql("SELECT MAX(tbl1.InDegreeCount) FROM "
+ "(SELECT object,COUNT(subject) AS InDegreeCount FROM Graph GROUP BY object)tbl1");

List<Row> rowMaxInDegree = maxInDegreeFrame.collectAsList();

return rowMaxInDegree.get(0).getLong(0);
}

public static long getHighestOutDegree(){
Dataset<Row> maxOutDegreeFrame = Service.spark().sqlContext()
.sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

List<Row> rowMaxOutDegree = maxOutDegreeFrame.collectAsList();
return rowMaxOutDegree.get(0).getLong(1);
}
/*
* BullShit
*/
public static String calculateStartNode(){
/*
* We calculate 4 different values because. It is not necessary that the node which has
* highest in-degree also has highest outdegree. So if a node has highest out-degree we
* also calculate it's in-degree. And the other way around. Hence we end up with 4 
* values
*/
// node with highest out-degree
Dataset<Row> maxOutDegreeFrame = Service.spark().sqlContext()
.sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

List<Row> rowMaxOutDegree = maxOutDegreeFrame.collectAsList();

// in-degree of node with highest out-degree
String maxInDegreeOfOutDegree = CalculateInDegree(rowMaxOutDegree.get(0).getString(0));

// node with highest in-degree
Dataset<Row> maxInDegreeFrame = Service.spark().sqlContext()
.sql("SELECT first(tbl1.object),MAX(tbl1.OutdegreeCount) FROM"
+ "(SELECT object,COUNT(subject) AS OutdegreeCount FROM Graph GROUP BY object)tbl1");

List<Row> rowMaxInDegree = maxInDegreeFrame.collectAsList();
// out-degree of node with highest in-degree
String maxOutDegreeOfInDegree = CalculateInDegree(rowMaxInDegree.get(0).getString(0));

System.out.println("[LOG]Working until here yuppie");
long maxOutdegreeTotal = rowMaxOutDegree.get(0).getLong(1) + Integer.parseInt(maxInDegreeOfOutDegree);
long maxIndegreeTotal = rowMaxInDegree.get(0).getLong(1) + Integer.parseInt(maxOutDegreeOfInDegree);
if(maxOutdegreeTotal < maxIndegreeTotal){
return rowMaxOutDegree.get(0).getString(0);
}
return rowMaxInDegree.get(0).getString(0);

}

}
