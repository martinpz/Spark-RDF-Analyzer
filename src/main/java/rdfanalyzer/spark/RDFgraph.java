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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Serializable class to represent RDF graph.
 */
public class RDFgraph implements Serializable {
	private static final long serialVersionUID = 1L;

	private String subject;
	private String predicate;
	private String object;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = removeHarmfulCharacters(subject);
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = removeHarmfulCharacters(predicate);
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = removeHarmfulCharacters(object);
	}

	private String removeHarmfulCharacters(String s) {
		return s.replaceAll("\"|'|`|´", "");
	}

	/**
	 * Converts a URI to a shorter representation which is shown to users.
	 * 
	 * @param inputURI
	 * @return
	 */
	public static String shortenURI(String inputURI) {
		String result = "";

		int index0 = inputURI.indexOf("^^");
		int index1 = inputURI.lastIndexOf('#');
		int index2 = inputURI.lastIndexOf('/');

		// If we found a literal, we do not shorten the URL at all.
		if (index0 > 0) {
			return "";
		}

		if (index1 > index2) {
			result = inputURI.substring(index1 + 1, inputURI.length() - 1);
		} else if (index2 > index1) {
			result = inputURI.substring(index2 + 1, inputURI.length() - 1);
		}

		try {
			result = java.net.URLDecoder.decode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return result;
	}
}
