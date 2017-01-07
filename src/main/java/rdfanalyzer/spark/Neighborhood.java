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

import org.json.JSONObject;

/**
 * This class uses a detailed and advanced method to calculate number of nodes.
 */
public class Neighborhood {
	/**
	 * Computes the neighbors w.r.t. their distance and amount.
	 * 
	 * @param graph
	 *            The name of the graph to use.
	 * @param centralNode
	 *            The URI of the central node.
	 * @param num
	 *            How many neighbors to return.
	 * @param distance
	 *            How far away a neighbor may be.
	 * 
	 * @return A JSONObject of the URIs of the neighbors.
	 */
	public static JSONObject getNeighbors(String graph, String centralNode, int num, int distance) {
		if (num <= 0) {
			throw new IllegalArgumentException("Requested number of neighbors must be greater than zero.");
		}

		if (distance <= 0) {
			throw new IllegalArgumentException("Distance must be greater than zero.");
		}

		JSONObject jsonObj = new JSONObject();

		jsonObj.put("neighborTest", "This/is/a/simple/test");
		jsonObj.put("neighborMaybe", "Here/is/another/URI/maybe");

		return jsonObj;
	}
}
