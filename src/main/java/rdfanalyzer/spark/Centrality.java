package rdfanalyzer.spark;

public class Centrality {
	
	public static String main(String metricType,String dataset, String nodeName){
		
		if(metricType.equals("1")){
			return CalculateInDegree(nodeName);
		}
		else if(metricType.equals("2")){
			return CalculateOutDegree(nodeName);
		}
		else if(metricType.equals("3")){
			return CalculateBetweenness(nodeName);
		}
		else if(metricType.equals("4")){
			return CalculateCloseness(nodeName);
		}
		
		return "none";
	}
	
	public static String CalculateInDegree(String node){
		return "in degree";
	}
	public static String CalculateOutDegree(String node){
		return "out degree";
	}
	
	public static String CalculateBetweenness(String node){
		
		return "betweenness";
	}
	public static String CalculateCloseness(String node){
		
		return "closeness";
	}

}
