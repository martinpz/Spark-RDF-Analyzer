// ########################## RDF Browser Graphical ##########################
var s; // Global variable for the sigma graph instance.

function prepareVisualBrowser(centralNode, centralNodeURI) {
	var xhttp = new XMLHttpRequest();

	showLoader(centralNode);
	updateBrowsingHistory(centralNode, centralNodeURI);
	
	// Show button for SVG export. But disable in Safari.
	$('#btnExportGraphSVG').show();

	if (navigator.userAgent.match(/Version\/[\d\.]+.*Safari/)) {
		$('#btnExportGraphSVG').prop('disabled', true);
		$('#btnExportGraphSVG').prop('title', 'SVG Export does not work in Safari.');
	}

	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			updateBrowserHeight();
			displayNodesVisual(centralNode, centralNodeURI, JSON.parse(xhttp.responseText));
		}
	}

    xhttp.open('GET', getNeighborhoodRequest(centralNodeURI), true);
	xhttp.send();
}

function displayNodesVisual(centralNode, centralNodeURI, neighbors) {
	// Clear the container.
	$('#browserBody').html('<div id="container"></div>');

	var edgeCount = 0;
	var numNeighbors = Object.keys(neighbors).length;
	var g = {
		nodes: [],
		edges: []
    };

    // Add central node to the graph instance.
    g.nodes.push({
		id: centralNodeURI.slice(1, -1),
		label: centralNode,
		x: 0,
		y: 0,
		size: 5,
		color: 'brown'
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function(URI, props) {
		// [Log] URI=<http://dbpedia.org/resource/Harry_and_the_Potters>
		// [Log] {predicate: "artist", predicateURI: "<http://dbpedia.org/property/artist>", name: "Harry_and_the_Potters", URI: "<http://dbpedia.org/resource/Harry_and_the_Potters>", direction: "out"}

		var name = props.name == '' ? URI.slice(1, -1) : props.name;
		var source = props.direction == 'out' ? centralNodeURI : URI;
		var target = props.direction == 'out' ? URI : centralNodeURI;
		var colorNode = props.direction == 'out' ? 'lightseagreen' : 'lightseagreen';
		var hoverColorNode = props.direction == 'out' ? 'seagreen' : 'seagreen';
		var colorEdge = props.direction == 'out' ? 'lightgreen' : 'coral';
		var hoverColorEdge = props.direction == 'out' ? 'green' : 'orangered';

		g.nodes.push({
			id: URI.slice(1, -1),
			label: name,
			x: Math.cos(Math.PI * 2 * edgeCount / numNeighbors),
			y: Math.sin(Math.PI * 2 * edgeCount / numNeighbors),
			size: 2,
			color: colorNode,
			hover_color: hoverColorNode
		});

		g.edges.push({
			id: 'e' + edgeCount,
			label: props.predicate,
			source: source.slice(1, -1),
			target: target.slice(1, -1),
			size: 1, // Math.random(),
			color: colorEdge,
			hover_color: hoverColorEdge,
			type: 'arrow'
		});

		++edgeCount;
	});

    // Finally, let's ask our sigma instance to refresh:
	s = new sigma({
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
			edgeLabelSize: 'proportional',
			edgeHoverSizeRatio: 3,
			edgeHoverExtremities: true
		}
	});

	// Bind event handlers to nodes and edges.
	s.bind('overNode outNode clickNode', function(e) {
		// console.log(e.type, e.data.node.label, e.data.captor);

		if (e.type === 'overNode') {
			// console.log('HOVER!!!');
		} else if (e.type === 'outNode') {
			// console.log('EXITED!!!');
		} else if (e.type === 'clickNode') {
			prepareVisualBrowser(e.data.node.label, '<' + e.data.node.id + '>');
		}
	});

	s.bind('overEdge outEdge clickEdge', function(e) {
		// console.log(e.type, e.data.edge, e.data.captor);
	});
}

function exportGraphAsSVG() {
	var output = s.toSVG({
		download: true,
		filename: 'graphExport.svg',
		size: 1000,
		labels: true,
		data: true
	});
}
