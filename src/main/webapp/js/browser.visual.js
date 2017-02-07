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
		x: 0, //-($('#container').width() / 2),
		y: 0, //-($('#container').height() / 2),
		size: 1,
		type: 'rect',
		data: {
			type: 'CENTRALNODE',
			direction: '',
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
			type: 'rect',
			x: position.x,
			y: position.y,
			size: 1,
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
	layoutGraph();
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
						return value.substr(0, 22);
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
	const yFactor = (quadrant == 2 || quadrant == 3) ? 1 : -1;
	const outerMargin = 50;
	const container = {
		width: $('#container').width() / 2 - outerMargin,
		height: $('#container').height() / 2 - outerMargin
	};

	return {
		x: xFactor * container.width * Math.random(),
		y: yFactor * container.height * Math.random()
	};
}

function layoutGraph() {
	switch (getLayoutAlgorithm()) {
		case 'noverlap':
			performNOverlap();
			break;
		case 'forcelink':
			performForceLink();
			break;
		case 'fruchterman':
			performFruchtermanReingold();
			break;

		default:
			// Do not layout the graph.
			break;
	}
}

function performNOverlap() {
	var no = s.configNoverlap({
		nodeMargin: 5.0,
		scaleNodes: 1.0,
		gridSize: 50,
		permittedExpansion: 1.1,
		speed: 2,
		maxIterations: 500,
		easing: 'cubicInOut', // animation transition function (see sigma.utils.easing for available transitions)
		duration: 2000 // animation duration
	});
	s.startNoverlap();
}

function performForceLink() {
	var fl = sigma.layouts.configForceLink(s, {
		linLogMode: false, // def=false true = alternative energy model with linear repulsion force and logarithmic attraction force.
		outboundAttractionDistribution: false, // def=false
		autoadjustSizes: true, // def=false
		scaleRatio: 1, // scalingRatio def=1
		stringGravityMode: false, // def=false
		gravity: 1, // def=1
		barnesHutOptimize: false, // def=false should we use the algorithm's Barnes-Hut to improve repulsion's scalability? This is useful for large graph but harmful to small ones.
		barnesHutTheta: 0.5, // def=0.5
		slowDown: 1, // def=1
		startingIterations: 1, // def=1 number of iterations to be run before the first render.
		iterationsPerRender: 1, // def=1 number of iterations to be run before each render.
		maxIterations: 300, // def=1000 set a limit if autoStop: true
		avgDistanceThreshold: 0.01, // def=0.01 this is the normal stopping condition of autoStop: true. When the average displacements of nodes is below this threshold, the layout stops.
		autoStop: true, // def=false
		worker: true, // def=true should the layout use a web worker?
		background: true, // def=false run the layout on background, apply the new nodes position on stop.
		easing: 'cubicInOut',
		randomize: 'local', // def='' randomize the initial x and y coordinates of the nodes. Available values: globally || local
		randomizeFactor:1 // def=1 multiplicator of the Math.random() function if the randomize setting is used.
	});
	sigma.layouts.startForceLink();
}

function performFruchtermanReingold() {
	var fr = sigma.layouts.fruchtermanReingold.configure(s, {
		autoArea: true,
		area: 1,
		gravity: 1,
		speed: 0.1,
		iterations: 1000,
		easing: 'quadraticInOut',
		duration: 2000
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