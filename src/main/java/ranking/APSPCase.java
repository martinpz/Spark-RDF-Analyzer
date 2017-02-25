package ranking;

import java.io.Serializable;
import java.util.List;

public class APSPCase implements Serializable{

	private Long sourceNode;
	private List<Integer> nodeDistances;
	private List<Integer> nodeShortestPaths; 
	private List<Long> destinationNodes; 
	
	public void setSourceNodes(Long sourceNode) { this.sourceNode = sourceNode; }
	public void setDestinationNodes(List<Long> destinationNodes) { this.destinationNodes = destinationNodes; }
	public void setNodeDistances(List<Integer> node) { this.nodeDistances = node; }
	public void setNodeShortestPaths(List<Integer> importance) { this.nodeShortestPaths = importance; }

	public Long getSourceNodes() { return this.sourceNode; }
	public List<Long> getDestinationNodes() { return this.destinationNodes; }
	public List<Integer> getNodeDistances() { return this.nodeDistances; }
	public List<Integer> getNodeShortestPaths() { return this.nodeShortestPaths; }

}
