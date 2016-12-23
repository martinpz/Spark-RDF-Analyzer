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
 * This class calculates the number of nodes for a given graph.
 */
public class CountNodes {
	public static String main(String[] args) throws Exception {
		String result = "";

		// Check if arguments have been passed.
		// TODO: Not done in Cluster version.
		if (args.length != 1) {
			System.out.println("Missing Arguments <INPUT>");
			System.exit(0);
		}

		// Read graph from parquet
		DataFrame graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + args[0] + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		// Run SQL over loaded Graph.
		DataFrame resultsFrame = Service.sqlCtx()
				.sql("SELECT COUNT(DISTINCT MyTable1.subject) FROM (SELECT subject FROM Graph"
						+ " UNION ALL SELECT object FROM Graph" + " ) MyTable1");

		// Get the results and format them in desired format.
		Row[] rows = resultsFrame.collect();
		result = Long.toString(rows[0].getLong(0));

		return "<h1>" + result + "</h1>";
	}
}
