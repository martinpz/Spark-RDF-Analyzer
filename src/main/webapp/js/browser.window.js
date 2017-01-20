// ########################## RDF Browser Window ##########################
function showBrowser(centralNode, centralNodeURI) {
	$('#browser').show(ANIMATION_SPEED);
	$('#entrypoint').hide(ANIMATION_SPEED);

	// Fill browser div with content.
	if(useTextualBrowsing()) {
		prepareTextualBrowser(centralNode, centralNodeURI);
	} else {
		prepareVisualBrowser(centralNode, centralNodeURI);
	}
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
