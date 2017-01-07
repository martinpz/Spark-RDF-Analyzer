package rdfanalyzer.spark;

public class Centrality {
	
	public static String main(String metricType,String dataset, String nodeName){
		
		if(metricType == "1"){
			return CalculateInDegree(nodeName);
		}
		else if(metricType == "2"){
			return CalculateOutDegree(nodeName);
		}
		else if(metricType == "3"){
			return CalculateBetweenness(nodeName);
		}
		else if(metricType == "4"){
			return CalculateCloseness(nodeName);
		}
		
		return "";
	}
	
	public static String CalculateInDegree(String node){
		return "";
	}
	public static String CalculateOutDegree(String node){
		return "";
	}
	
	public static String CalculateBetweenness(String node){
		
		return "";
	}
	public static String CalculateCloseness(String node){
		
		return "";
	}

}
