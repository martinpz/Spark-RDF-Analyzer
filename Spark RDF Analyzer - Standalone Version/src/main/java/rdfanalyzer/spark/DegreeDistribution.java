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

public class DegreeDistribution {
	public static String main(String[] args) throws Exception {
		String result = "";

		/*
		 * Read graph from parquet
		 */
		DataFrame schemaRDF = WebService.sqlContext
				.parquetFile(Configuration.properties.getProperty("Storage") + args[0] + ".parquet");
		schemaRDF.cache().registerTempTable("Graph");

		// SQL can be run over RDDs that have been registered as tables.
		DataFrame predicatesFrame = WebService.sqlContext
				.sql("SELECT degree, COUNT(degree) AS instances FROM (SELECT subject, COUNT(predicate)"
						+ " AS degree FROM Graph GROUP BY subject) MyTable1 GROUP BY degree ORDER BY instances DESC");

		// The results of SQL queries are DataFrames and support all the normal
		// RDD operations.

		Row[] resultRows = predicatesFrame.collect();
		if (args[1].equals("Table")) {
			result = "<table class=\"table table-striped\">";
			result += "<thead><tr><th style=\"text-align: center;\">Degree</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";
			int i = 0;
			for (Row r : resultRows) {
				i++;
				if (i == 30) {
					break;
				}
				result += "<tr><td>" + Long.toString(r.getLong(0)) + "</td><td>" + Long.toString(r.getLong(1))
						+ "</td></tr>";
			}
			result += "</table>";
		} else if (args[1].equals("Chart")) {
			String X = "";
			String Y = "";
			int i = 1;
			for (Row r : resultRows) {
				if (i > 10) {
					break;
				}
				i++;
				X += Long.toString(r.getLong(0)) + "|";
				Y += Long.toString(r.getLong(1)) + "|";
			}
			result = X + "$" + Y;
		}
		return result;
	}
}
