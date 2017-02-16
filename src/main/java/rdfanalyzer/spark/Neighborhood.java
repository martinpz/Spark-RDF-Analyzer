/*
 * Copyright (C) 2016 University of Freiburg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rdfanalyzer.spark;

import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.json.JSONObject;

/**
 * This class is for computation of neighborhoods.
 */
public class Neighborhood {
	/**
	 * Computes the neighbors w.r.t. their distance and amount.
	 * 
	 * @param graph
	 *            The name of the graph to query from.
	 * @param centralNode
	 *            The URI of the central node.
	 * @param num
	 *            How many neighbors to return.
	 * 
	 * @return A JSONObject mapping the URIs of the neighbors to a JSONObject of
	 *         their properties for each neighbor.
	 */
	public static JSONObject getNeighbors(String graph, String centralNode, int num) {
		if (num < 0) {
			throw new IllegalArgumentException("Requested number of neighbors must be greater or equal to zero.");
		}

		JSONObject neighbors = new JSONObject();

		for (String neighbor : queryNeighbors(graph, centralNode, num)) {
			// Convert the neighbor String back to a JSONObject.
			JSONObject jsonNeighbor = new JSONObject(neighbor);

			// Add element to neighbors. Format "URI" => {properties}
			neighbors.put(jsonNeighbor.getString("URI"), jsonNeighbor);
		}

		return neighbors;
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
	private static List<String> queryNeighbors(String graph, String centralNode, int num) {
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + graph + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		DataFrame resultsFrame = Service.sqlCtx().sql(getSQLQuery(graph, centralNode, num));

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
	 * Builds the SQL query to find all neighbors.
	 * 
	 * @param graph
	 *            The name of the graph to query from.
	 * @param centralNode
	 *            The URI of the central node.
	 * @param num
	 *            How many neighbors to return.
	 * 
	 * @return The query String.
	 */
	private static String getSQLQuery(String graph, String centralNode, int num) {
		StringBuilder sb = new StringBuilder();

		// Build an outer query for modifications after the union took place.
		sb.append(" SELECT neighbor, connection, direction ");
		sb.append(" FROM ( ");

		// All nodes which have the central node as source ...
		sb.append(" SELECT object AS neighbor, predicate AS connection, 'out' AS direction ");
		sb.append(" FROM Graph ");
		sb.append(" WHERE subject='" + centralNode + "' ");
		// sb.append(" LIMIT " + num);

		// ... combine these with ...
		sb.append(" UNION ");

		// ... all nodes which have the central node as target.
		sb.append(" SELECT subject AS neighbor, predicate AS connection, 'in' AS direction ");
		sb.append(" FROM Graph ");
		sb.append(" WHERE object='" + centralNode + "' ");
		// sb.append(" LIMIT " + num);

		// Close the outer query and apply modifications.
		sb.append(" ) tmp ");

		// Only limit the neighbors if a number bigger than zero is given.
		// Zero means that no LIMIT should be applied.
		if (num > 0) {
			sb.append(" LIMIT " + num);
		}

		return sb.toString();
	}

	/**
	 * Converts a SQL row with a neighbor node into a JSONObject, represented as
	 * a String for serializability.
	 * 
	 * @param row
	 *            The SQL row to convert.
	 * @return The String representation of a JSONObject with the neighbors
	 *         properties.
	 */
	private static String convertSQLRowToJSON(Row row) {
		JSONObject neighbor = new JSONObject();

		String URI = row.getString(0);
		String predicate = row.getString(1);
		String direction = row.getString(2);

		neighbor.put("URI", URI);
		neighbor.put("name", RDFgraph.shortenURI(URI));
		neighbor.put("predicateURI", predicate);
		neighbor.put("predicate", RDFgraph.shortenURI(predicate));
		neighbor.put("direction", direction);

		return neighbor.toString();
	}
}
