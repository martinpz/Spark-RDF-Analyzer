package ranking;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import rdfanalyzer.spark.Configuration;
import rdfanalyzer.spark.ConnAdapter;
import rdfanalyzer.spark.Service;
import scala.Function1;
import scala.Tuple2;

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

}
