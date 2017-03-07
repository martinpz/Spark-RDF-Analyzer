// ########################## RDF Browser Graphical ##########################
const VISIBLE_CHARS = 25; // The maximum number of characters to show for labels.
var cy; // Global variable for the sigma graph instance.

function arrangeNodes(centralNode, centralNodeURI, neighbors, layout, withEdges) {
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
			type: 'CENTRALNODE',
			disableGoTo: 'disabled'
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
				classes: props.direction
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
			node.classes = 'literal';
			edge.data.target = node.data.id;
		}

		graph.push(node);
		graph.push(edge);

		++nodeCount
		++edgeCount;
	});

	// Instantiate the Cytoscape instance.
	cy = getCytoscapeInstance(graph, layout, withEdges);

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
var skipNextClick = false;

function bindListeners() {
	cy.on('tap', 'node', function (evt) {
		// Only browse when clicking a neighbor. Not on central node or a literal.
		if (!skipNextClick && (this.id()).startsWith('NEIGHBOR')) {
			prepareBrowser(this.data('name'), this.data('uri'));
		}
		skipNextClick = false;
	});

	cy.on('cxttap', 'node', function (evt) {
		showTooltip(this);
	});

	cy.on('taphold', 'node', function (evt) {
		skipNextClick = true;
		showTooltip(this);
	});
}

function showTooltip(node) {
	node.qtip({
		content: getTooltip(node.data()),
		show: {
			event: 'cxttap taphold',
			ready: true,
			solo: true
		}
	});
}

// ==================== Resizing ==================== //

function updateGraphSize() {
	cy.resize().layout();
}

// ==================== Export ==================== //

function exportGraphAsPNG() {
	const img = cy.png();
	$('#imgGraphExport').attr('src', '' + img);
}