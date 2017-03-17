package ranking;

public class ClosenessBean {

	private String node;
	private double importance;
	
	public ClosenessBean(String node,double closeness){
		this.node = node;
		this.importance = closeness;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public double getCloseness() {
		return importance;
	}

	public void setCloseness(double closeness) {
		this.importance = closeness;
	}

	
}
