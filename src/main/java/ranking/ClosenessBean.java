package ranking;

public class ClosenessBean {

	private String node;
	private double closeness;
	
	public ClosenessBean(String node,double closeness){
		this.node = node;
		this.closeness = closeness;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public double getCloseness() {
		return closeness;
	}

	public void setCloseness(double closeness) {
		this.closeness = closeness;
	}

	
}
