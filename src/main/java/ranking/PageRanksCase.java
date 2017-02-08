package ranking;

import java.io.Serializable;

public  class PageRanksCase implements Serializable{
	
	private String node;
	private double importance; 
	
	
	public void setNode(String node) { this.node = node; }
	public void setImportance(double importance) { this.importance = importance; }

	public String getNode() { return this.node; }
	public double getImportance() { return this.importance; }
}

