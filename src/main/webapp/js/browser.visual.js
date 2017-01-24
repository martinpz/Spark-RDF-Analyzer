// ########################## RDF Browser Graphical ##########################
var s; // Global variable for the sigma graph instance.

function arrangeNodesCircular(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, calculatePositionCircular);
}

function arrangeNodesByDirection(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, calculatePositionByDirection);
}

function calculatePositionCircular(currEdgeNum, totalNumNeighbors, direction) {
	return { 
		x: Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors) 
	};
}

function calculatePositionByDirection(currEdgeNum, totalNumNeighbors, direction) {
	return {
		x: ( direction == 'out' ? 2 : -2 ) * Math.abs(Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors)),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors)
	};
}

function arrangeNodes(centralNode, centralNodeURI, neighbors, calculatePosition) {
	const OPACITY = 0.4;
	var literalCount = 0;
	var edgeCount = 0;
	var numNeighbors = Object.keys(neighbors).length;
	var centralNodeID = centralNodeURI.slice(1, -1);
	var g = {
		nodes: [],
		edges: []
    };

	// Add central node to the graph instance.
    g.nodes.push({
		id: centralNodeID,
		label: centralNode,
		type: 'centralNode',
		x: 0,
		y: 0,
		size: 10,
		color: 'rgb(' + getColorScheme().centralNode + ')'
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function(URI, props) {
		// [Log] URI=<http://dbpedia.org/resource/Harry_and_the_Potters>
		// [Log] {predicate: "artist", predicateURI: "<http://dbpedia.org/property/artist>", name: "Harry_and_the_Potters", URI: "<http://dbpedia.org/resource/Harry_and_the_Potters>", direction: "out"}
		
		// Calculate position for node with given function.
		var position = calculatePosition(edgeCount, numNeighbors, props.direction);
		
		// Create default node and edge. Assume OUT-going connection.
		var node = {
			id: URI.slice(1, -1),
			label: props.name,
			type: 'neighbor',
			x: position.x,
			y: position.y,
			size: 2,
			color: 'rgba(' + getColorScheme().neighbor + ',  ' + OPACITY + ')',
			hover_color: 'rgb(' + getColorScheme().neighbor + ')'
		};

		var edge = {
			id: props.direction + '_e_' + edgeCount,
			label: props.predicate,
			source: centralNodeID,
			target: node.id,
			size: 1,
			color: 'rgba(' + getColorScheme().outEdge + ', ' + OPACITY + ')',
			hover_color: 'rgb(' + getColorScheme().outEdge + ')'
		};

		if( props.direction == 'in' ) {
			// Change properties for IN-going connection.
			edge.source = node.id;
			edge.target = centralNodeID;
			edge.color = 'rgba(' + getColorScheme().inEdge + ', ' + OPACITY + ')';
			edge.hover_color = 'rgb(' + getColorScheme().inEdge + ')';
		} else if (props.name == '') {
			// Special handling for literals. They don't have a name, but only an URI.
			node.id = 'LITERAL_' + literalCount;
			node.label = URI.slice(1, -1);
			node.color = 'rgba(127, 127, 127, ' + OPACITY + ')';
			node.hover_color = 'rgb(127, 127, 127)';

			edge.target = node.id;

			++literalCount;
		}

		g.nodes.push(node);
		g.edges.push(edge);

		++edgeCount;
	});

	instantiateGraph(g);
	bindListeners();
}

function instantiateGraph(g) {
 	// Instantiate the sigma instance with the graph data.
	s = new sigma({
		graph: g,
		renderer: {
			container: document.getElementById('container'),
			type: 'canvas'
		},
		settings: SIGMA_GRAPH_SETTINGS
	});
}

function bindListeners() {
	s.bind('overNode', function(e) {
		// TODO
		// console.log(e.type, e.data.node, e.data.captor);
	});

	s.bind('outNode', function(e) {
		// TODO
		// console.log(e.type, e.data.node, e.data.captor);
	});

	s.bind('clickNode', function(e) {
		// Don't browse when clicking a literal.
		if ( !(e.data.node.id).startsWith('LITERAL') ) {
			prepareBrowser(e.data.node.label, '<' + e.data.node.id + '>');
		}
	});

	s.bind('overEdge', function(e) {
		// TODO
		// console.log(e.type, e.data.edge, e.data.captor);
	});

	s.bind('outEdge', function(e) {
		// TODO
		// console.log(e.type, e.data.edge, e.data.captor);
	});

	s.bind('clickEdge', function(e) {
		// TODO
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
