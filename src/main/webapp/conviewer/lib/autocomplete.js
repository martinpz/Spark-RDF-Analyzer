function checkNode(Node)
{
	document.getElementById("myModalLabel").setAttribute('nodeName', Node );
     var xhttp = new XMLHttpRequest();
     xhttp.onreadystatechange = function() {
      if (xhttp.readyState == 4 && xhttp.status == 200) {
    
	      var response = xhttp.responseText;
	      
	      document.getElementById("listNodes").innerHTML = xhttp.responseText;

      }}
      
     
      var inputNode = document.getElementById('input'+Node).value;
      var inputType = "Node";   	  

  
      var selectedGraphValue = getCookie('graphName');
      if(inputNode.length<2)
      {
    	  document.getElementById("listNodes").innerHTML = "Your query should have two or more characters";
    	  document.getElementById("listButton").className="btn btn-primary disabled"
          $('#listButton').attr('disabled', 'true');
      }
      else
      {
    	   xhttp.open("GET", REST_API + "autoComplete/"+selectedGraphValue+"/"+inputNode+"/"+Node, true);
     	   xhttp.send();  
      }
}
function checkEnable()
{
		if(document.getElementById('divSearch').style.visibility == 'visible')
		{
			document.getElementById("btnSubmit").className="btn btn-success disabled";
		}
		else
		{
			 if(document.getElementById("btnNode1").className=="btn btn-warning disabled" && document.getElementById("btnNode2").className=="btn btn-warning disabled")
			 {
				 document.getElementById("btnSubmit").className="btn btn-success";
			 }
			 else
			 {
				 document.getElementById("btnSubmit").className="btn btn-success disabled";
			 }
		
		}


}


function deleteChoice(selectedNode)
{
	 document.getElementById("input"+selectedNode).value = '';
	 document.getElementById("div"+selectedNode).className="form-group";
	  $('#input'+selectedNode).removeAttr("disabled");
	  document.getElementById("btn"+selectedNode).className="btn btn-warning"
	 document.cookie="selected"+selectedNode+"=null";
	  checkEnable();
}
function deletePredicate(Predicate)
{
	var elem = document.getElementById('li'+Predicate);
	elem.parentNode.removeChild(elem);	


}
function selectNode()
{
 var selectedText = $('input[name="optradio"]:checked').val().split(":");
 var selectedValue = selectedText[0];
 var selectedURI = selectedText[1]+":"+selectedText[2];
 var selectedNode = document.getElementById("myModalLabel").getAttribute('nodeName');
 
 if(selectedNode == "Predicate")
 {
	 document.getElementById("input"+selectedNode).value = "";
	 var ulPredicates = document.getElementById("predicatesUL"); 
	 var entry = document.createElement('li');
	 entry.innerHTML = '<a href=# class="badge" onclick="deletePredicate(\''+selectedValue+'\')">X</a>'+selectedValue;
	 entry.className = "list-group-item";
	 entry.setAttribute('fullName', selectedURI);
	 entry.id = "li"+selectedValue;
	 entry.style.color = 'gray';
	 ulPredicates.appendChild(entry);
		 
	 closeDialog();
	 
 }
 else
 {
	 document.getElementById("input"+selectedNode).value = selectedValue+": "+selectedURI;
	 document.getElementById("div"+selectedNode).className="form-group has-success has-feedback";
	  $('#input'+selectedNode).attr('disabled', 'true');
	 document.cookie="selected"+selectedNode+"="+selectedURI;
	 document.getElementById("btn"+selectedNode).className="btn btn-warning disabled"
	 closeDialog();
	 
	 
	 if($('#pagetype').val() == "centrality"){
		 // this function is defined in centrality.html
		 enableCalculateButton();
	 }
	 else{
		 // it's conviewer
		 checkEnable(); 
	 }
 }
}

function closeDialog()
{
	document.getElementById("listButton").className="btn btn-primary"
	$('#listButton').removeAttr("disabled");
    document.getElementById("listNodes").innerHTML = "<p><span class=\"glyphicon glyphicon-search\" aria-hidden=\"true\"></span>&nbsp;Searching on graph for URIs that match your query.</p>";
	document.getElementById("myModalLabel").setAttribute('nodeName', '');
}
function getGraphName()
{
	var name = getCookie('graphName');
	document.getElementById("GraphName").innerHTML = name;
}
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}
function startJob()
{

  document.getElementById('divSearch').style.visibility = 'visible';		
  var xhttp = new XMLHttpRequest();
   
  var selectedGraphValue = getCookie('graphName');
  var Node1 = getCookie('selectedNode1');
  Node1 = Node1.replaceAll("/","$");
  Node1 = Node1.replaceAll("#","&");
  
  var Node2 = getCookie('selectedNode2');
  Node2 = Node2.replaceAll("/","$");
  Node2 = Node2.replaceAll("#","&");
  
  var Predicate = $('input[name="radioPredicate"]:checked').val()+"--";
  var ul = document.getElementById("predicatesUL");
  var items = ul.getElementsByTagName("li");
  
  for (var i = 0; i < items.length; ++i) {
    var fullN = items[i].getAttribute('fullName');
	fullN = fullN.replaceAll("/","$");
	fullN = fullN.replaceAll("#","&");
	
	Predicate += fullN+",";
  }
  var Pattern = "";
  for (var i=1; i<=14; i++)
  {
	  
	  if($("#chPattern"+i).attr('checked')=="checked")
	  {
		  Pattern += "1,";
	  }
	  else
	  {
		 Pattern += "0,"; 
	  }
  }
 
  checkEnable();
  
  xhttp.open("GET", REST_API + "connViewer/"+Node1+"/"+Node2+"/"+selectedGraphValue+"/"+Predicate+"/"+Pattern, true);
  xhttp.send();

}
function getResult()
{

  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState == 4 && xhttp.status == 200) {
	  
	 
	    if(xhttp.responseText=="END")
	    {
	    	document.getElementById('divSearch').style.visibility = 'hidden';
	    	checkEnable();
	    }
	    else if(xhttp.responseText!="")
	    {
	    	
	    	
		    //alert(document.getElementById('code').value.length +"---"+xhttp.responseText.length);
	        if(document.getElementById('code').value.length != xhttp.responseText.length)
	        {
	        	
	        	document.getElementById('code').value = xhttp.responseText;
	        	document.getElementById('code').focus();
				var e = jQuery.Event("keydown");
				e.which = 32; // # Some key code value
				$("#code").trigger(e);
				document.getElementById('editor').style.display = 'none';
				document.getElementById('viewport').width = (parseInt($('#IDbardh').get(0).offsetWidth) - 100);
				document.getElementById('dashboard').width = (parseInt($('#IDbardh').get(0).offsetWidth) - 100);
				document.getElementById('viewport').height=500;
	        }
	        else
        	{
        	
        	}
		    
			
			
		}
		
		
		
    }
  }
  
    
  xhttp.open("GET", REST_API + "connViewerResult", true);
  xhttp.send();

}
