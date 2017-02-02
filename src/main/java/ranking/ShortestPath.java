package ranking;

import java.util.ArrayList;
import java.util.List;

import rdfanalyzer.spark.Service;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.graphx.*;
import org.apache.spark.graphx.lib.*;
import org.apache.spark.rdd.RDD;
import org.apache.spark.storage.StorageLevel;

import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.Map;
import scala.collection.immutable.Seq;

public class ShortestPath {
	
	public List<Tuple2<Object,String>> vertices = new ArrayList<>();
	public List<Edge<String>> edges = new ArrayList<>();

	public ShortestPath(){
		
		
		createVertices();
		createEdges();
		System.out.println("creating");
		
		

		JavaRDD<Tuple2<Object,String>> distData = Service.sparkCtx().parallelize(vertices);
		JavaRDD<Edge<String>> edgeData = Service.sparkCtx().parallelize(edges);

		RDD<Tuple2<Object,String>> counters= JavaRDD.toRDD(distData);
		RDD<Edge<String>> edgeCounters= JavaRDD.toRDD(edgeData);
		System.out.println("created datasets");

		List<Object> longIds = new ArrayList<>();
		longIds.add(1L);
		longIds.add(5L);

		Seq<Object> s = scala.collection.JavaConversions.asScalaBuffer(longIds).toList().toSeq();

		System.out.println("created seq object counter");
		
		System.out.println("created seq object");
		
		Graph<String, String> graph = Graph.apply(
				counters,
				edgeCounters, 
				"",
				StorageLevel.MEMORY_AND_DISK(),
				StorageLevel.MEMORY_AND_DISK(),
				scala.reflect.ClassTag$.MODULE$.apply("".getClass()),
				scala.reflect.ClassTag$.MODULE$.apply("".getClass()));

		System.out.println("number of edges in graph = "+graph.edges().count());
		System.out.println("number of vertices in graph = "+graph.vertices().count());
		
		 System.out.println("created the graph");
		 Graph<scala.collection.immutable.Map<Object,Object>,String> shortestpaths = 
				 ShortestPaths.run(graph, s,scala.reflect.ClassTag$.MODULE$.apply("".getClass()));
		
		 System.out.println("applied shortest path");

		 JavaRDD<Tuple2<Object,Map<Object,Object>>> vert =  shortestpaths.vertices().toJavaRDD();
		 System.out.println("converting short path to rdd");
		
		
		vert.map(new Function<Tuple2<Object,Map<Object,Object>>, Tuple2<Object,Map<Object,Object>>>() {

			@Override
			public Tuple2<Object, Map<Object, Object>> call(Tuple2<Object, Map<Object, Object>> arg0) throws Exception {
				
				System.out.println("This is the parent -> " + (String)arg0._1);

				Iterator<Tuple2<Object,Object>> iter = arg0._2.iterator();

				while (iter.hasNext())
				{
				    System.out.println("mapKey = "+(String)iter.next()._1 + " MapValue = "+(String)iter.next()._2);
				}
				
				return arg0;
			}
		});

	}

	private  void createVertices(){
		vertices.add(new Tuple2<Object,String>(1L,"node1"));
		vertices.add(new Tuple2<Object,String>(2,"node2"));
		vertices.add(new Tuple2<Object,String>(3,"node3"));
		vertices.add(new Tuple2<Object,String>(4,"node4"));
		vertices.add(new Tuple2<Object,String>(5,"node5"));
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
