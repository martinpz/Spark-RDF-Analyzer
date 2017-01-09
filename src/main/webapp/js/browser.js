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

	prepareBrowser(selectedValue, selectedURI);
}

// ########################## RDF Browser ##########################
function prepareBrowser(selectedValue, centralNode) {
	var xhttp = new XMLHttpRequest();

	updateBrowsingHistory(selectedValue, centralNode);

	$('#browserModalBody').html(
			'<p>Computing the neighbors for ' + selectedValue + ' ...</p>'
					+ loader);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displayNodes(centralNode, JSON.parse(xhttp.responseText));
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNode)
			+ '&numNeighbors=5', true);
	xhttp.send();
}

function updateBrowsingHistory(currentName, currentNode) {
	// Add link to pre-last element. Remove active marker.
	var lastNode = $('#browsingHistory #list li').last();
	var lastURI = lastNode.attr('data-uri');
	var lastName = lastNode.text();

	lastNode.removeClass('active');
	lastNode.html('<a href="#" onclick="' + 'prepareBrowser(\'' + lastName
			+ '\', \'' + lastURI + '\')' + '">' + lastName + '</a>');

	// Append new last (= current) element. Make it active.
	$('#browsingHistory #list').append(
			'<li class="active" data-uri="' + currentNode + '">' + currentName
					+ '</li>');
}

function displayNodes(centralNode, neighbors) {
	// Remove < and > from URI.
	var toShow = '<p><strong>Central Node: ' + centralNode.slice(1, -1)
			+ '</strong></p>';

	$.each(neighbors, function(URI, name) {
		toShow += '<a href="#" onclick="';
		toShow += 'prepareBrowser(\'' + name + '\', \'' + URI + '\')';
		toShow += '">' + name + '</a>';
		toShow += '<br>';
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
