package ranking;

import java.io.Serializable;

public class UniqueNodeCase implements Serializable{
	private Long id;
	private String name; 
	
	public void setNodeID(long id) { this.id = id; }
	public void setNodeName(String name) { this.name = name; }

	public long getNodeID() { return this.id; }
	public String getNodeName() { return this.name; }



}
