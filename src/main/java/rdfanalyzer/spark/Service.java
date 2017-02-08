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

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

/**
 * This class holds all required services for performing computations within the
 * RDF Analyzer. For consistency it follows the singleton pattern to have
 * exactly one instance of each service.
 * 
 * @author marcoprobst
 */
public class Service {
	/**
	 * Private inner class which not gets initialized before it is called by the
	 * outer class.
	 */
	private static final class Instance {
		/*
		 * Initialization of class variables will only happen once. In addition
		 * the ClassLoader will implicitly synchronize this initialization.
		 */
		static final JavaSparkContext SPARK_CTX = new JavaSparkContext(Configuration.sparkConf());
		static final SQLContext SQL_CTX = new SQLContext(SPARK_CTX);
	}

	private Service() {
		// Block initialization of this class from elsewhere.
	}

	public static JavaSparkContext sparkCtx() {
		return Instance.SPARK_CTX;
	}

	public static SQLContext sqlCtx() {
		return Instance.SQL_CTX;
	}

	/**
	 * Stops all services.
	 */
	public static void shutdown() {
		Instance.SQL_CTX.clearCache();
		Instance.SPARK_CTX.cancelAllJobs();
		Instance.SPARK_CTX.stop();
	}
}
