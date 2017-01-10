package models;

public class Predicate {

	long id;
	long subjectid;
	long objectid;
	String name;
	
	public Predicate(long id, long subjectid, String predicate_name, long objectid){
		this.id = id;
		this.subjectid = subjectid;
		this.objectid = objectid;
		this.name = predicate_name;
	}
	
	public String getPredName() { return this.name; }
	public long getSubjectID() { return this.subjectid; }
	public long getObjectID() { return this.objectid; }
	public long getPredicateID() { return this.id; }
}
