// ########################## RDF Browser OnClick Events ##########################
$(document).ready(function() {
	// Buttons for entry point.
	$('#btnSearch').click( function() {
		showAutocompletionModal();
	});
	$('#btnReturnToBrowser').click( function() {
		returnToBrowser();
	});

	// Configuration options.
	$('#textualBrowsing').change( function() {
		changeConfigOptions();
    });

	// Buttons inside browser.
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
