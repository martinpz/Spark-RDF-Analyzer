package ranking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rdfanalyzer.spark.Service;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.graphx.Edge;
import org.apache.spark.sql.DataFrame;

import breeze.linalg.sum;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

public class oldTests2 implements Serializable{
	
	public List<Tuple2<String,String>> vertices = new ArrayList<>();
	public List<Edge<String>> edges = new ArrayList<>();
	
	public static boolean firstTime = true;
	
	public JavaPairRDD<String,Tuple4<List<String>,Integer,Integer,Integer>> breakloopChecker ;
	public JavaPairRDD<String,Tuple4<List<String>,Integer,Integer,Integer>> mappedValues ;

	public int sumOfCOlorColumn = 0;
	public int lastsumOfCOlorColumn = 0;


	public void test(DataFrame records) throws Exception{
		
//		JavaPairRDD<String,String> counters = records.select("subject","object").toJavaRDD().mapToPair(
//				new PairFunction<Row,String,String>(){
//
//					@Override
//					public Tuple2<String, String> call(Row row) throws Exception {
//						return new Tuple2<String, String>(row.getString(0), row.getString(1));
//					}
//				// this can be optimized if we use reduceByKey instead of groupByKey
//		});

		createVertices();
		
		JavaPairRDD<String,String> distData = Service.sparkCtx().parallelizePairs(vertices);

		
		JavaPairRDD<String, Tuple4<List<String>,Integer,Integer, Integer>> adjacencyMatrix = reduceToAdjacencyMatrix(distData);

		System.out.println("Count hai baby" + distData.count());
		
		
//		adjacencyMatrix.mapToPair(new PairFunction<Tuple2<String,
//				Tuple4<List<String>,Integer,Integer, Integer>>, 
//				String, 
//				Tuple4<List<String>,Integer,Integer, Integer>>() {
//
//			@Override
//			public Tuple2<String, Tuple4<List<String>,Integer,Integer, Integer>> call(
//					Tuple2<String, Tuple4<List<String>,Integer,Integer, Integer>> line) throws Exception {
//					
//				System.out.println("lakhdilanat");
//				applyBFSForNode(line._1,adjacencyMatrix);
//				
//				
//				return line;
//			}
//		}).foreach(x -> System.out.println(x));;	
		
		for(Tuple2<String,String> v:vertices){
			applyBFSForNode(v._1, adjacencyMatrix);
		}
	}	
	
	
	
	private boolean breakloop(JavaPairRDD<String, Tuple4<List<String>,Integer,Integer, Integer>> adjacencyMatrix){
		
		
		sumOfCOlorColumn = adjacencyMatrix.mapValues(new Function<Tuple4<List<String>,Integer,Integer,Integer>, Integer>() {

			@Override
			public Integer call(Tuple4<List<String>, Integer, Integer, Integer> arg0) throws Exception {
				return arg0._3();
			}
		}).values().collect().stream().mapToInt(Integer::intValue).sum();
		
		System.out.println("Sum of breakloop = " +sumOfCOlorColumn);
		
		if(lastsumOfCOlorColumn == sumOfCOlorColumn){
			return true;
		}
		
		lastsumOfCOlorColumn = sumOfCOlorColumn;

		return false;
	}
	
	/*
	 * Convert <Key,[Neighbors]> To <key, Tuple4 < [Neighbors] , Distance, Color, ShortestPaths >
	 */
	
	private void applyBFSForNode(final String sourceNode, JavaPairRDD<String, Tuple4<List<String>,Integer,Integer, Integer>> adjacencyMatrixx){

		/*
		 *  We won't have any grey nodes in the initial dataset hence we'll never go inside the if condition defined below.
		 *  So our initial grey node is the sourceNode. Hence this check will only run for the first time.
		 */
		
		firstTime = true;
		
		/*
		 *  2 = black color. So if all the items are 2 i.e black. Than we can break. Hence our breakPoint is
		 *  itemCount * 2. And once we reduce we will check if we get this value from our reducer than we'll break.
		 */

		
//		while(true){
			

			// break if all vertices are black.
//			if(breakPoint == currentPoint){
//				break;
//			}
			
			
			mappedValues = PerformBFSMapOperation(sourceNode,adjacencyMatrixx);

			adjacencyMatrixx = PerformBFSReduceOperation(mappedValues);
			
//			breakloopChecker = adjacencyMatrixx;
			
//			breakloop(breakloopChecker);
			
			adjacencyMatrixx.foreach(x->System.out.println(x));
			System.out.println("Made it to the iteration end");
//		}
		
		
		
	}
	
	private JavaPairRDD<String, Tuple4<List<String>, Integer, Integer, Integer>> PerformBFSReduceOperation(JavaPairRDD<String,Tuple4<List<String>,Integer,Integer,Integer>> mappedValues){
		
		Function<Tuple4<List<String>,Integer,Integer,Integer>,Tuple4<List<String>,Integer,Integer,Integer>> createCombiner 
						= new Function<Tuple4<List<String>,Integer,Integer,Integer>,Tuple4<List<String>,Integer,Integer,Integer>>() {
			
			@Override
			public Tuple4<List<String>,Integer,Integer,Integer> call(Tuple4<List<String>,Integer,Integer,Integer> line) throws Exception {
				return new Tuple4<List<String>,Integer,Integer,Integer>(line._1(),line._2(),line._3(),line._4());
			}
		};

		Function2<Tuple4<List<String>,Integer,Integer,Integer>,
		Tuple4<List<String>,Integer,Integer,Integer>,
		Tuple4<List<String>,Integer,Integer,Integer>> merger = new Function2<Tuple4<List<String>,Integer,Integer,Integer>,
				Tuple4<List<String>,Integer,Integer,Integer>,
				Tuple4<List<String>,Integer,Integer,Integer>>() {
			
			// this is called when we face the key next time. So we add an item to the arraylist of that key.

			@Override
			public Tuple4<List<String>, Integer, Integer, Integer> call(
					Tuple4<List<String>, Integer, Integer, Integer> previousKey,
					Tuple4<List<String>, Integer, Integer, Integer> newKey) throws Exception {

				/*
				 *  Step 1: Check if one of the vertices being reduced is black. If yes. Than take that vertex. Otherwise go to step2.
				 *  
				 *  Step 2: If one vertex is grey and the other is white. Take the distance of grey 
				 *  		while the neighbor of the one not null. Otherwise go to step3.
				 *  
				 *  Step 3: If the vertices are same, there colors are same, there 
				 *  		distances are same. Simply add 1 to the shortest paths.
				 */
				
				return getReducedData(previousKey,newKey);
			}
		};
		
		Function2<Tuple4<List<String>, Integer, Integer, Integer>,
		Tuple4<List<String>, Integer, Integer, Integer>,
		Tuple4<List<String>, Integer, Integer, Integer>> mergeCombiners = new Function2<Tuple4<List<String>, Integer, Integer, Integer>,
				Tuple4<List<String>, Integer, Integer, Integer>,
				Tuple4<List<String>, Integer, Integer, Integer>>(){


			@Override
			public Tuple4<List<String>, Integer, Integer, Integer> call(
					Tuple4<List<String>, Integer, Integer, Integer> arg0,
					Tuple4<List<String>, Integer, Integer, Integer> arg1) throws Exception {

				
				Tuple4<List<String>, Integer, Integer, Integer> finalReturn = getReducedData(arg0,arg1);
				
				return finalReturn;
			}
		};
		
		
		
//		mappedValues.combineByKey(createCombiner, merger, mergeCombiners).foreach(x->System.out.println(x));;
//		return null;
		return mappedValues.combineByKey(createCombiner, merger, mergeCombiners);
	}
	
	private static Tuple4<List<String>, Integer, Integer, Integer> getReducedData(
			Tuple4<List<String>, Integer, Integer, Integer> previousKey,
			Tuple4<List<String>, Integer, Integer, Integer> newKey){
		
		Tuple4<List<String>, Integer, Integer, Integer> result = null;
		
		/* Step 1 */
		if(previousKey._3() == 2){
			result = previousKey;
		}
		else if(newKey._3() == 2){
			result = newKey;
		}

		/* Step 2 */
		else if(newKey._3() == 1 && previousKey._3() == 0){
			result = new Tuple4<List<String>,Integer,Integer,Integer>(previousKey._1(),newKey._2(),newKey._3(),newKey._4());
		}
		else if(newKey._3() == 0 && previousKey._3() == 1){
			result = new Tuple4<List<String>,Integer,Integer,Integer>(newKey._1(),previousKey._2(),previousKey._3(),previousKey._4());
		}
		
		/* Step 3 */
		else if((newKey._3() == 1 && previousKey._3() == 1) && 
				(newKey._1() == null && previousKey._1() == null) &&
				(newKey._2() == previousKey._2())){

			result = new Tuple4<List<String>,Integer,Integer,Integer>(newKey._1(),newKey._2(),newKey._3(),newKey._4()+1);
		}

		return result;
	
	}
	
	
	private JavaPairRDD<String,Tuple4<List<String>,Integer,Integer,Integer>> PerformBFSMapOperation(final String sourceNode, JavaPairRDD<String, Tuple4<List<String>,Integer,Integer, Integer>> adjacencyMatrix){
		

		return adjacencyMatrix.flatMapToPair(new PairFlatMapFunction<Tuple2<String,Tuple4<List<String>,Integer,Integer,Integer>>, String, Tuple4<List<String>,Integer,Integer, Integer>>() {

			@Override
			public Iterable<Tuple2<String, Tuple4<List<String>,Integer,Integer, Integer>>> call(
					Tuple2<String, Tuple4<List<String>, Integer, Integer, Integer>> line)
					throws Exception {

				
				List<Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>>> results = new ArrayList<Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>>>();
				System.out.println("andar coming"+ line._1+" firstime="+ firstTime);
				// If this is a grey node. Go inside.
				if((line._2._3() == 1) || (firstTime && line._1.equals(sourceNode))){
					System.out.println("andar andar coming");
					
					firstTime = false;

					/*
					 * 	Step 1 . Convert this node to black.
					 *  Step 2 . Add the neighbors of this node as keys. 
					 */
					
					 
					/*
					 *	Step 1 
					 *  
					 *  For Tuple2
					 *  @param1: The original key for which this loop is called.
					 *  @param2: Tuple4 defined below.
					 *  
					 *  For Tuple4
					 *  @param1: same as original
					 *  @param2: same as original
					 *  @param3: we set this to 2 to mark this node as black(visited)
					 *  @param4: same as original
					 *  
					 */
					Tuple4<List<String>, Integer, Integer, Integer> currentNodeT4 = new Tuple4<List<String>, Integer, Integer, Integer>(line._2._1(), line._2._2(), 2, line._2._4());
					Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>> currentNodeT2 = new Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>>(line._1,currentNodeT4);

					results.add(currentNodeT2);

					
					// Step 2
					for(int i=0;i<line._2._1().size();i++){
						
						/*
						 *  Tuple2
						 *  @param1: one of the keys of neighbors. This means if the neighbor has 3 keys than we create 3 instances in the loop here.
						 *  @param2: Tuple4 defined below.
						 *  
						 *  Tuple4
						 *  @param1: make it null, since we don't know the childs of the exploded neighbors
						 *  @param2: Add 1 to the line._2._2() means , we add 1 more distance to the exploded grey field
						 *  @param3: assign it 1 because now this is a grey field. This should be expanded next.
						 *  @param4: Number of shortest path remains the same between sourceNode and this node.
						 *  
						 *  @param4 is only updated in the reduce Phase.
						 *  
						 */
						
						Tuple4<List<String>, Integer, Integer, Integer> neighborNodeT4 
								= new Tuple4<List<String>, Integer, Integer, Integer>(null, line._2._2()+1, 1, line._2._4());

						Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>> neighborNodeT2
						  		= new Tuple2<String,Tuple4<List<String>, Integer, Integer, Integer>>(line._2._1().get(i),neighborNodeT4);
						
						results.add(neighborNodeT2);
					}
				} // if condition
				else{
					results.add(line);
				}
				
				return results;
			}
		});
		
	}
	
	
	
	
	/* 
	 * Convert <Key,Value> to <Key,Tuple4<[Neighbors], Distance, Color, ShortestPaths >>  
	 * 
	 *  Where @ShortestPaths represents the number of shortest paths between the 
	 *  sourceKey(passed as param to this function) and this key.
	 *  
	 *  Where Color = White,Grey,Black represents if the node is visited or needs to be visited or the next one to be expanded.
	 * 
	 * WHITE = needs to be visited.   --> Code = 0
	 * GREY  = Expanded Next.         --> Code = 1
	 * BLACK = Already visited.       --> Code = 2
	 * 
	 */

	private JavaPairRDD<String, Tuple4<List<String>,Integer,Integer, Integer>> reduceToAdjacencyMatrix(JavaPairRDD<String,String> repeatedValues){

		Function<String,Tuple4<List<String>,Integer,Integer, Integer>> createCombiner = new Function<String,
				Tuple4<List<String>,Integer,Integer, Integer>>() {

			@Override
			public Tuple4<List<String>,Integer,Integer, Integer> call(String arg0) throws Exception {
				
				List<String> newList = new ArrayList<String>();
				newList.add(arg0);
				return new Tuple4<List<String>,Integer,Integer, Integer>(newList,0,0,1);
			}};


			Function2<Tuple4<List<String>,Integer,Integer, Integer>,
			String,
			Tuple4<List<String>,Integer,Integer, Integer>> merger = new Function2<Tuple4<List<String>,Integer,Integer, Integer>,
					String,
					Tuple4<List<String>,Integer,Integer, Integer>>() {
				
				@Override
				public Tuple4<List<String>,Integer,Integer, Integer> call(
						Tuple4<List<String>,Integer,Integer, Integer> existingValue, String newValue)
						throws Exception {
					
					existingValue._1().add(newValue);

					return existingValue;
				}
			};

			Function2<Tuple4<List<String>,Integer,Integer, Integer>,Tuple4<List<String>,Integer,Integer, Integer>,
			Tuple4<List<String>,Integer,Integer, Integer>>
			mergeCombiners = new Function2<Tuple4<List<String>,Integer,Integer, Integer>,
					Tuple4<List<String>,Integer,Integer, Integer>,
					Tuple4<List<String>,Integer,Integer, Integer>>(){

				@Override
				public Tuple4<List<String>,Integer,Integer, Integer> call(Tuple4<List<String>,Integer,Integer, Integer> combine1,
						Tuple4<List<String>,Integer,Integer, Integer> combine2) throws Exception {
					
					combine1._1().addAll(combine2._1());
					
					return combine1;
				}
				
			};

			
		return repeatedValues.combineByKey(createCombiner, merger, mergeCombiners);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void ShortestPaths(){
		
		
//		createVertices();
//		createEdges();
//		System.out.println("creating");
//		
//		JavaPairRDD<String,String> distData = JavaPairRDD.fromJavaRDD(Service.sparkCtx().parallelize(vertices));
//		JavaRDD<Edge<String>> edgeData = Service.sparkCtx().parallelize(edges);
//
//		RDD<Tuple2<Object,String>> counters= JavaRDD.toRDD(distData);
//		RDD<Edge<String>> edgeCounters= JavaRDD.toRDD(edgeData);
//		System.out.println("created datasets");
//
//		List<Object> longIds = new ArrayList<>();
//		longIds.add(1L);
//		longIds.add(5L);
//
//		Seq<Object> s = scala.collection.JavaConversions.asScalaBuffer(longIds).toList().toSeq();
//
//		System.out.println("created seq object counter");
//		
//		System.out.println("created seq object");
//		
//		Graph<String, String> graph = Graph.apply(
//				counters,
//				edgeCounters, 
//				"",
//				StorageLevel.MEMORY_AND_DISK(),
//				StorageLevel.MEMORY_AND_DISK(),
//				scala.reflect.ClassTag$.MODULE$.apply("".getClass()),
//				scala.reflect.ClassTag$.MODULE$.apply("".getClass()));
//		
//
//		System.out.println("number of edges in graph = "+graph.edges().count());
//
//		System.out.println("number of vertices in graph = "+graph.vertices().count());
//		
		 System.out.println("created the graph");
//		 Graph<scala.collection.immutable.Map<Object,Object>,String> shortestpaths = 
//				 ShortestPaths.run(graph, s,scala.reflect.ClassTag$.MODULE$.apply("".getClass()));
//		
//		 System.out.println("applied shortest path");
//
//		 JavaRDD<Tuple2<Object,Map<Object,Object>>> vert =  shortestpaths.vertices().toJavaRDD();
//		 System.out.println("converting short path to rdd");
		
//		
//		vert.map(new Function<Tuple2<Object,Map<Object,Object>>, Tuple2<Object,Map<Object,Object>>>() {
//
//			@Override
//			public Tuple2<Object, Map<Object, Object>> call(Tuple2<Object, Map<Object, Object>> arg0) throws Exception {
//				
//				System.out.println("This is the parent -> " + (String)arg0._1);
//
//				Iterator<Tuple2<Object,Object>> iter = arg0._2.iterator();
//
//				while (iter.hasNext())
//				{
//				    System.out.println("mapKey = "+(String)iter.next()._1 + " MapValue = "+(String)iter.next()._2);
//				}
//				
//				return arg0;
//			}
//		});

	}

	private  void createVertices(){
		vertices.add(new Tuple2<String,String>("node1","node3"));
		vertices.add(new Tuple2<String,String>("node1","node2"));
		vertices.add(new Tuple2<String,String>("node1","node4"));
		vertices.add(new Tuple2<String,String>("node2","node4"));
		vertices.add(new Tuple2<String,String>("node2","node5"));
		vertices.add(new Tuple2<String,String>("node3","node6"));
		vertices.add(new Tuple2<String,String>("node4","node5"));
		vertices.add(new Tuple2<String,String>("node6","node5"));
	}
	
	private void createEdges(){
		
		edges.add(new Edge<String>(1,2,"edge12"));
		edges.add(new Edge<String>(1,3,"edge13"));
		edges.add(new Edge<String>(1,4,"edge14"));
		
		edges.add(new Edge<String>(2,1,"edge21"));
		edges.add(new Edge<String>(2,5,"edge25"));

		edges.add(new Edge<String>(3,2,"edge32"));
		edges.add(new Edge<String>(3,4,"edge34"));
		edges.add(new Edge<String>(3,1,"edge31"));

		edges.add(new Edge<String>(4,1,"edge41"));
		edges.add(new Edge<String>(4,2,"edge42"));

		edges.add(new Edge<String>(5,4,"edge54"));
		edges.add(new Edge<String>(5,3,"edge53"));
		edges.add(new Edge<String>(5,2,"edge52"));
		
}
}
