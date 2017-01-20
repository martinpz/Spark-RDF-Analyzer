// ########################## RDF Browser Configuration ##########################
const LOADER = '<div class="progress progress-striped active page-progress-bar"><div class="progress-bar" style="width: 100%;"></div></div>';
const ANIMATION_SPEED = 'fast';

// function changeConfigOptions() {
// 	// When textual browsing is selected: Disable properties related to visual browsing.
// 	if( $('#textualBrowsing').is(":checked") ) {
// 		$('#groupByPredicate').prop('checked', false).prop('disabled', true);
// 	} else {
// 		$('#groupByPredicate').prop('disabled', false);
// 	}
// }

function useTextualBrowsing() {
	return $('#textualBrowsing').prop('checked');
}

function groupByPredicate() {
	return $('#groupByPredicate').prop('checked');
}

function numNeighbors() {
	return $('#numNeighbors').val();
}

function getNeighborhoodRequest() {
    return REST_API + 'directNeighbors/' + getCookie('graphName')
		+ '?centralNode=' + encodeURIComponent(centralNodeURI)
		+ '&numNeighbors=' + numNeighbors()
        + '&groupNeighborsByPredicate=' + groupByPredicate();
}

$(document).ready(function() {
    $("#numNeighbors").slider({
        tooltip: 'always'
    });

	// $('#textualBrowsing').change( function() {
	// 	changeConfigOptions();
    // });
});
