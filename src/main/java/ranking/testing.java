package ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.DataFrame;

import rdfanalyzer.spark.Service;
import scala.Tuple2;
import scala.Tuple3;

public class testing {

	public static List<Tuple2<String,String>> list = new ArrayList<>();
	
	public static void doTest(){

		createData();

		JavaPairRDD<String,String> counters = Service.sparkCtx().parallelizePairs(list);
//		GroupByKeyTest(counters);
		
		// this gives us object,[subject] from object,subject. Or we can say key,[names]
		JavaPairRDD<String,Tuple3<ArrayList<String>,Double,Double>> list =  CombinerOutGoingEdgesWrtKey(counters);
		
		
		// this gives us key,(name,pj,1/n), note that here, we convert keys to value and values(first flat them) to keys.
		JavaPairRDD<String,Tuple3<String,Double,Double>> flattedPair = PerformOperationReshuffle(list);	

		
		// this will give us key,([names],[1/n],[pj*1/n])
		JavaPairRDD<String,Tuple3<ArrayList<String>, Double, ArrayList<Double>>> shuffledwithnumbers = CombinerOutGoingToIncoming(flattedPair);

		
		// here we just finalized new pjs by converting [pj*1/n] to [(pj*1/n)*0.85+0.15]
		shuffledwithnumbers = shuffledwithnumbers.mapValues(new Function<Tuple3<ArrayList<String>,Double,ArrayList<Double>>, Tuple3<ArrayList<String>,Double,ArrayList<Double>>>() {

			@Override
			public Tuple3<ArrayList<String>, Double, ArrayList<Double>> call(
					Tuple3<ArrayList<String>, Double, ArrayList<Double>> line) throws Exception {
				return new Tuple3<ArrayList<String>,Double,ArrayList<Double>>(line._1(),(line._2()*0.85)+0.15,line._3());
			}
		});
		
		shuffledwithnumbers.foreach(line->System.out.println(line));
		
		

		// now multiply all 1/n * pj values with 0.85 and add them to 0.15.

		
		
//		// this combines the object,subject to object,[subject]
//		JavaPairRDD<String,ArrayList<String>> newlyCombined = CombinerOutGoingEdgesWrtKey(flattedPair);
		
	}
	
//	public static JavaPairRDD<String,String> PerformOperationReshuffle(JavaPairRDD<String,Tuple3<ArrayList<String>,Double,Double>> list){
//		return list.flatMapToPair(new PairFlatMapFunction<Tuple2<String,ArrayList<String>>, String,String>() {
//
//			@Override
//			public Iterable<Tuple2<String, String>> call(Tuple2<String, ArrayList<String>> arg0) throws Exception {
//				// TODO Auto-generated method stub
//				List<Tuple2<String, String>> results = new ArrayList<>();
//				
//				for(String item:arg0._2){
//					results.add(new Tuple2<String,String>(item,arg0._1));
//				}
//				
//				return results;
//			}
//		});
//
//	}

	public static JavaPairRDD<String,Tuple3<String,Double,Double>> PerformOperationReshuffle(JavaPairRDD<String,Tuple3<ArrayList<String>,Double,Double>> list){
		return list.flatMapToPair(new PairFlatMapFunction<Tuple2<String,Tuple3<ArrayList<String>,Double,Double>>, String, Tuple3<String,Double,Double>>() {

			@Override
			public Iterable<Tuple2<String, Tuple3<String,Double,Double>>> call(Tuple2<String, Tuple3<ArrayList<String>, Double, Double>> flattedData)
					throws Exception {

				List<Tuple2<String, Tuple3<String,Double,Double>>> results = new ArrayList<>();
				
				
				/*
				 * flattedData._1 = values
				 * flattedData._2._1() = keys
				 * flattedData._2._2() = pj
				 * flattedData._2._3() = 1/n
				 */
				for(String item:flattedData._2._1()){
					results.add(new Tuple2<String,Tuple3<String,Double,Double>>(item,new Tuple3(flattedData._1,flattedData._2._2(),flattedData._2._3())));
				}
				return results;
			}
		});
		
	}

//	public static JavaPairRDD<String,Iterable<String>> GroupByKeyTest(JavaPairRDD<String,String> counters){
//		
//		counters.groupByKey(4).distinct().cache().foreach(line->System.out.println(line));
//		return counters.groupByKey(4).distinct().cache();
//	}
	

	public static JavaPairRDD<String,Tuple3<ArrayList<String>,Double,Double>> CombinerOutGoingEdgesWrtKey(JavaPairRDD<String,String> counters){
		
		Function<String,Tuple3<ArrayList<String>,Double,Double>> createCombiner = new Function<String, Tuple3<ArrayList<String>,Double,Double>>() {
			
			// this is called when we face the key for the first time. So we initialize our arraylist w.r.t key.
			@Override
			public Tuple3<ArrayList<String>,Double,Double> call(String arg0) throws Exception {
				
				ArrayList list = new ArrayList<String>();
				list.add(arg0);
				
				return new Tuple3(list,1.0,1.0/1.0);
			}
		};
		
		Function2<Tuple3<ArrayList<String>,Double,Double>,String,Tuple3<ArrayList<String>,Double,Double>> merger = new Function2<Tuple3<ArrayList<String>,Double,Double>, String, Tuple3<ArrayList<String>,Double,Double>>() {
			
			// this is called when we face the key next time. So we add an item to the arraylist of that key.
			@Override
			public Tuple3<ArrayList<String>,Double,Double> call(Tuple3<ArrayList<String>,Double,Double> existingListForSameKey, String newValueForSameKey) throws Exception {
				ArrayList newlist = existingListForSameKey._1();
				newlist.add(newValueForSameKey);
				
				double sizeofList = newlist.size();
				
				return new Tuple3<ArrayList<String>,Double,Double>(newlist,sizeofList,1.0/sizeofList);
			}
		};
		
		Function2<Tuple3<ArrayList<String>,Double,Double>,Tuple3<ArrayList<String>,Double,Double>,Tuple3<ArrayList<String>,Double,Double>> mergeCombiners = new Function2<Tuple3<ArrayList<String>,Double,Double>,Tuple3<ArrayList<String>,Double,Double>,Tuple3<ArrayList<String>,Double,Double>>(){

			// this is called to merge different arraylists for the same key being merged at different partitions.
			@Override
			public Tuple3<ArrayList<String>,Double,Double> call(Tuple3<ArrayList<String>,Double,Double> tuplePartition1, Tuple3<ArrayList<String>,Double,Double> tuplePartition2) throws Exception {
				
				ArrayList<String> newlist = tuplePartition1._1();
				
				newlist.addAll(tuplePartition2._1());
				
				double sizeofList = newlist.size();

				return new Tuple3(newlist,sizeofList,1.0/sizeofList);
			}
			
		};
		
		counters.combineByKey(createCombiner, merger, mergeCombiners).foreach(line->System.out.println(line));
		return counters.combineByKey(createCombiner, merger, mergeCombiners);
	}

	
	public static JavaPairRDD<String,Tuple3<ArrayList<String>, Double, ArrayList<Double>>> CombinerOutGoingToIncoming(JavaPairRDD<String,Tuple3<String,Double,Double>> counters){
		
		Function<Tuple3<String,Double,Double>,Tuple3<ArrayList<String>,Double, ArrayList<Double>>> createCombiner = new Function<Tuple3<String,Double,Double>, Tuple3<ArrayList<String>,Double, ArrayList<Double>>>() {
			
			// this is called when we face the key for the first time. So we initialize our arraylist w.r.t key.

			@Override
			public Tuple3<ArrayList<String>, Double, ArrayList<Double>> call(Tuple3<String, Double, Double> line)
					throws Exception {
				
				ArrayList<String> names = new ArrayList<>();
				ArrayList<Double> one_by_n = new ArrayList<>();
				Double pj_into_1_by_n = 0.0;
				
				

				names.add(line._1());
				pj_into_1_by_n = line._2()*line._3();
				one_by_n.add(line._3());
				
				return new Tuple3<ArrayList<String>, Double, ArrayList<Double>>(names,pj_into_1_by_n,one_by_n);
			}
		};
		
		Function2<Tuple3<ArrayList<String>,Double,ArrayList<Double>>,
				  Tuple3<String,Double,Double>,
				  Tuple3<ArrayList<String>,Double,ArrayList<Double>>> merger = new Function2<Tuple3<ArrayList<String>,Double,ArrayList<Double>>, Tuple3<String,Double,Double>, Tuple3<ArrayList<String>,Double,ArrayList<Double>>>() {
					
					@Override
					public Tuple3<ArrayList<String>, Double, ArrayList<Double>> call(
							Tuple3<ArrayList<String>, Double, ArrayList<Double>> prevRecordForSameKey, Tuple3<String, Double, Double> newRecordForSameKey)
							throws Exception {
						
						ArrayList<String> names = prevRecordForSameKey._1();
						ArrayList<Double> one_by_n = prevRecordForSameKey._3();
						Double pj_into_1_by_n = prevRecordForSameKey._2();

						names.add(newRecordForSameKey._1());
						one_by_n.add(newRecordForSameKey._3());
						pj_into_1_by_n = (newRecordForSameKey._2()*newRecordForSameKey._3()) + prevRecordForSameKey._2();
						
						return new Tuple3<ArrayList<String>,Double,ArrayList<Double>>(names,pj_into_1_by_n,one_by_n);
					}
				};

			
		
		Function2<Tuple3<ArrayList<String>, Double, ArrayList<Double>>
		,Tuple3<ArrayList<String>, Double, ArrayList<Double>>,
		Tuple3<ArrayList<String>, Double, ArrayList<Double>>> mergeCombiners = new Function2<Tuple3<ArrayList<String>, Double, ArrayList<Double>>,
				Tuple3<ArrayList<String>, Double, ArrayList<Double>>,
				Tuple3<ArrayList<String>, Double, ArrayList<Double>>>(){

			// this is called to merge different arraylists for the same key being merged at different partitions.
//			@Override
//			public Tuple3<ArrayList<String>,Double,Double> call(Tuple3<ArrayList<String>,Double,Double> tuplePartition1, Tuple3<ArrayList<String>,Double,Double> tuplePartition2) throws Exception {
//				
//				ArrayList<String> newlist = tuplePartition1._1();
//				
//				newlist.addAll(tuplePartition2._1());
//				
//				double sizeofList = newlist.size();
//
//				return new Tuple3(newlist,sizeofList,1.0/sizeofList);
//			}

			@Override
			public Tuple3<ArrayList<String>, Double, ArrayList<Double>> call(
					Tuple3<ArrayList<String>, Double, ArrayList<Double>> partition1,
					Tuple3<ArrayList<String>, Double, ArrayList<Double>> partition2) throws Exception {
				
				ArrayList<String> names = partition1._1();
				ArrayList<Double> one_by_n = partition1._3();
				Double pj_into_1_by_n = partition1._2() + partition2._2();

				one_by_n.addAll(partition2._3());
				names.addAll(partition2._1());
				
				return new Tuple3<ArrayList<String>, Double, ArrayList<Double>>(names,pj_into_1_by_n,one_by_n);
			}
			
		};
		
		return counters.combineByKey(createCombiner, merger, mergeCombiners);
	}

	
	public static void createData(){
		
		list.add(new Tuple2<String,String>("node1","node2"));
		list.add(new Tuple2<String,String>("node1","node3"));
		list.add(new Tuple2<String,String>("node1","node4"));
		list.add(new Tuple2<String,String>("node2","node1"));
		list.add(new Tuple2<String,String>("node2","node5"));
		list.add(new Tuple2<String,String>("node3","node1"));
		list.add(new Tuple2<String,String>("node3","node2"));
		list.add(new Tuple2<String,String>("node3","node4"));
		list.add(new Tuple2<String,String>("node3","node5"));
		list.add(new Tuple2<String,String>("node4","node1"));
		list.add(new Tuple2<String,String>("node4","node2"));
		list.add(new Tuple2<String,String>("node5","node2"));
		list.add(new Tuple2<String,String>("node5","node3"));
		list.add(new Tuple2<String,String>("node5","node4"));
	}
}
