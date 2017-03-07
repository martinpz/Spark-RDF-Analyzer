// ########################## RDF Browser Window ##########################
function showBrowser(centralNode, centralNodeURI) {
	$('#browser').show(ANIMATION_SPEED);
	$('#entrypoint').hide(ANIMATION_SPEED);

	prepareBrowser(centralNode, centralNodeURI);
}

function prepareBrowser(centralNode, centralNodeURI) {
	var xhttp = new XMLHttpRequest();

	showLoader(centralNode);
	updateBrowsingHistory(centralNode, centralNodeURI);

	xhttp.onreadystatechange = function () {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			updateBrowserHeight();
			displayNodes(centralNode, centralNodeURI, JSON.parse(xhttp.responseText));
		}
	}

	xhttp.open('GET', getNeighborhoodRequest(centralNodeURI), true);
	xhttp.send();
}

function displayNodes(centralNode, centralNodeURI, neighbors) {
	// Clear the container.
	$('#browserBody').html('<div id="container" data-central-node="' + centralNode + '" data-central-node-uri="' + centralNodeURI + '"></div>');

	// Enable the export for visual representations and expand the browser body.
	enableVisualActions(true);
	$('#browserBody').addClass('visual');
	updateColorKeys();

	// Determine how to display the graph.
	const browsingType = getBrowsingType();

	if (browsingType == 'TEXTUAL') {
		enableVisualActions(false);
		$('#browserBody').removeClass('visual');
		displayNodesTextual(centralNode, centralNodeURI, neighbors);
	} else {
		arrangeNodes(centralNode, centralNodeURI, neighbors, browsingType);
	}
}

function enableVisualActions(enable) {
	$('#btnExportGraph').prop('disabled', !enable);
	$('#btnShowKey').prop('disabled', !enable);
}

function updateColorKeys() {
	const colorScheme = getColorScheme();
	$('#keyForUsedColors li span.central').css('background-color', COLORS[colorScheme].central.back);
	$('#keyForUsedColors li span.in').css('background-color', COLORS[colorScheme].in.back);
	$('#keyForUsedColors li span.out').css('background-color', COLORS[colorScheme].out.back);
	$('#keyForUsedColors li span.literal').css('background-color', COLORS[colorScheme].literal.back);
}

function showLoader(centralNode) {
	$('#browserBody').html('<p>Computing the neighbors for ' + centralNode + ' ...</p>' + LOADER);
}

function updateBrowserHeight() {
	// Update the height of the body div w.r.t. to the outer divs.
	var headerTop = $('#browserHeader').offset().top;
	var headerHeight = $('#browserHeader').outerHeight();
	var bottomSpace = 40;

	// For a fullscreen browser, we only have to respect the header height.
	var heightDiff = $('#browser').hasClass('fullscreen') ?
		headerHeight :
		headerTop + headerHeight + bottomSpace;

	$('#browserBody').css('height', 'calc(100vh - ' + heightDiff + 'px)');
}

function reloadGraph() {
	// Reload the graph with the stored values for the central node.
	var centralNode = $('#container').attr('data-central-node');
	var centralNodeURI = $('#container').attr('data-central-node-uri');

	removeLastHistoryElement();
	prepareBrowser(centralNode, centralNodeURI);
}

function toggleBrowserFullscreen() {
	$('#browser').toggleClass('fullscreen');
	$('#btnFullscreenBrowser > span').toggleClass('glyphicon-resize-full glyphicon-resize-small');
	updateBrowserHeight();
	updateGraphSize();
}

function closeBrowser() {
	$('#browser').hide(ANIMATION_SPEED);
	$('#entrypoint').show(ANIMATION_SPEED);
	showReturnToBrowser();
}

function returnToBrowser() {
	$('#browser').show(ANIMATION_SPEED);
	$('#entrypoint').hide(ANIMATION_SPEED);
}

$(document).ready(function () {
	$('#btnReloadGraph').click(function () {
		reloadGraph();
	});
	$('#btnExportGraph').click(function () {
		exportGraphAsPNG();
	});
	$('#btnFullscreenBrowser').click(function () {
		toggleBrowserFullscreen();
	});
	$('#btnCloseBrowser').click(function () {
		closeBrowser();
	});
});