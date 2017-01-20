// ########################## RDF Browser Configuration ##########################
var loader = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
var animationSpeed = 'fast';

function changeConfigOptions() {
	// When textual browsing is selected: Disable properties related to visual browsing.
	if( $('#textualBrowsing').is(":checked") ) {
		$('#groupBySubject').prop('checked', false).prop('disabled', true);
	} else {
		$('#groupBySubject').prop('disabled', false);
	}
}

function useTextualBrowsing() {
	return $('#textualBrowsing').prop('checked');
}
