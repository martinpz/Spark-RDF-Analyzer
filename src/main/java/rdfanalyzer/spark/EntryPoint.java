package rdfanalyzer.spark;

import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.json.JSONArray;
import org.json.JSONObject;

public class EntryPoint {
	/**
	 * Reads the suggested entry points from initially computed files.
	 * 
	 * @param graph
	 *            The name of the graph to query from.
	 * @param method
	 *            The ranking method which should be used to determine the top
	 *            ranked items.
	 * @param num
	 *            How many suggestions to return.
	 * 
	 * @return A JSONArray mapping the URIs of the neighbors to a JSONObject of
	 *         their properties for each neighbor.
	 */
	public static JSONArray getSuggestions(String graph, String method, int num) {
		if (num <= 0) {
			throw new IllegalArgumentException("Requested number of suggestions must be greater than zero.");
		}

		return new JSONArray(querySuggestions(graph + method, num));
	}

	/**
	 * Queries the neighbors from the graph.
	 * 
	 * @param graph
	 *            The name of the graph to query from.
	 * @param centralNode
	 *            The URI of the central node.
	 * @param num
	 *            How many neighbors to return.
	 * 
	 * @return A List of JSON represented neighbors.
	 */
	private static List<String> querySuggestions(String graph, int num) {
		final String tmpGraphName = "RankingGraph" + graph;
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + graph + ".parquet");
		graphFrame.cache().registerTempTable(tmpGraphName);

		// Only select valid URIs from the data.
		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT node, importance FROM " + tmpGraphName
				+ " WHERE node LIKE '<%' ORDER BY importance DESC LIMIT " + num);

		@SuppressWarnings("serial")
		List<String> neighbors = resultsFrame.javaRDD().map(new Function<Row, String>() {
			@Override
			public String call(Row row) {
				return convertSQLRowToJSON(row);
			}
		}).collect();

		return neighbors;
	}

	/**
	 * Converts a SQL row with a suggested node into a JSONObject, represented
	 * as a String for serializability.
	 * 
	 * @param row
	 *            The SQL row to convert.
	 * @return The String representation of a JSONObject with the suggested
	 *         nodes properties.
	 */
	private static String convertSQLRowToJSON(Row row) {
		JSONObject suggestion = new JSONObject();

		String URI = row.getString(0);
		double importance = row.getDouble(1);

		suggestion.put("URI", URI);
		suggestion.put("name", RDFgraph.shortenURI(URI));
		suggestion.put("importance", importance);

		return suggestion.toString();
	}
}
