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

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

/**
 * This class calculates Chord Data and formats them for required output: either
 * Chord or Table.
 */
public class EdgeFinder {
	public static Row[] cachedRows;

	public static String main(String[] args) throws Exception {
		String result = "";

		// Read graph from parquet
		DataFrame graphFrame = WebService.sqlContext
				.parquetFile(Configuration.properties.getProperty("Storage") + args[0] + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		// Run SQL over loaded Graph.
		DataFrame resultsFrame = WebService.sqlContext
				.sql("SELECT t2o AS s, t1p AS p, t4o AS o, Count(*) AS nr FROM (SELECT t1.subject AS t1s, t2.object AS t2o, t1.predicate AS t1p, t1.object AS t1o FROM Graph t1, Graph t2"
						+ " WHERE (t2.predicate = '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>') AND t1.subject = t2.subject) MyTable1, "
						+ "(SELECT t3.object AS t3o, t3.subject AS t3s, t3.predicate AS t3p, t4.object AS t4o FROM Graph t3, Graph t4"
						+ " WHERE (t4.predicate = '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>') AND t3.object = t4.subject) MyTable2"
						+ " WHERE t1s=t3s AND t1p=t3p AND t1o=t3o GROUP BY t2o, t1p, t4o");

		// Format output based on if output is table or chart
		Row[] resultRows = resultsFrame.collect();

		if (args[1].equals("Table")) {
			result = "<table class=\"table table-striped\">";
			result += "<thead><tr><th style=\"text-align: center;\">Subject</th><th style=\"text-align: center;\">Predicate</th><th style=\"text-align: center;\">Object</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";

			for (Row r : resultRows) {
				result += "<tr><td data-toggle=\"tooltip\" title=\"" + r.getString(0) + "\">"
						+ Configuration.shortenURI(r.getString(0)) + "</td><td data-toggle=\"tooltip\" title=\""
						+ r.getString(1) + "\">" + Configuration.shortenURI(r.getString(1))
						+ "</td><td data-toggle=\"tooltip\" title=\"" + r.getString(2) + "\">"
						+ Configuration.shortenURI(r.getString(2)) + "</td><td>" + Long.toString(r.getLong(3))
						+ "</td></tr>";
			}

			result += "</table>";
		} else if (args[1].equals("Chart")) {
			cachedRows = resultRows;

			String strClasses = GetClasses.main(args);
			String[] classNames = strClasses.split(",");

			int[][] matrix = new int[classNames.length][classNames.length];

			for (int[] row : matrix) {
				Arrays.fill(row, 0);
			}

			for (Row r : resultRows) {
				matrix[ArrayUtils.indexOf(classNames, Configuration.shortenURI(r.getString(0)))][ArrayUtils
						.indexOf(classNames, Configuration.shortenURI(r.getString(2)))] += Integer
								.parseInt(Long.toString(r.getLong(3)));
			}

			for (int i = 0; i < classNames.length; i++) {
				for (int j = 0; j < classNames.length; j++) {
					result += Integer.toString(matrix[i][j]) + ",";
				}

				result = result.substring(0, result.length() - 1);
				result += "$";
			}

			result = result.substring(0, result.length() - 1);
			result += "&" + strClasses;
		}

		return result;
	}

	/**
	 * This method offers partial reading of results when chord diagram is
	 * clicked.
	 * 
	 * @param inputSubject
	 * @return
	 */
	public static String partialRead(String inputSubject) {
		String result = "<table class=\"table table-striped\">";
		result += "<thead><tr><th colspan=\"4\" style=\"text-align: center;\">Class: " + inputSubject + "</th></tr>";
		result += "<tr><th style=\"text-align: center;\">Subject</th><th style=\"text-align: center;\">Predicate</th><th style=\"text-align: center;\">Object</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";

		for (Row r : cachedRows) {
			String Subject = Configuration.shortenURI(r.getString(0));
			String Object = Configuration.shortenURI(r.getString(2));

			if (Subject.equals(inputSubject) || Object.equals(inputSubject)) {
				result += "<tr><td data-toggle=\"tooltip\" title=\"" + r.getString(0) + "\">" + Subject
						+ "</td><td data-toggle=\"tooltip\" title=\"" + r.getString(1) + "\">"
						+ Configuration.shortenURI(r.getString(1)) + "</td><td data-toggle=\"tooltip\" title=\""
						+ r.getString(2) + "\">" + Object + "</td><td>" + Long.toString(r.getLong(3)) + "</td></tr>";
			}
		}

		return result + "</table>";
	}
}
