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
 * This class uses a detailed and advanced method to calculate number of nodes.
 */
public class Neighborhood {
	/**
	 * Computes the neighbors w.r.t. their distance and amount.
	 * 
	 * @param graph
	 *            The name of the graph to use.
	 * @param centralNode
	 *            The URI of the central node.
	 * @param num
	 *            How many neighbors to return.
	 * 
	 * @return A JSONObject of the URIs of the neighbors.
	 */
	public static JSONObject getNeighbors(String graph, String centralNode, int num) {
		if (num <= 0) {
			throw new IllegalArgumentException("Requested number of neighbors must be greater than zero.");
		}

		JSONObject jsonObj = new JSONObject();

		for (String neighbor : queryNeighbors(graph, centralNode, num)) {
			// Add element to neighbors (URI => name)
			jsonObj.put(neighbor, RDFgraph.shortenURI(neighbor));
		}

		return jsonObj;
	}

	private static List<String> queryNeighbors(String graph, String centralNode, int num) {
		// Read graph from parquet
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + graph + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		// Run neighbor query over loaded graph.
		DataFrame resultsFrame = Service.sqlCtx()
				.sql("SELECT subject AS neighbor " + "FROM Graph " + "WHERE object='" + centralNode + "'" + " UNION "
						+ "SELECT object AS neighbor " + "FROM Graph " + "WHERE subject='" + centralNode + "'"
						+ " LIMIT " + num);

		// Collect neighbors from result.
		@SuppressWarnings("serial")
		List<String> neighbors = resultsFrame.javaRDD().map(new Function<Row, String>() {
			@Override
			public String call(Row row) {
				return row.getString(0);
			}
		}).collect();

		return neighbors;
	}
}
