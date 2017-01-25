package ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.mapred.lib.HashPartitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import rdfanalyzer.spark.Service;
import scala.Tuple2;
import scala.Tuple3;

public class testing {

	public static List<Tuple2<String,String>> list = new ArrayList<>();
	
	public static void doTest(DataFrame records){

//		createData();
		JavaPairRDD<String,String> counters = records.select("subject","object").toJavaRDD().mapToPair(
				new PairFunction<Row,String,String>(){

					@Override
					public Tuple2<String, String> call(Row row) throws Exception {
						return new Tuple2<String, String>(row.getString(0), row.getString(1));
					}
				// this can be optimized if we use reduceByKey instead of groupByKey
		});
				
//		JavaPairRDD<String,String> counters = Service.sparkCtx().parallelizePairs(list);
//		GroupByKeyTest(counters);
		
		// this gives us object,[subject] from object,subject. Or we can say key,[names]
		JavaPairRDD<String,Tuple3<ArrayList<String>,Double,Double>> list =  CombinerOutGoingEdgesWrtKey(counters);
		
		
		// this gives us key,(name,pj,1/n), note that here, we convert keys to value and values(first flat them) to keys.
		JavaPairRDD<String,Tuple3<String,Double,Double>> flattedPair = PerformOperationReshuffle(list);	

		
		// this will give us key,([names],[pj],[1/n])
		JavaPairRDD<String,Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> shuffledwithnumbers = CombinerOutGoingToIncoming(flattedPair).cache();
		
		JavaPairRDD<String,Double> pairedrdd = null;

		for(int i = 0 ; i< 10; i++){
		// here we created the new pjs by multiplying the values.
			
		pairedrdd = returnNewPjsForKeys(shuffledwithnumbers);
		if(i == 0){
			break;
		}
		
		
		JavaPairRDD<String,Tuple2<Tuple2<String,Double>,Double>> pair = shuffledwithnumbers.flatMapToPair(new PairFlatMapFunction<Tuple2<String,Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>, String, Tuple2<String,Double>>() {

			@Override
			public Iterable<Tuple2<String, Tuple2<String,Double>>> call(
					Tuple2<String, Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> arg0)
					throws Exception {
				
				
				List<Tuple2<String, Tuple2<String,Double>>> results = new ArrayList<>();
				
				for(int i=0;i<arg0._2._1().size();i++){
					
					results.add(new Tuple2<String,Tuple2<String,Double>>(arg0._2._1().get(i),new Tuple2(arg0._1,arg0._2._3().get(i))));
				}
				
				return results;
			}
		}).join(pairedrdd);

		// key,(name,pj,1/n)
		 JavaPairRDD<String, Tuple3<String, Double, Double>> doo = pair.mapToPair(new PairFunction<Tuple2<String,Tuple2<Tuple2<String,Double>,Double>>, String, Tuple3<String,Double,Double>>() {

			@Override
			public Tuple2<String, Tuple3<String, Double, Double>> call(
					Tuple2<String, Tuple2<Tuple2<String, Double>, Double>> arg0) throws Exception {

				return new Tuple2<String, Tuple3<String, Double, Double>>(arg0._2._1._1,new Tuple3(arg0._1,arg0._2._1._2,arg0._2._2));
			}
		});
		 
		 shuffledwithnumbers = performFinalCombiner(doo);
		}		
		System.out.println("donedonedonedonedonedonedone");
		
		List<Tuple2<Double, String>> importantNodes = GetTopNNodes(pairedrdd,5);
		for(Tuple2<Double, String> tuple:importantNodes){
			System.out.println("The important node is "+tuple._2);
		}
		
	}
	
	public static List<Tuple2<Double, String>> GetTopNNodes(JavaPairRDD<String,Double> pairedrdd,int NImportantNodes){
		return pairedrdd.mapToPair(new PairFunction<Tuple2<String,Double>, Double, String>() {

			@Override
			public Tuple2<Double, String> call(Tuple2<String, Double> arg0) throws Exception {
				// TODO Auto-generated method stub
				return new Tuple2(arg0._2,arg0._1);
			}
		}).cache().sortByKey().take(NImportantNodes);
	}
	
	public static JavaPairRDD<String,Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> performFinalCombiner(JavaPairRDD<String, Tuple3<String, Double, Double>> finalCombiner){
		
		Function<Tuple3<String, Double, Double>,Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>> createCombiner = new Function<Tuple3<String, Double, Double>, Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>() {
			
			// this is called when we face the key for the first time. So we initialize our arraylist w.r.t key.
			@Override
			public Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>> call(Tuple3<String, Double, Double> arg0) throws Exception {
				
				ArrayList names = new ArrayList<String>();
				ArrayList one_by_n = new ArrayList<Double>();
				ArrayList pjs = new ArrayList<Double>();

				names.add(arg0._1());
				pjs.add(arg0._2());
				one_by_n.add(arg0._3());
			
				
				return new Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>(names,pjs,one_by_n);
			}
		};

		Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,
		Tuple3<String, Double, Double>,
		Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>> merger = new Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>, Tuple3<String, Double, Double>, Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>() {
			
			// this is called when we face the key next time. So we add an item to the arraylist of that key.
			@Override
			public Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>> call(
					Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>> existingListForSameKey,
					Tuple3<String, Double, Double> newValueForSameKey) throws Exception {
				
				existingListForSameKey._1().add(newValueForSameKey._1());
				existingListForSameKey._2().add(newValueForSameKey._2());
				existingListForSameKey._3().add(newValueForSameKey._3());
				
				return existingListForSameKey;
				
			}
		};
		
		
		Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>
		mergeCombiners = new Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,
				Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,
				Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>(){


			@Override
			public Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> call(
					Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> part1,
					Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> part2) throws Exception {
				
				part1._1().addAll(part2._1());
				part1._2().addAll(part2._2());
				part1._3().addAll(part2._3());
				
				return new Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>(part1._1(),part1._2(),part1._3());
			}
			
		};
//		finalCombiner.combineByKey(createCombiner, merger, mergeCombiners).foreach(line->System.out.println(line));
		return finalCombiner.combineByKey(createCombiner, merger, mergeCombiners);
	}
	
	public static JavaPairRDD<String,Double> returnNewPjsForKeys(JavaPairRDD<String,Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> shuffledwithnumbers){
		return shuffledwithnumbers.mapValues(new Function<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>, Double>() {

			@Override
			public Double call(
					Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> line) throws Exception {
				
				double newrank = 0.0;
				for(int i=0;i<line._2().size();i++){
					newrank += line._2().get(i)*line._3().get(i);
				}
				
				newrank = (newrank*0.85)+0.15;
				
				return newrank;
			}
		}).cache();
	}
	

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
		
//		counters.combineByKey(createCombiner, merger, mergeCombiners).foreach(line->System.out.println(line));
		return counters.combineByKey(createCombiner, merger, mergeCombiners);
	}

	
	public static JavaPairRDD<String, Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> CombinerOutGoingToIncoming(JavaPairRDD<String,Tuple3<String,Double,Double>> counters){
		
		Function<Tuple3<String,Double,Double>,Tuple3<ArrayList<String>,ArrayList<Double>, ArrayList<Double>>> createCombiner = new Function<Tuple3<String,Double,Double>, Tuple3<ArrayList<String>,ArrayList<Double>, ArrayList<Double>>>() {
			
			// this is called when we face the key for the first time. So we initialize our arraylist w.r.t key.

			@Override
			public Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> call(Tuple3<String, Double, Double> line)
					throws Exception {
				
				ArrayList<String> names = new ArrayList<>();
				ArrayList<Double> one_by_n = new ArrayList<>();
				ArrayList<Double> pj = new ArrayList<>();
				
				

				names.add(line._1());
				pj.add(line._2());
				one_by_n.add(line._3());
				
				return new Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>(names,pj,one_by_n);
			}
		};
		
		Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>,
				  Tuple3<String,Double,Double>,
				  Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>> merger = new Function2<Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>, Tuple3<String,Double,Double>, Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>>() {
					
					@Override
					public Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> call(
							Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> prevRecordForSameKey, Tuple3<String, Double, Double> newRecordForSameKey)
							throws Exception {
						
						ArrayList<String> names = prevRecordForSameKey._1();
						ArrayList<Double> one_by_n = prevRecordForSameKey._3();
						ArrayList<Double> pj = prevRecordForSameKey._2();

						names.add(newRecordForSameKey._1());
						one_by_n.add(newRecordForSameKey._3());
						pj.add(newRecordForSameKey._2());
						
						return new Tuple3<ArrayList<String>,ArrayList<Double>,ArrayList<Double>>(names,pj,one_by_n);
					}
				};

			
		
		Function2<Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>
		,Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>,
		Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>> mergeCombiners = new Function2<Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>,
				Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>,
				Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>>(){


			@Override
			public Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> call(
					Tuple3<ArrayList<String>,ArrayList<Double>, ArrayList<Double>> partition1,
					Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>> partition2) throws Exception {
				
				ArrayList<String> names = partition1._1();
				ArrayList<Double> one_by_n = partition1._3();
				ArrayList<Double> pj = partition1._2();

				one_by_n.addAll(partition2._3());
				names.addAll(partition2._1());
				pj.addAll(partition2._2());
				
				return new Tuple3<ArrayList<String>, ArrayList<Double>, ArrayList<Double>>(names,pj,one_by_n);
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
