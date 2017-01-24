// ########################## RDF Browser Graphical ##########################
var s; // Global variable for the sigma graph instance.

function arrangeNodesCircular(centralNode, centralNodeURI, neighbors) {
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

	instantiateGraph(g);
	bindListeners();
}

function arrangeNodesByDirection(centralNode, centralNodeURI, neighbors) {
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
		var factor = -2;

		if( props.direction == 'out' ) {
			source = centralNodeURI;
			target = URI;
			colorEdge = 'rgba(' + getColorScheme().outEdge + ', ' + opacity + ')';
			colorEdgeHover = 'rgb(' + getColorScheme().outEdge + ')';
			factor = 2;
		}

		g.nodes.push({
			id: URI.slice(1, -1),
			label: name,
			type: 'neighbor',
			x: factor * Math.abs(Math.cos(Math.PI * 2 * edgeCount / numNeighbors)),
			y: Math.sin(Math.PI * 2 * edgeCount / numNeighbors),
			size: 3,
			color: colorNode,
			hover_color: colorNodeHover
		});

		g.edges.push({
			id: 'e' + edgeCount,
			label: props.predicate,
			source: source.slice(1, -1),
			target: target.slice(1, -1),
			size: 3, // Math.random(),
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
