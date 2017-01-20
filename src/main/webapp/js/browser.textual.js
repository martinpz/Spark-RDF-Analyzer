// ########################## RDF Browser Text-Based ##########################
function prepareTextualBrowser(centralNode, centralNodeURI) {
	var xhttp = new XMLHttpRequest();

	showLoader(centralNode);
	updateBrowsingHistory(centralNode, centralNodeURI);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displayNodesTextual(centralNode, centralNodeURI, JSON.parse(xhttp.responseText));
			updateBrowserHeight();
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNodeURI)
			+ '&numNeighbors=12', true);
	xhttp.send();
}

function displayNodesTextual(centralNode, centralNodeURI, neighbors) {
	// Remove < and > from URI.
	var toShow = '<p><strong>Selected Node:</strong> <a href="' + centralNodeURI.slice(1, -1) + '">' + centralNodeURI.slice(1, -1) + '</a></p>';
	toShow += '<table class="tableBrowser">';

	$.each(neighbors, function(URI, props) {
		toShow += '<tr>';

		// An arrow. Indicating if central node is source or target.
		// Right arrow = central node is source.
		var direction = props.direction == 'out' ? 'right' : 'left';
		var arrow = '<span class="glyphicon glyphicon-circle-arrow-' + direction + '"></span>';

		// The type of the connection, e.g. the predicate.
		var type = '<a href="' + props.predicateURI.slice(1, -1) + '" target="_blank"">' + props.predicate + '</a>';

		// The link to browse to the neighbor node.
		// OR the literal to be shown.
		var neighbor = '';

		if (props.name !== '') {
			// When a name is set, use it for the neighbor.
			neighbor = '<a href="#" onclick="prepareTextualBrowser';
			neighbor += '(\'' + props.name + '\', \'' + URI + '\')';
			neighbor += '">';
			neighbor += props.name + '</a>';
		} else {
			// When there is no name, we have a literal.
			neighbor = '<span style="font-style: italic;">';
			neighbor += props.URI + '</span>';
		}

		// Central node is source => write it left, otherwise right
		toShow += '<td>' + arrow + '</td>';
		toShow += '<td>' + ( direction == 'right' ? centralNode : neighbor ) + '</td>';
		toShow += '<td>' + type + '</td>';
		toShow += '<td>' + ( direction == 'right' ? neighbor : centralNode ) + '</td>';
		toShow += '</tr>';
	});

	toShow += '</table>';

	$('#browserBody').html(toShow);
}
