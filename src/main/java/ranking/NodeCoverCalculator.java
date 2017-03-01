//package ranking;
//
//import java.io.Serializable;
//import java.util.List;
//
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.function.PairFunction;
//import org.apache.spark.sql.DataFrame;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Encoder;
//import org.apache.spark.sql.Encoders;
//import org.apache.spark.sql.Row;
//
//import rdfanalyzer.spark.Service;
//import scala.Tuple2;
//import scala.Tuple3;
//import scala.Tuple4;
//
//public class NodeCoverCalculator implements Serializable{
//	
//	private SSSP apsp ;
//	private JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrix;
//	private Row[] uniqueNodesRows;
//	
//	private long fullbfsSource;
//
//	public NodeCoverCalculator(DataFrame relations,Row[] uniqueNodesRows){
//		
//		apsp = new SSSP();
//
//		JavaPairRDD<Long,Long> rdd = DFToRDD(relations);
//		adjacencyMatrix = apsp.reduceToAdjacencyMatrix(rdd);
//		adjacencyMatrix.cache();
//
//	
//		this.uniqueNodesRows = uniqueNodesRows;
//	}
//
//	public void applyFirstBFS(long node){
//		
//		this.fullbfsSource = node;
//		
//		JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> fullbfsMapResult = apsp.applyBFSForNode(node,adjacencyMatrix);
//		JavaPairRDD<Long, Tuple3<List<Long>, List<Integer>, List<Integer>>> fullbfsReduceResult  = this.apsp.finalReduce(fullbfsMapResult);
//	}
//	
//	
//	public void ApplyRestBFS(){
//
//		apsp.fullBfs = false;
//		
//		for(Row r:uniqueNodesRows){
//			
//			if(r.getLong(1) == this.fullbfsSource){
//				continue;
//			}
//			
//			apsp.applyBFSForNode(r.getLong(1), adjacencyMatrix);
//			
//		}
//		
//	}
//	
//	
//	private JavaPairRDD<Long,Long> DFToRDD(DataFrame relations){
//		return relations.select("subId","objId").toJavaRDD().mapToPair(new PairFunction<Row, Long, Long>() {
//
//			@Override
//			public Tuple2<Long, Long> call(Row arg0) throws Exception {
//				
//				
//				return new Tuple2<Long,Long>(arg0.getLong(0),arg0.getLong(1));
//			}
//		});
//	}
//	
//
//
//	private void ConvertFullBFSResultToRDD(){
//		
//	}
//	
//
//	private void WriteInfoToParquet(JavaRDD<SSSPCase> finalData){
//
//		try{
//			org.apache.spark.sql.catalyst.encoders.OuterScopes.addOuterScope(this);
//
//			Encoder<SSSPCase> encoder = Encoders.bean(SSSPCase.class);
//			Dataset<SSSPCase> javaBeanDS = Service.sqlCtx().createDataset(
//			  finalData.collect(),
//			  encoder
//			);
//			javaBeanDS.toDF().write().parquet(rdfanalyzer.spark.Configuration.storage() + "sib200APSPAll.parquet");
//		}
//		catch(NullPointerException e){
//			System.out.println("We are in the error");
//			System.out.println(e.getMessage());
//		}
//	}
//}
