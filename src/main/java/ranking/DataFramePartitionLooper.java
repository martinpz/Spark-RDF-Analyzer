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
	
	
	public void ApplyBFSOnInterimFiles(){
		for(int i=0;i<NODE_DIVIDER;i++){
			
			DataFrame interimFile = Service.sqlCtx().parquetFile(rdfanalyzer.spark.Configuration.storage()
					 + "sib200APSPInterim"+i+".parquet");
		}
	}
	
	
	public void CreateInterimFilesForBFS(DataFrame nodeslist,int index){
		System.out.println("show kring");
		nodeslist.show();
		System.out.println("Doing step 1 full parquet");
		JavaPairRDD<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long, Long>> completeNodes = AddConstantColumn(nodeslist);
		System.out.println("Doing step 2 full parquet = "+completeNodes.count());
		JavaRDD<RepeatedRowsCase> caseRDD = ConvertToCaseRDD(completeNodes);
		System.out.println("Doing step 3 full parquet = " + caseRDD.count());
		ConvertToParquet(caseRDD, index);
		System.out.println("Doing step 4 full parquet");
		
	}
	
	public JavaRDD<RepeatedRowsCase> ConvertToCaseRDD(JavaPairRDD<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long, Long>> completeNodes ){
		return completeNodes.map(new Function<Tuple2<Long,Tuple6<List<Long>,Integer,Integer,Integer,Long,Long>>, RepeatedRowsCase>() {

			@Override
			public RepeatedRowsCase call(Tuple2<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>> arg0)
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
	
	
	
	private JavaPairRDD<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>> AddConstantColumn(DataFrame nodeslist){
		
		Row[] uniqueNodes = nodeslist.collect();

		return adjacencyMatrix.flatMapToPair(new PairFlatMapFunction<Tuple2<Long,Tuple4<List<Long>,Integer,Integer,Integer>>, Long, Tuple6<List<Long>,Integer,Integer,Integer,Long,Long>>() {

			@Override
			public Iterable<Tuple2<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>>> call(
					Tuple2<Long, Tuple4<List<Long>, Integer, Integer, Integer>> arg0) throws Exception {
				
				/*
				 * 		@param1 : neighbors 
				 *		@param2 : distance 
				 *		@param3 : color
				 *		@param4 : NShortestPaths
				 *		@param5 : SourceID ( source for this adjacencyMatrix )
				 *		@param6 : ConstantID
				 *		
				 *		@param5 helps us identify separate id's from the nodes. Using this we will update our keys before starting our 
				 *		bfs to make sure all the keys are different for each group. By group here we mean an adjacencyMatrix is initialized
				 *		for each unique node meaning that for n nodes we have n adjacencyMatrix on which the bfs will be applied.
				 *		This param will help us not to mix up those adjacencyMatrix records for the keys column w.r.t each unique node.
				 *
				 *		@param0 which is the key. It is calculated by multiplying the key in the adjacencyMatrix for which we're in this 
				 *		function right now multiply by the source node for this adjacency matrix multiply by the random unique constant
				 *		we retrieved in Centrality class for uniqueNodes. By doing this we'll ensure that the keys in the adjacency matrix
				 *		while applying bfs to it are not same for same sourceNode.
				 *
				 *		For.eg we've 10 adjacency matrix which has keys 1 ... 10. Now we've random 10 values for each 10 keys and also
				 *		in 10 adjacency matrix we've 1 sourceNode which is why we've 10 adjacencyMatrix repeated i.e one for every sourceNode.
				 *		
				 *		So now we can make the key using sourceNode*RandomConstant*nodeOfThatRowOfAdjacencyMatrix. giving us a unique node
				 *		which will be repeated only for the adjacencyMatrix and not for others.
				 *
				 *
				 */
				
				List<Tuple2<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>>> result = new ArrayList<Tuple2<Long, Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>>>();

				for (Row r :uniqueNodes)
				{
					Tuple6<List<Long>, Integer, Integer, Integer, Long, Long> tuple6 = new Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>(arg0._2._1(), arg0._2._2(), arg0._2._3(), arg0._2._4(), r.getLong(1) ,r.getLong(2));
					Tuple2<Long,Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>> abc = new Tuple2<Long,Tuple6<List<Long>, Integer, Integer, Integer, Long,Long>>(arg0._1* r.getLong(2) * r.getLong(1),tuple6);
					result.add(abc);
					break;
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
	
	public void run(long nodeid){

//		for(int i=0;i<uniqueNodesRows.length;i++){
//			
//			System.out.println("Finding the shortest path for a node.");

				System.out.print("algo started");
				result = this.apsp.finalReduce
					(this.apsp.applyBFSForNode(nodeid, adjacencyMatrix));
				System.out.print("algo finished");
		
				
				WriteDataToFile();
//		}
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