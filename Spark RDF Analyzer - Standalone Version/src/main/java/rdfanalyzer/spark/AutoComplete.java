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
 * This class deals with autocomplete of URI search for Connectivity Viewer.
 */
public class AutoComplete {
	public static String main(String graphName, String userInput, String Type) throws Exception {
		String result = "";
		userInput = userInput.toLowerCase();

		// Read graph from parquet
		DataFrame schemaRDF = Service.sqlCtx().parquetFile(Configuration.props("storage") + graphName + ".parquet");
		schemaRDF.cache().registerTempTable("Graph");

		// Predicate OR node Autocomplete.
		if (Type.equals("Predicate")) {
			// Get list of predicates
			// SQL can be run over RDDs that have been registered as tables.
			DataFrame predicatesListFrame = Service.sqlCtx()
					.sql("SELECT predicate as predicate FROM Graph GROUP BY predicate");
			predicatesListFrame.cache().registerTempTable("Predicates");

			// Search for matching predicates
			DataFrame firstRound = Service.sqlCtx()
					.sql("SELECT predicate AS match, regexp_extract(predicate, '([^/]+$)') AS matchExtracted1, regexp_extract(predicate, '([^#]+$)') AS matchExtracted2 FROM Predicates WHERE LOWER(predicate) LIKE '%"
							+ userInput + "%'");
			firstRound.registerTempTable("firstRound");

			DataFrame secondRound = Service.sqlCtx().sql("SELECT match " + " FROM firstRound "
					+ " WHERE matchExtracted1 = '" + userInput + ">' OR matchExtracted2 = '" + userInput + ">'");

			DataFrame thirdRound = Service.sqlCtx()
					.sql("SELECT DISTINCT match, matchExtracted FROM "
							+ "(SELECT match, matchExtracted1 AS matchExtracted" + " FROM firstRound "
							+ " WHERE LOWER(matchExtracted1) LIKE '" + userInput + "%' " + " UNION ALL "
							+ " SELECT match, matchExtracted2 AS matchExtracted" + " FROM firstRound	 "
							+ " WHERE LOWER(matchExtracted2) LIKE '" + userInput + "%') MyTable " + " LIMIT 20");

			// The results of SQL queries are DataFrames and support all the
			// normal RDD operations. Save result to file
			// Perform search in multiple rounds.
			Row[] rows = secondRound.collect();

			if (rows.length > 0) {
				String shortURI = RDFgraph.shortenURI(rows[0].getString(0));
				String fullURI = "<xmp style=\"display : inline\">" + rows[0].getString(0) + "</xmp>";
				String fullText = "<b>" + shortURI + " :</b> " + fullURI;
				String fullValue = shortURI + ":" + rows[0].getString(0);
				result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\"" + fullValue
						+ "\">" + fullText + "</label></div>";
			}

			Row[] rows2 = thirdRound.collect();

			for (Row r : rows2) {
				if (rows.length > 0) {
					if (!r.getString(0).equals(rows[0].getString(0))) {
						String shortURI = RDFgraph.shortenURI(r.getString(0));
						String fullURI = "<xmp style=\"display : inline\">" + r.getString(0) + "</xmp>";
						String fullText = "<b>" + shortURI + " :</b> " + fullURI;
						String fullValue = shortURI + ":" + r.getString(0);
						result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\""
								+ fullValue + "\">" + fullText + "</label></div>";
					}
				} else {
					String shortURI = RDFgraph.shortenURI(r.getString(0));
					String fullURI = "<xmp style=\"display : inline\">" + r.getString(0) + "</xmp>";
					String fullText = "<b>" + shortURI + " :</b> " + fullURI;
					String fullValue = shortURI + ":" + r.getString(0);
					result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\"" + fullValue
							+ "\">" + fullText + "</label></div>";
				}
			}
		} else {
			DataFrame firstRound = Service.sqlCtx()
					.sql("SELECT subject AS match, regexp_extract(subject, '([^/]+$)', 0) AS matchExtracted1, regexp_extract(subject, '([^#]+$)', 0) AS matchExtracted2  FROM Graph WHERE LOWER(subject) LIKE '%"
							+ userInput + "%' UNION ALL "
							+ "SELECT object AS match, regexp_extract(object, '([^/]+$)', 0) AS matchExtracted1, regexp_extract(object, '([^#]+$)', 0) AS matchExtracted2 FROM Graph WHERE LOWER(object) LIKE '%"
							+ userInput + "%' AND object NOT LIKE '\"%'");
			firstRound.registerTempTable("firstRound");

			DataFrame secondRound = Service.sqlCtx().sql("SELECT match " + " FROM firstRound "
					+ " WHERE matchExtracted1 = '" + userInput + ">' OR matchExtracted2 = '" + userInput + ">'");

			DataFrame thirdRound = Service.sqlCtx()
					.sql("SELECT DISTINCT match, matchExtracted FROM "
							+ "(SELECT match, matchExtracted1 AS matchExtracted" + " FROM firstRound "
							+ " WHERE LOWER(matchExtracted1) LIKE '" + userInput + "%' " + " UNION ALL "
							+ " SELECT match, matchExtracted2 AS matchExtracted" + " FROM firstRound	 "
							+ " WHERE LOWER(matchExtracted2) LIKE '" + userInput + "%') MyTable ");
			thirdRound.registerTempTable("thirdRound");

			schemaRDF = Service.sqlCtx().parquetFile(Configuration.props("Storage") + graphName + "Ranking.parquet");
			schemaRDF.cache().registerTempTable("Ranking");

			DataFrame fourthRound = Service.sqlCtx().sql("SELECT thirdRound.match FROM " + " thirdRound, Ranking "
					+ " WHERE thirdRound.match = Ranking.subject " + " ORDER BY Ranking.nr DESC " + " LIMIT 20");

			Row[] rows = secondRound.collect();

			if (rows.length > 0) {
				String shortURI = RDFgraph.shortenURI(rows[0].getString(0));
				String fullURI = "<xmp style=\"display : inline\">" + rows[0].getString(0) + "</xmp>";
				String fullText = "<b>" + shortURI + " :</b> " + fullURI;
				String fullValue = shortURI + ":" + rows[0].getString(0);
				result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\"" + fullValue
						+ "\">" + fullText + "</label></div>";
			}

			Row[] rows2 = fourthRound.collect();

			for (Row r : rows2) {
				if (rows.length > 0) {
					if (!r.getString(0).equals(rows[0].getString(0))) {
						String shortURI = RDFgraph.shortenURI(r.getString(0));
						String fullURI = "<xmp style=\"display : inline\">" + r.getString(0) + "</xmp>";
						String fullText = "<b>" + shortURI + " :</b> " + fullURI;
						String fullValue = shortURI + ":" + r.getString(0);
						result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\""
								+ fullValue + "\">" + fullText + "</label></div>";
					}
				} else {
					String shortURI = RDFgraph.shortenURI(r.getString(0));
					String fullURI = "<xmp style=\"display : inline\">" + r.getString(0) + "</xmp>";
					String fullText = "<b>" + shortURI + " :</b> " + fullURI;
					String fullValue = shortURI + ":" + r.getString(0);
					result += "<div class=\"radio\"><label><input type=\"radio\" name=\"optradio\" value=\"" + fullValue
							+ "\">" + fullText + "</label></div>";
				}
			}
		}

		return result;
	}
}
