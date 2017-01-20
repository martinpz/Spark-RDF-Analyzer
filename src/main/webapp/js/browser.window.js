// ########################## RDF Browser Window ##########################
function showBrowser(centralNode, centralNodeURI) {
	$('#browser').show(animationSpeed);
	$('#entrypoint').hide(animationSpeed);

	// Fill browser div with content.
	if(useTextualBrowsing()) {
		prepareTextualBrowser(centralNode, centralNodeURI);
	} else {
		prepareVisualBrowser(centralNode, centralNodeURI);
	}
}

function showLoader(centralNode) {
	$('#browserBody').html('<p>Computing the neighbors for ' + centralNode + ' ...</p>' + loader);
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

function exportGraphAsSVG() {
	var output = s.toSVG({
		download: true,
		filename: 'graphExport.svg',
		size: 1000,
		labels: true,
		data: true
	});
}

function toggleBrowserFullscreen() {
	$('#browser').toggleClass('fullscreen');
	updateBrowserHeight();
}

function closeBrowser() {
	$('#browser').hide(animationSpeed);
	$('#entrypoint').show(animationSpeed);
	showReturnToBrowser();
}
