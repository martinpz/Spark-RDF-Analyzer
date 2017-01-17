var loader = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';

// ########################## Entry Point ##########################
function showAutocompletionModal() {
	var input = $('#entryNode').val();

	// Only consider inputs with at least two characters.
	if (input.length < 2) {
		$('#modalTitle').text('Query too short!');
		$('#modalBody').text(
				'Please enter at least two characters before searching!');
		$('#btnStartBrowsing').hide();
	} else {
		$('#modalTitle').text('Select your entry point!');
		$('#modalBody').html(
				'<p>Computing the autocomplete suggestions ...</p>' + loader);
		$('#btnStartBrowsing').show();

		var xhttp = new XMLHttpRequest();

		xhttp.onreadystatechange = function() {
			if (xhttp.readyState == 4 && xhttp.status == 200) {
				$('#modalBody').html(xhttp.responseText);
			}
		}

		xhttp.open('GET', REST_API + 'autoComplete/' + getCookie('graphName')
				+ '/' + input + '/Node', true);
		xhttp.send();
	}
}

function startBrowsing(event) {
	var selectedText = $('input[name="optradio"]:checked').val();

	// When no option is selected, we cannot continue.
	if (typeof selectedText === 'undefined') {
		event.stopPropagation();
		return false;
	}

	var selectedText_arr = selectedText.split(':'); // u52,<http,//www.ins.cwi.nl/sib/user/u52>
	var selectedValue = selectedText_arr[0]; // u52
	var selectedURI = selectedText_arr[1] + ':' + selectedText_arr[2]; // <http://www.ins.cwi.nl/sib/user/u52>

	// Close modal.
	$('#btnCloseModal').click();

	showBrowser(selectedValue, selectedURI);
}

// ########################## RDF Browser ##########################
function showBrowser(centralNode, centralNodeURI) {
	$('#browser').show('fast');
	$('#entrypoint').hide('fast');

	// Fill browser div with content.
	if(useTextualBrowsing()) {
		prepareTextualBrowser(centralNode, centralNodeURI);
	} else {
		prepareVisualBrowser(centralNode, centralNodeURI);
	}
}

function useTextualBrowsing() {
	return $('#textualBrowsing').prop('checked');
}

function showLoader(centralNode) {
	$('#browserBody').html('<p>Computing the neighbors for ' + centralNode + ' ...</p>' + loader);
}

function updateBrowsingHistory(currentName, currentURI) {
	// Add link to pre-last element. Remove active marker.
	var lastNode = $('#browsingHistory #list li').last();
	var lastURI = lastNode.attr('data-uri');
	var lastName = lastNode.text();

	lastNode.removeClass('active');
	lastNode.html('<a href="#" onclick="' + 'prepareTextualBrowser(\'' + lastName + '\', \'' + lastURI + '\')' + '">' + lastName + '</a>');

	// Append new last (= current) element. Make it active.
	$('#browsingHistory #list').append('<li class="active" data-uri="' + currentURI + '">' + currentName + '</li>');
}

function updateBrowserHeight() {
	// Update the height of the body div w.r.t. to the header.
	var headerTop = $('#browserHeader').offset().top;
	var headerHeight = $('#browserHeader').outerHeight();
	var bottomSpace = 40;
	var heightDiff = headerTop + headerHeight + bottomSpace;

	$('#browserBody').css('max-height', 'calc(100vh - ' + heightDiff + 'px)');
}

// ########################## Textual RDF Browser ##########################
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

// ########################## Visual RDF Browser ##########################
function prepareVisualBrowser(centralNode, centralNodeURI) {
	return;

	var xhttp = new XMLHttpRequest();

	showLoader(centralNode);
	updateBrowsingHistory(centralNode, centralNodeURI);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displayNodesVisual(centralNode, centralNodeURI, JSON.parse(xhttp.responseText));
			updateBrowserHeight();
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNodeURI)
			+ '&numNeighbors=12', true);
	xhttp.send();
}

function displayNodesVisual(centralNode, centralNodeURI, neighbors) {
	// Remove < and > from URI.
	var toShow = '';

	$.each(neighbors, function(URI, props) {
		toShow += '';
	});

	$('#browserBody').html(toShow);
}

// ########################## Utility Functions ##########################
function getGraphName() {
	$('#GraphName').html(getCookie('graphName'));
}

function getCookie(name) {
	var value = '; ' + document.cookie;
	var parts = value.split('; ' + name + '=');

	if (parts.length == 2) {
		return parts.pop().split(';').shift();
	}
}
