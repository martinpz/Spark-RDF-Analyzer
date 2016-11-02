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

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.*;
/*
 * This method gets already preprocessed graphs list and sends it to front end.
 */
public class GetGraphs  extends Configured {
	public static String main(String[] args) throws Exception {

		 String result = "";
		 /* 
		  * Get Folder Names
		  */
		FileSystem fs = FileSystem.get(new URI(Configuration.properties.getProperty("StorageURL")),new org.apache.hadoop.conf.Configuration());
		
		Path src=new Path(Configuration.properties.getProperty("StoragePath"));
		FileStatus[] subdirs = fs.listStatus(src);

	
		/*
		 *Generate new graph Thumbnail 
		 */
		result += "<div class=\"col-sm-2 col-md-3\">"+
				 "<div class=\"thumbnail\">"+
				 "<p><a href=\"#\"  style=\"text-align:right; visibility:hidden; margin:0px;\" class=\"btn btn-danger\" role=\"button\" onClick=\"deleteGraph('')\"><span class=\"glyphicon glyphicon-remove\" aria-hidden=\"true\"></span></a></p>"+
				 "<img src=\"./img/ng.png\" alt=\"\">"+
				 "<div class=\"caption\" style=\"text-align:center\">"+
				 "<h3> &nbsp; </h3>"+
				 "<p><a href=\"newGraph.html\" class=\"btn btn-success\" role=\"button\"><span class=\"glyphicon glyphicon-plus\" aria-hidden=\"true\"></span>&nbsp;Add new Graph</a></p>"+
				 "</div>"+
				 "</div>"+
				 "</div>";
		/*
		 * Generate Graph thumbnails
		 */
		for(FileStatus f : subdirs)
		 {
			result += generateThumbnail(f.getPath().getName());
		 }
			
	   return result;

	   }
public static String generateThumbnail(String graphName)
{
	String result = "";
	if(!graphName.contains("Ranking.parquet"))
	{
	graphName = graphName.substring(0, graphName.indexOf('.')); 
	
	result = "<div class=\"col-sm-2 col-md-3\">"+
			 "<div class=\"thumbnail\">"+
			 "<p><a href=\"#\"  style=\"text-align:right; margin:0px;\" class=\"btn btn-danger\" role=\"button\" onClick=\"deleteGraph('"+graphName+"')\"><span class=\"glyphicon glyphicon-remove\" aria-hidden=\"true\"></span></a></p>"+
			 "<img src=\"./img/nw.jpg\" alt=\"\">"+
			 "<div class=\"caption\" style=\"text-align:center\">"+
			 "<h3>"+graphName+"</h3>"+
			 "<p><a href=\"#\" class=\"btn btn-primary\" role=\"button\" onClick=\"chooseGraph('"+graphName+"')\"><span class=\"glyphicon glyphicon-folder-open\" aria-hidden=\"true\"></span>&nbsp;&nbsp;Select</a></p>"+
			 "</div>"+
			 "</div>"+
			 "</div>";
	}
	return result;
}
public static void deleteGraph(String graphName) throws IOException, URISyntaxException
{
	FileSystem fs = FileSystem.get(new URI(Configuration.properties.getProperty("StorageURL")),new org.apache.hadoop.conf.Configuration());
	
	fs.delete(new Path(Configuration.properties.getProperty("StoragePath")+graphName+".parquet"), true);
	fs.delete(new Path(Configuration.properties.getProperty("StoragePath")+graphName+"Ranking.parquet"), true);

}
}