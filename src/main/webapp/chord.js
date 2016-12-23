 function generateChord(theMatrix,theArray,theArray2) {

 var the_id = -1;
var the_id2 = -1;

var width = 550;
var height = 550;

var svg = d3.select("#result")
        .append("svg")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

 

var matrix5x5 = theMatrix;


var colors = ["#778899","#9370DB","#20B2AA","#008080","#6A5ACD","#DB7093","#20B2AA","#DEB887","#8FBC8F","#BDB76B"];
var range5 = [];
for (var i = 0; i<theMatrix.length; i++)
{
	range5[i] =  colors[i%10];
}
var chord = d3.layout.chord()
        .padding(.05)
        .sortSubgroups(d3.descending)
        .matrix(matrix5x5);

var fill = d3.scale.ordinal()
        .domain(d3.range(range5.length))
        .range(range5);


var innerRadius = Math.min(width, height) * .41;
var outerRadius = innerRadius * 1.1;
var range5_artists = theArray;


svg.append("g")
        .selectAll("path")
        .data(chord.groups)
        .enter().append("path")
        .style("fill", function(d) {
            return fill(d.index);
        })
        .style("stroke", function(d) {
            return fill(d.index);
        })
		.attr("id", function(d, i){return "group-" + i;})
		.attr("name", function(d) {
            return  range5_artists[d.index];
        })
        .attr("d", d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius))
		.on("click", function(){
        	getTable(this.getAttribute("name"));
        
        })
        .on("mouseover", fade(.1,svg))
        .on("mouseout", fade(1,svg));

svg.append("g")
        .attr("class", "chord")
        .selectAll("path")
        .data(chord.chords)
        .enter().append("path")
        .style("fill", function(d) {
            return fill(d.target.index);
        })
        .attr("d", d3.svg.chord().radius(innerRadius))
        .style("opacity", 1);



var matrix = [];



svg.selectAll("text")
        .data(chord.groups)
        .enter()
        .append("text")
        .attr("id", function(d) {
			 the_id = the_id+1;
            return  theArray[the_id];
        })
		.attr("name", function(d) {
            return  range5_artists[d.index];
        })
		.attr("font-size", "11px")
		.attr("style", "cursor:pointer;")
        .attr("fill", function(d) {
            return  range5[d.index];
        })
		.on("click", function(){
        	getTable(this.getAttribute("name"));
        
        })
        .on("mouseover", fade(.1,svg))
        .on("mouseout", fade(1,svg));
		  
		svg.append("g").selectAll("text")
        .data(chord.chords)
    .enter()
  .append("sgv:text")
			.on("mouseover", fade(.1,svg))
        .on("mouseout", fade(1,svg))
		.attr("style", "cursor:pointer;")
        .attr("x", 4)
        .attr("dy", 15)
        .attr("name", function(d, i){return theArray[i];})

			
	
        .append("svg:textPath")
            .attr("xlink:href", function(d, i){return "#group-" + i;})
            .text(function(d,i) {return theArray2[i];})
			 .attr("id", function(d, i){return theArray[i];})
			 .attr("name", function(d, i){return theArray[i];})
			 .on("click", function(){
        	getTable(this.id);
        
        })
			 

		
			.filter(function(d, i){return i === 0 || true ? true : false;})
            .attr("style", "fill:white;")  
			.append("svg:text")
		}
		
		;

function updateM(theMatrix,theArray,theArray2)
{
	 var values = [];
	  var AllSum = 0;
	  
	  for(var i=0;i<theMatrix.length;i++)
	  {
		  var rowSum = 0;
		  for(var j=0;j<theMatrix[0].length;j++)
		  {
			  AllSum += theMatrix[i][j];
			  rowSum += theMatrix[i][j];
	      }
		  values[i] = rowSum;
	  }

	  for(var i=0;i<values.length;i++)
	  {
		  var condition = values[i]/AllSum;
		  if(condition<0.05)
		  {
			  theArray2[i] = " ";
			  
		  }
		  else
		  {
			  theArray2[i] = theArray[i];
		  }
	  }

}
		
function fade(opacity,svg) {
    return function(g, i) {
    	if(opacity==1)
    	{
    		document.getElementById("placeri").style.visibility = 'hidden';
    	}
    	else
    	{
    		document.getElementById("placeri").innerHTML = this.getAttribute("name");
    		document.getElementById("placeri").style.visibility = 'visible';
    	}
        svg.selectAll("g.chord path")
                .filter(function(d) {
                    return d.source.index != i && d.target.index != i;
                })
                .transition()
                .style("opacity", opacity);
    };
}

	

