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
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.spark.SparkConf;

/**
 * This class is used to (1) read configuration files and (2) set up the Spark
 * Configuration.
 */
public class Configuration {
	/**
	 * Private inner class which not gets initialized before it is called by the
	 * outer class.
	 */
	private static final class ConfigHolder {
		/*
		 * Initialization of class variables will only happen once. In addition
		 * the ClassLoader will implicitly synchronize this initialization.
		 */
		static final Properties PROPERTIES = getProperties();
		static final SparkConf SPARK_CONF = getSparkConf();

		/**
		 * Reads the config files and stores the properties.
		 * 
		 * @return
		 */
		static final Properties getProperties() {
			Properties properties = new Properties();

			try {
				ClassLoader classloader = Thread.currentThread().getContextClassLoader();
				InputStream is = classloader.getResourceAsStream("app.properties");

				properties.load(is);
			} catch (IOException e) {
				// TODO: This was removed on Cluster!
				e.printStackTrace();
			}

			return properties;
		}

		/**
		 * Sets up the new Spark Configuration.
		 * 
		 * @return new SparkConf
		 */
		static final SparkConf getSparkConf() {
			SparkConf sparkConf = new SparkConf();

			// sparkConf.setAppName(PROPERTIES.getProperty("spark.appname"));
			// sparkConf.setMaster(PROPERTIES.getProperty("spark.master"));

			for (Entry<Object, Object> prop : PROPERTIES.entrySet()) {
				String key = (String) prop.getKey();
				String val = (String) prop.getValue();

				System.out.println("Key='" + key + "' => Val='" + val + "'");

				if (key.startsWith("spark")) {
					System.out.println(">>> SETTING IT TO THE CONFIG");
					// sparkConf.set(key, val);
				}
			}

			// sparkConf.set("spark.executor.memory", "2g");
			// sparkConf.set("spark.sql.parquet.binaryAsString", "true");
			// sparkConf.set("spark.core.connection.ack.wait.timeout", "200");
			// sparkConf.set("spark.core.connection.auth.wait.timeout", "200");
			// sparkConf.set("spark.akka.timeout", "200");
			// sparkConf.set("spark.storage.blockManagerSlaveTimeoutMs",
			// "200000");
			// sparkConf.set("spark.shuffle.io.connectionTimeout", "200");
			// sparkConf.set("spark.sql.parquet.filterPushdown", "true");
			// sparkConf.set("spark.rdd.compress", "true");
			// sparkConf.set("spark.default.parallelism", "32");
			// sparkConf.set("spark.sql.inMemoryColumnarStorage.compressed",
			// "true");
			// sparkConf.set("spark.sql.shuffle.partitions", "32");

			// sparkConf.set("spark.eventLog.enabled","true");
			// sparkConf.set("spark.eventLog.dir",
			// "hdfs://isydney.informatik.uni-freiburg.de:8020/user/teamprojekt2015/logs/");
			// sparkConf.set("spark.sql.codegen", "true");

			return sparkConf;
		}
	}

	private Configuration() {
		// Block initialization of this class from other methods.
	}

	public static Properties props() {
		return ConfigHolder.PROPERTIES;
	}

	public static String props(String key) {
		return ConfigHolder.PROPERTIES.getProperty(key);
	}

	public static SparkConf sparkConf() {
		return ConfigHolder.SPARK_CONF;
	}

	/**
	 * Sets system properties.
	 */
	public static void setHadoopProps() {
		Service.sparkCtx().hadoopConfiguration().set("fs.defaultFS", props("hadoop.homefolder"));
		System.setProperty("HADOOP_USER_NAME", props("hadoop.user"));
	}
}
