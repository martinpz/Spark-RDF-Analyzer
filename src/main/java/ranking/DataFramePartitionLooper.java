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
	
	private Row[] uniqueNodesRows;
	
	private int index = 0;
	
	private boolean start = true;

	public DataFramePartitionLooper(DataFrame relations,Row[] uniqueNodesRows) {
		
		apsp = new SSSP();
		
		this.uniqueNodesRows = uniqueNodesRows;
		
		// remove self loops.
//		relations = relations.select("subId","objId").where(relations.col("subId").notEqual("objId"));
//		relations.show();
		
		JavaPairRDD<Long,Long> rdd = DFToRDD(relations);
		
		// get the adjacency matrix first.
		adjacencyMatrix = apsp.reduceToAdjacencyMatrix(rdd);
		adjacencyMatrix.cache();
	}

	// returnes key, (node,nodeConstant)
	
	/*
	 *  nodeConstant will help us to differentiate the keys in the breadth first search.
	 */
	public JavaPairRDD<Long, Tuple2<String, Long>> ConvertNodesDFToRDD(DataFrame sourceNodes){
		return sourceNodes.select("id","nodes","randomConstants").toJavaRDD().mapToPair(new PairFunction<Row, Long, Tuple2<String,Long>>() {

			@Override
			public Tuple2<Long, Tuple2<String,Long>> call(Row arg0) throws Exception {
				
				return new Tuple2<Long,Tuple2<String,Long>>(arg0.getLong(0),new Tuple2<String,Long>(arg0.getString(1),arg0.getLong(2)));
			}
		});
	}
	
	public void ApplyBFSForSeveralNodes(DataFrame nodeslist,int index){
		
		JavaPairRDD<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>> completeNodes = AddConstantColumn(nodeslist);
		JavaRDD<RepeatedRowsCase> caseRDD = ConvertToCaseRDD(completeNodes);
		ConvertToParquet(caseRDD, index);
		
	}
	
	public JavaRDD<RepeatedRowsCase> ConvertToCaseRDD(JavaPairRDD<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>> completeNodes ){
		return completeNodes.map(new Function<Tuple2<Long,Tuple5<List<Long>,Integer,Integer,Integer,Long>>, RepeatedRowsCase>() {

			@Override
			public RepeatedRowsCase call(Tuple2<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>> arg0)
					throws Exception {
				
				RepeatedRowsCase casee = new RepeatedRowsCase();
				casee.setSourceNodes(arg0._1);
				casee.setDestinationNodes(arg0._2._1());
				casee.setNodeDistances(arg0._2()._2());
				casee.setNodeColor(arg0._2._3());
				casee.setNodeShortestPaths(arg0._2._4());
				casee.setNodeConstantID(arg0._2._5());
				
				return null;
			}
		});
	}	
	private void ConvertToParquet(JavaRDD<RepeatedRowsCase> finalData,int index){

		try{
			org.apache.spark.sql.catalyst.encoders.OuterScopes.addOuterScope(this);

			Encoder<RepeatedRowsCase> encoder = Encoders.bean(RepeatedRowsCase.class);
			Dataset<RepeatedRowsCase> javaBeanDS = Service.sqlCtx().createDataset(
			  finalData.collect(),
			  encoder
			);
			javaBeanDS.toDF().write().parquet(rdfanalyzer.spark.Configuration.storage() + "sib200APSPInterim"+index+".parquet");
		}
		catch(NullPointerException e){
			System.out.println("We are in the error");
			System.out.println(e.getMessage());
		}
	}
	
	
	
	private JavaPairRDD<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>> AddConstantColumn(DataFrame nodeslist){
		
		Row[] uniqueNodes = nodeslist.collect();
		return adjacencyMatrix.flatMapToPair(new PairFlatMapFunction<Tuple2<Long,Tuple4<List<Long>,Integer,Integer,Integer>>, Long, Tuple5<List<Long>,Integer,Integer,Integer,Long>>() {

			@Override
			public Iterable<Tuple2<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>>> call(
					Tuple2<Long, Tuple4<List<Long>, Integer, Integer, Integer>> arg0) throws Exception {
				
				/*
				 * 		@param1 : neighbors 
				 *		@param2 : distance 
				 *		@param3 : color
				 *		@param4 : NShortestPaths
				 *		@param5 : outerLoopIDs
				 *		
				 *		@param5 helps us identify separate id's from the nodes. Using this we will update our keys before starting our 
				 *		bfs to make sure all the keys are different for each group. By group here we mean an adjacencyMatrix is initialized
				 *		for each unique node meaning that for n nodes we have n adjacencyMatrix on which the bfs will be applied.
				 *		This param will help us not to mix up those adjacencyMatrix records for the keys column w.r.t each unique node.
				 *
				 */
				
				List<Tuple2<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>>> result = new ArrayList<Tuple2<Long, Tuple5<List<Long>, Integer, Integer, Integer, Long>>>();

				for (Row r :uniqueNodes)
				{
					Tuple5<List<Long>, Integer, Integer, Integer, Long> tuple5 = new Tuple5<List<Long>, Integer, Integer, Integer, Long>(arg0._2._1(), arg0._2._2(), arg0._2._3(), arg0._2._4(), r.getLong(2));
					Tuple2<Long,Tuple5<List<Long>, Integer, Integer, Integer, Long>> abc = new Tuple2<Long,Tuple5<List<Long>, Integer, Integer, Integer, Long>>(arg0._1,tuple5);
					result.add(abc);
				}
				return result;
			}
		});
	}
	
	
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

	
	public void WriteDataToFile(){
		JavaRDD<SSSPCase> apspRDD = ConvertPairRDDToCaseRDD(result);
		WriteInfoToParquet(apspRDD);
	}
	
	private void WriteInfoToParquet(JavaRDD<SSSPCase> finalData){

		try{
			org.apache.spark.sql.catalyst.encoders.OuterScopes.addOuterScope(this);

			Encoder<SSSPCase> encoder = Encoders.bean(SSSPCase.class);
			Dataset<SSSPCase> javaBeanDS = Service.sqlCtx().createDataset(
			  finalData.collect(),
			  encoder
			);
			javaBeanDS.toDF().write().parquet(rdfanalyzer.spark.Configuration.storage() + "sib200APSPAll.parquet");
		}
		catch(NullPointerException e){
			System.out.println("We are in the error");
			System.out.println(e.getMessage());
		}
	}

	private JavaPairRDD<Long,Long> DFToRDD(DataFrame relations){
		return relations.select("subId","objId").toJavaRDD().mapToPair(new PairFunction<Row, Long, Long>() {

			@Override
			public Tuple2<Long, Long> call(Row arg0) throws Exception {
				
				
				return new Tuple2<Long,Long>(arg0.getLong(0),arg0.getLong(1));
			}
		});
	}
	
	public void run(){

		for(int i=0;i<uniqueNodesRows.length;i++){
			
			System.out.println("Finding the shortest path for a node.");

			ApplyBFS(uniqueNodesRows[i].getLong(1),index);
		}
	}
	
	
	public void ApplyBFS(long subject,int index){
//		
//		 System.out.println("chilgoza loop is starting");
//		 if(this.start){
//
//			System.out.println("chilgoza start if condition" + subject);
//			result = this.apsp.applyBFSForNode(subject, adjacencyMatrix);
//			this.start = false;
//		 }
//		 else{
//
//			System.out.println("chilgoza start else condition"+ subject);
//			result = result.union(this.apsp.applyBFSForNode(subject, adjacencyMatrix));
//		 }
//		 
	}


//	@Override
//	public BoxedUnit apply(Iterator<Row> iterator) {
//		
//		System.out.println("iterator called");
//		
//		while (iterator.hasNext()) {
//			 Row row = iterator.next();
//			 
//			 long subject = row.getLong(1);
//			 
//			 if(starter){
//				starter = false;
//				result = this.apsp.applyBFSForNode(subject, adjacencyMatrix);
//			 }
//			 else{
//				 
//				result = result.union(this.apsp.applyBFSForNode(subject, adjacencyMatrix));
//			 }
//		}
//		
//		return BoxedUnit.UNIT;
//	}

}