package ranking;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.distributed.CoordinateMatrix;
import org.apache.spark.mllib.linalg.distributed.IndexedRow;
import org.apache.spark.mllib.linalg.distributed.IndexedRowMatrix;
import org.apache.spark.mllib.linalg.distributed.MatrixEntry;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import com.google.common.collect.Iterables;

import rdfanalyzer.spark.Service;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

public class Ranking {
	
	/*
	 * We perform 4 steps. 
	 * Step 1: We get the masterData in the following format --> key,([nodeNames],pj,1/n)
	 * Step 2: We get the References in the following format --> key,(pj,1/n)
	 * Step 3: We get the combined data in this format       --> key,([nodenames],[[1/n],[pjs]],1/n,pj)
	 * Step 4: We get the masterData with new pjs            --> key,([nodeNames],pj,1/n)
	 * 
	 * After step 4, we see the data is in the same format. Hence we can go back to step 1 to perform steps with
	 * the new pj achieved in step 4. We can repeat this process until the values of pj converges.
	 * 
	 */

	public static Map<String,Tuple2<Double,Double>> refData;
	public static JavaPairRDD<String, Tuple3<Iterable<String>,Double,Double>> newpjs; 
	public static JavaPairRDD<String, Tuple3<Iterable<String>,Double,Double>> masterData;
	public static JavaPairRDD<String, Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double>> reshuffledNodes;
	
	public static void CreateAdjacency(DataFrame records){
		
		JavaPairRDD<String,Iterable<String>> rows = records.select("subject","object").toJavaRDD().mapToPair(
				new PairFunction<Row,String,String>(){

					@Override
					public Tuple2<String, String> call(Row row) throws Exception {
						return new Tuple2<String, String>(row.getString(0), row.getString(1));
					}
					
				}).distinct().groupByKey().cache();
		

		/*
		 *  adding 2 more columns to key,[value] such that it becomes key,[[value],(1/count(value)),cij]
		 *  cij = suppose if node1 has a link to node2 and node3. Than cij for node1 = 2.
		 */
		 
		masterData = rows.mapValues(new Function<Iterable<String>,Tuple3<Iterable<String>,Double,Double>>() {
	        @Override
	        public Tuple3<Iterable<String>,Double,Double> call(Iterable<String> b) {
	        	double listSize = (double)Iterables.size(b);
	        	return new Tuple3<Iterable<String>,Double,Double>(b,listSize,1/listSize);
	        }
	      });
		
		

		/*
		 *  We are saving here String,<Double,Double> so that we can save
		 * 	NodeName,< pj , 1/n > values in this format
		 */
        refData = new HashMap<String,Tuple2<Double,Double>>();
        masterData.collect().forEach(line -> refData.put(line._1, new Tuple2(line._2._2(),line._2._3())));
	    

        System.out.println(" --- Final Data in Step 2 --- ");

        reshuffledNodes = reshuffleFromMasterData(masterData,refData);

        // <key,[[nodeNames], [node 1/n's], nodepjs]>
        newpjs  = CalculateNewPjs(reshuffledNodes);
      
        // update the new pjs for references
        Map<String,Double> refData2 = new HashMap<String,Double>();
        newpjs.collect().forEach(line -> refData2.put(line._1, line._2._3()));
        
        
        
	}
	
	
	
	// [ [nodes],[ [1/n],[pjs] ] ]
	public static JavaPairRDD<String, Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double>> reshuffleFromMasterData(
			JavaPairRDD<String, Tuple3<Iterable<String>,Double,Double>> masterData,
			Map<String,Tuple2<Double,Double>> refData){
		
        return masterData.mapValues(new Function<Tuple3<Iterable<String>,Double,Double>,Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double>>(){

        	@Override
			public Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double> call(Tuple3<Iterable<String>,Double, Double> line)
					throws Exception {
        		// key,([names],Tuple2[ [1/n's],[pj's] ],1/n,pj)
        		return new Tuple4(line._1(),getPointsToN(refData, line._1()),line._2(),line._3());
			}
        });
	}

	public static JavaPairRDD<String, Tuple2<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>>> reshuffleFromMasterDataLater(
			JavaPairRDD<String, Tuple3<Iterable<String>,ArrayList<Double>,Double>> masterData,
			Map<String,Double> refData2){

			
		return null;
	}

	// [[nodes],[1/n],newpjs]
	public static JavaPairRDD<String, Tuple3<Iterable<String>,Double,Double>> CalculateNewPjs(
			JavaPairRDD<String, Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double>> reshuffledNodes){
		
		return reshuffledNodes.mapValues(new Function<Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double>,  Tuple3<Iterable<String>,Double,Double>>(){

			@Override
			public  Tuple3<Iterable<String>,Double,Double> call(Tuple4<Iterable<String>,Tuple2<ArrayList<Double>,ArrayList<Double>>,Double,Double> records)
					throws Exception {
				
				Tuple2<ArrayList<Double>, ArrayList<Double>> tuple =  records._2();
				
				double finalSum = 0;
				
				// here we're summing the pj*1/n values.
				for(int i=0;i<tuple._1.size();i++){
					finalSum += tuple._1.get(i) * tuple._1.get(i);
				}
				
				finalSum = (finalSum*0.85)+0.15;
						
				
				return new Tuple3(records._1(),finalSum,records._4());
			}
	    	  
	      });
	}
	
	public static void ChangeReferencesForNewPjs(){
		
	}
	
	
    public static Tuple2<ArrayList<Double>,ArrayList<Double>> getPointsToN(Map<String, Tuple2<Double,Double>> refData, Iterable<String> pointsTo){
        ArrayList<Double> nodesOneByN = new ArrayList<Double>();
        ArrayList<Double> nodesPj = new ArrayList<Double>();
        for(String node: pointsTo)
            if(refData.containsKey(node)){
            	
            	/*
            	 * We have Tuple2 = NodeName, < pj , 1/n >
            	 * _1 = pj
            	 * _2 = 1/n
            	 * 
            	 */
            	nodesPj.add(refData.get(node)._1);
            	nodesOneByN.add(refData.get(node)._2);
            }
        return new Tuple2<ArrayList<Double>,ArrayList<Double>>(nodesOneByN,nodesPj);
    }
}
