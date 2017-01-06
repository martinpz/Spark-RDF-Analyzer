package rdfanalyzer.spark;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class Simrank {

	String query;
	
	public static String preCalculateSimrank(String graphName) throws Exception {
		
		// Read graph from parquet
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + graphName + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

//		query = "SELECT subject, predicate, object FROM " + graphName + " WHERE subject = '" + startN;

		String[] args = { graphName, "2" };

		String nodes = CountNodes.main(args);

		return getColumnsInformation();
	}
	
	public static String getColumnsInformation(){
		String result = "";
		
		// Run SQL over loaded Graph.
		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT * FROM Graph").limit(5);

		Row[] rows = resultsFrame.collect();
		for(int i=0;i<rows.length;i++){
			result+="<h1>" + rows[i] + "</h1>";
		}
		
		return result;
	}
}
