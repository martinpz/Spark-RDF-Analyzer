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

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;

/**
 * This class loads the graph stored in HDFS. If the graph is already in nTriple
 * format it saves to a parquet file. Otherwise the file is first converted from
 * turtle to nTriple and then saved to parquet.
 */
public class GraphLoader {
	public static String main(String Input, String Name, Boolean nTriple) throws Exception {
		String result = "";

		// Normalize input
		Input = Input.replace('$', '/');

		// Check if input is in turtle format.
		if (!nTriple) {
			// TODO: Not done in Cluster.
			// TTL2NTV2.prefixHashtable.clear();
			result = TTL2NTV2.main(Input, Name);
			return result;
		}

		// Load a text file and convert each line to a Java Bean.
		JavaRDD<RDFgraph> RDF = Service.sparkCtx().textFile(Input + "/*", 18).map(new Function<String, RDFgraph>() {
			public RDFgraph call(String line) {
				String[] parts = line.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				RDFgraph entry = new RDFgraph();

				if (parts[1].length() > 1) {
					entry.setSubject(parts[0]);
					entry.setPredicate(parts[1]);
					entry.setObject(parts[2]);
				}

				return entry;
			}
		});

		// Apply a schema to an RDD of Java Beans and register it as a table.
		DataFrame schemaRDF = Service.sqlCtx().createDataFrame(RDF, RDFgraph.class);

		String storageDir = Configuration.storage();
		schemaRDF.saveAsParquetFile(storageDir + Name + ".parquet");
		// TODO: Following is from Cluster.
		// schemaRDF.saveAsParquetFile("parquet/"+Name+".parquet");

		result = "Success";

		String[] rankingArguments = { Name };
		CalculateRanking.main(rankingArguments);

		return result;
	}
}
