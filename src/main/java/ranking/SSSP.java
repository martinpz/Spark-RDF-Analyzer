	package ranking;

	import java.io.Serializable;
	import java.math.BigDecimal;
	import java.math.RoundingMode;
	import java.util.ArrayList;
	import java.util.List;

	import rdfanalyzer.spark.Service;

	import org.apache.spark.api.java.JavaPairRDD;
	import org.apache.spark.api.java.function.Function;
	import org.apache.spark.api.java.function.Function2;
	import org.apache.spark.api.java.function.PairFlatMapFunction;
	import org.apache.spark.api.java.function.PairFunction;
	import org.apache.spark.graphx.Edge;

	import scala.Tuple2;
	import scala.Tuple3;
	import scala.Tuple4;

	public class SSSP implements Serializable{
		
		public List<Tuple2<Long,Long>> vertices = new ArrayList<>();
		public List<Edge<Long>> edges = new ArrayList<>();
		
		public static boolean firstTime = true;
		
		public JavaPairRDD<Long,Tuple4<List<Long>,Integer,Integer,Integer>> mappedValues ;

		public int sumOfCOlorColumn = 0;
		public int lastsumOfCOlorColumn = 0;
		
		public boolean fullBfs = true;
		
		
		JavaPairRDD<Long, Tuple3<List<Long>, List<Integer>, List<Integer>>> masterNodeBFS;

		/**
		 * 
		 * 	This function tests the data using the relations defined in the createVertices function.
		 */

		public void test() throws Exception{
			

			createVertices();
			
			JavaPairRDD<Long,Long> distData = Service.sparkCtx().parallelizePairs(vertices);

			
			JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrix = reduceToAdjacencyMatrix(distData);

			applyBFSForNode(1L, adjacencyMatrix);
		}

		/*
		 *  The function is responsible for summing the color columns of the bfs. This means if the sum of all the colors
		 *  remains than no node has been further expanded. Hence we can return true i.e break out of the loop.
		 */
		private boolean breakloop(JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrix){
			
			sumOfCOlorColumn = adjacencyMatrix.mapValues(new Function<Tuple4<List<Long>,Integer,Integer,Integer>, Integer>() {

				@Override
				public Integer call(Tuple4<List<Long>, Integer, Integer, Integer> arg0) throws Exception {
					return arg0._3();
				}
			}).values().collect().stream().mapToInt(Integer::intValue).sum();
			
//			System.out.println("Sum of breakloop = " +sumOfCOlorColumn);
			
			if(lastsumOfCOlorColumn == sumOfCOlorColumn){
				return true;
			}
			
			lastsumOfCOlorColumn = sumOfCOlorColumn;

			return false;
		}

		
		
		/**
		 * Convert <Key,[Neighbors]> To <key, Tuple4 < [Neighbors] , Distance, Color, ShortestPaths >
		 */
		
		public JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> applyBFSForNode(long sourceNode, JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrixx){

			
			
			/**
			 *  We won't have any grey nodes in the initial dataset hence we'll never go inside the if condition defined below.
			 *  So our initial grey node is the sourceNode. Hence this check will only run for the first time.
			 */
			
			firstTime = true;
			
			/**
			 *  2 = black color. So if all the items are 2 i.e black. Than we can break. Hence our breakPoint is
			 *  itemCount * 2. And once we reduce we will check if we get this value from our reducer than we'll break.
			 */


			while(true){
				
				
				/**
				 *  This function does the following.
				 *  
				 *  1. Expand the nodes with status 1.
				 *  2. Increment the distance of the node that is expanded.
				 */
				mappedValues = PerformBFSMapOperation(sourceNode,adjacencyMatrixx).cache();
				
				/**
				 *  Check the comments defined inside this function.
				 */
				adjacencyMatrixx = PerformBFSReduceOperation(mappedValues);
				
				
				
				
				if(breakloop(adjacencyMatrixx)){
					break;
				}

			}
			
			adjacencyMatrixx.cache();
			
			/**
			 * Now we've got the final distances of source node to all other nodes.
			 * Hence we can perform the final step. Which is to convert the data from tabular from
			 * to reduce the data wrt the source node such that we have the following format.
			 * 
			 * sourceNode, [otherNodes], [distancestoOtherNodes] , [ ShortestPathsBetweenThoseNodes]
			 * 
			 */
				
			JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> mappeddata = finalMap(adjacencyMatrixx, sourceNode);

			printSumOfWholeBFS(mappeddata,sourceNode);

			return mappeddata;
		}
		
		
		private JavaPairRDD<Long, Tuple4<List<Long>, Integer, Integer, Integer>> PluckGreyNodes(JavaPairRDD<Long, Tuple4<List<Long>, Integer, Integer, Integer>> adjacencyMatrixx){
			return adjacencyMatrixx.filter(new Function<Tuple2<Long,Tuple4<List<Long>,Integer,Integer,Integer>>, Boolean>() {
				
				@Override
				public Boolean call(Tuple2<Long, Tuple4<List<Long>, Integer, Integer, Integer>> arg0) throws Exception {
					
					if(arg0._2()._2() == 1){
						return true;
					}
					return false;
				}
			});
		}
		
		/**
		 *  The function simply sums the distance column and divides it by 1. This gives us the closeness centrality
		 *  w.r.t the sourcenode sent as an argument to this function at the end of the algorithm.
		 */
		public void printSumOfWholeBFS(JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> mappedData,long sourceNode){
			int finalvalues = mappedData.mapValues(new Function<Tuple3<Long,Integer,Integer>, Integer>() {

				@Override
				public Integer call(Tuple3<Long, Integer, Integer> line) throws Exception {
					
					return line._2();
				}
			}).values().collect().stream().mapToInt(Integer::intValue).sum();
			
			System.out.println("Final value for complete bfs = "+finalvalues + " for node " + sourceNode);
			double closeness = ((double)1/(double)finalvalues);
			System.out.printf("dexp final ballay ballay: %f\n", round(closeness,9));
		}
		
		public double round(double value, int places) {
		    if (places < 0) throw new IllegalArgumentException();

		    BigDecimal bd = new BigDecimal(value);
		    bd = bd.setScale(places, RoundingMode.HALF_UP);
		    return bd.doubleValue();
		}

		
		/**
		 *  This converts the finalResult into sourceNode, DestNode, Distance, NPaths
		 */
		private JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> finalMap(JavaPairRDD<Long, Tuple4<List<Long>, Integer, Integer, Integer>> finalresult,final Long sourceNode){

			return finalresult.mapToPair(new PairFunction<Tuple2<Long,Tuple4<List<Long>,Integer,Integer,Integer>>, Long, Tuple3<Long,Integer,Integer>>() {

				@Override
				public Tuple2<Long, Tuple3<Long, Integer, Integer>> call(
						Tuple2<Long, Tuple4<List<Long>, Integer, Integer, Integer>> line) throws Exception {
					
					Tuple3<Long,Integer,Integer> item = new Tuple3<Long,Integer,Integer>(line._1,line._2._2(),line._2._4());

					return new Tuple2<Long,Tuple3<Long, Integer, Integer>>(sourceNode,item);
				}
			});
		}
		
		/**
		 *  This reduces the final result retrieve from the map phase. Giving us the data in the following structure.
		 *  node, [ array nodes it can reach ], [ array of distances it can reach those nodes in ] , [ no. of shortest paths between the source and these nodes ].
		 *  
		 *  note that the square brackets represents the array and the item at each index for each array will have values for the same nodes.
		 */
		
		public JavaPairRDD<Long, Tuple3<List<Long>, List<Integer>, List<Integer>>> finalReduce(JavaPairRDD<Long, Tuple3<Long, Integer, Integer>> finalMappedData){
			
			
			Function<Tuple3<Long, Integer, Integer>,Tuple3<List<Long>,List<Integer>,List<Integer>>> createCombiner = new Function<Tuple3<Long,Integer,Integer>, Tuple3<List<Long>,List<Integer>,List<Integer>>>() {
				
				@Override
				public Tuple3<List<Long>, List<Integer>, List<Integer>> call(Tuple3<Long, Integer, Integer> arg0)
						throws Exception {
					
					List<Long> dstNode = new ArrayList<Long>();
					List<Integer> dstNodeDist = new ArrayList<Integer>();
					List<Integer> dstNodePaths = new ArrayList<Integer>();
					
					dstNode.add(arg0._1());
					dstNodeDist.add(arg0._2());
					dstNodePaths.add(arg0._3());
					
					return new Tuple3<List<Long>, List<Integer>, List<Integer>>(dstNode,dstNodeDist,dstNodePaths);
				}
			};
			Function2<Tuple3<List<Long>,List<Integer>,List<Integer>>,
			Tuple3<Long, Integer, Integer>,
			Tuple3<List<Long>,List<Integer>,List<Integer>>> merger = new Function2<Tuple3<List<Long>,List<Integer>,List<Integer>>,
					Tuple3<Long, Integer, Integer>,
					Tuple3<List<Long>,List<Integer>,List<Integer>>>() {
				
				// this is called when we face the key next time. So we add an item to the arraylist of that key.

				@Override
				public Tuple3<List<Long>, List<Integer>, List<Integer>> call(
						Tuple3<List<Long>, List<Integer>, List<Integer>> arg0, Tuple3<Long, Integer, Integer> arg1)
						throws Exception {

					
					List<Long> dstNode = arg0._1();
					List<Integer> dstNodeDist = arg0._2();
					List<Integer> dstNodePaths = arg0._3();

					dstNode.add(arg1._1());
					dstNodeDist.add(arg1._2());
					dstNodePaths.add(arg1._3());
					
					return new Tuple3<List<Long>, List<Integer>, List<Integer>>(dstNode,dstNodeDist,dstNodePaths);
					
					
				}
			};

			Function2<Tuple3<List<Long>,List<Integer>,List<Integer>>,
			Tuple3<List<Long>,List<Integer>,List<Integer>>,
			Tuple3<List<Long>,List<Integer>,List<Integer>>> mergeCombiners = new Function2<Tuple3<List<Long>,List<Integer>,List<Integer>>,
					Tuple3<List<Long>,List<Integer>,List<Integer>>,
					Tuple3<List<Long>,List<Integer>,List<Integer>>>(){

						@Override
						public Tuple3<List<Long>, List<Integer>, List<Integer>> call(
								Tuple3<List<Long>, List<Integer>, List<Integer>> arg0,
								Tuple3<List<Long>, List<Integer>, List<Integer>> arg1) throws Exception {

							List<Long> dstNode = arg0._1();
							List<Integer> dstNodeDist = arg0._2();
							List<Integer> dstNodePaths = arg0._3();

							dstNode.addAll(arg1._1());
							dstNodeDist.addAll(arg1._2());
							dstNodePaths.addAll(arg1._3());

							return new Tuple3<List<Long>, List<Integer>, List<Integer>>(dstNode,dstNodeDist,dstNodePaths);
						}

			};

			return finalMappedData.combineByKey(createCombiner, merger, mergeCombiners);
		}
		
		private JavaPairRDD<Long, Tuple4<List<Long>, Integer, Integer, Integer>> PerformBFSReduceOperation(JavaPairRDD<Long,Tuple4<List<Long>,Integer,Integer,Integer>> mappedValues){
			
			Function<Tuple4<List<Long>,Integer,Integer,Integer>,Tuple4<List<Long>,Integer,Integer,Integer>> createCombiner 
							= new Function<Tuple4<List<Long>,Integer,Integer,Integer>,Tuple4<List<Long>,Integer,Integer,Integer>>() {
				
				@Override
				public Tuple4<List<Long>,Integer,Integer,Integer> call(Tuple4<List<Long>,Integer,Integer,Integer> line) throws Exception {
					return line;
				}
			};

			Function2<Tuple4<List<Long>,Integer,Integer,Integer>,
			Tuple4<List<Long>,Integer,Integer,Integer>,
			Tuple4<List<Long>,Integer,Integer,Integer>> merger = new Function2<Tuple4<List<Long>,Integer,Integer,Integer>,
					Tuple4<List<Long>,Integer,Integer,Integer>,
					Tuple4<List<Long>,Integer,Integer,Integer>>() {
				
				// this is called when we face the key next time. So we add an item to the arraylist of that key.

				@Override
				public Tuple4<List<Long>, Integer, Integer, Integer> call(
						Tuple4<List<Long>, Integer, Integer, Integer> previousKey,
						Tuple4<List<Long>, Integer, Integer, Integer> newKey) throws Exception {

					/**
					 *  Step 1: Check if one of the vertices being reduced is black. If yes. Than take that vertex. Otherwise go to step2.
					 *  
					 *  Step 2: If one vertex is grey and the other is white. Take the distance of grey 
					 *  		while the neighbor of the one not null. Otherwise go to step3.
					 *  
					 *  Step 3: If the vertices are same, there colors are same, there 
					 *  		distances are same. Simply add 1 to the shortest paths.
					 */
					
					
					Tuple4<List<Long>, Integer, Integer, Integer> finalReturn = getReducedData(previousKey,newKey);


					return finalReturn;
				}
			};
			
			Function2<Tuple4<List<Long>, Integer, Integer, Integer>,
			Tuple4<List<Long>, Integer, Integer, Integer>,
			Tuple4<List<Long>, Integer, Integer, Integer>> mergeCombiners = new Function2<Tuple4<List<Long>, Integer, Integer, Integer>,
					Tuple4<List<Long>, Integer, Integer, Integer>,
					Tuple4<List<Long>, Integer, Integer, Integer>>(){


				@Override
				public Tuple4<List<Long>, Integer, Integer, Integer> call(
						Tuple4<List<Long>, Integer, Integer, Integer> arg0,
						Tuple4<List<Long>, Integer, Integer, Integer> arg1) throws Exception {

					
					Tuple4<List<Long>, Integer, Integer, Integer> finalReturn = getMergeCombinerReducedData(arg0,arg1);


					return finalReturn ;
				}
			};
			
			return mappedValues.combineByKey(createCombiner, merger, mergeCombiners);
		}
		
		private static Tuple4<List<Long>, Integer, Integer, Integer> getReducedData(
				Tuple4<List<Long>, Integer, Integer, Integer> previousKey,
				Tuple4<List<Long>, Integer, Integer, Integer> newKey){
			
			Tuple4<List<Long>, Integer, Integer, Integer> result = null;
			
			
			
			/* Step 1 */
			if(previousKey._3() == 2){
				result = previousKey;
			}
			else if(newKey._3() == 2){
				result = newKey;
			}

			/* Step 2 */
			else if(newKey._3().equals(1) && previousKey._3().equals(0)){
				result = new Tuple4<List<Long>,Integer,Integer,Integer>(previousKey._1(),newKey._2(),newKey._3(),newKey._4());
			}
			else if(newKey._3().equals(0) && previousKey._3().equals(1)){
				result = new Tuple4<List<Long>,Integer,Integer,Integer>(newKey._1(),previousKey._2(),previousKey._3(),previousKey._4());
			}
			
			/* Step 3 */
			else if((newKey._3().equals(1) && previousKey._3().equals(1)) && 
					(newKey._1().size() == 0 && previousKey._1().size() == 0) &&
					(newKey._2().equals(previousKey._2()))){

				result = new Tuple4<List<Long>,Integer,Integer,Integer>(newKey._1(),newKey._2(),newKey._3(),newKey._4()+1);
			}
			else{
				result = getMergeCombinerReducedData(previousKey,newKey);
			}

			return result;
		
		}
		
		
		private static Tuple4<List<Long>, Integer, Integer, Integer> getMergeCombinerReducedData(
				Tuple4<List<Long>, Integer, Integer, Integer> previousKey,
				Tuple4<List<Long>, Integer, Integer, Integer> newKey){
			/**
			 *  This function will run in situations when mergerCombiner runs. 
			 *  Now in one partition we performed the combine operation due to which the data comes in a final format which we
			 *  are not handling in the above conditions. Hence we come here and consider it here.
			 */
			
			// if both keys have same color
			Integer color = previousKey._3();
			Integer distance = previousKey._3();
			Integer paths = previousKey._4();
			
			// if both records have the list values. Since they belong to the same key. They'll have same list values i.e neighbors.
			List<Long> list = previousKey._1();

			// take the key with the greater color if exists.
			if(previousKey._3() > newKey._3()){
				color = previousKey._3();
			}
			else if(previousKey._3() < newKey._3()){
				color = newKey._3();
			}
			
			
			// take the key data which has list values.
			if(previousKey._1().size() > newKey._1().size()){
				list = previousKey._1();
			}
			else if(previousKey._1().size() < newKey._1().size()){
				list = newKey._1();
			}

			
			// take the one with the greater distance of the two.
			if(previousKey._2() > newKey._2()){
				distance = previousKey._2();
			}
			else if(previousKey._2() < newKey._2()){
				distance = newKey._2();
			}

			// take the one with the greater number of paths of the two.
			if(previousKey._4() > newKey._4()){
				paths = previousKey._4();
			}
			else if(previousKey._4() < newKey._4()){
				paths = newKey._4();
			}

			return new Tuple4<List<Long>,Integer,Integer,Integer>(list,distance,color,paths);
		}
		
		
		private JavaPairRDD<Long,Tuple4<List<Long>,Integer,Integer,Integer>> PerformBFSMapOperation(final Long sourceNode, JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> adjacencyMatrix){
			

			return adjacencyMatrix.flatMapToPair(new PairFlatMapFunction<Tuple2<Long,Tuple4<List<Long>,Integer,Integer,Integer>>, Long, Tuple4<List<Long>,Integer,Integer, Integer>>() {

				@Override
				public Iterable<Tuple2<Long, Tuple4<List<Long>,Integer,Integer, Integer>>> call(
						Tuple2<Long, Tuple4<List<Long>, Integer, Integer, Integer>> line)
						throws Exception {

					
					List<Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>>> results = new ArrayList<Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>>>();
					// If this is a grey node. Go inside.
					if((line._2._3() == 1) || (firstTime && line._1.equals(sourceNode))){
						
						firstTime = false;

						/**
						 * 	Step 1 . Convert this node to black.
						 *  Step 2 . Add the neighbors of this node as keys. 
						 */
						
						 
						/**
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
						Tuple4<List<Long>, Integer, Integer, Integer> currentNodeT4 = new Tuple4<List<Long>, Integer, Integer, Integer>(line._2._1(), line._2._2(), 2, line._2._4());
						Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>> currentNodeT2 = new Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>>(line._1,currentNodeT4);

						results.add(currentNodeT2);

						
						// Step 2
						for(int i=0;i<line._2._1().size();i++){
							
							/**
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
							
							Tuple4<List<Long>, Integer, Integer, Integer> neighborNodeT4 
									= new Tuple4<List<Long>, Integer, Integer, Integer>(new ArrayList<Long>(), line._2._2()+1, 1, line._2._4());

							Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>> neighborNodeT2
							  		= new Tuple2<Long,Tuple4<List<Long>, Integer, Integer, Integer>>(line._2._1().get(i),neighborNodeT4);
							
							
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
		
		
		
		
		/**
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

		public JavaPairRDD<Long, Tuple4<List<Long>,Integer,Integer, Integer>> reduceToAdjacencyMatrix(JavaPairRDD<Long,Long> repeatedValues){

			Function<Long,Tuple4<List<Long>,Integer,Integer, Integer>> createCombiner = new Function<Long,
					Tuple4<List<Long>,Integer,Integer, Integer>>() {

				@Override
				public Tuple4<List<Long>,Integer,Integer, Integer> call(Long arg0) throws Exception {
					
					List<Long> newList = new ArrayList<Long>();
					newList.add(arg0);
					return new Tuple4<List<Long>,Integer,Integer, Integer>(newList,0,0,1);
				}};


				Function2<Tuple4<List<Long>,Integer,Integer, Integer>,
				Long,
				Tuple4<List<Long>,Integer,Integer, Integer>> merger = new Function2<Tuple4<List<Long>,Integer,Integer, Integer>,
						Long,
						Tuple4<List<Long>,Integer,Integer, Integer>>() {
					
					@Override
					public Tuple4<List<Long>,Integer,Integer, Integer> call(
							Tuple4<List<Long>,Integer,Integer, Integer> existingValue, Long newValue)
							throws Exception {
						
						existingValue._1().add(newValue);

						return existingValue;
					}
				};

				Function2<Tuple4<List<Long>,Integer,Integer, Integer>,Tuple4<List<Long>,Integer,Integer, Integer>,
				Tuple4<List<Long>,Integer,Integer, Integer>>
				mergeCombiners = new Function2<Tuple4<List<Long>,Integer,Integer, Integer>,
						Tuple4<List<Long>,Integer,Integer, Integer>,
						Tuple4<List<Long>,Integer,Integer, Integer>>(){

					@Override
					public Tuple4<List<Long>,Integer,Integer, Integer> call(Tuple4<List<Long>,Integer,Integer, Integer> combine1,
							Tuple4<List<Long>,Integer,Integer, Integer> combine2) throws Exception {
						
						combine1._1().addAll(combine2._1());
						
						return combine1;
					}
					
				};

				
			return repeatedValues.combineByKey(createCombiner, merger, mergeCombiners);
		}
		
		
		
		
		
		
		
		
		
		// for testing purposes only.
		
		

		private  void createVertices(){
			vertices.add(new Tuple2<Long,Long>(1L,3L));
			vertices.add(new Tuple2<Long,Long>(1L,2L));
			vertices.add(new Tuple2<Long,Long>(1L,4L));
			vertices.add(new Tuple2<Long,Long>(2L,4L));
			vertices.add(new Tuple2<Long,Long>(2L,5L));
			vertices.add(new Tuple2<Long,Long>(3L,6L));
			vertices.add(new Tuple2<Long,Long>(4L,5L));
			vertices.add(new Tuple2<Long,Long>(6L,5L));

			vertices.add(new Tuple2<Long,Long>(6L,7L));
			vertices.add(new Tuple2<Long,Long>(6L,8L));
			vertices.add(new Tuple2<Long,Long>(6L,9L));
			vertices.add(new Tuple2<Long,Long>(6L,10L));
			vertices.add(new Tuple2<Long,Long>(6L,11L));
			vertices.add(new Tuple2<Long,Long>(11L,7L));
			vertices.add(new Tuple2<Long,Long>(11L,2L));
			vertices.add(new Tuple2<Long,Long>(11L,3L));

			vertices.add(new Tuple2<Long,Long>(3L,10L));
			vertices.add(new Tuple2<Long,Long>(3L,8L));
	}
		
	}
