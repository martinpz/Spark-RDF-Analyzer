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

import java.util.ArrayList;
import java.util.List;

public class ConnViewerHelper {
	public static String strQuery01;
	public static String strQuery02;
	public static String strQuery03;
	public static String strQuery04;
	public static String strQuery05;
	public static String strQuery06;

	public static String strQuery11;
	public static String strQuery12;
	public static String strQuery13;
	public static String strQuery14;
	public static String strQuery15;
	public static String strQuery16;
	public static String strQuery17;
	public static String strQuery18;

	public static String strQuery21;
	public static String strQuery22;
	public static String strQuery23;
	public static String strQuery24;
	public static String strQuery25;
	public static String strQuery26;
	public static String strQuery27;
	public static String strQuery28;

	public static List<String> outputTables = new ArrayList<String>();
	public static List<String> updateResultTables = new ArrayList<String>();

	public static void generateQueries(String startN, String endN, String graphName) {
		strQuery01 = "SELECT subject, predicate, object FROM " + graphName + " WHERE subject = '" + startN
				+ "' AND object = '" + endN + "'";
		strQuery02 = "SELECT subject, predicate, object FROM " + graphName + " WHERE subject = '" + endN
				+ "' AND object = '" + startN + "'";
		strQuery03 = "SELECT subject, predicate, object FROM " + graphName + " WHERE subject = '" + startN
				+ "' AND object != '" + startN + "' AND object NOT LIKE '\"%'";
		strQuery04 = "SELECT subject, predicate, object FROM " + graphName + " WHERE subject = '" + endN
				+ "' AND object != '" + endN + "' AND object NOT LIKE '\"%'";
		strQuery05 = "SELECT subject, predicate, object FROM " + graphName + " WHERE object = '" + endN
				+ "' AND subject != '" + endN + "'";
		strQuery06 = "SELECT subject, predicate, object FROM " + graphName + " WHERE object = '" + startN
				+ "' AND subject != '" + startN + "'";

		strQuery11 = "SELECT tbl03.subject AS subject1, tbl03.predicate AS predicate1, tbl03.object AS object, tbl04.predicate as predicate2, tbl04.subject as subject2 FROM tbl03, tbl04 WHERE tbl03.object = tbl04.object";
		strQuery12 = "SELECT tbl03.subject AS subject1, tbl03.predicate AS predicate1, tbl03.object AS object, tbl05.predicate as predicate2, tbl05.object as subject2 FROM tbl03, tbl05 WHERE tbl03.object = tbl05.subject";
		strQuery13 = "SELECT tbl04.subject AS subject1, tbl04.predicate AS predicate1, tbl04.object AS object, tbl06.predicate as predicate2, tbl06.object as subject2 FROM tbl04, tbl06 WHERE tbl04.object = tbl06.subject";
		strQuery14 = "SELECT tbl05.object AS subject1, tbl05.predicate AS predicate1, tbl05.subject AS object, tbl06.predicate as predicate2, tbl06.object as subject2 FROM tbl05, tbl06 WHERE tbl05.subject = tbl06.subject";
		strQuery15 = "SELECT tbl03.subject AS subject1, tbl03.predicate AS predicate1, tbl03.object AS object, "
				+ graphName + ".predicate as predicate2, " + graphName + ".object as object2 FROM tbl03, " + graphName
				+ " WHERE tbl03.object = " + graphName + ".subject AND " + graphName + ".object != tbl03.subject";
		strQuery16 = "SELECT tbl04.subject AS subject1, tbl04.predicate AS predicate1, tbl04.object AS object, "
				+ graphName + ".predicate as predicate2, " + graphName + ".object as object2 FROM tbl04, " + graphName
				+ " WHERE tbl04.object = " + graphName + ".subject AND " + graphName + ".object != tbl04.subject";
		strQuery17 = "SELECT " + graphName + ".object AS object1, " + graphName + ".predicate AS predicate1, "
				+ graphName + ".subject AS subject, tbl05.predicate as predicate2, tbl05.object as subject2 FROM "
				+ graphName + ", tbl05 WHERE " + graphName + ".subject = tbl05.subject AND tbl05.object != " + graphName
				+ ".object";
		strQuery18 = "SELECT tbl06.object AS subject1, tbl06.predicate AS predicate1, tbl06.subject AS subject, "
				+ graphName + ".predicate as predicate2, " + graphName + ".object as object1 FROM tbl06, " + graphName
				+ " WHERE tbl06.subject = " + graphName + ".subject AND " + graphName + ".object != tbl06.object";

		strQuery21 = "SELECT tbl15.subject1, tbl15.predicate1, tbl15.object, tbl15.predicate2, tbl15.object2, tbl05.predicate, tbl05.object FROM tbl15, tbl05 WHERE tbl15.object2 = tbl05.subject";
		strQuery22 = "SELECT tbl15.subject1, tbl15.predicate1, tbl15.object, tbl15.predicate2, tbl15.object2, tbl04.predicate, tbl04.subject FROM tbl15, tbl04 WHERE tbl15.object2 = tbl04.object";
		strQuery23 = "SELECT tbl16.subject1, tbl16.predicate1, tbl16.object, tbl16.predicate2, tbl16.object2, tbl06.predicate, tbl06.object FROM tbl16, tbl06 WHERE tbl16.object2 = tbl06.subject";
		strQuery24 = "SELECT tbl16.subject1, tbl16.predicate1, tbl16.object, tbl16.predicate2, tbl16.object2, tbl03.predicate, tbl03.subject FROM tbl16, tbl03 WHERE tbl16.object2 = tbl03.object";
		strQuery25 = "SELECT tbl03.subject, tbl03.predicate, tbl03.object, tbl17.predicate1, tbl17.subject, tbl17.predicate2, tbl17.subject2  FROM tbl03, tbl17 WHERE tbl03.object = tbl17.object1";
		strQuery26 = "SELECT tbl06.object, tbl06.predicate, tbl06.subject, tbl17.predicate1, tbl17.subject, tbl17.predicate2, tbl17.subject2  FROM tbl06, tbl17 WHERE tbl06.subject = tbl17.object1";
		strQuery27 = "SELECT tbl18.subject1, tbl18.predicate1, tbl18.subject, tbl18.predicate2, tbl18.object1, tbl05.predicate, tbl05.object  FROM tbl18, tbl05 WHERE tbl18.object1 = tbl05.subject";
		strQuery28 = "SELECT tbl18.subject1, tbl18.predicate1, tbl18.subject, tbl18.predicate2, tbl18.object1, tbl04.predicate, tbl04.subject  FROM tbl18, tbl04 WHERE tbl18.object1 = tbl04.object";

	}

	public static void fillLists() {
		outputTables.add("tbl01");
		outputTables.add("tbl02");
		outputTables.add("tbl11");
		outputTables.add("tbl12");
		outputTables.add("tbl13");
		outputTables.add("tbl14");
		outputTables.add("tbl21");
		outputTables.add("tbl22");
		outputTables.add("tbl23");
		outputTables.add("tbl24");
		outputTables.add("tbl25");
		outputTables.add("tbl26");
		outputTables.add("tbl27");
		outputTables.add("tbl28");

		updateResultTables.add("tbl02");
		updateResultTables.add("tbl14");
		updateResultTables.add("tbl24");
		updateResultTables.add("tbl26");
		updateResultTables.add("tbl28");

	}
}
