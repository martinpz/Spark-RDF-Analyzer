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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class TTL2NTV2 {

	public static Hashtable<String, String> prefixHashtable = new Hashtable<String, String>();
	static Broadcast<Hashtable<String, String>> broadcastPrefixes;

	public static String main(String Input, String Name) throws Exception {

		String result = "";
		System.out.println("=== Data source: RDD ===");
		// Load a text file and convert each line to a Java Bean.

		/*
		 * Read Prefixes
		 */
		JavaRDD<RDFgraph> RDF1 = WebService.ctx.textFile(Input + "/*", 18).map(new Function<String, RDFgraph>() {
			public RDFgraph call(String line) {

				String[] parts = line.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

				RDFgraph entry = new RDFgraph();
				if (parts[0].equals("@prefix")) {
					entry.setSubject(parts[0]);
					entry.setPredicate(parts[1]);
					entry.setObject(parts[2]);
				} else {
					entry.setSubject("E");
					entry.setPredicate("E");
					entry.setObject("E");
				}

				return entry;

			}
		});
		DataFrame schemaRDF1 = WebService.sqlContext.createDataFrame(RDF1, RDFgraph.class);
		schemaRDF1.registerTempTable("Prefixes");

		DataFrame prefixesFrame = WebService.sqlContext
				.sql("SELECT subject,predicate,object FROM Prefixes Where subject NOT LIKE 'E'");
		Row[] prefixes = prefixesFrame.collect();
		/*
		 * Put prefixes to a hashList.
		 */
		for (Row r : prefixes) {
			prefixHashtable.put(r.getString(1), r.getString(2));
		}
		/*
		 * Broadcast prefixes to all nodes.
		 */
		broadcastPrefixes = WebService.ctx.broadcast(prefixHashtable);

		/*
		 * Read Graph and replace prefixes
		 */
		JavaRDD<RDFgraph> RDF = WebService.ctx.textFile(Input + "/*", 18).map(new Function<String, RDFgraph>() {
			public RDFgraph call(String line) {

				String[] parts = line.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				RDFgraph entry = new RDFgraph();
				if (!parts[0].equals("@prefix")) {
					entry.setSubject(replacePrefix(parts[0]));
					entry.setPredicate(replacePrefix(parts[1]));
					entry.setObject(replacePrefix(parts[2]));
				}

				return entry;

			}
		});

		// Apply a schema to an RDD of Java Beans and register it as a table.
		DataFrame schemaRDF = WebService.sqlContext.createDataFrame(RDF, RDFgraph.class);

		String storageDir = Configuration.properties.getProperty("Storage");
		schemaRDF.saveAsParquetFile(storageDir + Name + ".parquet");
		result = "Success";

		String[] rankingArguments = { Name };
		CalculateRanking.main(rankingArguments);

		return result;

	}

	/*
	 * Replaces prefix for each node.
	 */
	public static String replacePrefix(String resource) {
		String prefix = "";

		if (resource.startsWith("\"")) {
			return resource;
		} else if (resource.equalsIgnoreCase("a")) {
			return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		} else {
			String[] resource_prefix = resource.split(":");
			if (resource_prefix.length == 2) {
				prefix = broadcastPrefixes.value().get(resource_prefix[0] + ":");
				prefix = prefix.substring(0, prefix.length() - 1);
				String fullURI = prefix + resource_prefix[1] + ">";
				return fullURI;
			} else {
				return " ";
			}
		}

	}
}
