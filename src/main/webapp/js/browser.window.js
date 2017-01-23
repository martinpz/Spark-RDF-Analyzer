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

	xhttp.onreadystatechange = function() {
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
	$('#browserBody').html('<div id="container"></div>');

	// Determine how to display the graph.
	switch (getBrowsingType()) {
		case 'circular':
			arrangeNodesCircular(centralNode, centralNodeURI, neighbors);
			showSVGexport();
			break;
		case 'direction':
			arrangeNodesByDirection(centralNode, centralNodeURI, neighbors);
			showSVGexport();
			break;
		case 'textual':
			displayNodesTextual(centralNode, centralNodeURI, neighbors);
			hideSVGexport();
			break;
		default:
			console.error('Undefined browsing type.');
			break;
	}
}

function showSVGexport() {
	// Show button for SVG export. But disable in Safari, since it is not supported.
	$('#btnExportGraphSVG').show();

	if (navigator.userAgent.match(/Version\/[\d\.]+.*Safari/)) {
		$('#btnExportGraphSVG').prop('disabled', true);
		$('#btnExportGraphSVG').prop('title', 'SVG Export does not work in Safari.');
	} else {
		$('#btnExportGraphSVG').prop('disabled', false);
		$('#btnExportGraphSVG').prop('title', 'Export the graph as a vector graphic.');
	}
}

function hideSVGexport() {
	$('#btnExportGraphSVG').hide();
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
	var heightDiff = $('#browser').hasClass('fullscreen')
		? headerHeight
		: headerTop + headerHeight + bottomSpace;

	$('#browserBody').css('height', 'calc(100vh - ' + heightDiff + 'px)');
}

function toggleBrowserFullscreen() {
	$('#browser').toggleClass('fullscreen');
	updateBrowserHeight();
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

$(document).ready(function() {
	$('#btnExportGraphSVG').click( function() {
		exportGraphAsSVG();
	});
	$('#btnFullscreenBrowser').click( function() {
		toggleBrowserFullscreen();
	});
	$('#btnCloseBrowser').click( function() {
		closeBrowser();
	});
});
