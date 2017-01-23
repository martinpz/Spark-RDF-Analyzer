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
	if ($("#limitNeighbors").checked) {
		return $('#numNeighbors').val();
	} else {
		return false;
	}
}

function getNeighborhoodRequest(centralNodeURI) {
    return REST_API + 'directNeighbors/' + getCookie('graphName')
		+ '?centralNode=' + encodeURIComponent(centralNodeURI)
		+ '&numNeighbors=' + numNeighbors();
}

$(document).ready( function() {
    $("#numNeighbors").slider({
        tooltip: 'hide'
    });
	
	$("#numNeighbors").on("slide", function(slideEvt) {
		$("#sliderVal").text(slideEvt.value);
	});

	// Enable slider when neighbor limitation is selected.
	$("#limitNeighbors").click( function() {
		if (this.checked) {
			$("#numNeighborsDiv").show(ANIMATION_SPEED);
		} else {
			$("#numNeighborsDiv").hide(ANIMATION_SPEED);
		}
	});
});
