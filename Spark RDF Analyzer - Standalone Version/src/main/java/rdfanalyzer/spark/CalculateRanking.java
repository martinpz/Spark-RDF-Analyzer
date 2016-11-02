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


public class CalculateRanking {
	public static String main(String[] args) throws Exception {

		 String result = "";
		
		/*
		 * Read graph from parquet 
		 */
	   DataFrame schemaRDF = WebService.sqlContext.parquetFile(Configuration.properties.getProperty("Storage")+args[0]+".parquet");
	   schemaRDF.cache().registerTempTable("Graph");



	   // SQL can be run over RDDs that have been registered as tables.
	   DataFrame predicatesFrame = WebService.sqlContext.sql("SELECT subject, COUNT(*) as nr FROM Graph GROUP BY subject");
	   result = Long.toString(predicatesFrame.count());
       predicatesFrame.write().parquet(Configuration.properties.getProperty("Storage")+args[0]+"Ranking.parquet");
	   return result;

	   }
}