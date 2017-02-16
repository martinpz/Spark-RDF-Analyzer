package ranking;

import java.io.Serializable;
import java.util.List;

public class APSPCase implements Serializable{

	private List<Integer> nodeDistances;
	private List<Integer> nodeShortestPaths; 
	private List<String> destinationNodes; 
	
	
	public void setDestinationNodes(List<String> destinationNodes) { this.destinationNodes = destinationNodes; }
	public void setNodeDistances(List<Integer> node) { this.nodeDistances = node; }
	public void setNodeShortestPaths(List<Integer> importance) { this.nodeShortestPaths = importance; }

	public List<String> getDestinationNodes() { return this.destinationNodes; }
	public List<Integer> getNodeDistances() { return this.nodeDistances; }
	public List<Integer> getNodeShortestPaths() { return this.nodeShortestPaths; }

}
