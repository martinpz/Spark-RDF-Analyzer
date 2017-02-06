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

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * This class calculates node ranking for a given graph & saves it to parquet.
 */
public class CalculateRanking {
	public static String main(String[] args) throws Exception {
		String result = "";

		// Read graph from parquet
		Dataset<Row> graphFrame = Service.spark().read().parquet(Configuration.storage() + args[0] + ".parquet");
		graphFrame.cache().createOrReplaceTempView("Graph");

		// Run SQL over loaded Graph.
		Dataset<Row> resultsFrame = Service.spark().sql("SELECT subject, COUNT(*) as nr FROM Graph GROUP BY subject");
		result = Long.toString(resultsFrame.count());

		// Write results to parquet.
		resultsFrame.write().parquet(Configuration.storage() + args[0] + "Ranking.parquet");

		return result;
	}
}
