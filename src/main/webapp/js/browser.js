var loader = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
var animationSpeed = 'fast';

// ########################## Entry Point ##########################
function simulateClickOnSearch() {
	$('#btnSearch').click();
}

function showReturnToBrowser() {
	$('#btnReturnToBrowser').removeClass('invisible');
}

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
	$('#browser').show(animationSpeed);
	$('#entrypoint').hide(animationSpeed);

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
	// Remove links from all elements.
	$('#browsingHistory #list li').html('');
	
	// Append new last (=current) element.
	$('#browsingHistory #list').append('<li data-name="' + currentName + '" data-uri="' + currentURI + '"></li>');

	// For all elements equal to the current element:
	// add the 'active' class and only display the name.
	var sameElements = $('#browsingHistory #list li[data-uri=\'' + currentURI + '\']');

	$.each(sameElements, function() {
		$(this).addClass('active');
		$(this).text( $(this).attr('data-name') );
	});

	// For all other elements = not equal to the current one:
	// add the 'data-uri' as the link for the name.
	var others = $('#browsingHistory #list li[data-uri!=\'' + currentURI + '\']');

	$.each(others, function() {
		var name = $(this).attr('data-name');
		var URI = $(this).attr('data-uri');

		$(this).removeClass('active');
		$(this).html('<a href="#" onclick="' + 'prepareTextualBrowser(\'' + name + '\', \'' + URI + '\')' + '">' + name + '</a>');
	});
}

function updateBrowserHeight() {
	// Update the height of the body div w.r.t. to the outer divs.
	var headerTop = $('#browserHeader').offset().top;
	var headerHeight = $('#browserHeader').outerHeight();
	var bottomSpace = 40;

	// For a fullscreen browser, we only have to respect the header height.
	var heightDiff = $('#browser').hasClass('fullscreen')
		? headerHeight
		: headerTop + headerHeight + bottomSpace;

	$('#browserBody').css('height', 'calc(100vh - ' + heightDiff + 'px)');
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
	var xhttp = new XMLHttpRequest();

	showLoader(centralNode);
	updateBrowsingHistory(centralNode, centralNodeURI);

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			updateBrowserHeight();
			displayNodesVisual(centralNode, centralNodeURI, JSON.parse(xhttp.responseText));
		}
	}

	xhttp.open('GET', REST_API + 'directNeighbors/' + getCookie('graphName')
			+ '?centralNode=' + encodeURIComponent(centralNodeURI)
			+ '&numNeighbors=12', true);
	xhttp.send();
}

function displayNodesVisual(centralNode, centralNodeURI, neighbors) {
	// Clear the container.
	$('#browserBody').html('<div id="container"></div>');

	console.log('Let us do the parsing.');

	var edgeCount = 0;
	var g = {
      nodes: [],
      edges: []
    };

    // Add central nodes to the graph instance.
    g.nodes.push({
		id: centralNodeURI,
		label: centralNode,
		x: 0,
		y: 0,
		size: 3,
		color: 'darkred'
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function(URI, props) {
		// toShow += '';

		// [Log] URI=<http://dbpedia.org/resource/Harry_and_the_Potters>
		// [Log] {predicate: "artist", predicateURI: "<http://dbpedia.org/property/artist>", name: "Harry_and_the_Potters", URI: "<http://dbpedia.org/resource/Harry_and_the_Potters>", direction: "out"}

		var name = props.name == '' ? URIS : props.name;
		var src = props.direction == 'out' ? centralNodeURI : URI;
		var tgt = props.direction == 'out' ? URI : centralNodeURI;

		g.nodes.push({
			id: URI,
			label: name,
			x: Math.random(),
			y: Math.random(),
			size: 1,
			color: 'lightblue',
			hover_color: 'darkblue'
		});

		g.edges.push({
			id: 'e' + edgeCount,
			label: props.predicate,
			source: centralNodeURI,
			target: URI,
			size: Math.random(),
			color: 'green',
			hover_color: 'orange',
			type: 'arrow'
		});

		++edgeCount;
	});

    // Finally, let's ask our sigma instance to refresh:
	var s = new sigma({
		graph: g,
		renderer: {
			container: document.getElementById('container'),
			type: 'canvas'
		},
		settings: {
			doubleClickEnabled: false,
			minEdgeSize: 0.5,
			maxEdgeSize: 4,
			enableEdgeHovering: true,
			edgeHoverColor: 'edge',
			defaultEdgeHoverColor: 'pink',
			edgeLabelSize: 'proportional',
			edgeHoverSizeRatio: 1,
			edgeHoverExtremities: true,
		}
	});

	// Bind event handlers to nodes and edges.
	s.bind('overNode outNode clickNode', function(e) {
		console.log(e.type, e.data.node.label, e.data.captor);
	});

	s.bind('overEdge outEdge clickEdge', function(e) {
		console.log(e.type, e.data.edge, e.data.captor);
	});

    // s.refresh();

	console.log('kay,done.');
}

// ########################## OnClick Events ##########################
function toggleBrowserFullscreen() {
	$('#browser').toggleClass('fullscreen');
	updateBrowserHeight();
}

function closeBrowser() {
	$('#browser').hide(animationSpeed);
	$('#entrypoint').show(animationSpeed);
	showReturnToBrowser();
}

function returnToBrowser() {
	$('#browser').show(animationSpeed);
	$('#entrypoint').hide(animationSpeed);
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
