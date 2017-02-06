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

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * This class calculates the number of edges for a given graph.
 */
public class CountEdges {
	public static String main(String[] args) throws Exception {
		String result = "";

		// Read graph from parquet
		Dataset<Row> graphFrame = Service.spark().read().parquet(Configuration.storage() + args[0] + ".parquet");
		graphFrame.cache().createOrReplaceTempView("Graph");

		// Run SQL over loaded Graph.
		Dataset<Row> resultsFrame = Service.spark().sql("SELECT Count(*) FROM Graph");

		// Read Ranking table from parquet (Not needed in this computation but
		// to cache it for later)
		Dataset<Row> rankingFrame = Service.spark().read()
				.parquet(Configuration.storage() + args[0] + "Ranking.parquet");
		rankingFrame.cache().createOrReplaceTempView("Ranking");

		// Get the results and format them in desired format.
		List<Row> rows = resultsFrame.collectAsList();

		result = Long.toString(rows.get(0).getLong(0));
		return "<h1>" + result + "</h1>";
	}
}
