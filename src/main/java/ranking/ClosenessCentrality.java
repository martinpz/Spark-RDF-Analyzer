package ranking;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import rdfanalyzer.spark.ConnAdapter;
import scala.Tuple2;
import scala.Tuple3;

public class ClosenessCentrality implements Serializable {

	/*
	 * Step 1: Add the selected node to visited Array.
	 * Step 2: Get the objects of the selected node ( subject ).
	 * Step 3: Get the objects into a separate list.
	 * Step 4: Add the objects of the selected node to visited Array.
	 * Step 5: Get the objects of the objects.
	 * 
	 * Repeat from step 2.
	 * 
	 */
	
	
	List<String> visited = new ArrayList<>();

	// this is mentioned in step 4
	List<String> nextQueryArray = new ArrayList<>();
	
	private int sum = 0;
	private final int HOPS = 3; 
	
	public static ConnAdapter objAdapter = new ConnAdapter();
	public static DataFrame graphFrame,allSubjectsDF;

	private JavaPairRDD<String,String> objectsOfSubjects;
	
	private String selectedNode;

	
	
	
	public void calculateCloseness(DataFrame record,String node) throws Exception{

		visited.add(node);

		nextQueryArray.add(node);

		for(int i = 0; i < HOPS; i++){

			// Step 2
			objectsOfSubjects = getObjectsOfSubjects(record);

			// Step 3
			nextQueryArray = new ArrayList<>();
			nextQueryArray = objectsOfSubjects.values().collect();


			sum += (i+1) * nextQueryArray.size();

			System.out.println("working step 1");
			nextQueryArray = getUniqueValues(nextQueryArray,visited);
			
			// Step 4
			visited.addAll(nextQueryArray);
		}
		
		double closeness = ((double)1/(double)sum);
		System.out.println("the sum is = "+ sum);
		System.out.printf("dexp: %f\n", round(closeness,15));
	}
	
	/*
	 * This method will return us the values which are not contained in visited list.
	 * Because maybe the objects of a subject have some nodes which are already visited.
	 * Hence it is useless to query their objects again.
	 */
	private List<String> getUniqueValues(List<String> objects, List<String> alreadyVisited){

		List<String> union = new ArrayList<String>(alreadyVisited);
		union.addAll(objects);

		List<String> intersection = new ArrayList<String>(alreadyVisited);
		intersection.retainAll(objects);

		List<String> symmetricDifference = new ArrayList<String>(union);
		symmetricDifference.removeAll(intersection);

		return symmetricDifference;
	}


	// Get objects of unique subjects
	private JavaPairRDD<String,String> getObjectsOfSubjects(DataFrame records) throws Exception{

		return records.
				filter(records.col("subject").isin(nextQueryArray.stream().toArray())).toJavaRDD().mapToPair(
		new PairFunction<Row,String,String>(){

			@Override
			public Tuple2<String, String> call(Row row) throws Exception {
				return new Tuple2<String, String>(row.getString(0), row.getString(1));
			}
		});
	}

	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**********************************************************************************************************************/
	/**************************************************Under Development***************************************************/
	/**********************************************************************************************************************/
	/**********************************************************************************************************************/
	
	
	
	private List<String> listToQuery = new ArrayList<>();
	private List<String> alreadyVisitedNodes = new ArrayList<>();
	private int ITERATIONS = 1;
	
	


	/*
	 * Here since we will already have the closeness for neighbour hence we just need
	 * to calculate the closeness of it's neighbour hence that node itself won't be added.
	 * 
	 * 
	 * Here @param2 is the node which is currently shown in frontend to the user. So we'll
	 * get the 3 hops closeness of the neighbour of it. While in the above function we 
	 * also added the node from which we are starting.
	 */
	private void calculateClosenessArray(DataFrame record,String node,boolean firstTime) throws Exception{

		listToQuery = new ArrayList<>();
		
		listToQuery.add(node);

		if(firstTime){
			// Add current node and it's neighbors
			listToQuery.addAll(this.getObjectsOfSubjects(listToQuery,record).values().collect());
		}
		else{

			// Add only neighbors of current node
			listToQuery = this.getObjectsOfSubjects(listToQuery,record).values().collect();
		}
		
		
		alreadyVisitedNodes.addAll(listToQuery);
		
		/*
		 *  now get the objects of all the subjects in listToQuery Array, 
		 *  than reduceByKey to key,[values],countOfValues ( values = objects )
		 *  
		 *  This happened for hop 1.
		 */

		JavaPairRDD<String,Tuple2<List<String>, Double>> reducedResults = reduceResults(this.getObjectsOfSubjects(listToQuery,record));
	}
	
	
	private JavaPairRDD<String,String> getObjectsOfSubjects(List<String> subjects,DataFrame records){
		return records.
				filter(records.col("subject").isin(subjects.stream().toArray())).toJavaRDD().mapToPair(
		new PairFunction<Row,String,String>(){

			@Override
			public Tuple2<String, String> call(Row row) throws Exception {
				return new Tuple2<String, String>(row.getString(0), row.getString(1));
			}
		});
	}
	
	
	// takes the objects of subjects and return key, [ objects ] , countOfObjects.
	public JavaPairRDD<String,Tuple2<List<String>, Double>> reduceResults(JavaPairRDD<String, String> objectsOfSubjects){
		
		
		Function< String,Tuple2<List<String>, Double>> createCombiner = new Function<String,Tuple2<List<String>, Double>>() {
			
			// this is called when we face the key for the first time. So we initialize our arraylist w.r.t key.
			@Override
			public Tuple2<List<String>, Double> call( String line) throws Exception {

				/*
				 *  add the new item of key into an array. This will give us key, [ value ] . 
				 *  Here value is the object while is the subject.
				 */
				
				List<String> objects = new ArrayList<String>();
				objects.add(line);
				
				return new Tuple2<List<String>,Double>(objects,1.0);
			}
		};

		Function2<Tuple2<List<String>, Double>,
		String,
		Tuple2<List<String>, Double>> merger = new Function2<Tuple2<List<String>, Double>,
				String,
				Tuple2<List<String>, Double>>() {
			
			// this is called when we face the key next time. So we add an item to the arraylist of that key.
			@Override
			public Tuple2<List<String>, Double> call(
					Tuple2<List<String>, Double> existingListForSameKey,
					String newValueForSameKey) throws Exception {
				
				existingListForSameKey._1().add(newValueForSameKey);
				
				// one more neighbour(object) found for the same key.
				double sizeOfList = existingListForSameKey._2;

				sizeOfList++;
				
				return new Tuple2<List<String>, Double>(existingListForSameKey._1, sizeOfList);
				
			}
		};
		

		Function2<Tuple2<List<String>, Double>,Tuple2<List<String>, Double>,Tuple2<List<String>, Double>>
		mergeCombiners = new Function2<Tuple2<List<String>, Double>,
				Tuple2<List<String>, Double>,
				Tuple2<List<String>, Double>>(){


			@Override
			public Tuple2<List<String>, Double> call(
					Tuple2<List<String>, Double> part1,
					Tuple2<List<String>, Double> part2) throws Exception {
				
				part1._1().addAll(part2._1());
				
				// total items in the list for the same key.
				double newSize = part1._2 + part2._2;
				
				return new Tuple2<List<String>, Double>(part1._1(),newSize);
			}
			
		};
//		finalCombiner.combineByKey(createCombiner, merger, mergeCombiners).foreach(line->System.out.println(line));
		return objectsOfSubjects.combineByKey(createCombiner, merger, mergeCombiners);
	}

	
	
	
	/**********************************************************************************************************************/
	/**************************************************Under Development***************************************************/
	/**********************************************************************************************************************/
	/**********************************************************************************************************************/
}