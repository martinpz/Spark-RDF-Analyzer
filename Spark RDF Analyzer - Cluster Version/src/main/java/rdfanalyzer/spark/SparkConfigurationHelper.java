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

import org.apache.spark.SparkConf;
/*
 * In this class SPARK Configuration for Cluster is applied.
 * It is defined in app.config file. 
 */
public class SparkConfigurationHelper {
public static SparkConf getConfiguration()
{
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
	  // sparkConf.set("spark.eventLog.enabled","true");
	  // sparkConf.set("spark.eventLog.dir", "hdfs://isydney.informatik.uni-freiburg.de:8020/user/teamprojekt2015/logs/");
	   //sparkConf.set("spark.sql.codegen", "true");
	   sparkConf.set("spark.sql.shuffle.partitions", "32");
	  
	return sparkConf;
}

public static void setOthers()
{
	WebService.ctx.hadoopConfiguration().set("fs.defaultFS", "hdfs://sydney.informatik.privat:8020/user/teamprojekt2015/");
	System.setProperty("HADOOP_USER_NAME", Configuration.properties.getProperty("HadoopUser"));
}
}
