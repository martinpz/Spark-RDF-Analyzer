function generateGraph(array,arrayNumbers) {
	 
		var CHART_DATA = [];
		for (var i = 0; i < array.length; i++) {
   		 CHART_DATA.push({
      	   y: arrayNumbers[i], label: array[i]
  	  });	  
}


	  
    var chart = new CanvasJS.Chart("result",
    {
      title:{
        text: ""    
      },
      animationEnabled: true,
      axisY: {
        title: ""
      },
      legend: {
        verticalAlign: "bottom",
        horizontalAlign: "center"
      },
      theme: "theme2",
      data: [

      {        
        type: "column",  
        showInLegend: false, 
        
        dataPoints: CHART_DATA
      }   
      ]
    });

    chart.render();
  }