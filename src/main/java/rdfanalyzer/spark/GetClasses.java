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
 * This class calculates the list of distinct classes for a given graph.
 */
public class GetClasses {
	public static String main(String[] args) throws Exception {
		String result = "";

		// Read graph from parquet
		Dataset<Row> graphFrame = Service.spark().read().parquet(Configuration.storage() + args[0] + ".parquet");
		graphFrame.cache().createOrReplaceTempView("Graph");

		// Run SQL over loaded Graph.
		Dataset<Row> resultsFrame = Service.spark().sql(
				"SELECT DISTINCT object FROM Graph WHERE predicate='<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>'");

		// Format output results. Based if output is table or chart it is
		// returned into different formats.
		List<Row> resultRows = resultsFrame.collectAsList();

		if (args[1].equals("Normal")) {
			result = "<table class=\"table table-striped\">";
			result += "<thead><tr><th style=\"text-align: center;\">Class</th></tr></thead>";

			for (Row r : resultRows) {
				result += "<tr><td data-toggle=\"tooltip\" title=\"" + r.getString(0) + "\">"
						+ RDFgraph.shortenURI(r.getString(0)) + "</td></tr>";
			}

			result += "</table>";
		} else if (args[1].equals("Chart")) {
			for (Row r : resultRows) {
				result += RDFgraph.shortenURI(r.getString(0)) + ",";
			}

			result = result.substring(0, result.length() - 1);
		}

		return result;
	}
}
