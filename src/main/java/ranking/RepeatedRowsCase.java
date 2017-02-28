package ranking;
import java.util.List;

public class RepeatedRowsCase {

	private Long sourceNode;
	private Integer nodeDistances;
	private Integer nodeShortestPaths; 
	private List<Long> destinationNodes; 
	private Long nodeConstantID; 
	private Integer nodeColor; 
	
	public void setSourceNodes(Long sourceNode) { this.sourceNode = sourceNode; }
	public void setDestinationNodes(List<Long> destinationNodes) { this.destinationNodes = destinationNodes; }
	public void setNodeDistances(Integer node) { this.nodeDistances = node; }
	public void setNodeShortestPaths(Integer importance) { this.nodeShortestPaths = importance; }
	public void setNodeConstantID(Long constantid) { this.nodeConstantID = constantid; }
	public void setNodeColor(Integer color) { this.nodeColor = color; }

	public Long getSourceNodes() { return this.sourceNode; }
	public List<Long> getDestinationNodes() { return this.destinationNodes; }
	public Integer getNodeDistances() { return this.nodeDistances; }
	public Integer getNodeShortestPaths() { return this.nodeShortestPaths; }
	public Long getNodeConstantID() { return this.nodeConstantID; }
	public Integer getNodeColor() { return this.nodeColor; }
}
