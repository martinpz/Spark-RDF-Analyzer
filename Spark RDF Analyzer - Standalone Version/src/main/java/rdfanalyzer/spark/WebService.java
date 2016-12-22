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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

@Path("/ALL")
public class WebService {
	public static SparkConf sparkConf;
	public static JavaSparkContext ctx;
	public static SQLContext sqlContext;
	public static Configuration configuration;

	@GET
	@Path("/loadGraph/{inputPath}/{inputName}/{inputFormat}")
	public String getMsgg(@PathParam("inputPath") String inputPath, @PathParam("inputName") String inputName,
			@PathParam("inputFormat") boolean inputFormat) {
		String dalja = "";

		try {
			dalja = GraphLoader.main(inputPath, inputName, inputFormat);
		} catch (Exception e) {
			dalja = "Graph Loading Failed!\nError Message:" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/countEdges/{DataSet}")
	public String getMsg(@PathParam("DataSet") String dataSet) {

		String[] args = { dataSet };
		String dalja = "";

		try {
			dalja = CountEdges.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!!  " + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/predicateDistribution/{DataSet}/{Type}")
	public String getMsg2(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {

		String[] args = { dataSet, viewType };
		String dalja = "";

		try {
			dalja = PredicateDistribution.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/countNodes/{DataSet}")
	public String getMsg3(@PathParam("DataSet") String dataSet) {

		String[] args = { dataSet };
		String dalja = "";

		try {
			dalja = CountNodes.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! ";
		}

		return dalja;

	}

	@GET
	@Path("/getClasses/{DataSet}")
	public String getMsg5(@PathParam("DataSet") String dataSet) {

		String[] args = { dataSet, "Normal" };
		String dalja = "";

		try {
			dalja = GetClasses.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!!";
		}

		return dalja;

	}

	@GET
	@Path("/collapsedGraph/{DataSet}/{Type}")
	public String getMsg6(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) throws Exception {

		String[] args = { dataSet, Type };
		String dalja = "";
		dalja = CollapsedGraph.main(args);
		try {

		} catch (Exception e) {
			// dalja = "Calculation Failed!:"e.getMessage();
		}

		return dalja;

	}

	@GET
	@Path("/edgeFinder/{DataSet}/{Type}")
	public String getMsg7(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {

		String[] args = { dataSet, Type };
		String dalja = "";

		try {
			dalja = EdgeFinder.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/inDegree/{DataSet}/{Type}")
	public String getMsg8(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {

		String[] args = { dataSet, Type };
		String dalja = "";

		try {
			dalja = InDegree.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!!";
		}

		return dalja;
	}

	@GET
	@Path("/outDegree/{DataSet}/{Type}")
	public String getMsg9(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {

		String[] args = { dataSet, Type };
		String dalja = "";

		try {
			dalja = OutDegree.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!!";
		}

		return dalja;
	}

	@GET
	@Path("/classDistribution/{DataSet}/{Type}")
	public String getMsg10(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {

		String[] args = { dataSet, viewType };
		String dalja = "";

		try {
			dalja = ClassDistribution.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!!";
		}

		return dalja;
	}

	@GET
	@Path("/degreeDistribution/{DataSet}/{Type}")
	public String getMsg11(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {

		String[] args = { dataSet, viewType };
		String dalja = "";

		try {
			dalja = DegreeDistribution.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! " + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/countNodesV2/{DataSet}")
	public String getMsg12(@PathParam("DataSet") String dataSet) {

		String[] args = { dataSet };
		String dalja = "";

		try {
			dalja = CountNodesV2.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! ";
		}

		return dalja;
	}

	@GET
	@Path("/getGraphs")
	public String getMsg12() {

		String[] args = {};
		String dalja = "";

		try {

			dalja = GetGraphs.main(args);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! " + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/partialRead/{inputSubject}/{inputType}")
	public String getMsg13(@PathParam("inputSubject") String inputSubject, @PathParam("inputType") String inputType) {

		String dalja = "";

		try {
			if (inputType.equals("edgeFinder")) {
				dalja = EdgeFinder.partialRead(inputSubject);
			} else {
				dalja = CollapsedGraph.partialRead(inputSubject);
			}
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/autoComplete/{DataSet}/{UserInput}/{Type}")
	public String getMsg14(@PathParam("DataSet") String dataSet, @PathParam("UserInput") String userInput,
			@PathParam("Type") String Type) {

		String dalja = "";

		try {
			dalja = AutoComplete.main(dataSet, userInput, Type);
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/connViewerTest")
	public String getMsg15() {

		String dalja = "";

		try {
			ConnAdapter objAdapter = new ConnAdapter();
			objAdapter.setStartNode("User1");
			objAdapter.setEndNode("User2");
			objAdapter.testResult();
			dalja = objAdapter.getResults();
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/connViewer/{Node1}/{Node2}/{DataSet}/{Predicates}/{Pattern}")
	public String getMsg16(@PathParam("DataSet") String dataSet, @PathParam("Node1") String startN,
			@PathParam("Node2") String endN, @PathParam("Predicates") String Predicates,
			@PathParam("Pattern") String Pattern) {

		String dalja = "";

		try {
			startN = startN.replace('$', '/');
			startN = startN.replace('&', '#');
			endN = endN.replace('$', '/');
			endN = endN.replace('&', '#');
			ConnViewer.main(startN, endN, dataSet, Predicates, Pattern);
			dalja = "Started";
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/connViewerResult")
	public String getMsg17() {

		String dalja = "";

		try {

			dalja = ConnViewer.objAdapter.getResults();
		} catch (Exception e) {
			dalja = "Calculationd Failed!! :" + e.getMessage();
		}

		return dalja;
	}

	@GET
	@Path("/deleteGraph/{DataSet}")
	public String getMsg18(@PathParam("DataSet") String dataSet) {

		String dalja = "";

		try {
			GetGraphs.deleteGraph(dataSet);
			dalja = "Success";
		} catch (Exception e) {
			dalja = "Calculation Failed!! ";
		}

		return dalja;
	}
}