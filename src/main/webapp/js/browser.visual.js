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
		type: 'centralNode',
		x: 0,
		y: 0,
		size: 5,
		color: 'rgb(' + getColorScheme().centralNode + ')'
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function(URI, props) {
		// [Log] URI=<http://dbpedia.org/resource/Harry_and_the_Potters>
		// [Log] {predicate: "artist", predicateURI: "<http://dbpedia.org/property/artist>", name: "Harry_and_the_Potters", URI: "<http://dbpedia.org/resource/Harry_and_the_Potters>", direction: "out"}

		var name = props.name == '' ? URI.slice(1, -1) : props.name;
		var source = URI;
		var target = centralNodeURI;
		var opacity = 0.5;
		var colorNode = 'rgba(' + getColorScheme().neighbor + ',  ' + opacity + ')';
		var colorNodeHover = 'rgb(' + getColorScheme().neighbor + ')';
		var colorEdge = 'rgba(' + getColorScheme().inEdge + ', ' + opacity + ')';
		var colorEdgeHover = 'rgb(' + getColorScheme().inEdge + ')';

		// Calculate position for node with given function.
		var position = calculatePosition(edgeCount, numNeighbors, props.direction);

		if( props.direction == 'out' ) {
			source = centralNodeURI;
			target = URI;
			colorEdge = 'rgba(' + getColorScheme().outEdge + ', ' + opacity + ')';
			colorEdgeHover = 'rgb(' + getColorScheme().outEdge + ')';
		}

		g.nodes.push({
			id: URI.slice(1, -1),
			label: name,
			type: 'neighbor',
			x: position.x,
			y: position.y,
			size: 3,
			color: colorNode,
			hover_color: colorNodeHover
		});

		g.edges.push({
			id: 'e' + edgeCount,
			label: props.predicate,
			source: source.slice(1, -1),
			target: target.slice(1, -1),
			size: 3,
			color: colorEdge,
			hover_color: colorEdgeHover,
			type: 'arrow'
		});

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
		settings: {
			doubleClickEnabled: false,
			drawEdgeLabels: true,
			drawLabels: false, // on nodes
			enableCamera: true, // false = disable zoom and movement
			enableEdgeHovering: true,
			enableHovering: true, // for nodes
			edgeLabelSize: 'proportional',
			//edgeHoverSizeRatio: 3,
			edgeHoverExtremities: true, // true = hover the nodes, connected to an edge, too
			sideMargin: 0.05
		}
	});
}

function bindListeners() {
	// Bind event handlers to nodes and edges.
	s.bind('overNode outNode clickNode', function(e) {
		// console.log(e.type, e.data.node.label, e.data.captor);

		if (e.type === 'overNode') {
			// console.log('HOVER!!!');
		} else if (e.type === 'outNode') {
			// console.log('EXITED!!!');
		} else if (e.type === 'clickNode') {
			prepareBrowser(e.data.node.label, '<' + e.data.node.id + '>');
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
