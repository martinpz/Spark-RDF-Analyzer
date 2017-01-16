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
	// Set up the browser div with the user's configuration.
	var conf = getBrowserConfiguration();
	var browserType = conf.textual ? 'textual' : 'visual';

	$('#browser').addClass(browserType).show('fast');
	$('#entrypoint').hide('fast');

	// Fill browser div with content.
	prepareBrowser(centralNode, centralNodeURI);
}

function getBrowserConfiguration() {
	var textualBrowsing = $('#textualBrowsing').prop('checked');
	var conf = {textual: textualBrowsing};

	return conf;
}

function prepareBrowser(centralNode, centralNodeURI) {
	var xhttp = new XMLHttpRequest();

	updateBrowsingHistory(centralNode, centralNodeURI);

	$('#browserModalBody').html(
			'<p>Computing the neighbors for ' + centralNode + ' ...</p>'
					+ loader);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displayNodes(centralNode, centralNodeURI, JSON
					.parse(xhttp.responseText));

			// Update the height of the body div w.r.t. to the header.
			var headerHeight = $('#browserModalHeader').outerHeight();
			$('#browserModalBody').css('height',
					'calc(100vh - ' + headerHeight + 'px)');
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNodeURI)
			+ '&numNeighbors=12', true);
	xhttp.send();
}

function updateBrowsingHistory(currentName, currentURI) {
	// Add link to pre-last element. Remove active marker.
	var lastNode = $('#browsingHistory #list li').last();
	var lastURI = lastNode.attr('data-uri');
	var lastName = lastNode.text();

	lastNode.removeClass('active');
	lastNode.html('<a href="#" onclick="' + 'prepareBrowser(\'' + lastName
			+ '\', \'' + lastURI + '\')' + '">' + lastName + '</a>');

	// Append new last (= current) element. Make it active.
	$('#browsingHistory #list').append(
			'<li class="active" data-uri="' + currentURI + '">' + currentName
					+ '</li>');
}

function displayNodes(centralNode, centralNodeURI, neighbors) {
	// Remove < and > from URI.
	var toShow = '<p><strong>Central Node:</strong> <a href="'
			+ centralNodeURI.slice(1, -1) + '">' + centralNodeURI.slice(1, -1)
			+ '</a></p>';

	$.each(neighbors, function(URI, props) {
		// An arrow. Indicating if central node is source or target.
		// Right arrow = central node is source.
		var direction = props.direction == 'out' ? 'right' : 'left';
		var arrow = '<span class="glyphicon glyphicon-circle-arrow-'
				+ direction + '" style="margin-right: 10px;"></span>';

		var showCentralNode = '<span style="margin-right: 5px;"><strong>'
				+ centralNode + '</strong></span>';

		// The type of the connection, e.g. the predicate.
		var type = '<a href="' + props.predicateURI.slice(1, -1)
				+ '" target="_blank" style="margin-right: 5px;">'
				+ props.predicate + '</a>';

		// The link to browse to the neighbor node.
		// OR the literal to be shown.
		var neighbor = '';

		if (props.name !== '') {
			// When a name is set, use it for the neighbor.
			neighbor = '<a href="#" onclick="prepareBrowser';
			neighbor += '(\'' + props.name + '\', \'' + URI + '\')';
			neighbor += '" style="margin-right: 5px;">';
			neighbor += props.name + '</a>';
		} else {
			// When there is no name, we have a literal.
			neighbor = '<span style="margin-right: 5px; font-style: italic;">';
			neighbor += props.URI + '</span>';
		}

		// Central node is source => write it left, otherwise right
		toShow += '<div>' + arrow;
		toShow += direction == 'right' ? showCentralNode + type + neighbor
				: neighbor + type + showCentralNode;
		toShow += '</div>';
	});

	$('#browserModalBody').html(toShow);
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
