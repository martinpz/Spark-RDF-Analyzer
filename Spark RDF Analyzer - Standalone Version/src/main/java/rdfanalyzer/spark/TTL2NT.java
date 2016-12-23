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

import java.util.Hashtable;
import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

// TODO: This class is not even present in the cluster version.
// Maybe we can simply remove it or keep it fore reference?!
public class TTL2NT {
	public static final Hashtable<String, String> prefixHashtable = new Hashtable<String, String>();

	public static DataFrame main() throws Exception {

		/*
		 * Converting input from turtle format to n-triples
		 */
		// Get all prefixes
		DataFrame prefixes = WebService.sqlContext
				.sql("Select Graph.predicate, Graph.object from Graph WHERE Graph.subject='@prefix'");
		prefixes.cache().registerTempTable("Prefixes");
		List<String> prefixList = prefixes.toJavaRDD().map(new Function<Row, String>() {
			public String call(Row row) {
				return row.getString(0) + " " + row.getString(1);
			}
		}).collect();

		// Put all prefixes in a hashtable
		for (String name : prefixList) {

			String[] splitted = name.split(" ");
			prefixHashtable.put(splitted[0], splitted[1]);
		}
		// // Add special cases.
		// prefixHashtable.put("a:",
		// "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");

		// Get rows that contains data.
		DataFrame rows = WebService.sqlContext.sql("Select * from Graph WHERE Graph.subject<>'@prefix'");
		rows.cache().registerTempTable("Rows");

		// Replaces the short resource with the full URI
		UDF1<String, String> replacePrefix = new UDF1<String, String>() {

			public String call(String resource) throws Exception {

				try {
					String prefix = "";

					if (resource.startsWith("\"")) {
						return resource;
					} else if (resource.equalsIgnoreCase("a")) {
						return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
					} else {
						String[] resource_prefix = resource.split(":");
						if (resource_prefix.length == 2) {
							prefix = prefixHashtable.get(resource_prefix[0] + ":");
							prefix = prefix.substring(0, prefix.length() - 1);
							String fullURI = prefix + resource_prefix[1] + ">";
							return fullURI;
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage() + "\r\n" + e.getLocalizedMessage() + "\r\n" + e.getStackTrace());
				}

				return "Unable to process... " + resource;
			}
		};
		// Register the udf in order to use it in the sql context
		WebService.sqlContext.udf().register("replacePrefix", replacePrefix, DataTypes.StringType);

		// Convert the file format from Turtle to N-triples.
		DataFrame output = WebService.sqlContext
				.sql("Select replacePrefix(subject) as subject, replacePrefix(predicate) as predicate, replacePrefix(object) as object "
						+ "FROM Rows ");

		return output;
	}
}