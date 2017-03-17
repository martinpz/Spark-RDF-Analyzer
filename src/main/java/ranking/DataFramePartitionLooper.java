package ranking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import scala.Tuple5;
import scala.Tuple6;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import rdfanalyzer.spark.Service;

public class DataFramePartitionLooper 
//extends AbstractFunction1<Iterator<Row>, BoxedUnit> 
implements Serializable {

	private static final long serialVersionUID = -1919222653470217466L;
	
	private SSSP apsp ;
	private JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrix;
	private JavaPairRDD<Long, Tuple3<List<Long>, List<Integer>, List<Integer>>> result;
	
	public final int NODE_DIVIDER = 1000;
	
	
	/**
	 *  
	 * @param relations
	 * 
	 * initializes the SSSP algorithm and uses the SSSP function to create an initial adjacency Matrix of the dataset.
	 */
	public DataFramePartitionLooper(DataFrame relations) {
		
		apsp = new SSSP();
		
		JavaPairRDD<Long,Long> rdd = DFToRDD(relations);
		
		// get the adjacency matrix first.
		adjacencyMatrix = apsp.reduceToAdjacencyMatrix(rdd);
		adjacencyMatrix.cache();
	}


	/**
	 * 
	 * @param result
	 * @return JavaRDD<SSSPCase>
	 * 
	 * Converts the JavaPairRDD data into case class based JavaRDD. This helps us revert the data into a DataFrame and
	 * save it into a parquet file.
	 */
	private JavaRDD<SSSPCase> ConvertPairRDDToCaseRDD(JavaPairRDD<Long, Tuple3<List<Long>, List<Integer>, List<Integer>>> result){
		return result.map(new Function<Tuple2<Long,Tuple3<List<Long>, List<Integer>, List<Integer>>>, SSSPCase>() {

			@Override
			public SSSPCase call(Tuple2<Long,Tuple3<List<Long>, List<Integer>, List<Integer>>> line) throws Exception {
				
				SSSPCase apspcase = new SSSPCase();
				apspcase.setSourceNodes(line._1);
				apspcase.setNodeDistances(line._2._2());
				apspcase.setNodeShortestPaths(line._2._3());
				apspcase.setDestinationNodes(line._2._1());
				
				return apspcase;
			}
		});

	}

	/**
	 * 
	 * @param dataset
	 * 
	 * Takes the dataset name, converts the data and saves it into a parquet file.
	 */
	public void WriteDataToFile(String dataset){
		JavaRDD<SSSPCase> apspRDD = ConvertPairRDDToCaseRDD(result);
		WriteInfoToParquet(apspRDD, dataset);
	}
	
	/**
	 * 	Writes the data into a parquet file.
	 */
	private void WriteInfoToParquet(JavaRDD<SSSPCase> finalData, String dataset){

		try{
			org.apache.spark.sql.catalyst.encoders.OuterScopes.addOuterScope(this);

			Encoder<SSSPCase> encoder = Encoders.bean(SSSPCase.class);
			Dataset<SSSPCase> javaBeanDS = Service.sqlCtx().createDataset(
			  finalData.collect(),
			  encoder
			);
			javaBeanDS.toDF().write().parquet(rdfanalyzer.spark.Configuration.storage() + dataset + "TopClosenessNodes.parquet");
		}
		catch(NullPointerException e){
			System.out.println("We are in the error");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 
	 * @param relations
	 * @return JavaPairRDD<Long,Long>
	 * 
	 * Converts the dataFrame into a subjectID,objectID based JavaPairRDD for further processing in the BFS.
	 */
	private JavaPairRDD<Long,Long> DFToRDD(DataFrame relations){
		return relations.select("subId","objId").toJavaRDD().mapToPair(new PairFunction<Row, Long, Long>() {

			@Override
			public Tuple2<Long, Long> call(Row arg0) throws Exception {
				
				
				return new Tuple2<Long,Long>(arg0.getLong(0),arg0.getLong(1));
			}
		});
	}
	
	/**
	 *  
	 * @param nodeid
	 * @param firstTime
	 * @throws Exception
	 * 
	 * Main function for running the BFS. After calculating the BFs for first sourceNode. It assigns it to the result.
	 * While for the next nodes it just unions it to the previous result giving us a final result of the form.
	 * 
	 *  sourceNodeID, [ array of nodes it can reach ], [ array of distances it can reach those nodes in ] , [ no. of shortest paths between the source and these nodes ].
	 */
	public void run(long nodeid,boolean firstTime) throws Exception{

			if(firstTime){
				result = this.apsp.finalReduce
						(this.apsp.applyBFSForNode(nodeid, adjacencyMatrix));
				
				this.apsp.applyBFSForNode(nodeid, adjacencyMatrix);
			}
			else{
				result = result.union(this.apsp.finalReduce
						(this.apsp.applyBFSForNode(nodeid, adjacencyMatrix)));
			}
		
	}
	
	



}