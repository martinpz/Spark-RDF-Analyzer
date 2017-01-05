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
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;

/**
 * This class is used to (1) read configuration files and (2) set up the Spark
 * Configuration.
 */
public class Configuration {
	private final static Logger logger = Logger.getLogger(Configuration.class);

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

			logger.info("Reading properties ...");
			for (Entry<Object, Object> prop : PROPERTIES.entrySet()) {
				String key = (String) prop.getKey();
				String val = (String) prop.getValue();

				// Set all spark properties to the configuration.
				if (key.startsWith("spark")) {
					logger.info("[" + key + "] => '" + val + "'");
					sparkConf.set(key, val);
				}
			}

			return sparkConf;
		}
	}

	private Configuration() {
		// Block initialization of this class from other methods.
	}

	/**
	 * @return Properties object
	 */
	private static Properties props() {
		return ConfigHolder.PROPERTIES;
	}

	/**
	 * @param key
	 *            of the property
	 * @return value of the property
	 */
	public static String props(String key) {
		return props().getProperty(key);
	}

	/**
	 * @return Path of the storage directory
	 */
	public static String storage() {
		return props("storage.dir");
	}

	/**
	 * @return Spark Configuration
	 */
	public static SparkConf sparkConf() {
		return ConfigHolder.SPARK_CONF;
	}

	/**
	 * Sets hadoop system properties and distributes JAR file.
	 */
	public static void setupHadoop() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		String hdpHome = props("hadoop.homefolder");
		String hdpUser = props("hadoop.user");
		URL projectJAR = classloader.getResource("spark-rdfanalyzer2.jar");

		// Setup hadoop configuration if it exists.
		// This will only be the case on the cluster.
		if (hdpHome != null) {
			Service.sparkCtx().hadoopConfiguration().set("fs.defaultFS", hdpHome);
		}

		if (hdpUser != null) {
			System.setProperty("HADOOP_USER_NAME", hdpUser);
		}

		if (projectJAR != null) {
			Service.sparkCtx().addJar(projectJAR.getFile());
		}
	}
}
