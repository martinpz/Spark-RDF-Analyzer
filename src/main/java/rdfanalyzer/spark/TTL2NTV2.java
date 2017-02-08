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

/**
 * This class converts a graph from Turtle to nTripple format and saves it to
 * parquet.
 */
public class TTL2NTV2 {
	public static Hashtable<String, String> prefixHashtable = new Hashtable<String, String>();
	static Broadcast<Hashtable<String, String>> broadcastPrefixes;

	public static JavaRDD<RDFgraph> main(String Input, String Name) throws Exception {
		// Read Prefixes.
		JavaRDD<RDFgraph> RDF1 = Service.sparkCtx().textFile(Input + "/*", Configuration.numPartitions())
				.map(new Function<String, RDFgraph>() {
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

		DataFrame schemaRDF1 = Service.sqlCtx().createDataFrame(RDF1, RDFgraph.class);
		schemaRDF1.registerTempTable("Prefixes");

		DataFrame prefixesFrame = Service.sqlCtx()
				.sql("SELECT subject,predicate,object FROM Prefixes Where subject NOT LIKE 'E'");
		Row[] prefixes = prefixesFrame.collect();

		// Put prefixes to a hashList.
		for (Row r : prefixes) {
			prefixHashtable.put(r.getString(1), r.getString(2));
		}

		// Broadcast prefixes to all nodes.
		broadcastPrefixes = Service.sparkCtx().broadcast(prefixHashtable);

		// Read Graph and replace prefixes
		JavaRDD<RDFgraph> RDF = Service.sparkCtx().textFile(Input + "/*", Configuration.numPartitions())
				.map(new Function<String, RDFgraph>() {
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

		return RDF;
	}

	/**
	 * Replaces prefix for each node.
	 * 
	 * @param resource
	 * @return full URI
	 */
	public static String replacePrefix(String resource) {
		if (resource.startsWith("\"")) {
			return resource;
		} else if (resource.equalsIgnoreCase("a")) {
			return "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
		} else {
			String[] resource_prefix = resource.split(":");

			if (resource_prefix.length == 2) {
				String prefix = broadcastPrefixes.value().get(resource_prefix[0] + ":");
				prefix = prefix.substring(0, prefix.length() - 1);

				// Return the full URI.
				return prefix + resource_prefix[1] + ">";
			} else {
				return " ";
			}
		}
	}
}
