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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.spark.SparkConf;

/**
 * This class is used to (1) read configuration files and (2) set up the Spark
 * Configuration.
 */
public class Configuration {
	public static Properties properties = new Properties();

	/**
	 * Reads the configuration files and stores loaded properties.
	 */
	public Configuration() {
		try {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream("app.properties");
			properties.load(is);
		} catch (IOException e) {
			// TODO: This was removed on Cluster!
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the new Spark Configuration.
	 * 
	 * @return new SparkConf
	 */
	public static SparkConf getSparkConf() {
		SparkConf sparkConf = new SparkConf();

		sparkConf.setAppName("Spark RDF Analyzer");
		sparkConf.setMaster(Configuration.properties.getProperty("SparkMaster"));

		sparkConf.set("spark.executor.memory", "2g");
		sparkConf.set("spark.sql.parquet.binaryAsString", "true");
		sparkConf.set("spark.core.connection.ack.wait.timeout", "200");
		sparkConf.set("spark.core.connection.auth.wait.timeout", "200");
		sparkConf.set("spark.akka.timeout", "200");
		sparkConf.set("spark.storage.blockManagerSlaveTimeoutMs", "200000");
		sparkConf.set("spark.shuffle.io.connectionTimeout", "200");
		sparkConf.set("spark.sql.parquet.filterPushdown", "true");
		sparkConf.set("spark.rdd.compress", "true");
		sparkConf.set("spark.default.parallelism", "32");
		sparkConf.set("spark.sql.inMemoryColumnarStorage.compressed", "true");
		sparkConf.set("spark.sql.shuffle.partitions", "32");

		// sparkConf.set("spark.eventLog.enabled","true");
		// sparkConf.set("spark.eventLog.dir",
		// "hdfs://isydney.informatik.uni-freiburg.de:8020/user/teamprojekt2015/logs/");
		// sparkConf.set("spark.sql.codegen", "true");

		return sparkConf;
	}

	/**
	 * Sets system properties.
	 */
	public static void setOthers() {
		WebService.ctx.hadoopConfiguration().set("fs.defaultFS",
				"hdfs://sydney.informatik.privat:8020/user/teamprojekt2015/");
		System.setProperty("HADOOP_USER_NAME", Configuration.properties.getProperty("HadoopUser"));
	}
}
