package ranking;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import rdfanalyzer.spark.Service;

public class ClosenessNodes {
	
	public static DataFrame run(DataFrame graphFrame, String dataset){
		return getFarthestNodes(getClosenessCandidates(),graphFrame, dataset);
	}

	public static DataFrame getFarthestNodes(DataFrame nodesWithHighestOutDegree,DataFrame graphFrame, String dataset){
		
		/*
		 *  Top 10 nodes with maximum outdegrees. Add a column in the front with 1 that represents them as grey nodes.
		 *  i.e the ones to be expanded next.
		 */
		
		
		DataFrame subjectOfObjectDF1 = nodesWithHighestOutDegree;
		subjectOfObjectDF1.registerTempTable("sourceNodes");
		subjectOfObjectDF1.show();
		
		
		int distancepath = 1;
		
		DataFrame subjectOfObjectOfDF1 = initEmptyDF();

			subjectOfObjectOfDF1 = initEmptyDF();
			
			// get names of subjects in terms of list.
			Object[] subjects = getSubjectNames(subjectOfObjectDF1);
			distancepath = 1;
			
			subjectOfObjectDF1 = graphFrame.select("subject","object")
					.filter(col("object").isin(subjects))
					.filter(col("object").notEqual(col("subject")))
					.withColumn("distance", lit(distancepath));
			
			subjectOfObjectDF1.registerTempTable("frame1");

			boolean firstIteration = true;
			
			/**
			 *  @lastcount keeps track of the sum of distances in the last iteration.
			 *  It gets compared on every iteration with that iterations sum count. 
			 *  If they are both the same. We break out of the loop.
			 */
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
				subjectOfObjectDF1.registerTempTable("frame1");
				

				
				
				/*
				 *  The breaker will break us out of the loop if we get the same rows for 3 consecutive times.
				 */
				if(subjectOfObjectDF1.count() == lastcount){
						break;
				}
				
				
				lastcount = subjectOfObjectDF1.count();
			}
			
		subjectOfObjectDF1 = Service.sqlCtx().sql("SELECT subject,COUNT(object) as intersections FROM frame1 Group By subject "
				+ " ORDER BY intersections DESC LIMIT 10");
		
		subjectOfObjectDF1.write().parquet(rdfanalyzer.spark.Configuration.storage() + dataset +
				 "closenessList.parquet");
		
		return subjectOfObjectDF1;
		
	}
	
	/**
	 * finds the top 10% nodes with most outdegree values.
	 * 
	 */
	public static DataFrame getClosenessCandidates(){

//		// node with max outdegree
//		DataFrame allNodes = Service.sqlCtx().sql("SELECT DISTINCT"
//				 + " a.nodes FROM "
//				 + "(SELECT subject as nodes from Graph "
//				 + " UNION ALL "
//				 + " SELECT object as nodes FROM Graph) a");
//
//		
//		long rangeTo = allNodes.count();
//		
//		/*
//		 * 	10% of all the nodes. Suppose if we have total 100 nodes in the graph.
//		 *  We'll select the 10% of those nodes which have the top outdegree values.
//		 */
//		long percentCut = (rangeTo * 1) / 100;
		
		

		// select nodes whose outDegree is greater than rangeFrom
		DataFrame nodesInMax10PercentRange = Service.sqlCtx().sql("SELECT subject,COUNT(object) as OutDegreeCount "
				+ "FROM Graph Group BY subject ORDER BY OutDegreeCount DESC LIMIT 10");
		
		return nodesInMax10PercentRange;
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
	
	/** 
	 * 	Creates an empty dataframe so that we can insert the 2nd dataframe values into it.
	 */
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

	
	
	/**
	 * 
	 * @return DataFrame
	 * 
	 * This function is to return a dummy graph that can be used to test our algorithm.
	 * The highest outdegree nodes in this dataset can be seen as 4L,5L,15L,9L
	 */
	public static DataFrame getDummyGraphFrame(){
		
		JavaRDD<Row> relationsRow = Service.sparkCtx()
				.parallelize(Arrays.asList(RowFactory.create("3L","4L"),
						RowFactory.create("4L","100L"), RowFactory.create("2L","3L"),RowFactory.create("3L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("12L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("1L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("6L","5L"),RowFactory.create("1L","6L"),
						RowFactory.create("4L","100L"), RowFactory.create("6L","1L"),
						RowFactory.create("4L","100L"), RowFactory.create("1L","8L"),RowFactory.create("8L","9L"),
						RowFactory.create("4L","100L"),

						RowFactory.create("4L","100L"), RowFactory.create("1L","8L"), RowFactory.create("1L","10L"),
						RowFactory.create("4L","100L"), RowFactory.create("8L","9L"),RowFactory.create("3L","2L"),
						RowFactory.create("4L","100L"), RowFactory.create("11L","1L"),RowFactory.create("6L","1L"),
						RowFactory.create("4L","100L"), RowFactory.create("13L","11L"),RowFactory.create("9L","1L"),
						RowFactory.create("4L","100L"), RowFactory.create("10L","1L"),RowFactory.create("8L","6L"),
						RowFactory.create("5L","200L"), RowFactory.create("14L","10L"), RowFactory.create("10L","15L"),
						RowFactory.create("5L","200L"), RowFactory.create("16L","14L"),
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
						RowFactory.create("9L","300L"), RowFactory.create("9L","300L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L"),
						RowFactory.create("15L","400L"), RowFactory.create("15L","400L")
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
}
