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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class CollapsedGraph {
	public static List<CollapsedEntry> cachedData = new ArrayList<CollapsedEntry>();

	public static String main(String[] args) throws Exception {

		String result = "";
		/*
		 * Check if arguments have been passed.
		 */
		if (args.length != 2) {
			System.out.println("Missing Arguments <INPUT>");
			System.exit(0);
		}

		/*
		 * Read graph from parquet
		 */
		DataFrame schemaRDF = WebService.sqlContext
				.parquetFile(Configuration.properties.getProperty("Storage") + args[0] + ".parquet");
		schemaRDF.cache().registerTempTable("Graph");

		// SQL can be run over RDDs that have been registered as tables.
		DataFrame predicatesFrame = WebService.sqlContext
				.sql("SELECT t2o AS s, t1p AS p, t4o AS o, Count(*) AS nr FROM (SELECT t1.subject AS t1s, t2.object AS t2o, t1.predicate AS t1p, t1.object AS t1o FROM Graph t1, Graph t2"
						+ " WHERE (t2.predicate = '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>') AND t1.subject = t2.subject) MyTable1, "
						+ "(SELECT t3.object AS t3o, t3.subject AS t3s, t3.predicate AS t3p, t4.object AS t4o FROM Graph t3, Graph t4"
						+ " WHERE (t4.predicate = '<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>') AND t3.object = t4.subject) MyTable2"
						+ " WHERE t1s=t3s AND t1p=t3p AND t1o=t3o GROUP BY t2o, t1p, t4o ORDER BY t1p");

		// DataFrame collapsedFrame = sqlContext.sql("Select * FROM EdgesTable
		// ORDER BY p");
		// The results of SQL queries are DataFrames and support all the normal
		// RDD operations.

		Row[] resultRows = predicatesFrame.collect();

		result = "<table class=\"table table-striped\">";
		result += "<thead><tr><th style=\"text-align: center;\">Subject</th><th style=\"text-align: center;\">Predicate</th><th style=\"text-align: center;\">Object</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";

		Set<String> subjects = new HashSet<String>();
		String predicate = "";
		Set<String> objects = new HashSet<String>();
		Long sum = (long) 0.0;
		cachedData.clear();
		for (Row r : resultRows) {
			if (!predicate.equals(r.getString(1))) {
				if (!predicate.equals("")) {
					result += generateRow(subjects, predicate, objects, sum, args[1]);
				}
				predicate = r.getString(1);
				subjects.clear();
				objects.clear();
				sum = (long) 0.0;
			}
			subjects.add(r.getString(0));
			objects.add(r.getString(2));
			sum += r.getLong(3);

		}

		result += generateRow(subjects, predicate, objects, sum, args[1]);
		result += "</table>";
		if (args[1].equals("Table")) {
			return result;
		} else {
			result = "";
			Set<String> classNames = new HashSet<String>();

			for (CollapsedEntry objEntry : cachedData) {
				String tempClass = "";
				for (String s : objEntry.subjects) {
					tempClass += Configuration.shortenURI(s) + "-";
				}
				tempClass = tempClass.substring(0, tempClass.length() - 1);
				classNames.add(tempClass);
				tempClass = "";
				for (String o : objEntry.objects) {
					tempClass += Configuration.shortenURI(o) + "-";
				}
				tempClass = tempClass.substring(0, tempClass.length() - 1);
				classNames.add(tempClass);
			}

			int[][] matrix = new int[classNames.size()][classNames.size()];
			for (int[] row : matrix)
				Arrays.fill(row, 0);
			String[] classNamesArray = new String[classNames.size()];
			String strClasses = "";
			int k = 0;
			for (String s : classNames) {
				strClasses += s + ",";
				classNamesArray[k] = s;
				k++;
			}
			for (CollapsedEntry objEntry : cachedData) {
				String class1 = "";
				String class2 = "";
				Long nrEntries = objEntry.sum;
				for (String s : objEntry.subjects) {
					class1 += Configuration.shortenURI(s) + "-";
				}
				class1 = class1.substring(0, class1.length() - 1);

				for (String o : objEntry.objects) {
					class2 += Configuration.shortenURI(o) + "-";
				}
				class2 = class2.substring(0, class2.length() - 1);

				matrix[ArrayUtils.indexOf(classNamesArray, class1)][ArrayUtils.indexOf(classNamesArray,
						class2)] += Integer.parseInt(Long.toString(nrEntries));
			}

			for (int i = 0; i < classNames.size(); i++) {
				for (int j = 0; j < classNames.size(); j++) {
					result += Integer.toString(matrix[i][j]) + ",";
				}
				result = result.substring(0, result.length() - 1);
				result += "$";
			}

			strClasses = strClasses.substring(0, strClasses.length() - 1);
			result = result.substring(0, result.length() - 1);
			result += "&" + strClasses;
			return result;
		}

	}

	public static String generateRow(Set<String> subjects, String predicate, Set<String> objects, long sum,
			String Type) {
		String result = "";
		if (Type.equals("Table")) {
			String strSubjects = "";
			String strObjects = "";

			for (String s : subjects) {
				strSubjects += "<span data-toggle=\"tooltip\" title=\"" + s + "\">" + Configuration.shortenURI(s)
						+ "</span><br>";
			}

			for (String o : objects) {
				strObjects += "<span data-toggle=\"tooltip\" title=\"" + o + "\">" + Configuration.shortenURI(o)
						+ "</span><br>";
			}

			result = "<tr><td>" + strSubjects + "</td><td><span data-toggle=\"tooltip\" title=\"" + predicate + "\">"
					+ Configuration.shortenURI(predicate) + "</span></td><td>" + strObjects + "</td><td>"
					+ Long.toString(sum) + "</td></tr>";
			return result;
		} else {
			CollapsedEntry objEntry = new CollapsedEntry();

			objEntry.subjects.addAll(subjects);
			objEntry.objects.addAll(objects);
			objEntry.predicate = predicate;
			objEntry.sum = sum;
			cachedData.add(objEntry);
			return "";
		}

	}

	public static String partialRead(String inputSubject) {

		String result = "";

		result = "<table class=\"table table-striped\">";
		result += "<thead><tr><th colspan=\"4\" style=\"text-align: center;\"><b>Class: " + inputSubject
				+ "</b></th></tr>";
		result += "<thead><tr><th style=\"text-align: center;\">Subject</th><th style=\"text-align: center;\">Predicate</th><th style=\"text-align: center;\">Object</th><th style=\"text-align: center;\">Nr. of occurencies</th></tr></thead>";

		for (CollapsedEntry objEntry : cachedData) {
			String subjects = "";
			String objects = "";

			for (String s : objEntry.subjects) {
				subjects += Configuration.shortenURI(s) + "-";
			}
			subjects = subjects.substring(0, subjects.length() - 1);

			for (String o : objEntry.objects) {
				objects += Configuration.shortenURI(o) + "-";
			}
			objects = objects.substring(0, objects.length() - 1);

			if (inputSubject.equals(subjects) || inputSubject.equals(objects)) {
				result += generateRow(objEntry.subjects, objEntry.predicate, objEntry.objects, objEntry.sum, "Table");
			}

		}

		result += "</table>";

		return result;
	}
}

class CollapsedEntry {
	Set<String> subjects = new HashSet<String>();
	String predicate = "";
	Set<String> objects = new HashSet<String>();
	Long sum = (long) 0.0;

}