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

import org.apache.spark.sql.Row;
/*
 * This class is used and an intermediate adapter between front end and back end.
 * It makes able dynamic display of results.
 */

public class ConnAdapter {

	private String startNode="";
	private String endNode="";
	public String Edges = "{color:none}\n; choices\n";
	public String Nodes = "; endings\n";
	public boolean End = false;
	public boolean ConfirmedEnd = false;

	public void reset()
	{
		this.startNode ="";
		this.endNode ="";
		this.Edges ="{color:none}\n; choices\n";
		this.Nodes ="; endings\n";
		this.End =false;
		this.ConfirmedEnd = false;
		
		
	}
	public String getResults()
	{
		String result = "";

		if(this.End==true && this.ConfirmedEnd == true)
		{
			return "END";
		}
		else if(this.End==true && this.ConfirmedEnd ==false)
		{
			this.ConfirmedEnd = true;
			result = Edges+Nodes+startNode+endNode;
			return result;
		}
		result = Edges+Nodes+startNode+endNode;
		return result;
	}
	public void setStartNode(String Node)
	{
		this.startNode = Node+" {color:#c6531e}\n";
	}
	public void setEndNode(String Node)
	{
		this.endNode = Node+" {color:#c6531e}\n";
	}
	
	
	/*
	 * This method converts output to HalfViz format.
	 */
	public void UpdateResults(Row[] rows, String type)
	{
		if(type.equals("tbl01") || type.equals("tbl02"))
		{
			for(Row r : rows)
			{ 
				ConnViewer.Counter++;
				String Subject = Configuration.shortenURI(r.getString(0));
				String Predicate = Configuration.shortenURI(r.getString(1));
				String Object = Configuration.shortenURI(r.getString(2));
				
				this.Edges += Subject+" -- "+Subject+"_"+Predicate+"_"+Object+"\n"; 
				this.Edges += Subject+"_"+Predicate+"_"+Object+" -> "+Object+"\n";
				
				this.Nodes += Subject+" {color:#95cde5}\n";
				this.Nodes += Object+" {color:#95cde5}\n";
				this.Nodes += Subject+"_"+Predicate+"_"+Object+" {label:"+Predicate+"}\n";
			}
		}
		else if(type.equals("tbl11"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Subject2 = Configuration.shortenURI(r.getString(4));
				
				this.Edges += Subject1+" -- "+Subject1+"_"+Predicate1+"_"+Object+"\n"; 
				this.Edges += Subject1+"_"+Predicate1+"_"+Object+" -> "+Object+"\n";
				this.Edges += Subject2+" -- "+Subject2+"_"+Predicate2+"_"+Object+"\n"; 
				this.Edges += Subject2+"_"+Predicate2+"_"+Object+" -> "+Object+"\n";
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object+" {color:#95cde5}\n";
				this.Nodes += Subject1+"_"+Predicate1+"_"+Object+" {label:"+Predicate1+"}\n";
				this.Nodes += Subject2+"_"+Predicate2+"_"+Object+" {label:"+Predicate2+"}\n";
				
			}
		}
		else if(type.equals("tbl12") || type.equals("tbl13"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Subject2 = Configuration.shortenURI(r.getString(4));
				
				this.Edges += Subject1+" -- "+Subject1+"_"+Predicate1+"_"+Object+"\n"; 
				this.Edges += Subject1+"_"+Predicate1+"_"+Object+" -> "+Object+"\n";
				this.Edges += Object+" -- "+Object+"_"+Predicate2+"_"+Subject2+"\n"; 
				this.Edges += Object+"_"+Predicate2+"_"+Subject2+" -> "+Subject2+"\n";
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object+" {color:#95cde5}\n";
				this.Nodes += Subject1+"_"+Predicate1+"_"+Object+" {label:"+Predicate1+"}\n";
				this.Nodes += Object+"_"+Predicate2+"_"+Subject2+" {label:"+Predicate2+"}\n";
				
			}
		}
		else if(type.equals("tbl14"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Subject2 = Configuration.shortenURI(r.getString(4));
				
				this.Edges += Object+" -- "+Object+"_"+Predicate1+"_"+Subject1+"\n"; 
				this.Edges += Object+"_"+Predicate1+"_"+Subject1+" -> "+Subject1+"\n";
				this.Edges += Object+" -- "+Object+"_"+Predicate2+"_"+Subject2+"\n"; 
				this.Edges += Object+"_"+Predicate2+"_"+Subject2+" -> "+Subject2+"\n";
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object+" {color:#95cde5}\n";
				this.Nodes += Object+"_"+Predicate1+"_"+Subject1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object+"_"+Predicate2+"_"+Subject2+" {label:"+Predicate2+"}\n";
				
			}
		}
		else if(type.equals("tbl21") || type.equals("tbl23"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Subject1+" -- "+Subject1+"_"+Predicate1+"_"+Object1+"\n"; 
				this.Edges += Subject1+"_"+Predicate1+"_"+Object1+" -> "+Object1+"\n";
						
				this.Edges += Object1+" -- "+Object1+"_"+Predicate2+"_"+Object2+"\n"; 
				this.Edges += Object1+"_"+Predicate2+"_"+Object2+" -> "+Object2+"\n";
				
				this.Edges += Object2+" -- "+Object2+"_"+Predicate3+"_"+Subject2+"\n"; 
				this.Edges += Object2+"_"+Predicate3+"_"+Subject2+" -> "+Subject2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Subject1+"_"+Predicate1+"_"+Object1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object1+"_"+Predicate2+"_"+Object2+" {label:"+Predicate2+"}\n";
				this.Nodes += Object2+"_"+Predicate3+"_"+Subject2+" {label:"+Predicate3+"}\n";
				
			}
		}
		else if(type.equals("tbl22") || type.equals("tbl24"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Subject1+" -- "+Subject1+"_"+Predicate1+"_"+Object1+"\n"; 
				this.Edges += Subject1+"_"+Predicate1+"_"+Object1+" -> "+Object1+"\n";
						
				this.Edges += Object1+" -- "+Object1+"_"+Predicate2+"_"+Object2+"\n"; 
				this.Edges += Object1+"_"+Predicate2+"_"+Object2+" -> "+Object2+"\n";
				
				this.Edges += Subject2+" -- "+Subject2+"_"+Predicate3+"_"+Object2+"\n"; 
				this.Edges += Subject2+"_"+Predicate3+"_"+Object2+" -> "+Object2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Subject1+"_"+Predicate1+"_"+Object1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object1+"_"+Predicate2+"_"+Object2+" {label:"+Predicate2+"}\n";
				this.Nodes += Subject2+"_"+Predicate3+"_"+Object2+" {label:"+Predicate3+"}\n";
				
			}
		}
		else if(type.equals("tbl25"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Subject1+" -- "+Subject1+"_"+Predicate1+"_"+Object1+"\n"; 
				this.Edges += Subject1+"_"+Predicate1+"_"+Object1+" -> "+Object1+"\n";
						
				this.Edges += Object2+" -- "+Object2+"_"+Predicate2+"_"+Object1+"\n"; 
				this.Edges += Object2+"_"+Predicate2+"_"+Object1+" -> "+Object1+"\n";
				
				this.Edges += Object2+" -- "+Object2+"_"+Predicate3+"_"+Subject2+"\n"; 
				this.Edges += Object2+"_"+Predicate3+"_"+Subject2+" -> "+Subject2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Subject1+"_"+Predicate1+"_"+Object1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object2+"_"+Predicate2+"_"+Object1+" {label:"+Predicate2+"}\n";
				this.Nodes += Object2+"_"+Predicate3+"_"+Subject2+" {label:"+Predicate3+"}\n";
				
			}
		}
		else if(type.equals("tbl26"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Object1+" -- "+Object1+"_"+Predicate1+"_"+Subject1+"\n"; 
				this.Edges += Object1+"_"+Predicate1+"_"+Subject1+" -> "+Subject1+"\n";
						
				this.Edges += Object2+" -- "+Object2+"_"+Predicate2+"_"+Object1+"\n"; 
				this.Edges += Object2+"_"+Predicate2+"_"+Object1+" -> "+Object1+"\n";
				
				this.Edges += Object2+" -- "+Object2+"_"+Predicate3+"_"+Subject2+"\n"; 
				this.Edges += Object2+"_"+Predicate3+"_"+Subject2+" -> "+Subject2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Object1+"_"+Predicate1+"_"+Subject1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object2+"_"+Predicate2+"_"+Object1+" {label:"+Predicate2+"}\n";
				this.Nodes += Object2+"_"+Predicate3+"_"+Subject2+" {label:"+Predicate3+"}\n";
				
			}
		}
		else if(type.equals("tbl27"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Object1+" -- "+Object1+"_"+Predicate1+"_"+Subject1+"\n"; 
				this.Edges += Object1+"_"+Predicate1+"_"+Subject1+" -> "+Subject1+"\n";
						
				this.Edges += Object1+" -- "+Object1+"_"+Predicate2+"_"+Object2+"\n"; 
				this.Edges += Object1+"_"+Predicate2+"_"+Object2+" -> "+Object2+"\n";
				
				this.Edges += Object2+" -- "+Object2+"_"+Predicate3+"_"+Subject2+"\n"; 
				this.Edges += Object2+"_"+Predicate3+"_"+Subject2+" -> "+Subject2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Object1+"_"+Predicate1+"_"+Subject1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object1+"_"+Predicate2+"_"+Object2+" {label:"+Predicate2+"}\n";
				this.Nodes += Object2+"_"+Predicate3+"_"+Subject2+" {label:"+Predicate3+"}\n";
				
			}
		}
		else if(type.equals("tbl28"))
		{
			for(Row r : rows)
			{
				ConnViewer.Counter++;
				String Subject1 = Configuration.shortenURI(r.getString(0));
				String Predicate1 = Configuration.shortenURI(r.getString(1));
				String Object1 = Configuration.shortenURI(r.getString(2));
				String Predicate2 = Configuration.shortenURI(r.getString(3));
				String Object2 = Configuration.shortenURI(r.getString(4));
				String Predicate3 = Configuration.shortenURI(r.getString(5));
				String Subject2 = Configuration.shortenURI(r.getString(6));
				
				this.Edges += Object1+" -- "+Object1+"_"+Predicate1+"_"+Subject1+"\n"; 
				this.Edges += Object1+"_"+Predicate1+"_"+Subject1+" -> "+Subject1+"\n";
						
				this.Edges += Object1+" -- "+Object1+"_"+Predicate2+"_"+Object2+"\n"; 
				this.Edges += Object1+"_"+Predicate2+"_"+Object2+" -> "+Object2+"\n";
				
				this.Edges += Subject2+" -- "+Subject2+"_"+Predicate3+"_"+Object2+"\n"; 
				this.Edges += Subject2+"_"+Predicate3+"_"+Object2+" -> "+Object2+"\n";
				
				
				this.Nodes += Subject1+" {color:#95cde5}\n";
				this.Nodes += Subject2+" {color:#95cde5}\n";
				this.Nodes += Object1+" {color:#95cde5}\n";
				this.Nodes += Object2+" {color:#95cde5}\n";
				this.Nodes += Object1+"_"+Predicate1+"_"+Subject1+" {label:"+Predicate1+"}\n";
				this.Nodes += Object1+"_"+Predicate2+"_"+Object2+" {label:"+Predicate2+"}\n";
				this.Nodes += Subject2+"_"+Predicate3+"_"+Object2+" {label:"+Predicate3+"}\n";
				
			}
		}
	}
}