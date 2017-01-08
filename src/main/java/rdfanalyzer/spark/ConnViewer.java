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
 * This is the main class of Connectivity Viewer module It communicates with
 * other modules and calculates connections based on user parameters.
 */
public class ConnViewer {
	public static ConnAdapter objAdapter = new ConnAdapter();
	public static boolean Update = false;
	public static int Counter = 0;

	public static void main(String startN, String endN, String graphName, String Predicates, String searchPattern) {
		searchPattern = searchPattern.substring(0, searchPattern.length() - 1);
		String[] Pattern = searchPattern.split(",");
		objAdapter.reset();

		String calculationGraphName = preprocess(graphName, Predicates);

		ConnViewerHelper.generateQueries(startN, endN, calculationGraphName);
		ConnViewerHelper.fillLists();
		objAdapter.setStartNode(RDFgraph.shortenURI(startN));
		objAdapter.setEndNode(RDFgraph.shortenURI(endN));

		Counter = 0;

		// For each pattern check if it is wanted by user.
		// In addition check if the maximum number of results is not exceeded.
		if (Pattern[0].equals("1"))
			createTable(ConnViewerHelper.strQuery01, "tbl01");
		if (Counter <= 30 && Pattern[1].equals("1"))
			createTable(ConnViewerHelper.strQuery02, "tbl02");

		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery03, "tbl03");
		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery04, "tbl04");
		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery05, "tbl05");
		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery06, "tbl06");

		if (Counter <= 30 && Pattern[2].equals("1"))
			createTable(ConnViewerHelper.strQuery11, "tbl11");
		if (Counter <= 30 && Pattern[3].equals("1"))
			createTable(ConnViewerHelper.strQuery12, "tbl12");
		if (Counter <= 30 && Pattern[4].equals("1"))
			createTable(ConnViewerHelper.strQuery13, "tbl13");
		if (Counter <= 30 && Pattern[5].equals("1"))
			createTable(ConnViewerHelper.strQuery14, "tbl14");

		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery15, "tbl15");
		if (Counter <= 30 && Pattern[6].equals("1"))
			createTable(ConnViewerHelper.strQuery21, "tbl21");
		if (Counter <= 30 && Pattern[7].equals("1"))
			createTable(ConnViewerHelper.strQuery22, "tbl22");

		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery16, "tbl16");
		if (Counter <= 30 && Pattern[8].equals("1"))
			createTable(ConnViewerHelper.strQuery23, "tbl23");
		if (Counter <= 30 && Pattern[9].equals("1"))
			createTable(ConnViewerHelper.strQuery24, "tbl24");

		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery17, "tbl17");
		if (Counter <= 30 && Pattern[10].equals("1"))
			createTable(ConnViewerHelper.strQuery25, "tbl25");
		if (Counter <= 30 && Pattern[11].equals("1"))
			createTable(ConnViewerHelper.strQuery26, "tbl26");

		if (Counter <= 30)
			createTable(ConnViewerHelper.strQuery18, "tbl18");
		if (Counter <= 30 && Pattern[12].equals("1"))
			createTable(ConnViewerHelper.strQuery27, "tbl27");
		if (Counter <= 30 && Pattern[13].equals("1"))
			createTable(ConnViewerHelper.strQuery27, "tbl27");

		objAdapter.End = true;
	}

	/**
	 * This method calculates a single pattern. If it is an output pattern then
	 * it forwards the result to adapter.
	 * 
	 * @param Query
	 * @param tableName
	 */
	public static void createTable(String Query, String tableName) {
		DataFrame predicatesFrame = Service.sqlCtx().sql(Query);
		predicatesFrame.registerTempTable(tableName);

		if (ConnViewerHelper.outputTables.contains(tableName)) {
			Row[] rows = predicatesFrame.collect();

			if ((Counter + rows.length) <= 30) {
				objAdapter.UpdateResults(rows, tableName);
			} else {
				Row[] rows2 = new Row[30 - Counter];
				int j = 0;

				for (int i = Counter; i < 30; i++) {
					rows2[j] = rows[j];
					j++;
				}

				objAdapter.UpdateResults(rows2, tableName);
				Counter = 300;
			}
		}
	}

	public static String preprocess(String graphName, String Predicates) {
		String[] list = Predicates.split("--");
		String Sign = "=";

		if (list[0].equals("Exclude")) {
			Sign = "!=";
		}

		if (list.length > 1) {
			String trimmedString = list[1].substring(0, list[1].length() - 1);
			String[] predicatesList = trimmedString.split(",");
			String Condition = "";

			for (int i = 0; i < (predicatesList.length); i++) {
				predicatesList[i] = predicatesList[i].replace('$', '/');
				predicatesList[i] = predicatesList[i].replace('&', '#');

				Condition += " predicate " + Sign + " '" + predicatesList[i] + "' ";

				if (Sign.equals("=")) {
					Condition += "OR";
				} else {
					Condition += "AND";
				}
			}

			Condition = Condition.substring(0, Condition.length() - 3);

			// Read graph from parquet
			DataFrame schemaRDF = Service.sqlCtx().parquetFile(Configuration.storage() + graphName + ".parquet");
			schemaRDF.cache().registerTempTable("Graph");

			DataFrame predicatesFrame = Service.sqlCtx()
					.sql("SELECT subject, predicate, object FROM Graph WHERE " + Condition);

			predicatesFrame.registerTempTable("Graph2");
			

			return "Graph2";
		} else {
			// Read graph from parquet
			DataFrame schemaRDF = Service.sqlCtx().parquetFile(Configuration.storage() + graphName + ".parquet");
			schemaRDF.cache().registerTempTable("Graph");

			return "Graph";
		}
	}
}
