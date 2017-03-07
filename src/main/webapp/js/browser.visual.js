// ########################## RDF Browser Graphical ##########################
const VISIBLE_CHARS = 25; // The maximum number of characters to show for labels.
var cy; // Global variable for the sigma graph instance.

function arrangeNodesCircular(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionCircular);
}

function arrangeNodesByDirection(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionByDirection);
}

function arrangeNodesRandomized(centralNode, centralNodeURI, neighbors) {
	arrangeNodes(centralNode, centralNodeURI, neighbors, true, calculatePositionRandomly);
}

function arrangeNodes(centralNode, centralNodeURI, neighbors, withEdges, calculatePosition) {
	var nodeCount = 0,
		edgeCount = 0,
		numNeighbors = Object.keys(neighbors).length,
		centralNodeID = 'CENTRALNODE',
		graph = [];

	// Add central node to the graph instance.
	graph.push({
		group: 'nodes',
		data: {
			id: centralNodeID,
			name: centralNode,
			label: centralNode.substr(0, VISIBLE_CHARS),
			uri: centralNodeURI,
			link: centralNodeURI.slice(1, -1),
			predicate: '',
			predicateLink: '#',
			type: 'CENTRALNODE'
		},
		position: {
			x: 0,
			y: 0
		}
	});

	// Add all neighbor nodes to the graph instance.
	$.each(neighbors, function (URI, props) {
		// Create default node and edge. Assume OUT-going connection.
		var node = {
				group: 'nodes',
				data: {
					id: 'NEIGHBOR_' + nodeCount,
					name: props.name,
					label: props.name.substr(0, VISIBLE_CHARS),
					uri: URI,
					link: URI.slice(1, -1),
					predicate: props.predicate,
					predicateLink: props.predicateURI.slice(1, -1),
					direction: props.direction,
					type: 'NEIGHBOR'
				},
				// Calculate initial position for node with given function.
				position: calculatePosition(edgeCount, numNeighbors, props.direction)
			},
			edge = {
				group: 'edges',
				data: {
					id: props.direction + '_e_' + edgeCount,
					label: props.predicate.substr(0, VISIBLE_CHARS),
					source: centralNodeID,
					target: node.data.id,
					isdirected: true
				}
			};

		if (props.direction == 'in') {
			// Change properties for IN-going connection.
			edge.data.source = node.data.id;
			edge.data.target = centralNodeID;
		} else if (props.name == '') {
			// Special handling for literals. They don't have a name, but only an URI.
			const LITERAL = evaluateRDFLiteral(URI);

			node.data.id = 'LITERAL_' + edgeCount;
			node.data.name = LITERAL;
			node.data.label = LITERAL.substr(0, VISIBLE_CHARS);
			node.data.uri = '';
			node.data.type = 'LITERAL';
			node.data.link = '#';
			node.data.disableGoTo = 'disabled';

			edge.data.target = node.data.id;
		}

		graph.push(node);

		if (withEdges) {
			graph.push(edge);
		}

		++nodeCount
		++edgeCount;
	});

	// Instantiate the Cytoscape instance.
	cy = getCytoscapeInstance(graph);

	bindListeners();
}

// ==================== Tooltips ==================== //

const TOOLTIP_TEMPLATE =
	'	<div class="tooltipHeader">' +
	'		{{type}}&nbsp;' +
	'		<span style="font-size: 90%;">' +
	'			{{direction}}' +
	'		</span>' +
	'		<div id="tooltipActionBtns" class="btn-group" role="group" aria-label="Actions">' +
	'			<button id="btnGoToNode" type="button" onclick="prepareBrowser(\'{{name}}\', \'{{uri}}\'); closeTooltip()" class="btn btn-primary" aria-label="Go to node" title="Navigate to this node." {{disableGoTo}}>' +
	'				<span class="glyphicon glyphicon-share-alt" aria-hidden="true"></span>' +
	'			</button>' +
	'			<button id="btnCloseTooltip" type="button" onclick="closeTooltip()" class="btn btn-default" aria-label="Close tooltip" title="Close this tooltip.">' +
	'				<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>' +
	'			</button>' +
	'		</div>' +
	'	</div>' +
	'	<div class="tooltipBody">' +
	'		{{name}}' +
	'		<br><br>' +
	'		<span style="white-space: nowrap;">Predicate: <a href="{{predicateLink}}" target="_blank">{{predicate}}</a></span><br>' +
	'		<span style="white-space: nowrap;">URI: <a href="{{link}}" target="_blank">{{uri}}</a></span>' +
	'	</div>';

function getTooltip(nodeData) {
	return Mustache.render(TOOLTIP_TEMPLATE, nodeData);
}

function closeTooltip() {
	$('.qtip').remove();
}

// ==================== Listeners ==================== //

function bindListeners() {
	cy.on('tap', 'node', function (evt) {
		// Only browse when clicking a neighbor. Not on central node or a literal.
		if ((this.id()).startsWith('NEIGHBOR')) {
			prepareBrowser(this.data('name'), this.data('uri'));
		}
	});

	cy.on('cxttap', 'node', function (evt) {
		this.qtip({
			content: getTooltip(this.data()),
			show: {
				event: 'cxttap',
				ready: true,
				solo: true
			}
		});
	});
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
			/*
			case 'forcelink':
				performForceLink();
				break;
			case 'fruchterman':
				performFruchtermanReingold();
				break;
			*/

		default:
			// Do not layout the graph.
			break;
	}
}

function performNOverlap() {
	s.configNoverlap(LAYOUT_NOVERLAP);
	s.startNoverlap();
}

/*
function performForceLink() {
	sigma.layouts.startForceLink(s, LAYOUT_FORCE_LINK);
}

function performFruchtermanReingold() {
	sigma.layouts.fruchtermanReingold.configure(s, LAYOUT_FRUCHTERMAN_REINGOLD);
	sigma.layouts.fruchtermanReingold.start(s);
}
*/

// ==================== Export ==================== //

function exportGraphAsPNG() {
	console.log('renderes:', s.renderers);
	sigma.plugins.image(s, s.renderers[0], EXPORT_PNG);
}

function exportGraphAsSVG() {
	var output = s.toSVG(EXPORT_SVG);
}