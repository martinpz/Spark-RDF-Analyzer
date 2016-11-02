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
/*
 * This class calculates AVG, MIN, and MAX out-degree for a given graph.
 */
public class OutDegree {
	public static String main(String[] args) throws Exception {

	   String result = "";
	   /*
		* Read graph from parquet 
		*/
	   DataFrame graphFrame = WebService.sqlContext.parquetFile(Configuration.properties.getProperty("Storage")+args[0]+".parquet");
	   graphFrame.cache().registerTempTable("Graph");

	   /*
	    * Run SQL over loaded Graph.
		*/
	   DataFrame resultsFrame = WebService.sqlContext.sql("SELECT "+args[1]+"(degree) FROM (SELECT subject, COUNT(predicate) AS degree FROM Graph"
		   		+ " GROUP BY subject) MyTable1");

	   /*
	    * Format output results. Based if input is AVG, MIN or MAX.
	    */ 
	   Row[] rows = resultsFrame.collect();
	   if(args[1].equals("MIN") || args[1].equals("MAX"))
	   {
		   result = Long.toString(rows[0].getLong(0));
	   }
	   else
	   {
	   result = Double.toString(rows[0].getDouble(0));
	   result = result.substring(0,4);
	   }

	   return "<h1>"+args[1]+": "+result+"</h1>";


	   }
}
