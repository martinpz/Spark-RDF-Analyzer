// ########################## RDF Browser Graphical ##########################
var s; // Global variable for the sigma graph instance.

function arrangeNodesCircular(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionCircular);
}

function arrangeNodesByDirection(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionByDirection);
}

function arrangeNodesRandomized(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, false, calculatePositionRandomly);
}

function arrangeNodes(centralNode, centralNodeURI, neighbors, withEdges, calculatePosition) {
	var nodeCount = 0,
		edgeCount = 0;
	var numNeighbors = Object.keys(neighbors).length;
	var centralNodeID = 'CENTRALNODE';
	var g = {
		nodes: [],
		edges: []
	};

	// Add central node to the graph instance.
	g.nodes.push({
		id: centralNodeID,
		label: centralNode,
		x: 0,
		y: 0,
		size: 1,
		level: 3,
		labelAlignment: 'bottom',
		type: 'circle',
		data: {
			direction: 'central',
			color: 'central',
			name: centralNode,
			uri: centralNodeURI,
			link: centralNodeURI.slice(1, -1),
			predicate: '',
			predicateLink: '#',
			disableGoTo: 'disabled'
		}
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function (URI, props) {
		// Calculate position for node with given function.
		var position = calculatePosition(edgeCount, numNeighbors, props.direction);

		// Create default node and edge. Assume OUT-going connection.
		var node = {
			id: 'NEIGHBOR_' + nodeCount,
			label: props.name,
			type: 'square',
			x: position.x,
			y: position.y,
			size: 0.3,
			level: 1,
			labelAlignment: 'top',
			data: {
				type: 'NEIGHBOR',
				direction: '(' + props.direction + ')',
				color: props.direction,
				name: props.name,
				uri: URI,
				link: URI.slice(1, -1),
				predicate: props.predicate,
				predicateLink: props.predicateURI.slice(1, -1)
			}
		};

		var edge = {
			id: props.direction + '_e_' + edgeCount,
			label: props.predicate,
			source: centralNodeID,
			target: node.id,
			size: 1
		};

		if (props.direction == 'in') {
			// Change properties for IN-going connection.
			edge.source = node.id;
			edge.target = centralNodeID;
			node.data.color = 'in';
		} else if (props.name == '') {
			// Special handling for literals. They don't have a name, but only an URI.
			node.id = 'LITERAL_' + edgeCount;
			node.label = URI;
			node.type = 'star';
			node.data.type = 'LITERAL';
			node.data.direction = '';
			node.data.color = 'literal';
			node.data.name = URI;
			node.data.uri = '';
			node.data.link = '#';
			node.data.disableGoTo = 'disabled';

			edge.target = node.id;
		}

		g.nodes.push(node);

		if (withEdges) {
			g.edges.push(edge);
		}

		++nodeCount
		++edgeCount;
	});

	instantiateGraph(g);
	instantiateTooltips();
	bindListeners();
	designGraph();
	// layoutGraph();
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

// ==================== Tooltips ==================== //

var tooltips;

function instantiateTooltips() {
	const TOOLTIP_CONFIG = {
		node: {
			show: 'rightClickNode',
			cssClass: 'sigma-tooltip',
			position: 'top',
			autoadjust: true,
			template: '<div class="arrow"></div>' +
				'	<div class="sigma-tooltip-header">' +
				'		{{data.type}}&nbsp;' +
				'		<span style="font-size: 90%;">' +
				'			{{data.direction}}' +
				'		</span>' +
				'		<div id="tooltipActionBtns" class="btn-group" role="group" aria-label="Actions">' +
				'			<button id="btnGoToNode" type="button" onclick="prepareBrowser(\'{{data.name}}\', \'{{data.uri}}\')" class="btn btn-primary" aria-label="Go to node" title="Navigate to this node." {{data.disableGoTo}}>' +
				'				<span class="glyphicon glyphicon-share-alt" aria-hidden="true"></span>' +
				'			</button>' +
				'			<button id="btnCloseTooltip" type="button" onclick="closeTooltips()" class="btn btn-default" aria-label="Close tooltip" title="Close this tooltip.">' +
				'				<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>' +
				'			</button>' +
				'		</div>' +
				'	</div>' +
				'	<div class="sigma-tooltip-body">' +
				'		{{data.name}}' +
				'		<br><br>' +
				'		<span style="white-space: nowrap;">Predicate: <a href="{{data.predicateLink}}" target="_blank">{{data.predicate}}</a></span><br>' +
				'		<span style="white-space: nowrap;">URI: <a href="{{data.link}}" target="_blank">{{data.uri}}</a></span>' +
				'	</div>',
			renderer: function (node, template) {
				return Mustache.render(template, node);
			}
		}
	};

	// Instantiate the tooltips plugin with a Mustache renderer for node tooltips.
	tooltips = sigma.plugins.tooltips(s, s.renderers[0], TOOLTIP_CONFIG);
}

function closeTooltips() {
	if (tooltips) {
		tooltips.close();
	}
}

// ==================== Listeners ==================== //

function bindListeners() {
	s.bind('doubleClickNode', function (e) {
		// Only browse when clicking a neighbor. Not on central node or a literal.
		if ((e.data.node.id).startsWith('NEIGHBOR')) {
			prepareBrowser(e.data.node.data.name, e.data.node.data.uri);
		}
	});
}

// ==================== Design ==================== //

function designGraph() {
	var design = sigma.plugins.design(s, {
		styles: {
			nodes: {
				label: {
					by: 'label',
					format: function (value) {
						return value.substr(0, 10);
					}
				},
				color: {
					by: 'data.color',
					scheme: getColorScheme()
				}
			},
			edges: {
				// TODO
			}
		},
		palette: COLORS
	});

	design.apply('nodes');
}

// ==================== Layout ==================== //

function calculatePositionCircular(currEdgeNum, totalNumNeighbors, direction) {
	return {
		x: Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors)
	};
}

function calculatePositionByDirection(currEdgeNum, totalNumNeighbors, direction) {
	return {
		x: (direction == 'out' ? 2 : -2) * Math.abs(Math.cos(Math.PI * 2 * currEdgeNum / totalNumNeighbors)),
		y: Math.sin(Math.PI * 2 * currEdgeNum / totalNumNeighbors)
	};
}

function calculatePositionRandomly(currEdgeNum, totalNumNeighbors, direction) {
	const quadrant = (currEdgeNum % 4) + 1;
	const xFactor = (quadrant == 1 || quadrant == 2) ? 1 : -1;
	const yFactor = (quadrant == 1 || quadrant == 4) ? 1 : -1;
	const container = {
		width: $('#container').width() / 2,
		height: $('#container').height() / 2
	};

	return {
		x: xFactor * Math.random() * container.width,
		y: yFactor * Math.random() * container.height
	};
}

function layoutGraph() {
	switch (getLayoutAlgorithm()) {
		case 'forceatlas':
			performForceAtlas();
			break;
		case 'forcelink':
			performForceLink();
			break;
		case 'fruchterman':
			performFruchtermanReingold();
			break;
		case 'noverlap':
			performNOverlap();
			break;

		default:
			console.log('Unsupported layout method.');
			break;
	}
}

function performNOverlap() {
	var noverlapListener = s.configNoverlap({
		maxIterations: 300,
		nodeMargin: 0.1,
		// scaleNodes: 1.2,
		gridSize: 50,
		easing: 'quadraticInOut', // animation transition function (see sigma.utils.easing for available transitions)
		duration: 1000 // animation duration
	});

	s.startNoverlap();
}

function performForceAtlas() {
	var fa = s.startForceAtlas2({
		worker: true
	});
}

function performForceLink() {
	var fa = sigma.layouts.configForceLink(s, {
		worker: true,
		barnesHutOptimize: false,
		autoStop: true,
		background: true,
		// scaleRatio: 10, // respectively 30 for arctic
		// gravity: 3,
		easing: 'cubicInOut'
	});

	fa.bind('start interpolate stop', function (e) {
		console.log('EVENT: ', e.type);
	});

	sigma.layouts.startForceLink();
}

function performFruchtermanReingold() {
	var fr = sigma.layouts.fruchtermanReingold.configure(s, {
		iterations: 500,
		easing: 'quadraticInOut',
		duration: 800
	});

	fr.bind('start stop interpolate', function (e) {
		console.log('Event: ', e.type);
	});

	sigma.layouts.fruchtermanReingold.start(s);
}

// ==================== Export ==================== //

function exportGraphAsPNG() {
	console.log('renderes:', s.renderers);
	sigma.plugins.image(s, s.renderers[0], {
		download: true,
		filename: 'graphExport.png',
		size: 500,
		margin: 50,
		background: 'white',
		zoomRatio: 1,
		labels: true
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