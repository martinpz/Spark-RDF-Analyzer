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

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

/**
 * This class calculates predicate distribution for a given graph.
 */
public class PredicateDistribution {
	public static String main(String[] args) throws Exception {
		String result = "";

		// Read graph from parquet
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.props("Storage") + args[0] + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		// Run SQL over loaded Graph.
		DataFrame resultsFrame = Service.sqlCtx()
				.sql("SELECT predicate as p, COUNT(predicate) as cnt FROM Graph GROUP BY predicate ORDER BY cnt DESC");

		// Format output results. Based if output is table or chart it is
		// returned into different formats.
		Row[] resultRows = resultsFrame.collect();

		if (args[1].equals("Table")) {
			result = "<table class=\"table table-striped\">";
			result += "<thead><tr><th style=\"text-align: center;\">Predicate</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";

			int i = 1;

			for (Row r : resultRows) {
				if (i > 20) {
					break;
				}

				i++;

				result += "<tr><td data-toggle=\"tooltip\" title=\"" + r.getString(0) + "\">"
						+ RDFgraph.shortenURI(r.getString(0)) + "</td><td>" + Long.toString(r.getLong(1))
						+ "</td></tr>";
			}

			result += "</table>";
		} else if (args[1].equals("Chart")) {
			String X = "";
			String Y = "";

			int i = 1;

			for (Row r : resultRows) {
				if (i > 8) {
					break;
				}

				i++;

				X += RDFgraph.shortenURI(r.getString(0)) + "|";
				Y += Long.toString(r.getLong(1)) + "|";
			}

			result = X + "$" + Y;
		}

		return result;
	}
}
