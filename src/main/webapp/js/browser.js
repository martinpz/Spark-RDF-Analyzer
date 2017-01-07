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
	var numNeighbors = 5;
	var xhttp = new XMLHttpRequest();

	$('#browserModalBody').html(
			'<p>Computing the neighbors for ' + selectedValue + ' ...</p>'
					+ loader);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			displayNodes(centralNode, xhttp.responseText);
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNode)
			+ '&numNeighbors=' + numNeighbors + '&distance=1', true);
	xhttp.send();
}

function displayNodes(centralNode, neighbors) {
	console.log('Received message ...');
	console.log('CentralNode=' + centralNode);
	console.log('--- neighbors ---');
	console.log(neighbors);

	$('#browserModalBody').text('Central Node: ' + centralNode);
	$('#browserModalBody').after('<div>' + neighbors + '</div>');
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
