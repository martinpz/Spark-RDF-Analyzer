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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

/**
 * This class is the REST web service which handles front end requests by
 * calling the desired module with specified parameters.
 */
@Path("/ALL")
public class WebService {
	@GET
	@Path("/loadGraph/{inputPath}/{inputName}/{inputFormat}")
	public String getMsgg(@PathParam("inputPath") String inputPath, @PathParam("inputName") String inputName,
			@PathParam("inputFormat") boolean inputFormat) {
		String objResponse = "";

		try {
			objResponse = GraphLoader.main(inputPath, inputName, inputFormat);
		} catch (Exception e) {
			objResponse = "Graph Loading Failed!<br>Error Message: " + e.getMessage();
		}

		return objResponse;
	}

	
	@GET
	@Path("/calculateCentrality/{DataSet}/{Node}/{Type}")
	public String calculateCentrality(@PathParam("DataSet") String dataSet, @PathParam("Node") String node,
			@PathParam("Type") String MetricType) {

		String objResponse = "";
		System.out.println("called calculateCentrality");
		System.out.println(MetricType);
		
		try {
			objResponse = Centrality.main(MetricType, dataSet, node);
		} catch (Exception e) {
			objResponse = "Error !<br>Error Message: " + e.getMessage();
		}

		return objResponse;
	}
	@GET
	@Path("/countEdges/{DataSet}")
	public String getMsg(@PathParam("DataSet") String dataSet) {
		String[] args = { dataSet };
		String objResponse = "";

		try {
			objResponse = CountEdges.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/predicateDistribution/{DataSet}/{Type}")
	public String getMsg2(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {
		String[] args = { dataSet, viewType };
		String objResponse = "";

		try {
			objResponse = PredicateDistribution.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/countNodes/{DataSet}")
	public String getMsg3(@PathParam("DataSet") String dataSet) {

		/*
		 *  1 = need the result in string for showing the output on frontend
		 *  2 = need the output in int to use to perform other operations
		 */
		String[] args = { dataSet, "1" };
		String objResponse = "";

		try {
			objResponse = CountNodes.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/getClasses/{DataSet}")
	public String getMsg5(@PathParam("DataSet") String dataSet) {
		String[] args = { dataSet, "Normal" };
		String objResponse = "";

		try {
			objResponse = GetClasses.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/collapsedGraph/{DataSet}/{Type}")
	public String getMsg6(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) throws Exception {
		String[] args = { dataSet, Type };
		String objResponse = "";

		try {
			objResponse = CollapsedGraph.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/edgeFinder/{DataSet}/{Type}")
	public String getMsg7(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {
		String[] args = { dataSet, Type };
		String objResponse = "";

		try {
			objResponse = EdgeFinder.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/inDegree/{DataSet}/{Type}")
	public String getMsg8(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {
		String[] args = { dataSet, Type };
		String objResponse = "";

		try {
			objResponse = InDegree.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/outDegree/{DataSet}/{Type}")
	public String getMsg9(@PathParam("DataSet") String dataSet, @PathParam("Type") String Type) {
		String[] args = { dataSet, Type };
		String objResponse = "";

		try {
			objResponse = OutDegree.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/classDistribution/{DataSet}/{Type}")
	public String getMsg10(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {
		String[] args = { dataSet, viewType };
		String objResponse = "";

		try {
			objResponse = ClassDistribution.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/degreeDistribution/{DataSet}/{Type}")
	public String getMsg11(@PathParam("DataSet") String dataSet, @PathParam("Type") String viewType) {
		String[] args = { dataSet, viewType };
		String objResponse = "";

		try {
			objResponse = DegreeDistribution.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/countNodesV2/{DataSet}")
	public String getMsg12(@PathParam("DataSet") String dataSet) {
		String[] args = { dataSet };
		String objResponse = "";

		try {
			objResponse = CountNodesV2.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/getGraphs")
	public String getMsg12() {
		String[] args = {};
		String objResponse = "";

		try {
			objResponse = GetGraphs.main(args);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/partialRead/{inputSubject}/{inputType}")
	public String getMsg13(@PathParam("inputSubject") String inputSubject, @PathParam("inputType") String inputType) {
		String objResponse = "";

		try {
			if (inputType.equals("edgeFinder")) {
				objResponse = EdgeFinder.partialRead(inputSubject);
			} else {
				objResponse = CollapsedGraph.partialRead(inputSubject);
			}
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/autoComplete/{DataSet}/{UserInput}/{Type}")
	public String getMsg14(@PathParam("DataSet") String dataSet, @PathParam("UserInput") String userInput,
			@PathParam("Type") String Type) {
		String objResponse = "";

		try {
			objResponse = AutoComplete.main(dataSet, userInput, Type);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/connViewer/{Node1}/{Node2}/{DataSet}/{Predicates}/{Pattern}")
	public String getMsg16(@PathParam("DataSet") String dataSet, @PathParam("Node1") String startN,
			@PathParam("Node2") String endN, @PathParam("Predicates") String Predicates,
			@PathParam("Pattern") String Pattern) {

		String objResponse = "";

		try {
			startN = startN.replace('$', '/');
			startN = startN.replace('&', '#');
			endN = endN.replace('$', '/');
			endN = endN.replace('&', '#');

			ConnViewer.main(startN, endN, dataSet, Predicates, Pattern);

			objResponse = "Started";
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/connViewerResult")
	public String getMsg17() {
		String objResponse = "";

		try {
			objResponse = ConnViewer.objAdapter.getResults();
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/deleteGraph/{DataSet}")
	public String getMsg18(@PathParam("DataSet") String dataSet) {
		String objResponse = "";

		try {
			objResponse = GetGraphs.deleteGraph(dataSet);
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/closeSession")
	public String getMsg19() {
		String objResponse = "";

		try {
			Service.sparkCtx().close();
			objResponse = "Context Closed.";
		} catch (Exception e) {
			objResponse = "Calculation Failed.<br>" + e.getMessage();
		}

		return objResponse;
	}

	@GET
	@Path("/directNeighbors/{graph}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDirectNeighbors(@PathParam("graph") String graph,
			@DefaultValue("") @QueryParam("centralNode") String centralNode,
			@DefaultValue("5") @QueryParam("numNeighbors") int numNeighbors) throws UnsupportedEncodingException {
		// Only compute neighbors when central node is selected.
		if (centralNode.isEmpty()) {
			throw new IllegalArgumentException("You MUST specifiy a central node.");
		}

		// Decode node URI.
		centralNode = URLDecoder.decode(centralNode, "UTF-8");

		// Actually compute the neighbors.
		JSONObject neighbors = Neighborhood.getNeighbors(graph, centralNode, numNeighbors);

		// Return the response to the client.
		return Response.ok().entity("" + neighbors).build();
	}
}
